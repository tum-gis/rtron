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

package io.rtron.transformer.converter.opendrive2roadspaces.roadspaces

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.flatten
import arrow.core.getOrHandle
import arrow.core.nonEmptyListOf
import arrow.core.some
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Report
import io.rtron.io.report.mergeReports
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.model.opendrive.objects.EObjectType
import io.rtron.model.opendrive.signal.RoadSignals
import io.rtron.model.opendrive.signal.RoadSignalsSignal
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceObjectIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Curve3DBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Solid3DBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Surface3DBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Vector3DBuilder
import io.rtron.model.opendrive.objects.RoadObjects as OpendriveRoadObjects
import io.rtron.model.opendrive.objects.RoadObjectsObject as OpendriveRoadObject
import io.rtron.model.opendrive.objects.RoadObjectsObjectRepeat as OpendriveRoadObjectRepeat

/**
 * Builder for [RoadspaceObject] which correspond to the OpenDRIVE road object class.
 */
class RoadspaceObjectBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _solid3DBuilder = Solid3DBuilder(configuration)
    private val _surface3DBuilder = Surface3DBuilder(configuration)
    private val _curve3DBuilder = Curve3DBuilder(configuration)
    private val _vector3DBuilder = Vector3DBuilder(configuration)

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
    ): ContextReport<List<RoadspaceObject>> {

        return roadObjects.roadObject
            .map { buildRoadObject(roadspaceId, it, roadReferenceLine, baseAttributes) }
            .mergeReports()
            .map { it.flatten() }
    }

    private fun buildRoadObject(
        id: RoadspaceIdentifier,
        roadObject: OpendriveRoadObject,
        roadReferenceLine: Curve3D,
        baseAttributes: AttributeList
    ): ContextReport<NonEmptyList<RoadspaceObject>> {
        val report = Report()

        // get general object type and geometry representation
        val type = getObjectType(roadObject)

        // build attributes
        val attributes = baseAttributes +
            buildAttributes(roadObject) +
            buildAttributes(roadObject.curveRelativePosition) +
            buildAttributes(roadObject.referenceLinePointRelativeRotation)

        val roadObjectsFromRepeat = roadObject.repeat.map { currentRoadObjectRepeat ->
            val repeatIdentifier = currentRoadObjectRepeat.additionalId.toEither { IllegalStateException("Additional outline ID must be available.") }.getOrHandle { throw it }

            val roadspaceObjectId = RoadspaceObjectIdentifier("${roadObject.id}_${repeatIdentifier.repeatIndex}", roadObject.name, id)
            val geometry = buildGeometries(roadObject, currentRoadObjectRepeat.some(), roadReferenceLine).handleReport { report += it }
            RoadspaceObject(roadspaceObjectId, type, geometry, attributes)
        }

        val roadObjects = if (roadObjectsFromRepeat.isEmpty()) {
            val roadspaceObjectId = RoadspaceObjectIdentifier(roadObject.id, roadObject.name, id)
            val geometry = buildGeometries(roadObject, None, roadReferenceLine).handleReport { report += it }
            nonEmptyListOf(RoadspaceObject(roadspaceObjectId, type, geometry, attributes))
        } else {
            NonEmptyList.fromListUnsafe(roadObjectsFromRepeat)
        }

        // build roadspace object
        return ContextReport(roadObjects, report)
    }

    private fun getObjectType(roadObject: OpendriveRoadObject): RoadObjectType = roadObject.type.fold({ RoadObjectType.NONE }, {
        when (it) {
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
    })

    /**
     * Reads in the OpenDRIVE road object geometries and builds up the implemented geometries of [AbstractGeometry3D].
     *
     * @param roadObject road object source model
     * @param roadObjectRepeat considered repeat element for building complex geometries like parametric sweeps
     * @return list of transformed geometries
     */
    private fun buildGeometries(
        roadObject: OpendriveRoadObject,
        roadObjectRepeat: Option<OpendriveRoadObjectRepeat>,
        roadReferenceLine: Curve3D
    ): ContextReport<AbstractGeometry3D> {
        val report = Report()

        // affine transformation matrix at the curve point of the object
        val curveAffine = roadReferenceLine.calculateAffine(roadObject.curveRelativePosition.toCurveRelative1D())

        // build up solid geometrical representations
        val geometries = mutableListOf<AbstractGeometry3D>()
        geometries += _solid3DBuilder.buildCuboids(roadObject, curveAffine).handleReport { report += it }
        geometries += _solid3DBuilder.buildCylinders(roadObject, curveAffine).handleReport { report += it }
        geometries += _solid3DBuilder.buildPolyhedronsByRoadCorners(roadObject, roadReferenceLine).handleReport { report += it }
        geometries += _solid3DBuilder.buildPolyhedronsByLocalCorners(roadObject, curveAffine).handleReport { report += it }

        // build up surface geometrical representations
        geometries += _surface3DBuilder.buildRectangles(roadObject, curveAffine).handleReport { report += it }
        geometries += _surface3DBuilder.buildCircles(roadObject, curveAffine).handleReport { report += it }
        geometries += _surface3DBuilder.buildLinearRingsByRoadCorners(roadObject, roadReferenceLine).handleReport { report += it }
        geometries += _surface3DBuilder.buildLinearRingsByLocalCorners(roadObject, curveAffine).handleReport { report += it }

        roadObjectRepeat.tap {
            geometries += _solid3DBuilder.buildParametricSweeps(it, roadReferenceLine).toList()
            geometries += _surface3DBuilder.buildParametricBoundedSurfacesByHorizontalRepeat(it, roadReferenceLine)
            geometries += _surface3DBuilder.buildParametricBoundedSurfacesByVerticalRepeat(it, roadReferenceLine)
        }

        // build up curve geometrical representations
        geometries += _curve3DBuilder.buildCurve3D(roadObject, roadReferenceLine)

        // if no other geometrical representation has been found, use a point instead
        if (geometries.isEmpty())
            geometries += _vector3DBuilder.buildVector3Ds(roadObject, curveAffine, force = true)

        check(geometries.size == 1) { "Exactly one geometry must be derived." }

        return ContextReport(geometries.first(), report)
    }

    private fun buildAttributes(roadObject: OpendriveRoadObject) =
        attributes("${configuration.attributesPrefix}roadObject_") {
            attribute("type", roadObject.type.toString())
            attribute("subtype", roadObject.subtype)
            roadObject.dynamic.tap {
                attribute("dynamic", it)
            }
            roadObject.validLength.tap {
                attribute("validLength", it)
            }
            roadObject.orientation.tap {
                attribute("orientation", it.toString())
            }
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
    ): List<RoadspaceObject> {

        return roadSignals.signal.map { buildRoadSignalsSignal(id, it, roadReferenceLine, baseAttributes) }
    }

    private fun buildRoadSignalsSignal(
        id: RoadspaceIdentifier,
        roadSignal: RoadSignalsSignal,
        roadReferenceLine: Curve3D,
        baseAttributes: AttributeList
    ): RoadspaceObject {

        val objectId = RoadspaceObjectIdentifier(roadSignal.id, roadSignal.name, id)

        val geometry = buildGeometries(roadSignal, roadReferenceLine)
        val attributes = baseAttributes +
            buildAttributes(roadSignal) +
            buildAttributes(roadSignal.curveRelativePosition) +
            buildAttributes(roadSignal.referenceLinePointRelativeRotation)

        return RoadspaceObject(objectId, RoadObjectType.SIGNAL, geometry, attributes)
    }

    private fun buildAttributes(signal: RoadSignalsSignal): AttributeList =
        attributes("${configuration.attributesPrefix}roadSignal_") {
            attribute("dynamic", signal.dynamic)
            attribute("orientation", signal.orientation.toString())
            signal.country.tap {
                attribute("countryCode", it.toString())
            }
            attribute("type", signal.type)
            attribute("subtype", signal.subtype)
            signal.value.tap {
                attribute("value", it)
            }
        }

    private fun buildGeometries(signal: RoadSignalsSignal, roadReferenceLine: Curve3D): AbstractGeometry3D {
        val curveAffine = roadReferenceLine.calculateAffine(signal.curveRelativePosition.toCurveRelative1D())

        return _vector3DBuilder.buildVector3Ds(signal, curveAffine, force = true)
    }
}
