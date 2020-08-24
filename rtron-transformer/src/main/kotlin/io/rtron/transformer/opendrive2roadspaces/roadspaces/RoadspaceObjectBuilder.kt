/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rtron.transformer.opendrive2roadspaces.roadspaces

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.model.opendrive.road.signals.RoadSignals
import io.rtron.model.opendrive.road.signals.RoadSignalsSignal
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObjectIdentifier
import io.rtron.std.handleAndRemoveFailureIndexed
import io.rtron.std.handleFailure
import io.rtron.transformer.opendrive2roadspaces.geometry.Curve3DBuilder
import io.rtron.transformer.opendrive2roadspaces.geometry.Solid3DBuilder
import io.rtron.transformer.opendrive2roadspaces.geometry.Surface3DBuilder
import io.rtron.transformer.opendrive2roadspaces.geometry.Vector3DBuilder
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesConfiguration
import io.rtron.model.opendrive.road.objects.RoadObjects as OpendriveRoadObjects
import io.rtron.model.opendrive.road.objects.RoadObjectsObject as OpendriveRoadObject


/**
 * Builder for [RoadspaceObject] which correspond to the OpenDRIVE road object class.
 */
class RoadspaceObjectBuilder(
        private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    private val _solid3DBuilder = Solid3DBuilder(_reportLogger, configuration.parameters)
    private val _surface3DBuilder = Surface3DBuilder(_reportLogger, configuration.parameters)
    private val _curve3DBuilder = Curve3DBuilder(_reportLogger, configuration.parameters)
    private val _vector3DBuilder = Vector3DBuilder()

    // Methods
    /**
     * Builds up a list of [RoadspaceObject].
     *
     * @param roadReferenceLine road reference line required to build the geometries of the road object
     * @param baseAttributes attributes attached to each road space object
     */
    fun buildRoadspaceObjects(roadspaceId: RoadspaceIdentifier, srcRoadObjects: OpendriveRoadObjects,
                              roadReferenceLine: Curve3D, baseAttributes: AttributeList): List<RoadspaceObject> =
            srcRoadObjects.roadObject
                    .map { buildRoadObject(roadspaceId, it, roadReferenceLine, baseAttributes) }
                    .handleAndRemoveFailureIndexed { index, failure ->
                        _reportLogger.log(failure, "ObjectId=${srcRoadObjects.roadObject[index].id}")
                    }


    private fun buildRoadObject(id: RoadspaceIdentifier, srcRoadObject: OpendriveRoadObject, roadReferenceLine: Curve3D,
                                baseAttributes: AttributeList): Result<RoadspaceObject, Exception> {

        // check whether source model is processable
        val roadspaceObjectId = RoadspaceObjectIdentifier(srcRoadObject.id, srcRoadObject.name, id)
        srcRoadObject.isProcessable()
                .map { _reportLogger.log(it, roadspaceObjectId.toString()) }
                .handleFailure { return it }

        // get general object type and geometry representation
        val type = getObjectType(srcRoadObject)
        val geometries =
                buildGeometries(roadspaceObjectId, srcRoadObject, roadReferenceLine)
                        .handleFailure { return it }

        // build attributes
        val attributes = baseAttributes + buildAttributes(srcRoadObject)

        // build roadspace object
        val roadspaceObject = RoadspaceObject(roadspaceObjectId, type, geometries, attributes)
        return Result.success(roadspaceObject)
    }

    private fun getObjectType(srcRoadObject: OpendriveRoadObject): RoadObjectType {
        return RoadObjectType.valueOf(srcRoadObject.type.toString().toUpperCase())
    }

    /**
     * Reads in the OpenDRIVE road object geometries and builds up the implemented geometries of [AbstractGeometry3D].
     *
     * @param id identifier of the road space object
     * @param srcRoadObject road object source model
     * @return list of transformed geometries
     */
    private fun buildGeometries(id: RoadspaceObjectIdentifier, srcRoadObject: OpendriveRoadObject,
                                roadReferenceLine: Curve3D): Result<List<AbstractGeometry3D>, Exception> {

        // affine transformation matrix at the curve point of the object
        val curveAffine = roadReferenceLine
                .calculateAffine(srcRoadObject.curveRelativePosition.toCurveRelative1D())
                .handleFailure { return it }

        // build up solid geometrical representations
        val geometry = mutableListOf<AbstractGeometry3D>()
        geometry += _solid3DBuilder.buildCuboids(srcRoadObject, curveAffine)
        geometry += _solid3DBuilder.buildCylinders(srcRoadObject, curveAffine)
        geometry += _solid3DBuilder.buildPolyhedronsByRoadCorners(id, srcRoadObject, roadReferenceLine)
        geometry += _solid3DBuilder.buildPolyhedronsByLocalCorners(id, srcRoadObject, curveAffine)
        geometry += _solid3DBuilder.buildParametricSweeps(srcRoadObject, roadReferenceLine)

        // build up surface geometrical representations
        geometry += _surface3DBuilder.buildRectangles(srcRoadObject, curveAffine)
        geometry += _surface3DBuilder.buildCircles(srcRoadObject, curveAffine)
        geometry += _surface3DBuilder.buildLinearRingsByRoadCorners(id, srcRoadObject, roadReferenceLine)
        geometry += _surface3DBuilder.buildLinearRingsByLocalCorners(id, srcRoadObject, curveAffine)
        if (srcRoadObject.repeat.isHorizontalLinearRing())
            _reportLogger.infoOnce("Horizontal linear ring in repeat object is not implemented.")
        if (srcRoadObject.repeat.isVerticalLinearRing())
            _reportLogger.infoOnce("Vertical linear ring in repeat object is not implemented.")

        // build up curve geometrical representations
        geometry += _curve3DBuilder.buildCurve3D(srcRoadObject, roadReferenceLine)

        // if no other geometrical representation has been found, use a point instead
        if (geometry.isEmpty())
            geometry += _vector3DBuilder
                    .buildVector3Ds(srcRoadObject, curveAffine, force = true)
                    .handleFailure { throw it.error }

        return Result.success(geometry)
    }

    private fun buildAttributes(srcRoadObject: OpendriveRoadObject) =
            attributes("${configuration.parameters.attributesPrefix}roadObject_") {
                attribute("type", srcRoadObject.type.toString())
                attribute("subtype", srcRoadObject.subtype)
                attribute("dynamic", srcRoadObject.dynamic)
                attribute("validLength", srcRoadObject.validLength)
                attribute("orientation", srcRoadObject.orientation)
            }


    fun buildRoadspaceObjects(id: RoadspaceIdentifier, srcRoadSignals: RoadSignals, roadReferenceLine: Curve3D,
                              baseAttributes: AttributeList): List<RoadspaceObject> =
            srcRoadSignals.signal
                    .map { buildRoadSignalsSignal(id, it, roadReferenceLine, baseAttributes) }
                    .handleAndRemoveFailureIndexed { index, failure ->
                        _reportLogger.log(failure, "ObjectId=${srcRoadSignals.signal[index].id}")
                    }

    private fun buildRoadSignalsSignal(id: RoadspaceIdentifier, srcSignal: RoadSignalsSignal, roadReferenceLine: Curve3D,
                                       baseAttributes: AttributeList): Result<RoadspaceObject, Exception> {

        val objectId = RoadspaceObjectIdentifier(srcSignal.id, srcSignal.name, id)

        val geometry = buildGeometries(srcSignal, roadReferenceLine).handleFailure { return it }
        val attributes = baseAttributes + buildAttributes(srcSignal)

        val roadObject = RoadspaceObject(objectId, RoadObjectType.SIGNAL, geometry, attributes)
        return Result.success(roadObject)
    }

    private fun buildAttributes(srcSignal: RoadSignalsSignal): AttributeList =
            attributes("${configuration.parameters.attributesPrefix}roadSignal_") {
                attribute("dynamic", srcSignal.dynamic)
                attribute("orientation", srcSignal.orientation.toString())
                attribute("countryCode", srcSignal.countryCode.code)
                attribute("type", srcSignal.type)
                attribute("subtype", srcSignal.subtype)
                attribute("value", srcSignal.value)
            }

    private fun buildGeometries(srcRoadObject: RoadSignalsSignal, roadReferenceLine: Curve3D):
            Result<List<AbstractGeometry3D>, Exception> {

        val curveAffine = roadReferenceLine
                .calculateAffine(srcRoadObject.curveRelativePosition.toCurveRelative1D())
                .handleFailure { return it }

        val geometry = mutableListOf<AbstractGeometry3D>()
        if (geometry.isEmpty())
            geometry += _vector3DBuilder
                    .buildVector3Ds(srcRoadObject, curveAffine, force = true)
                    .handleFailure { throw it.error }

        return Result.success(geometry)
    }
}
