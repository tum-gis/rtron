/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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
import io.rtron.io.logging.LogManager
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.model.opendrive.common.EObjectType
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
import io.rtron.std.mapAndHandleFailureOnOriginal
import io.rtron.transformer.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.opendrive2roadspaces.geometry.Curve3DBuilder
import io.rtron.transformer.opendrive2roadspaces.geometry.Solid3DBuilder
import io.rtron.transformer.opendrive2roadspaces.geometry.Surface3DBuilder
import io.rtron.transformer.opendrive2roadspaces.geometry.Vector3DBuilder
import io.rtron.model.opendrive.road.objects.RoadObjects as OpendriveRoadObjects
import io.rtron.model.opendrive.road.objects.RoadObjectsObject as OpendriveRoadObject

/**
 * Builder for [RoadspaceObject] which correspond to the OpenDRIVE road object class.
 */
class RoadspaceObjectBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    private val _solid3DBuilder = Solid3DBuilder(_reportLogger, configuration)
    private val _surface3DBuilder = Surface3DBuilder(_reportLogger, configuration)
    private val _curve3DBuilder = Curve3DBuilder(_reportLogger, configuration)
    private val _vector3DBuilder = Vector3DBuilder()

    // Methods
    /**
     * Builds up a list of [RoadspaceObject].
     *
     * @param roadReferenceLine road reference line required to build the geometries of the road object
     * @param baseAttributes attributes attached to each road space object
     */
    fun buildRoadspaceObjects(
        roadspaceId: RoadspaceIdentifier,
        roadObjects: OpendriveRoadObjects,
        roadReferenceLine: Curve3D,
        baseAttributes: AttributeList
    ): List<RoadspaceObject> =
        roadObjects.roadObject
            .map { buildRoadObject(roadspaceId, it, roadReferenceLine, baseAttributes) }
            .handleAndRemoveFailureIndexed { index, failure ->
                _reportLogger.log(failure, "ObjectId=${roadObjects.roadObject[index].id}")
            }

    private fun buildRoadObject(
        id: RoadspaceIdentifier,
        roadObject: OpendriveRoadObject,
        roadReferenceLine: Curve3D,
        baseAttributes: AttributeList
    ): Result<RoadspaceObject, Exception> {

        // check whether source model is processable
        val roadspaceObjectId = RoadspaceObjectIdentifier(roadObject.id, roadObject.name, id)
        roadObject.isProcessable()
            .map { _reportLogger.log(it, roadspaceObjectId.toString()) }
            .handleFailure { return it }

        // get general object type and geometry representation
        val type = getObjectType(roadObject)
        val geometries = buildGeometries(roadspaceObjectId, roadObject, roadReferenceLine)
            .handleFailure { return it }

        // build attributes
        val attributes = baseAttributes +
            buildAttributes(roadObject) +
            buildAttributes(roadObject.curveRelativePosition) +
            buildAttributes(roadObject.referenceLinePointRelativeRotation)

        // build roadspace object
        val roadspaceObject = RoadspaceObject(roadspaceObjectId, type, geometries, attributes)
        return Result.success(roadspaceObject)
    }

    private fun getObjectType(roadObject: OpendriveRoadObject): RoadObjectType = when (roadObject.type) {
        EObjectType.NONE -> RoadObjectType.NONE
        EObjectType.OBSTACLE -> RoadObjectType.OBSTACLE
        EObjectType.POLE -> RoadObjectType.POLE
        EObjectType.TREE -> RoadObjectType.TREE
        EObjectType.VEGETATION -> RoadObjectType.VEGETATION
        EObjectType.BARRIER -> RoadObjectType.BARRIER
        EObjectType.BUILDING -> RoadObjectType.BUILDING
        EObjectType.PARKING_SPACE -> RoadObjectType.PARKING_SPACE
        EObjectType.PATCH -> RoadObjectType.PATCH
        EObjectType.RAILING -> RoadObjectType.RAILING
        EObjectType.TRAFFIC_ISLAND -> RoadObjectType.TRAFFIC_ISLAND
        EObjectType.CROSSWALK -> RoadObjectType.CROSSWALK
        EObjectType.STREET_LAMP -> RoadObjectType.STREET_LAMP
        EObjectType.GANTRY -> RoadObjectType.GANTRY
        EObjectType.SOUND_BARRIER -> RoadObjectType.SOUND_BARRIER
        EObjectType.ROAD_MARK -> RoadObjectType.ROAD_MARK
    }

    /**
     * Reads in the OpenDRIVE road object geometries and builds up the implemented geometries of [AbstractGeometry3D].
     *
     * @param id identifier of the road space object
     * @param roadObject road object source model
     * @return list of transformed geometries
     */
    private fun buildGeometries(
        id: RoadspaceObjectIdentifier,
        roadObject: OpendriveRoadObject,
        roadReferenceLine: Curve3D
    ): Result<List<AbstractGeometry3D>, Exception> {

        // affine transformation matrix at the curve point of the object
        val curveAffine = roadReferenceLine
            .calculateAffine(roadObject.curveRelativePosition.toCurveRelative1D())
            .handleFailure { return it }

        // build up solid geometrical representations
        val geometry = mutableListOf<AbstractGeometry3D>()
        geometry += _solid3DBuilder.buildCuboids(roadObject, curveAffine)
        geometry += _solid3DBuilder.buildCylinders(roadObject, curveAffine)
        geometry += _solid3DBuilder.buildPolyhedronsByRoadCorners(id, roadObject, roadReferenceLine)
        geometry += _solid3DBuilder.buildPolyhedronsByLocalCorners(id, roadObject, curveAffine)
        geometry += _solid3DBuilder.buildParametricSweeps(roadObject, roadReferenceLine)

        // build up surface geometrical representations
        geometry += _surface3DBuilder.buildRectangles(roadObject, curveAffine)
        geometry += _surface3DBuilder.buildCircles(roadObject, curveAffine)
        geometry += _surface3DBuilder.buildLinearRingsByRoadCorners(id, roadObject, roadReferenceLine)
        geometry += _surface3DBuilder.buildLinearRingsByLocalCorners(id, roadObject, curveAffine)
        geometry += _surface3DBuilder.buildParametricBoundedSurfacesByHorizontalRepeat(id, roadObject.repeat, roadReferenceLine)
        geometry += _surface3DBuilder.buildParametricBoundedSurfacesByVerticalRepeat(id, roadObject.repeat, roadReferenceLine)

        // build up curve geometrical representations
        geometry += _curve3DBuilder.buildCurve3D(roadObject, roadReferenceLine)

        // if no other geometrical representation has been found, use a point instead
        if (geometry.isEmpty())
            geometry += _vector3DBuilder
                .buildVector3Ds(roadObject, curveAffine, force = true)
                .handleFailure { throw it.error }

        return Result.success(geometry)
    }

    private fun buildAttributes(roadObject: OpendriveRoadObject) =
        attributes("${configuration.attributesPrefix}roadObject_") {
            attribute("type", roadObject.type.toString())
            attribute("subtype", roadObject.subtype)
            attribute("dynamic", roadObject.dynamic)
            attribute("validLength", roadObject.validLength)
            attribute("orientation", roadObject.orientation)
        }

    private fun buildAttributes(curveRelativePosition: CurveRelativeVector3D) =
        attributes("${configuration.attributesPrefix}curveRelativePosition_") {
            attribute("curvePosition", curveRelativePosition.curvePosition)
            attribute("lateralOffset", curveRelativePosition.lateralOffset)
            attribute("heightOffset", curveRelativePosition.heightOffset)
        }

    private fun buildAttributes(rotation: Rotation3D) =
        attributes("${configuration.attributesPrefix}curveRelativeRotation_") {
            attribute("heading", rotation.heading)
            attribute("roll", rotation.roll)
            attribute("pitch", rotation.pitch)
        }

    fun buildRoadspaceObjects(
        id: RoadspaceIdentifier,
        roadSignals: RoadSignals,
        roadReferenceLine: Curve3D,
        baseAttributes: AttributeList
    ): List<RoadspaceObject> =
        roadSignals.signal
            .mapAndHandleFailureOnOriginal(
                { buildRoadSignalsSignal(id, it, roadReferenceLine, baseAttributes) },
                { failure, original -> _reportLogger.log(failure, RoadspaceObjectIdentifier(original.id, original.name, id).toString(), "Ignoring signal object.") }
            )

    private fun buildRoadSignalsSignal(
        id: RoadspaceIdentifier,
        roadSignal: RoadSignalsSignal,
        roadReferenceLine: Curve3D,
        baseAttributes: AttributeList
    ): Result<RoadspaceObject, Exception> {

        val objectId = RoadspaceObjectIdentifier(roadSignal.id, roadSignal.name, id)

        val geometry = buildGeometries(roadSignal, roadReferenceLine).handleFailure { return it }
        val attributes = baseAttributes +
            buildAttributes(roadSignal) +
            buildAttributes(roadSignal.curveRelativePosition) +
            buildAttributes(roadSignal.referenceLinePointRelativeRotation)

        val roadObject = RoadspaceObject(objectId, RoadObjectType.SIGNAL, geometry, attributes)
        return Result.success(roadObject)
    }

    private fun buildAttributes(signal: RoadSignalsSignal): AttributeList =
        attributes("${configuration.attributesPrefix}roadSignal_") {
            attribute("dynamic", signal.dynamic)
            attribute("orientation", signal.orientation.toString())
            attribute("countryCode", signal.countryCode.code)
            attribute("type", signal.type)
            attribute("subtype", signal.subtype)
            attribute("value", signal.value)
        }

    private fun buildGeometries(signal: RoadSignalsSignal, roadReferenceLine: Curve3D):
        Result<List<AbstractGeometry3D>, Exception> {

        val curveAffine = roadReferenceLine
            .calculateAffine(signal.curveRelativePosition.toCurveRelative1D())
            .handleFailure { return it }

        val geometry = mutableListOf<AbstractGeometry3D>()
        if (geometry.isEmpty())
            geometry += _vector3DBuilder
                .buildVector3Ds(signal, curveAffine, force = true)
                .handleFailure { throw it.error }

        return Result.success(geometry)
    }
}
