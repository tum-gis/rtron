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
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.io.report.mergeReports
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
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.module.GenericsModuleBuilder
import io.rtron.transformer.converter.roadspaces2citygml.module.IdentifierAdder
import io.rtron.transformer.converter.roadspaces2citygml.module.TransportationModuleBuilder
import io.rtron.transformer.converter.roadspaces2citygml.router.LaneRouter
import io.rtron.transformer.converter.roadspaces2citygml.router.RoadspaceObjectRouter
import io.rtron.transformer.report.of
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
    private val _genericsModuleBuilder = GenericsModuleBuilder(configuration, identifierAdder)
    private val _transportationModuleBuilder = TransportationModuleBuilder(configuration, identifierAdder)

    // Methods

    fun transformRoad(roadspaceName: String, roadspacesModel: RoadspacesModel): ContextReport<Option<CitygmlRoad>> {
        val report = Report()

        val roadFeature = _transportationModuleBuilder.createRoad()
        if (roadspaceName.isNotEmpty())
            roadFeature.names.add(Code(roadspaceName))

        val junctions = roadspacesModel.getAllJunctionIdentifiersContainingRoadspaces(roadspaceName)
        junctions.forEach {
            report += addIntersectionOrLink(it, roadspaceName, roadspacesModel, roadFeature)
        }

        val roads = roadspacesModel.getAllRoadspaceIdentifiersNotLocatedInJunctions(roadspaceName)
        roads.forEach {
            report += addSection(it, roadspacesModel, roadFeature)
        }

        return ContextReport(Some(roadFeature), report)
    }

    private fun addIntersectionOrLink(junctionId: JunctionIdentifier, roadspaceName: String, roadspacesModel: RoadspacesModel, dstRoad: CitygmlRoad): Report {
        val report = Report()

        val roadspacesInJunction = roadspacesModel.getRoadspacesWithinJunction(junctionId)
            .getOrHandle { throw it }
            .sortedBy { it.name.getOrElse { "" } } // TODO option

        if (roadspacesInJunction.first().name.getOrElse { "" } == roadspaceName && configuration.mappingBackwardsCompatibility) { // TODO option
            roadspacesInJunction.forEach {
                report += addRoadspace(it, roadspacesModel, dstRoad)
            }
        } else if (roadspacesInJunction.first().name.getOrElse { "" } == roadspaceName && !configuration.mappingBackwardsCompatibility) { // TODO option
            val intersectionFeature = _transportationModuleBuilder.createIntersection()
            roadspacesInJunction.forEach {
                report += addRoadspace(it, roadspacesModel, intersectionFeature)
            }
            dstRoad.intersections.add(IntersectionProperty(intersectionFeature))
        } else {
            dstRoad.intersections.add(IntersectionProperty(identifierAdder.getGmlIdentifier(junctionId)))
        }

        return report
    }

    private fun addSection(roadspaceId: RoadspaceIdentifier, roadspacesModel: RoadspacesModel, dstRoad: CitygmlRoad): Report {
        val report = Report()

        val roadspace = roadspacesModel.getRoadspace(roadspaceId).getOrHandle { throw it }

        if (configuration.mappingBackwardsCompatibility) {
            report += addRoadspace(roadspace, roadspacesModel, dstRoad)
        } else {
            val sectionFeature = _transportationModuleBuilder.createSection()
            report += addRoadspace(roadspace, roadspacesModel, sectionFeature)
            dstRoad.sections.add(SectionProperty(sectionFeature))
        }

        return report
    }

    fun transformAdditionalRoadLines(roadspace: Roadspace): ContextReport<List<AbstractCityObject>> {
        val report = Report()

        // transforms the road reference line
        val roadReferenceLine = _genericsModuleBuilder
            .createGenericOccupiedSpaceFeature(roadspace.id, "RoadReferenceLine", roadspace.referenceLine, roadspace.attributes)
            .handleReport { report += it }

        // transforms the lines of the center lane (id=0)
        val roadCenterLaneLines = roadspace.road.getAllCenterLanes()
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "RoadCenterLaneLine", it.second, it.third) }
            .mergeReports()
            .handleReport { report += it }

        // transforms lane boundaries and center lines of the lanes
        val leftLaneBoundaries = roadspace.road.getAllLeftLaneBoundaries()
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "LeftLaneBoundary", it.second, AttributeList.EMPTY) }
            .mergeReports()
            .handleReport { report += it }
        val rightLaneBoundaries = roadspace.road.getAllRightLaneBoundaries()
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "RightLaneBoundary", it.second, AttributeList.EMPTY) }
            .mergeReports()
            .handleReport { report += it }
        val laneCenterLines = roadspace.road.getAllCurvesOnLanes(0.5)
            .map { _genericsModuleBuilder.createGenericOccupiedSpaceFeature(it.first, "LaneCenterLine", it.second, AttributeList.EMPTY) }
            .mergeReports()
            .handleReport { report += it }

        val additionalRoadLines = listOf(roadReferenceLine) + roadCenterLaneLines + leftLaneBoundaries + rightLaneBoundaries + laneCenterLines
        return ContextReport(additionalRoadLines, report)
    }

    private fun addRoadspace(roadspace: Roadspace, roadspacesModel: RoadspacesModel, dstTransportationSpace: AbstractTransportationSpace): Report {
        val report = Report()

        roadspace.road.getAllLeftRightLaneIdentifiers().forEach { laneId ->
            val fillerSurface =
                if (configuration.generateLongitudinalFillerSurfaces) roadspacesModel.getFillerSurfaces(laneId).getOrHandle { throw it }
                else emptyList()
            report += addSingleLane(laneId, roadspace.road, fillerSurface, dstTransportationSpace)
        }
        roadspace.roadspaceObjects.forEach { addSingleRoadspaceObject(it, dstTransportationSpace) }
        roadspace.road.getAllLaneIdentifiers().forEach {
            report += addRoadMarkings(it, roadspace.road, dstTransportationSpace)
        }

        return report
    }

    private fun addSingleLane(id: LaneIdentifier, road: Road, longitudinalFillerSurfaces: List<FillerSurface>, dstTransportationSpace: AbstractTransportationSpace): Report {
        val report = Report()
        val lane = road.getLane(id)
            .getOrHandle { report += Message.of("${it.message} Ignoring lane.", id, isFatal = false, wasHealed = true); return report }
        val surface = road.getLaneSurface(id, configuration.discretizationStepSize)
            .getOrHandle { report += Message.of("${it.message} Ignoring lane.", id, isFatal = false, wasHealed = true); return report }
        val centerLine = road.getCurveOnLane(id, 0.5)
            .getOrHandle { report += Message.of("${it.message} Ignoring lane.", id, isFatal = false, wasHealed = true); return report }
        val innerLateralFillerSurface = road.getInnerLateralFillerSurface(id, configuration.discretizationStepSize)
            .getOrHandle { report += Message.of("${it.message} Ignoring lane.", id, isFatal = false, wasHealed = true); return report }.toList()
        val fillerSurfaces = innerLateralFillerSurface + longitudinalFillerSurfaces

        when (LaneRouter.route(lane)) {
            LaneRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> {
                report += _transportationModuleBuilder.addTrafficSpaceFeature(lane, surface, centerLine, fillerSurfaces, dstTransportationSpace)
            }
            LaneRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> {
                report += _transportationModuleBuilder.addAuxiliaryTrafficSpaceFeature(lane, surface, centerLine, fillerSurfaces, dstTransportationSpace)
            }
        }

        return report
    }

    private fun addSingleRoadspaceObject(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace): Report {
        val report = Report()

        when (RoadspaceObjectRouter.route(roadspaceObject)) {
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> {
                report += _transportationModuleBuilder.addTrafficSpaceFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> {
                report += _transportationModuleBuilder.addAuxiliaryTrafficSpaceFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_MARKING -> {
                report += _transportationModuleBuilder.addMarkingFeature(roadspaceObject, dstTransportationSpace)
            }
            RoadspaceObjectRouter.CitygmlTargetFeatureType.BUILDING_BUILDING -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.GENERICS_GENERICOCCUPIEDSPACE -> {}
            RoadspaceObjectRouter.CitygmlTargetFeatureType.VEGETATION_SOLITARYVEGEATIONOBJECT -> {}
        }

        return report
    }

    private fun addRoadMarkings(id: LaneIdentifier, road: Road, dstTransportationSpace: AbstractTransportationSpace): Report {
        val report = Report()
        road.getRoadMarkings(id, configuration.discretizationStepSize)
            .handleLeftAndFilter { report += Message.of(it.value.message!!, id, isFatal = false, wasHealed = true) } //    _reportLogger.log(it, id.toString(), "Ignoring road markings.")
            .forEach {
                report += _transportationModuleBuilder.addMarkingFeature(id, it.first, it.second, dstTransportationSpace)
            }

        return report
    }
}
