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

package io.rtron.transformer.converter.roadspaces2citygml.transformer

import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.getOrHandle
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.io.messages.mergeMessageLists
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.common.FillerSurface
import io.rtron.model.roadspaces.identifier.JunctionIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
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
    private val parameters: Roadspaces2CitygmlParameters,
    private val identifierAdder: IdentifierAdder
) {

    // Properties and Initializers
    private val _genericsModuleBuilder = GenericsModuleBuilder(parameters, identifierAdder)
    private val _transportationModuleBuilder = TransportationModuleBuilder(parameters, identifierAdder)

    // Methods

    fun transformRoad(roadspaceName: String, roadspacesModel: RoadspacesModel): ContextMessageList<Option<CitygmlRoad>> {
        val messageList = DefaultMessageList()

        val roadFeature = _transportationModuleBuilder.createRoad()
        if (roadspaceName.isNotEmpty())
            roadFeature.names.add(Code(roadspaceName))

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
            .getOrHandle { throw it }
            .sortedBy { it.name.getOrElse { "" } } // TODO option

        if (roadspacesInJunction.first().name.getOrElse { "" } == roadspaceName && parameters.mappingBackwardsCompatibility) { // TODO option
            roadspacesInJunction.forEach {
                messageList += addRoadspace(it, roadspacesModel, dstRoad)
            }
        } else if (roadspacesInJunction.first().name.getOrElse { "" } == roadspaceName && !parameters.mappingBackwardsCompatibility) { // TODO option
            val intersectionFeature = _transportationModuleBuilder.createIntersection()
            roadspacesInJunction.forEach {
                messageList += addRoadspace(it, roadspacesModel, intersectionFeature)
            }
            dstRoad.intersections.add(IntersectionProperty(intersectionFeature))
        } else {
            dstRoad.intersections.add(IntersectionProperty(identifierAdder.getGmlIdentifier(junctionId)))
        }

        return messageList
    }

    private fun addSection(roadspaceId: RoadspaceIdentifier, roadspacesModel: RoadspacesModel, dstRoad: CitygmlRoad): DefaultMessageList {
        val messageList = DefaultMessageList()

        val roadspace = roadspacesModel.getRoadspace(roadspaceId).getOrHandle { throw it }

        if (parameters.mappingBackwardsCompatibility) {
            messageList += addRoadspace(roadspace, roadspacesModel, dstRoad)
        } else {
            val sectionFeature = _transportationModuleBuilder.createSection()
            messageList += addRoadspace(roadspace, roadspacesModel, sectionFeature)
            dstRoad.sections.add(SectionProperty(sectionFeature))
        }

        return messageList
    }

    fun transformAdditionalRoadLines(roadspace: Roadspace): ContextMessageList<List<AbstractCityObject>> {
        val messageList = DefaultMessageList()

        // transforms the road reference line
        val roadReferenceLine = _genericsModuleBuilder
            .createGenericOccupiedSpaceFeature(roadspace.id, "RoadReferenceLine", roadspace.referenceLine, roadspace.attributes)
            .handleMessageList { messageList += it }

        // transforms the lines of the center lane (id=0)
        val roadCenterLaneLines = roadspace.road.getAllCenterLanes()
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "RoadCenterLaneLine", it.second, it.third) }
            .mergeMessageLists()
            .handleMessageList { messageList += it }

        // transforms lane boundaries and center lines of the lanes
        val leftLaneBoundaries = roadspace.road.getAllLeftLaneBoundaries()
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "LeftLaneBoundary", it.second, AttributeList.EMPTY) }
            .mergeMessageLists()
            .handleMessageList { messageList += it }
        val rightLaneBoundaries = roadspace.road.getAllRightLaneBoundaries()
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "RightLaneBoundary", it.second, AttributeList.EMPTY) }
            .mergeMessageLists()
            .handleMessageList { messageList += it }
        val laneCenterLines = roadspace.road.getAllCurvesOnLanes(0.5)
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "LaneCenterLine", it.second, AttributeList.EMPTY) }
            .mergeMessageLists()
            .handleMessageList { messageList += it }

        val additionalRoadLines = listOf(roadReferenceLine) + roadCenterLaneLines + leftLaneBoundaries + rightLaneBoundaries + laneCenterLines
        return ContextMessageList(additionalRoadLines, messageList)
    }

    private fun addRoadspace(roadspace: Roadspace, roadspacesModel: RoadspacesModel, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()

        roadspace.road.getAllLeftRightLaneIdentifiers().forEach { laneId ->
            val fillerSurface =
                if (parameters.generateLongitudinalFillerSurfaces) roadspacesModel.getFillerSurfaces(laneId).getOrHandle { throw it }
                else emptyList()
            messageList += addSingleLane(laneId, roadspace.road, fillerSurface, dstTransportationSpace)
        }
        roadspace.roadspaceObjects.forEach { addSingleRoadspaceObject(it, dstTransportationSpace) }
        roadspace.road.getAllLaneIdentifiers().forEach {
            messageList += addRoadMarkings(it, roadspace.road, dstTransportationSpace)
        }

        return messageList
    }

    private fun addSingleLane(id: LaneIdentifier, road: Road, longitudinalFillerSurfaces: List<FillerSurface>, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()
        val lane = road.getLane(id)
            .getOrHandle { messageList += DefaultMessage.of("", "${it.message} Ignoring lane.", id, Severity.WARNING, wasHealed = true); return messageList }
        val surface = road.getLaneSurface(id, parameters.discretizationStepSize)
            .getOrHandle { messageList += DefaultMessage.of("", "${it.message} Ignoring lane.", id, Severity.WARNING, wasHealed = true); return messageList }
        val centerLine = road.getCurveOnLane(id, 0.5)
            .getOrHandle { messageList += DefaultMessage.of("", "${it.message} Ignoring lane.", id, Severity.WARNING, wasHealed = true); return messageList }
        val innerLateralFillerSurface = road.getInnerLateralFillerSurface(id, parameters.discretizationStepSize)
            .getOrHandle { messageList += DefaultMessage.of("", "${it.message} Ignoring lane.", id, Severity.WARNING, wasHealed = true); return messageList }.toList()
        val fillerSurfaces = innerLateralFillerSurface + longitudinalFillerSurfaces

        when (LaneRouter.route(lane)) {
            LaneRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> {
                messageList += _transportationModuleBuilder.addTrafficSpaceFeature(lane, surface, centerLine, fillerSurfaces, dstTransportationSpace)
            }
            LaneRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> {
                messageList += _transportationModuleBuilder.addAuxiliaryTrafficSpaceFeature(lane, surface, centerLine, fillerSurfaces, dstTransportationSpace)
            }
        }

        return messageList
    }

    private fun addSingleRoadspaceObject(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()

        when (RoadspaceObjectRouter.route(roadspaceObject)) {
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> {
                messageList += _transportationModuleBuilder.addTrafficSpaceFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> {
                messageList += _transportationModuleBuilder.addAuxiliaryTrafficSpaceFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_MARKING -> {
                messageList += _transportationModuleBuilder.addMarkingFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.BUILDING_BUILDING -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.GENERICS_GENERICOCCUPIEDSPACE -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.VEGETATION_SOLITARYVEGEATIONOBJECT -> {}
        }

        return messageList
    }

    private fun addRoadMarkings(id: LaneIdentifier, road: Road, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()
        road.getRoadMarkings(id, parameters.discretizationStepSize)
            .handleLeftAndFilter { messageList += DefaultMessage.of("", it.value.message!!, id, Severity.WARNING, wasHealed = true) } //    _reportLogger.log(it, id.toString(), "Ignoring road markings.")
            .forEach {
                messageList += _transportationModuleBuilder.addMarkingFeature(id, it.first, it.second, dstTransportationSpace)
            }

        return messageList
    }
}
