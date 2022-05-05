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
import io.rtron.io.logging.LogManager
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.common.FillerSurface
import io.rtron.model.roadspaces.junction.JunctionIdentifier
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.std.handleAndRemoveFailure
import io.rtron.std.handleFailure
import io.rtron.std.toEither
import io.rtron.std.toResult
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.module.GenericsModuleBuilder
import io.rtron.transformer.converter.roadspaces2citygml.module.IdentifierAdder
import io.rtron.transformer.converter.roadspaces2citygml.module.TransportationModuleBuilder
import io.rtron.transformer.converter.roadspaces2citygml.router.LaneRouter
import io.rtron.transformer.converter.roadspaces2citygml.router.RoadspaceObjectRouter
import org.citygml4j.model.core.AbstractCityObject
import org.citygml4j.model.core.CityModel
import org.citygml4j.model.transportation.AbstractTransportationSpace
import org.citygml4j.model.transportation.IntersectionProperty
import org.citygml4j.model.transportation.SectionProperty
import org.xmlobjects.gml.model.basictypes.Code
import org.citygml4j.model.transportation.Road as CitygmlRoad

/**
 * Transforms [Road] classes (RoadSpaces model) to the [CityModel] (CityGML model).
 */
class RoadsTransformer(
    private val configuration: Roadspaces2CitygmlConfiguration,
    private val identifierAdder: IdentifierAdder
) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    private val _genericsModuleBuilder = GenericsModuleBuilder(configuration, identifierAdder)
    private val _transportationModuleBuilder = TransportationModuleBuilder(configuration, identifierAdder)

    // Methods

    fun transformRoad(roadspaceName: String, roadspacesModel: RoadspacesModel): Option<CitygmlRoad> {
        val roadFeature = _transportationModuleBuilder.createRoad()
        if (roadspaceName.isNotEmpty())
            roadFeature.names.add(Code(roadspaceName))

        val junctions = roadspacesModel.getAllJunctionIdentifiersContainingRoadspaces(roadspaceName)
        junctions.forEach { addIntersectionOrLink(it, roadspaceName, roadspacesModel, roadFeature) }

        val roads = roadspacesModel.getAllRoadspaceIdentifiersNotLocatedInJunctions(roadspaceName)
        roads.forEach { addSection(it, roadspacesModel, roadFeature) }

        return Some(roadFeature)
    }

    private fun addIntersectionOrLink(junctionId: JunctionIdentifier, roadspaceName: String, roadspacesModel: RoadspacesModel, dstRoad: CitygmlRoad) {
        val roadspacesInJunction = roadspacesModel.getRoadspacesWithinJunction(junctionId)
            .toResult()
            .handleFailure { throw it.error }
            .sortedBy { it.name.getOrElse { "" } } // TODO option

        if (roadspacesInJunction.first().name.getOrElse { "" } == roadspaceName && configuration.mappingBackwardsCompatibility) { // TODO option
            roadspacesInJunction.forEach { addRoadspace(it, roadspacesModel, dstRoad) }
        } else if (roadspacesInJunction.first().name.getOrElse { "" } == roadspaceName && !configuration.mappingBackwardsCompatibility) { // TODO option
            val intersectionFeature = _transportationModuleBuilder.createIntersection()
            roadspacesInJunction.forEach { addRoadspace(it, roadspacesModel, intersectionFeature) }
            dstRoad.intersections.add(IntersectionProperty(intersectionFeature))
        } else {
            dstRoad.intersections.add(IntersectionProperty(identifierAdder.getGmlIdentifier(junctionId)))
        }
    }

    private fun addSection(roadspaceId: RoadspaceIdentifier, roadspacesModel: RoadspacesModel, dstRoad: CitygmlRoad) {
        val roadspace = roadspacesModel.getRoadspace(roadspaceId).handleFailure { throw it.error }

        if (configuration.mappingBackwardsCompatibility) {
            addRoadspace(roadspace, roadspacesModel, dstRoad)
        } else {
            val sectionFeature = _transportationModuleBuilder.createSection()
            addRoadspace(roadspace, roadspacesModel, sectionFeature)
            dstRoad.sections.add(SectionProperty(sectionFeature))
        }
    }

    fun transformAdditionalRoadLines(roadspace: Roadspace): List<AbstractCityObject> {

        // transforms the road reference line
        val roadReferenceLine = _genericsModuleBuilder
            .createGenericOccupiedSpaceFeature(roadspace.id, "RoadReferenceLine", roadspace.referenceLine, roadspace.attributes)
            .toResult()
            .handleFailure { throw it.error }

        // transforms the lines of the center lane (id=0)
        val roadCenterLaneLines = roadspace.road.getAllCenterLanes()
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "RoadCenterLaneLine", it.second, it.third).toResult() }
            .handleAndRemoveFailure { _reportLogger.log(it.toEither()) }

        // transforms lane boundaries and center lines of the lanes
        val leftLaneBoundaries = roadspace.road.getAllLeftLaneBoundaries()
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "LeftLaneBoundary", it.second, AttributeList.EMPTY).toResult() }
            .handleAndRemoveFailure { _reportLogger.log(it.toEither()) }
        val rightLaneBoundaries = roadspace.road.getAllRightLaneBoundaries()
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "RightLaneBoundary", it.second, AttributeList.EMPTY).toResult() }
            .handleAndRemoveFailure { _reportLogger.log(it.toEither()) }
        val laneCenterLines = roadspace.road.getAllCurvesOnLanes(0.5)
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "LaneCenterLine", it.second, AttributeList.EMPTY).toResult() }
            .handleAndRemoveFailure { _reportLogger.log(it.toEither()) }

        return listOf(roadReferenceLine) + roadCenterLaneLines + leftLaneBoundaries + rightLaneBoundaries + laneCenterLines
    }

    private fun addRoadspace(roadspace: Roadspace, roadspacesModel: RoadspacesModel, dstTransportationSpace: AbstractTransportationSpace) {
        roadspace.road.getAllLeftRightLaneIdentifiers().forEach { laneId ->
            val fillerSurface =
                if (configuration.generateLongitudinalFillerSurfaces) roadspacesModel.getFillerSurfaces(laneId).toResult().handleFailure { throw it.error }
                else emptyList()
            addSingleLane(laneId, roadspace.road, fillerSurface, dstTransportationSpace)
        }
        roadspace.roadspaceObjects.forEach { addSingleRoadspaceObject(it, dstTransportationSpace) }
        roadspace.road.getAllLaneIdentifiers().forEach { addRoadMarkings(it, roadspace.road, dstTransportationSpace) }
    }

    private fun addSingleLane(id: LaneIdentifier, road: Road, longitudinalFillerSurfaces: List<FillerSurface>, dstTransportationSpace: AbstractTransportationSpace) {
        val lane = road.getLane(id)
            .toResult()
            .handleFailure { _reportLogger.log(it.toEither(), id.toString(), "Ignoring lane."); return }
        val surface = road.getLaneSurface(id, configuration.discretizationStepSize)
            .toResult()
            .handleFailure { _reportLogger.log(it.toEither(), id.toString(), "Ignoring lane."); return }
        val centerLine = road.getCurveOnLane(id, 0.5)
            .toResult()
            .handleFailure { _reportLogger.log(it.toEither(), id.toString(), "Ignoring lane."); return }
        val innerLateralFillerSurface = road.getInnerLateralFillerSurface(id, configuration.discretizationStepSize)
            .toResult()
            .handleFailure { _reportLogger.log(it.toEither(), id.toString(), "Ignoring lane."); return }.toList()
        val fillerSurfaces = innerLateralFillerSurface + longitudinalFillerSurfaces

        when (LaneRouter.route(lane)) {
            LaneRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE ->
                _transportationModuleBuilder.addTrafficSpaceFeature(lane, surface, centerLine, fillerSurfaces, dstTransportationSpace)
            LaneRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE ->
                _transportationModuleBuilder.addAuxiliaryTrafficSpaceFeature(lane, surface, centerLine, fillerSurfaces, dstTransportationSpace)
        }
    }

    private fun addSingleRoadspaceObject(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace) {
        when (RoadspaceObjectRouter.route(roadspaceObject)) {
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> _transportationModuleBuilder.addTrafficSpaceFeature(roadspaceObject, dstTransportationSpace)
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> _transportationModuleBuilder.addAuxiliaryTrafficSpaceFeature(roadspaceObject, dstTransportationSpace)
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_MARKING -> _transportationModuleBuilder.addMarkingFeature(roadspaceObject, dstTransportationSpace)
            RoadspaceObjectRouter.CitygmlTargetFeatureType.BUILDING_BUILDING -> return
            RoadspaceObjectRouter.CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE -> return
            RoadspaceObjectRouter.CitygmlTargetFeatureType.GENERICS_GENERICOCCUPIEDSPACE -> return
            RoadspaceObjectRouter.CitygmlTargetFeatureType.VEGETATION_SOLITARYVEGEATIONOBJECT -> return
        }
    }

    private fun addRoadMarkings(id: LaneIdentifier, road: Road, dstTransportationSpace: AbstractTransportationSpace) {
        road.getRoadMarkings(id, configuration.discretizationStepSize)
            .map { it.toResult() }
            .handleAndRemoveFailure { _reportLogger.log(it.toEither(), id.toString(), "Ignoring road markings.") }
            .forEach { _transportationModuleBuilder.addMarkingFeature(id, it.first, it.second, dstTransportationSpace) }
    }
}
