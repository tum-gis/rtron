/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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
import arrow.core.Some
import arrow.core.flatten
import arrow.core.getOrElse
import arrow.core.getOrNone
import arrow.core.nonEmptyListOf
import arrow.core.some
import arrow.core.toNonEmptyListOrNull
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.io.issues.mergeIssueLists
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.solid.AbstractSolid3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.math.range.Range
import io.rtron.math.transform.AffineSequence3D
import io.rtron.model.opendrive.objects.EObjectType
import io.rtron.model.opendrive.signal.RoadSignals
import io.rtron.model.opendrive.signal.RoadSignalsSignal
import io.rtron.model.roadspaces.identifier.LateralLaneRangeIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceObjectIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectBarrierSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectBuildingSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectCrosswalkSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectGantrySubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectObstacleSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectParkingSpaceSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectPoleSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectRoadMarkSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectRoadSurfaceSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectTrafficIslandSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectTreeSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectVegetationSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.std.toInt
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Curve3DBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Solid3DBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Surface3DBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Vector3DBuilder
import io.rtron.transformer.issues.opendrive.of
import io.rtron.model.opendrive.objects.RoadObjects as OpendriveRoadObjects
import io.rtron.model.opendrive.objects.RoadObjectsObject as OpendriveRoadObject
import io.rtron.model.opendrive.objects.RoadObjectsObjectRepeat as OpendriveRoadObjectRepeat
import io.rtron.model.roadspaces.roadspace.road.Road as RoadspaceRoad

/**
 * Builder for [RoadspaceObject] which correspond to the OpenDRIVE road object class.
 */
class RoadspaceObjectBuilder(
    private val parameters: Opendrive2RoadspacesParameters,
) {
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
        road: RoadspaceRoad,
        baseAttributes: AttributeList,
    ): ContextIssueList<List<RoadspaceObject>> =
        roadObjects.roadObject
            .map { buildRoadObject(roadspaceId, it, roadReferenceLine, road, baseAttributes) }
            .mergeIssueLists()
            .map { it.flatten() }

    private fun buildRoadObject(
        id: RoadspaceIdentifier,
        roadObject: OpendriveRoadObject,
        roadReferenceLine: Curve3D,
        road: RoadspaceRoad,
        baseAttributes: AttributeList,
    ): ContextIssueList<NonEmptyList<RoadspaceObject>> {
        val issueList = DefaultIssueList()

        // get general object type and geometry representation
        val (type, subType) = getObjectType(roadObject)

        // build attributes
        val attributes =
            baseAttributes +
                buildAttributes(roadObject) +
                buildAttributes(roadObject.curveRelativePosition) +
                buildAttributes(roadObject.referenceLinePointRelativeRotation)
        val laneRelations = buildLaneRelations(roadObject, road)

        val extrusionHeight: Option<Double> =
            if (parameters.generateRoadObjectTopSurfaceExtrusions) {
                parameters.roadObjectTopSurfaceExtrusionHeightPerObjectType.getOrNone(type)
            } else {
                None
            }
        val roadObjectsFromRepeat =
            roadObject.repeat.map { currentRoadObjectRepeat ->

                val repeatIdentifier =
                    currentRoadObjectRepeat.additionalId
                        .toEither { IllegalStateException("Additional outline ID must be available.") }
                        .getOrElse { throw it }
                val roadspaceObjectId =
                    RoadspaceObjectIdentifier(roadObject.id, repeatIdentifier.repeatIndex.some(), roadObject.name, type, id)

                val pointGeometry = buildPointGeometry(currentRoadObjectRepeat, roadReferenceLine)
                val boundingBoxGeometry = buildBoundingBoxGeometry(roadObject, roadReferenceLine)
                val complexGeometry =
                    buildComplexGeometry(
                        roadObject,
                        currentRoadObjectRepeat.some(),
                        roadReferenceLine,
                    ).handleIssueList { issueList += it }
                RoadspaceObject(
                    roadspaceObjectId,
                    type,
                    subType,
                    pointGeometry,
                    boundingBoxGeometry,
                    complexGeometry,
                    None,
                    laneRelations,
                    attributes,
                )
            }

        val roadObjects =
            if (roadObjectsFromRepeat.isEmpty()) {
                val roadspaceObjectId = RoadspaceObjectIdentifier(roadObject.id, None, roadObject.name, type, id)
                val pointGeometry = buildPointGeometry(roadObject, roadReferenceLine)
                val boundingBoxGeometry = buildBoundingBoxGeometry(roadObject, roadReferenceLine)
                val complexGeometry =
                    buildComplexGeometry(roadObject, None, roadReferenceLine).handleIssueList { issueList += it }
                val extrudedTopSurfaceGeometry =
                    extrusionHeight.flatMap { height ->
                        buildExtrudedTopSurfaceGeometry(roadObject, None, roadReferenceLine, height)
                            .handleIssueList { issueList += it }
                    }

                nonEmptyListOf(
                    RoadspaceObject(
                        roadspaceObjectId,
                        type,
                        subType,
                        pointGeometry,
                        boundingBoxGeometry,
                        complexGeometry,
                        extrudedTopSurfaceGeometry,
                        laneRelations,
                        attributes,
                    ),
                )
            } else {
                roadObjectsFromRepeat.toNonEmptyListOrNull()!!
            }

        // build roadspace object
        return ContextIssueList(roadObjects, issueList)
    }

    private fun getObjectType(roadObject: OpendriveRoadObject): Pair<RoadObjectType, Option<RoadObjectSubType>> {
        val type =
            roadObject.type.fold({ RoadObjectType.NONE }, {
                when (it) {
                    EObjectType.NONE -> RoadObjectType.NONE
                    EObjectType.OBSTACLE -> RoadObjectType.OBSTACLE
                    EObjectType.POLE -> RoadObjectType.POLE
                    EObjectType.TREE -> RoadObjectType.TREE
                    EObjectType.VEGETATION -> RoadObjectType.VEGETATION
                    EObjectType.BARRIER -> RoadObjectType.BARRIER
                    EObjectType.BUILDING -> RoadObjectType.BUILDING
                    EObjectType.PARKING_SPACE -> RoadObjectType.PARKING_SPACE
                    EObjectType.TRAFFIC_ISLAND -> RoadObjectType.TRAFFIC_ISLAND
                    EObjectType.CROSSWALK -> RoadObjectType.CROSSWALK
                    EObjectType.GANTRY -> RoadObjectType.GANTRY
                    EObjectType.ROAD_MARK -> RoadObjectType.ROAD_MARK
                    EObjectType.ROAD_SURFACE -> RoadObjectType.ROAD_SURFACE
                }
            })

        val roadObjectSubType = roadObject.subtype.fold({ return type to None }, { it })
        val subType: Option<RoadObjectSubType> =
            when (type) {
                RoadObjectType.NONE -> None
                RoadObjectType.OBSTACLE -> RoadObjectSubType.fromIdentifier<RoadObjectObstacleSubType>(roadObjectSubType)
                RoadObjectType.POLE -> RoadObjectSubType.fromIdentifier<RoadObjectPoleSubType>(roadObjectSubType)
                RoadObjectType.TREE -> RoadObjectSubType.fromIdentifier<RoadObjectTreeSubType>(roadObjectSubType)
                RoadObjectType.VEGETATION -> RoadObjectSubType.fromIdentifier<RoadObjectVegetationSubType>(roadObjectSubType)
                RoadObjectType.BARRIER -> RoadObjectSubType.fromIdentifier<RoadObjectBarrierSubType>(roadObjectSubType)
                RoadObjectType.BUILDING -> RoadObjectSubType.fromIdentifier<RoadObjectBuildingSubType>(roadObjectSubType)
                RoadObjectType.PARKING_SPACE -> RoadObjectSubType.fromIdentifier<RoadObjectParkingSpaceSubType>(roadObjectSubType)
                RoadObjectType.TRAFFIC_ISLAND -> RoadObjectSubType.fromIdentifier<RoadObjectTrafficIslandSubType>(roadObjectSubType)
                RoadObjectType.CROSSWALK -> RoadObjectSubType.fromIdentifier<RoadObjectCrosswalkSubType>(roadObjectSubType)
                RoadObjectType.GANTRY -> RoadObjectSubType.fromIdentifier<RoadObjectGantrySubType>(roadObjectSubType)
                RoadObjectType.ROAD_MARK -> RoadObjectSubType.fromIdentifier<RoadObjectRoadMarkSubType>(roadObjectSubType)
                RoadObjectType.ROAD_SURFACE -> RoadObjectSubType.fromIdentifier<RoadObjectRoadSurfaceSubType>(roadObjectSubType)
                RoadObjectType.SIGNAL -> None
            }

        return type to subType
    }

    private fun buildPointGeometry(
        roadObject: OpendriveRoadObject,
        roadReferenceLine: Curve3D,
    ): Vector3D {
        val curveAffine = roadReferenceLine.calculateAffine(roadObject.curveRelativePosition.toCurveRelative1D())
        return Vector3DBuilder.buildVector3Ds(roadObject, curveAffine)
    }

    private fun buildPointGeometry(
        roadObjectRepeat: OpendriveRoadObjectRepeat,
        roadReferenceLine: Curve3D,
    ): Vector3D {
        val curveAffine = roadReferenceLine.calculateAffine(roadObjectRepeat.curveRelativeStartPosition)
        val affineSequence = AffineSequence3D.of(curveAffine)
        return roadObjectRepeat.referenceLinePointRelativePosition.copy(affineSequence = affineSequence)
    }

    private fun buildBoundingBoxGeometry(
        roadObject: OpendriveRoadObject,
        roadReferenceLine: Curve3D,
    ): Option<AbstractGeometry3D> {
        check(
            roadObject.containsCuboid().toInt() +
                roadObject.containsCylinder().toInt() +
                roadObject.containsRectangle().toInt() +
                roadObject.containsCircle().toInt() <= 1,
        ) { "Bounding box must only be derived for a single geometry." }

        // affine transformation matrix at the curve point of the object
        val curveAffine = roadReferenceLine.calculateAffine(roadObject.curveRelativePosition.toCurveRelative1D())

        if (roadObject.containsCuboid()) {
            return Solid3DBuilder.buildCuboids(roadObject, curveAffine, parameters.numberTolerance).some()
        }
        if (roadObject.containsCylinder()) {
            return Solid3DBuilder.buildCylinders(roadObject, curveAffine, parameters.numberTolerance).some()
        }
        if (roadObject.containsRectangle()) {
            return Surface3DBuilder.buildRectangle(roadObject, curveAffine, parameters.numberTolerance).some()
        }
        if (roadObject.containsCircle()) {
            return Surface3DBuilder.buildCircle(roadObject, curveAffine, parameters.numberTolerance).some()
        }

        return None
    }

    /**
     * Reads in the OpenDRIVE road object geometries and builds up the implemented geometries of [AbstractGeometry3D].
     *
     * @param roadObject road object source model
     * @param roadObjectRepeat considered repeat element for building complex geometries like parametric sweeps
     */
    private fun buildComplexGeometry(
        roadObject: OpendriveRoadObject,
        roadObjectRepeat: Option<OpendriveRoadObjectRepeat>,
        roadReferenceLine: Curve3D,
    ): ContextIssueList<Option<AbstractGeometry3D>> {
        val issueList = DefaultIssueList()

        // affine transformation matrix at the curve point of the object
        val curveAffine = roadReferenceLine.calculateAffine(roadObject.curveRelativePosition.toCurveRelative1D())

        // build up solid geometrical representations
        val geometries = mutableListOf<AbstractGeometry3D>()
        geometries +=
            Solid3DBuilder
                .buildPolyhedronsByRoadCorners(
                    roadObject,
                    roadReferenceLine,
                    parameters.numberTolerance,
                ).handleIssueList { issueList += it }
        geometries +=
            Solid3DBuilder
                .buildPolyhedronsByLocalCorners(roadObject, curveAffine, parameters.numberTolerance)
                .handleIssueList { issueList += it }

        // build up surface geometrical representations
        geometries +=
            Surface3DBuilder
                .buildLinearRingsByRoadCorners(
                    roadObject,
                    roadReferenceLine,
                    parameters.numberTolerance,
                ).handleIssueList { issueList += it }
        geometries +=
            Surface3DBuilder
                .buildLinearRingsByLocalCorners(
                    roadObject,
                    curveAffine,
                    parameters.numberTolerance,
                ).handleIssueList { issueList += it }

        roadObjectRepeat.onSome { currentRepeat ->
            geometries +=
                Solid3DBuilder
                    .buildParametricSweep(
                        currentRepeat,
                        roadReferenceLine,
                        parameters.numberTolerance,
                    ).toList()
            geometries +=
                Surface3DBuilder.buildParametricBoundedSurfacesByHorizontalRepeat(
                    currentRepeat,
                    roadReferenceLine,
                    parameters.numberTolerance,
                )
            geometries +=
                Surface3DBuilder.buildParametricBoundedSurfacesByVerticalRepeat(
                    currentRepeat,
                    roadReferenceLine,
                    parameters.numberTolerance,
                )

            if (currentRepeat.containsRepeatedCuboid()) {
                issueList +=
                    DefaultIssue.of(
                        "RepeatCuboidNotSupported",
                        "Cuboid geometries in the repeat elements are currently not supported.",
                        roadObject.additionalId,
                        Severity.WARNING,
                        wasFixed = false,
                    )
            }
            if (currentRepeat.containsRepeatCylinder()) {
                issueList +=
                    DefaultIssue.of(
                        "RepeatCylinderNotSupported",
                        "Cylinder geometries in the repeat elements are currently not supported.",
                        roadObject.additionalId,
                        Severity.WARNING,
                        wasFixed = false,
                    )
            }
            if (currentRepeat.containsRepeatedRectangle()) {
                issueList +=
                    DefaultIssue.of(
                        "RepeatRectangleNotSupported",
                        "Rectangle geometries in the repeat elements are currently not supported.",
                        roadObject.additionalId,
                        Severity.WARNING,
                        wasFixed = false,
                    )
            }
            if (currentRepeat.containsRepeatCircle()) {
                issueList +=
                    DefaultIssue.of(
                        "RepeatCircleNotSupported",
                        "Circle geometries in the repeat elements are currently not supported.",
                        roadObject.additionalId,
                        Severity.WARNING,
                        wasFixed = false,
                    )
            }
        }

        // build up curve geometrical representations
        geometries += Curve3DBuilder.buildCurve3D(roadObject, roadReferenceLine, parameters.numberTolerance)

        if (geometries.size > 1) {
            issueList +=
                DefaultIssue.of(
                    "MultipleComplexGeometriesNotSupported",
                    "Conversion of road objects with multiple complex geometries is currently not supported.",
                    roadObject.additionalId,
                    Severity.WARNING,
                    wasFixed = false,
                )
        }
        val builtGeometry = if (geometries.isEmpty()) None else Some(geometries.first())
        return ContextIssueList(builtGeometry, issueList)
    }

    /**
     * Reads in the OpenDRIVE road object geometries and builds up an [AbstractSolid3D] by extruding the top surface.
     *
     * @param roadObject road object source model
     * @param roadObjectRepeat considered repeat element for building complex geometries like parametric sweeps
     */
    private fun buildExtrudedTopSurfaceGeometry(
        roadObject: OpendriveRoadObject,
        roadObjectRepeat: Option<OpendriveRoadObjectRepeat>,
        roadReferenceLine: Curve3D,
        extrusionHeight: Double,
    ): ContextIssueList<Option<AbstractSolid3D>> {
        val issueList = DefaultIssueList()

        // affine transformation matrix at the curve point of the object
        val curveAffine = roadReferenceLine.calculateAffine(roadObject.curveRelativePosition.toCurveRelative1D())

        // build up solid geometrical representations
        val geometries = mutableListOf<AbstractSolid3D>()
        geometries +=
            Solid3DBuilder
                .buildPolyhedronsByExtrudedTopRoadCorners(
                    roadObject,
                    roadReferenceLine,
                    extrusionHeight,
                    parameters.numberTolerance,
                ).handleIssueList { issueList += it }
        geometries +=
            Solid3DBuilder
                .buildPolyhedronsByExtrudedTopLocalCorners(roadObject, curveAffine, extrusionHeight, parameters.numberTolerance)
                .handleIssueList { issueList += it }

        if (roadObjectRepeat.isSome()) {
            issueList +=
                DefaultIssue.of(
                    "RepeatNotSupportedForExtrudedTopSurface",
                    "Derivation of extruded top surfaces from repeat elements are currently not supported.",
                    roadObject.additionalId,
                    Severity.WARNING,
                    wasFixed = false,
                )
        }

        val builtGeometry = if (geometries.isEmpty()) None else Some(geometries.first())
        return ContextIssueList(builtGeometry, issueList)
    }

    private fun buildAttributes(roadObject: OpendriveRoadObject) =
        attributes("${parameters.attributesPrefix}roadObject_") {
            roadObject.name.onSome {
                attribute("name", it)
            }
            roadObject.type.onSome {
                attribute("type", it.toString())
            }
            roadObject.subtype.onSome {
                attribute("subtype", it)
            }
            roadObject.dynamic.onSome {
                attribute("dynamic", it)
            }
            roadObject.validLength.onSome {
                attribute("validLength", it)
            }
            roadObject.orientation.onSome {
                attribute("orientation", it.toString())
            }
        }

    private fun buildAttributes(curveRelativePosition: CurveRelativeVector3D) =
        attributes("${parameters.attributesPrefix}curveRelativePosition_") {
            attribute("curvePosition", curveRelativePosition.curvePosition)
            attribute("lateralOffset", curveRelativePosition.lateralOffset)
            attribute("heightOffset", curveRelativePosition.heightOffset)
        }

    private fun buildAttributes(rotation: Rotation3D) =
        attributes("${parameters.attributesPrefix}curveRelativeRotation_") {
            attribute("heading", rotation.heading)
            attribute("roll", rotation.roll)
            attribute("pitch", rotation.pitch)
        }

    fun buildRoadspaceObjects(
        id: RoadspaceIdentifier,
        roadSignals: RoadSignals,
        roadReferenceLine: Curve3D,
        road: RoadspaceRoad,
        baseAttributes: AttributeList,
    ): ContextIssueList<List<RoadspaceObject>> =
        roadSignals.signal
            .map { buildRoadSignalsSignal(id, it, roadReferenceLine, road, baseAttributes) }
            .mergeIssueLists()

    private fun buildRoadSignalsSignal(
        id: RoadspaceIdentifier,
        roadSignal: RoadSignalsSignal,
        roadReferenceLine: Curve3D,
        road: RoadspaceRoad,
        baseAttributes: AttributeList,
    ): ContextIssueList<RoadspaceObject> {
        val issueList = DefaultIssueList()

        val objectId = RoadspaceObjectIdentifier(roadSignal.id, None, roadSignal.name, RoadObjectType.SIGNAL, id)

        val pointGeometry = buildPointGeometry(roadSignal, roadReferenceLine)
        val complexGeometry =
            buildComplexGeometry(roadSignal, roadReferenceLine).handleIssueList { issueList += it }
        val laneRelations = buildLaneRelations(roadSignal, road)
        val attributes =
            baseAttributes +
                buildAttributes(roadSignal) +
                buildAttributes(roadSignal.curveRelativePosition) +
                buildAttributes(roadSignal.referenceLinePointRelativeRotation)

        val roadspaceObject =
            RoadspaceObject(
                objectId,
                RoadObjectType.SIGNAL,
                None,
                pointGeometry,
                None,
                complexGeometry,
                None,
                laneRelations,
                attributes,
            )
        return ContextIssueList(roadspaceObject, issueList)
    }

    private fun buildAttributes(signal: RoadSignalsSignal): AttributeList =
        attributes("${parameters.attributesPrefix}roadSignal_") {
            signal.name.onSome {
                attribute("name", it)
            }
            attribute("type", signal.type)
            attribute("subtype", signal.subtype)
            attribute("dynamic", signal.dynamic)
            attribute("orientation", signal.orientation.toString())
            signal.country.onSome {
                attribute("countryCode", it.toString())
            }
            signal.value.onSome {
                attribute("value", it)
            }
        }

    private fun buildPointGeometry(
        signal: RoadSignalsSignal,
        roadReferenceLine: Curve3D,
    ): Vector3D {
        val curveAffine = roadReferenceLine.calculateAffine(signal.curveRelativePosition.toCurveRelative1D())
        return Vector3DBuilder.buildVector3Ds(signal, curveAffine)
    }

    private fun buildComplexGeometry(
        signal: RoadSignalsSignal,
        roadReferenceLine: Curve3D,
    ): ContextIssueList<Option<AbstractSurface3D>> {
        val issueList = DefaultIssueList()

        // affine transformation matrix at the curve point of the object
        val curveAffine = roadReferenceLine.calculateAffine(signal.curveRelativePosition.toCurveRelative1D())

        if (signal.containsRectangle()) {
            return ContextIssueList(
                Surface3DBuilder.buildRectangle(signal, curveAffine, parameters.numberTolerance).some(),
                issueList,
            )
        }
        if (signal.containsHorizontalLine()) {
            issueList +=
                DefaultIssue.of(
                    "SignalHorizontalLineNotSupported",
                    "Horizontal line geometry in road signal is currently not supported.",
                    signal.additionalId,
                    Severity.WARNING,
                    wasFixed = false,
                )
        }
        if (signal.containsVerticalLine()) {
            issueList +=
                DefaultIssue.of(
                    "SignalVerticalLineNotSupported",
                    "Vertical line geometry in road signal is currently not supported.",
                    signal.additionalId,
                    Severity.WARNING,
                    wasFixed = false,
                )
        }

        return ContextIssueList(None, issueList)
    }

    private fun buildLaneRelations(
        roadObject: OpendriveRoadObject,
        road: RoadspaceRoad,
    ): List<LateralLaneRangeIdentifier> {
        val laneSection =
            road.getLaneSection(roadObject.curveRelativePosition.toCurveRelative1D()).getOrElse { throw it }

        return if (roadObject.validity.isEmpty()) {
            listOf(laneSection.getCompleteLateralLaneRangeIdentifier())
        } else {
            roadObject.validity.map { LateralLaneRangeIdentifier(Range.closed(it.fromLane, it.toLane), laneSection.id) }
        }
    }

    private fun buildLaneRelations(
        roadSignal: RoadSignalsSignal,
        road: RoadspaceRoad,
    ): List<LateralLaneRangeIdentifier> {
        val laneSection =
            road.getLaneSection(roadSignal.curveRelativePosition.toCurveRelative1D()).getOrElse { throw it }

        return if (roadSignal.validity.isEmpty()) {
            listOf(laneSection.getCompleteLateralLaneRangeIdentifier())
        } else {
            roadSignal.validity.map { LateralLaneRangeIdentifier(Range.closed(it.fromLane, it.toLane), laneSection.id) }
        }
    }
}
