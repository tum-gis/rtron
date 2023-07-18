/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.io.messages.mergeMessageLists
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
import io.rtron.transformer.messages.roadspaces.of
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
    private val parameters: Roadspaces2CitygmlParameters
) {

    // Properties and Initializers
    private val genericsModuleBuilder = GenericsModuleBuilder(parameters)
    private val transportationModuleBuilder = TransportationModuleBuilder(parameters)

    // Methods

    fun transformRoad(roadspaceName: String, roadspacesModel: RoadspacesModel): ContextMessageList<Option<CitygmlRoad>> {
        val messageList = DefaultMessageList()

        val roadFeature = transportationModuleBuilder.createRoad()
        IdentifierAdder.addIdentifier(generateRoadIdentifier(roadspaceName, parameters.gmlIdPrefix), roadFeature)
        if (roadspaceName.isNotEmpty()) {
            roadFeature.names.add(Code(roadspaceName))
        }

        val junctions = roadspacesModel.getAllJunctionIdentifiersContainingRoadspaces(roadspaceName)
        junctions.forEach {
            messageList += addIntersectionOrLink(it, roadspaceName, roadspacesModel, roadFeature)
        }

        val roads = roadspacesModel.getAllRoadspaceIdentifiersNotLocatedInJunctions(roadspaceName)
        roads.forEach {
            messageList += addSection(it, roadspacesModel, roadFeature)
        }

        return ContextMessageList(Some(roadFeature), messageList)
    }

    private fun addIntersectionOrLink(junctionId: JunctionIdentifier, roadspaceName: String, roadspacesModel: RoadspacesModel, dstRoad: CitygmlRoad): DefaultMessageList {
        val messageList = DefaultMessageList()

        val roadspacesInJunction = roadspacesModel.getRoadspacesWithinJunction(junctionId)
            .getOrElse { throw it }
            .sortedBy { it.name }

        if (roadspacesInJunction.first().name == roadspaceName && parameters.mappingBackwardsCompatibility) {
            roadspacesInJunction.forEach {
                messageList += addRoadspace(it, roadspacesModel, dstRoad)
            }
        } else if (roadspacesInJunction.first().name == roadspaceName && !parameters.mappingBackwardsCompatibility) {
            val intersectionFeature = transportationModuleBuilder.createIntersection()
            IdentifierAdder.addIdentifier(junctionId.deriveIntersectionGmlIdentifier(parameters.gmlIdPrefix), intersectionFeature)
            roadspacesInJunction.forEach {
                messageList += addRoadspace(it, roadspacesModel, intersectionFeature)
            }
            dstRoad.intersections.add(IntersectionProperty(intersectionFeature))
        } else {
            dstRoad.intersections.add(IntersectionProperty(junctionId.deriveIntersectionGmlIdentifier(parameters.gmlIdPrefix)))
        }

        return messageList
    }

    private fun addSection(roadspaceId: RoadspaceIdentifier, roadspacesModel: RoadspacesModel, dstRoad: CitygmlRoad): DefaultMessageList {
        val messageList = DefaultMessageList()

        val roadspace = roadspacesModel.getRoadspace(roadspaceId).getOrElse { throw it }

        if (parameters.mappingBackwardsCompatibility) {
            messageList += addRoadspace(roadspace, roadspacesModel, dstRoad)
        } else {
            val sectionFeature = transportationModuleBuilder.createSection()
            IdentifierAdder.addIdentifier(roadspaceId.deriveSectionGmlIdentifier(parameters.gmlIdPrefix), sectionFeature)
            messageList += addRoadspace(roadspace, roadspacesModel, sectionFeature)
            dstRoad.sections.add(SectionProperty(sectionFeature))
        }

        return messageList
    }

    fun transformAdditionalRoadLines(roadspace: Roadspace): ContextMessageList<List<AbstractCityObject>> {
        val messageList = DefaultMessageList()

        // transforms the road reference line
        val roadReferenceLine = genericsModuleBuilder
            .createRoadReferenceLine(roadspace.id, roadspace.referenceLine, roadspace.attributes)
            .handleMessageList { messageList += it }

        // transforms the lines of the center lane (id=0)
        val roadCenterLaneLines = roadspace.road.getAllCenterLanes()
            .map { genericsModuleBuilder.createRoadCenterLaneLine(it.first, it.second, it.third) }
            .mergeMessageLists()
            .handleMessageList { messageList += it }

        // transforms lane boundaries and center lines of the lanes
        val leftLaneBoundaries = roadspace.road.getAllLeftLaneBoundaries()
            .map { genericsModuleBuilder.createLeftLaneBoundary(it.first, it.second) }
            .mergeMessageLists()
            .handleMessageList { messageList += it }
        val rightLaneBoundaries = roadspace.road.getAllRightLaneBoundaries()
            .map { genericsModuleBuilder.createRightLaneBoundary(it.first, it.second) }
            .mergeMessageLists()
            .handleMessageList { messageList += it }
        val laneCenterLines = roadspace.road.getAllCurvesOnLanes(0.5)
            .map { genericsModuleBuilder.createCenterLaneLine(it.first, it.second) }
            .mergeMessageLists()
            .handleMessageList { messageList += it }

        val additionalRoadLines = listOf(roadReferenceLine) + roadCenterLaneLines + leftLaneBoundaries + rightLaneBoundaries + laneCenterLines
        return ContextMessageList(additionalRoadLines, messageList)
    }

    private fun addRoadspace(roadspace: Roadspace, roadspacesModel: RoadspacesModel, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()

        if (parameters.generateLongitudinalFillerSurfaces) {
            roadspace.road.getAllLeftRightLaneIdentifiers().forEach { laneId ->
                val longitudinalFillerSurfaces =
                    roadspacesModel.getLongitudinalFillerSurfaces(laneId).getOrElse { throw it }

                val relatedObjects = roadspace.roadspaceObjects.filter { it.isRelatedToLane(laneId) }
                messageList += addSingleLane(laneId, roadspace.road, longitudinalFillerSurfaces, relatedObjects, dstTransportationSpace)
            }
        }

        roadspace.roadspaceObjects.forEach { addSingleRoadspaceObject(it, dstTransportationSpace) }
        roadspace.road.getAllLaneIdentifiers().forEach {
            messageList += addRoadMarkings(it, roadspace.road, dstTransportationSpace)
        }

        return messageList
    }

    private fun addSingleLane(id: LaneIdentifier, road: Road, longitudinalFillerSurfaces: List<LongitudinalFillerSurface>, relatedObjects: List<RoadspaceObject>, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()
        val lane = road.getLane(id)
            .getOrElse { messageList += DefaultMessage.of("", "${it.message} Ignoring lane.", id, Severity.WARNING, wasFixed = true); return messageList }
        val surface = road.getLaneSurface(id, parameters.discretizationStepSize)
            .getOrElse { messageList += DefaultMessage.of("", "${it.message} Ignoring lane.", id, Severity.WARNING, wasFixed = true); return messageList }
        val centerLine = road.getCurveOnLane(id, 0.5)
            .getOrElse { messageList += DefaultMessage.of("", "${it.message} Ignoring lane.", id, Severity.WARNING, wasFixed = true); return messageList }
        val lateralFillerSurface = road.getLateralFillerSurface(id, parameters.discretizationStepSize)
            .getOrElse { messageList += DefaultMessage.of("", "${it.message} Ignoring lane.", id, Severity.WARNING, wasFixed = true); return messageList }

        messageList += when (LaneRouter.route(lane)) {
            LaneRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> {
                transportationModuleBuilder.addTrafficSpaceFeature(lane, surface, centerLine, lateralFillerSurface, longitudinalFillerSurfaces, relatedObjects, dstTransportationSpace)
            }

            LaneRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> {
                transportationModuleBuilder.addAuxiliaryTrafficSpaceFeature(lane, surface, centerLine, lateralFillerSurface, longitudinalFillerSurfaces, dstTransportationSpace)
            }
        }

        return messageList
    }

    private fun addSingleRoadspaceObject(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()

        when (RoadspaceObjectRouter.route(roadspaceObject)) {
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> {
                messageList += transportationModuleBuilder.addTrafficSpaceFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> {
                messageList += transportationModuleBuilder.addAuxiliaryTrafficSpaceFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_MARKING -> {
                messageList += transportationModuleBuilder.addMarkingFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.BUILDING_BUILDING -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.GENERICS_GENERICOCCUPIEDSPACE -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.VEGETATION_SOLITARYVEGETATIONOBJECT -> {}
        }

        return messageList
    }

    private fun addRoadMarkings(id: LaneIdentifier, road: Road, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()
        road.getRoadMarkings(id, parameters.discretizationStepSize)
            .handleLeftAndFilter { messageList += DefaultMessage.of("", it.value.message!!, id, Severity.WARNING, wasFixed = true) } //    _reportLogger.log(it, id.toString(), "Ignoring road markings.")
            .forEachIndexed { index, (roadMarking, geometry) ->
                messageList += transportationModuleBuilder.addMarkingFeature(id, index, roadMarking, geometry, dstTransportationSpace)
            }

        return messageList
    }
}
