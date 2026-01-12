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

package io.rtron.transformer.converter.roadspaces2citygml.transformer

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.io.issues.mergeIssueLists
import io.rtron.math.geometry.euclidean.threed.solid.AbstractSolid3D
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.common.LongitudinalFillerSurface
import io.rtron.model.roadspaces.identifier.JunctionIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.std.handleLeftAndFilter
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.module.GenericsModuleBuilder
import io.rtron.transformer.converter.roadspaces2citygml.module.IdentifierAdder
import io.rtron.transformer.converter.roadspaces2citygml.module.TransportationModuleBuilder
import io.rtron.transformer.converter.roadspaces2citygml.router.LaneRouter
import io.rtron.transformer.converter.roadspaces2citygml.router.RoadspaceObjectRouter
import io.rtron.transformer.issues.roadspaces.of
import org.citygml4j.core.model.core.AbstractCityObject
import org.citygml4j.core.model.core.CityModel
import org.citygml4j.core.model.transportation.AbstractTransportationSpace
import org.citygml4j.core.model.transportation.IntersectionProperty
import org.citygml4j.core.model.transportation.SectionProperty
import org.xmlobjects.gml.model.basictypes.Code
import org.citygml4j.core.model.transportation.Road as CitygmlRoad

/**
 * Transforms [Road] classes (RoadSpaces model) to the [CityModel] (CityGML model).
 */
class RoadsTransformer(
    private val parameters: Roadspaces2CitygmlParameters,
) {
    // Properties and Initializers
    private val genericsModuleBuilder = GenericsModuleBuilder(parameters)
    private val transportationModuleBuilder = TransportationModuleBuilder(parameters)

    // Methods

    fun transformRoad(
        roadspaceName: String,
        roadspacesModel: RoadspacesModel,
    ): ContextIssueList<Option<CitygmlRoad>> {
        val issueList = DefaultIssueList()

        val roadFeature = transportationModuleBuilder.createRoad()
        IdentifierAdder.addIdentifier(generateRoadIdentifier(roadspaceName, parameters.gmlIdPrefix), roadFeature)
        if (roadspaceName.isNotEmpty()) {
            roadFeature.names.add(Code(roadspaceName))
        }

        val junctions = roadspacesModel.getAllJunctionIdentifiersContainingRoadspaces(roadspaceName)
        junctions.forEach {
            issueList += addIntersectionOrLink(it, roadspaceName, roadspacesModel, roadFeature)
        }

        val roads = roadspacesModel.getAllRoadspaceIdentifiersNotLocatedInJunctions(roadspaceName)
        roads.forEach {
            issueList += addSection(it, roadspacesModel, roadFeature)
        }

        return ContextIssueList(Some(roadFeature), issueList)
    }

    private fun addIntersectionOrLink(
        junctionId: JunctionIdentifier,
        roadspaceName: String,
        roadspacesModel: RoadspacesModel,
        dstRoad: CitygmlRoad,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        val roadspacesInJunction =
            roadspacesModel
                .getRoadspacesWithinJunction(junctionId)
                .getOrElse { throw it }
                .sortedBy { it.name }

        if (roadspacesInJunction.first().name == roadspaceName && parameters.mappingBackwardsCompatibility) {
            roadspacesInJunction.forEach {
                issueList += addRoadspace(it, roadspacesModel, dstRoad)
            }
        } else if (roadspacesInJunction.first().name == roadspaceName && !parameters.mappingBackwardsCompatibility) {
            val intersectionFeature = transportationModuleBuilder.createIntersection()
            IdentifierAdder.addIdentifier(junctionId.deriveIntersectionGmlIdentifier(parameters.gmlIdPrefix), intersectionFeature)
            roadspacesInJunction.forEach {
                issueList += addRoadspace(it, roadspacesModel, intersectionFeature)
            }
            dstRoad.intersections.add(IntersectionProperty(intersectionFeature))
        } else {
            dstRoad.intersections.add(IntersectionProperty(junctionId.deriveIntersectionGmlIdentifier(parameters.gmlIdPrefix)))
        }

        return issueList
    }

    private fun addSection(
        roadspaceId: RoadspaceIdentifier,
        roadspacesModel: RoadspacesModel,
        dstRoad: CitygmlRoad,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        val roadspace = roadspacesModel.getRoadspace(roadspaceId).getOrElse { throw it }

        if (parameters.mappingBackwardsCompatibility) {
            issueList += addRoadspace(roadspace, roadspacesModel, dstRoad)
        } else {
            val sectionFeature = transportationModuleBuilder.createSection()
            IdentifierAdder.addIdentifier(roadspaceId.deriveSectionGmlIdentifier(parameters.gmlIdPrefix), sectionFeature)
            issueList += addRoadspace(roadspace, roadspacesModel, sectionFeature)
            dstRoad.sections.add(SectionProperty(sectionFeature))
        }

        return issueList
    }

    fun transformAdditionalRoadLines(roadspace: Roadspace): ContextIssueList<List<AbstractCityObject>> {
        val issueList = DefaultIssueList()

        // transforms the road reference line
        val roadReferenceLine =
            genericsModuleBuilder
                .createRoadReferenceLine(roadspace.id, roadspace.referenceLine, roadspace.attributes)
                .handleIssueList { issueList += it }

        // transforms the lines of the center lane (id=0)
        val roadCenterLaneLines =
            roadspace.road
                .getAllCenterLanes()
                .map { genericsModuleBuilder.createRoadCenterLaneLine(it.first, it.second, it.third) }
                .mergeIssueLists()
                .handleIssueList { issueList += it }

        // transforms lane boundaries and center lines of the lanes
        val leftLaneBoundaries =
            roadspace.road
                .getAllLeftLaneBoundaries()
                .map { genericsModuleBuilder.createLeftLaneBoundary(it.first, it.second) }
                .mergeIssueLists()
                .handleIssueList { issueList += it }
        val rightLaneBoundaries =
            roadspace.road
                .getAllRightLaneBoundaries()
                .map { genericsModuleBuilder.createRightLaneBoundary(it.first, it.second) }
                .mergeIssueLists()
                .handleIssueList { issueList += it }
        val laneCenterLines =
            roadspace.road
                .getAllCurvesOnLanes(0.5)
                .map { genericsModuleBuilder.createCenterLaneLine(it.first, it.second) }
                .mergeIssueLists()
                .handleIssueList { issueList += it }

        val additionalRoadLines =
            listOf(roadReferenceLine) + roadCenterLaneLines + leftLaneBoundaries +
                rightLaneBoundaries + laneCenterLines
        return ContextIssueList(additionalRoadLines, issueList)
    }

    private fun addRoadspace(
        roadspace: Roadspace,
        roadspacesModel: RoadspacesModel,
        dstTransportationSpace: AbstractTransportationSpace,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        roadspace.road.getAllLeftRightLaneIdentifiers().forEach { laneId ->
            val longitudinalFillerSurfaces =
                if (parameters.generateLongitudinalFillerSurfaces) {
                    roadspacesModel.getLongitudinalFillerSurfaces(laneId).getOrElse { throw it }
                } else {
                    emptyList()
                }
            val relatedObjects = roadspace.roadspaceObjects.filter { it.isRelatedToLane(laneId) }
            issueList += addSingleLane(laneId, roadspace.road, longitudinalFillerSurfaces, relatedObjects, dstTransportationSpace)
        }

        roadspace.roadspaceObjects.forEach { addSingleRoadspaceObject(it, dstTransportationSpace) }
        roadspace.road.getAllLaneIdentifiers().forEach {
            issueList += addRoadMarkings(it, roadspace.road, dstTransportationSpace)
        }

        return issueList
    }

    private fun addSingleLane(
        id: LaneIdentifier,
        road: Road,
        longitudinalFillerSurfaces: List<LongitudinalFillerSurface>,
        relatedObjects: List<RoadspaceObject>,
        dstTransportationSpace: AbstractTransportationSpace,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()
        val lane =
            road
                .getLane(id)
                .getOrElse {
                    issueList +=
                        DefaultIssue.of(
                            "LaneNotConstructable",
                            "${it.message} Ignoring lane.",
                            id,
                            Severity.WARNING,
                            wasFixed = true,
                        )
                    return issueList
                }

        if (id.laneId == -3 && id.laneSectionId == 1 && id.roadspaceId == "1112000") {
            print("ok")
        }

        val surface =
            road
                .getLaneSurface(id, parameters.discretizationStepSize)
                .getOrElse {
                    issueList +=
                        DefaultIssue.of(
                            "LaneSurfaceNotConstructable",
                            "${it.message} Ignoring lane.",
                            id,
                            Severity.WARNING,
                            wasFixed = true,
                        )
                    return issueList
                }
        val extrudedSurface =
            if (parameters.generateLaneSurfaceExtrusions) {
                val trafficSpaceHeight =
                    parameters.laneSurfaceExtrusionHeightPerLaneType.getOrElse(
                        lane.type,
                    ) { parameters.laneSurfaceExtrusionHeight }

                val extrudedSurface: Option<AbstractSolid3D> =
                    road
                        .getExtrudedLaneSurface(id, parameters.discretizationStepSize, height = trafficSpaceHeight)
                        .fold({
                            issueList +=
                                DefaultIssue.of(
                                    "ExtrudedLaneSurfaceNotConstructable",
                                    "${it.message} Ignoring lane.",
                                    id,
                                    Severity.WARNING,
                                    wasFixed = true,
                                )
                            None
                        }, {
                            Some(it)
                        })
                extrudedSurface
            } else {
                None
            }

        val centerLine =
            road
                .getCurveOnLane(id, 0.5)
                .getOrElse {
                    issueList +=
                        DefaultIssue.of(
                            "CenterLineNotConstructable",
                            "${it.message} Ignoring lane.",
                            id,
                            Severity.WARNING,
                            wasFixed = true,
                        )
                    return issueList
                }
        val lateralFillerSurface =
            road
                .getLateralFillerSurface(id, parameters.discretizationStepSize)
                .getOrElse {
                    issueList +=
                        DefaultIssue.of(
                            "LateralFillerSurfaceNotConstructable",
                            "${it.message} Ignoring lane.",
                            id,
                            Severity.WARNING,
                            wasFixed = true,
                        )
                    return issueList
                }

        issueList +=
            when (LaneRouter.route(lane)) {
                LaneRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> {
                    transportationModuleBuilder.addTrafficSpaceFeature(
                        lane,
                        surface,
                        extrudedSurface,
                        centerLine,
                        lateralFillerSurface,
                        longitudinalFillerSurfaces,
                        relatedObjects,
                        dstTransportationSpace,
                    )
                }

                LaneRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> {
                    transportationModuleBuilder.addAuxiliaryTrafficSpaceFeature(
                        lane,
                        surface,
                        extrudedSurface,
                        centerLine,
                        lateralFillerSurface,
                        longitudinalFillerSurfaces,
                        dstTransportationSpace,
                    )
                }
            }

        return issueList
    }

    private fun addSingleRoadspaceObject(
        roadspaceObject: RoadspaceObject,
        dstTransportationSpace: AbstractTransportationSpace,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        when (RoadspaceObjectRouter.route(roadspaceObject)) {
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> {
                issueList += transportationModuleBuilder.addTrafficSpaceFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> {
                issueList += transportationModuleBuilder.addAuxiliaryTrafficSpaceFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_MARKING -> {
                issueList += transportationModuleBuilder.addMarkingFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.BUILDING_BUILDING -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.GENERICS_GENERICOCCUPIEDSPACE -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.VEGETATION_SOLITARYVEGETATIONOBJECT -> {}
        }

        return issueList
    }

    private fun addRoadMarkings(
        id: LaneIdentifier,
        road: Road,
        dstTransportationSpace: AbstractTransportationSpace,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()
        road
            .getRoadMarkings(id, parameters.discretizationStepSize)
            .handleLeftAndFilter {
                issueList += DefaultIssue.of("RoadMarkingNotConstructable", it.value.message!!, id, Severity.WARNING, wasFixed = true)
            }.forEachIndexed { index, (roadMarking, geometry) ->
                issueList += transportationModuleBuilder.addMarkingFeature(id, index, roadMarking, geometry, dstTransportationSpace)
            }

        return issueList
    }
}
