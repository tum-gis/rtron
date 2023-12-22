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

package io.rtron.transformer.converter.opendrive2roadspaces.roadspaces

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssueList
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.threed.surface.CurveRelativeParametricSurface3D
import io.rtron.math.range.Range
import io.rtron.math.range.shiftLowerEndpointTo
import io.rtron.model.opendrive.junction.EContactPoint
import io.rtron.model.opendrive.lane.RoadLanesLaneSection
import io.rtron.model.roadspaces.identifier.JunctionIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.LaneSectionIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.ContactPoint
import io.rtron.model.roadspaces.roadspace.RoadspaceContactPointIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.road.LaneSection
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.model.roadspaces.roadspace.road.RoadLinkage
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.model.opendrive.road.Road as OpendriveRoad

/**
 * Builder for [Road] objects of the RoadSpaces data model.
 */
class RoadBuilder(
    val parameters: Opendrive2RoadspacesParameters
) {

    // Properties and Initializers
    private val laneBuilder = LaneBuilder(parameters)

    // Methods

    /**
     * Builds a single road from the OpenDRIVE data model.
     *
     * @param id identifier of the road space
     * @param road source road model of OpenDRIVE
     * @param roadSurface road surface with torsion applied
     * @param roadSurfaceWithoutTorsion road surface without torsion applied (needed for lanes with true level entry)
     * @param baseAttributes attributes attached to each element of the road (e.g. lanes)
     */
    fun buildRoad(
        id: RoadspaceIdentifier,
        road: OpendriveRoad,
        roadSurface: CurveRelativeParametricSurface3D,
        roadSurfaceWithoutTorsion: CurveRelativeParametricSurface3D,
        baseAttributes: AttributeList
    ): ContextIssueList<Road> {
        require(road.lanes.getLaneSectionLengths(road.length).all { it >= parameters.numberTolerance }) { "All lane sections must have a length above the tolerance threshold." }
        val issueList = DefaultIssueList()

        val laneOffset = road.lanes.getLaneOffsetEntries().fold({ LinearFunction.X_AXIS }, { FunctionBuilder.buildLaneOffset(it) })
        val laneSections: NonEmptyList<LaneSection> = road.lanes.getLaneSectionsWithRanges(road.length)
            .mapIndexed { currentId, currentLaneSection ->
                buildLaneSection(
                    LaneSectionIdentifier(currentId, id),
                    currentLaneSection.first,
                    currentLaneSection.second,
                    baseAttributes
                ).handleIssueList { issueList += it }
            }
            .let { it.toNonEmptyListOrNull()!! }

        val roadLinkage = buildRoadLinkage(id, road)

        val roadspaceRoad = Road(id, roadSurface, roadSurfaceWithoutTorsion, laneOffset, laneSections, roadLinkage)
        return ContextIssueList(roadspaceRoad, issueList)
    }

    /**
     * Builds a [LaneSection] which corresponds to OpenDRIVE's concept of lane sections.
     */
    private fun buildLaneSection(
        laneSectionIdentifier: LaneSectionIdentifier,
        curvePositionDomain: Range<Double>,
        laneSection: RoadLanesLaneSection,
        baseAttributes: AttributeList
    ): ContextIssueList<LaneSection> {
        require(laneSection.center.lane.size == 1) { "Lane section ($laneSectionIdentifier) must contain exactly one center lane." }
        require(laneSection.getNumberOfLeftLanes() + laneSection.getNumberOfRightLanes() >= 1) { "Lane section ($laneSectionIdentifier) must contain at least one left or right lane." }

        val issueList = DefaultIssueList()

        val localCurvePositionDomain = curvePositionDomain.shiftLowerEndpointTo(0.0)

        val laneSectionAttributes = buildAttributes(laneSection)
        val lanes = laneSection.getLeftRightLanes()
            .map { (currentLaneId, currentSrcLane) ->
                val laneIdentifier = LaneIdentifier(currentLaneId, laneSectionIdentifier)
                val attributes = baseAttributes + laneSectionAttributes
                laneBuilder.buildLane(laneIdentifier, localCurvePositionDomain, currentSrcLane, attributes)
                    .handleIssueList { issueList += it }
            }

        val centerLane = laneBuilder.buildCenterLane(
            laneSectionIdentifier,
            localCurvePositionDomain,
            laneSection.center.getIndividualCenterLane(),
            baseAttributes
        ).handleIssueList { issueList += it }

        val roadspaceLaneSection = LaneSection(laneSectionIdentifier, curvePositionDomain, lanes, centerLane)
        return ContextIssueList(roadspaceLaneSection, issueList)
    }

    private fun buildRoadLinkage(id: RoadspaceIdentifier, road: OpendriveRoad): RoadLinkage {
        val belongsToJunctionId = road.getJunctionOption()
            .map { JunctionIdentifier(it) }

        val predecessorRoadspaceContactPointId = road.link
            .flatMap { it.predecessor }
            .flatMap { it.getRoadPredecessorSuccessor() }
            .map { RoadspaceContactPointIdentifier(it.second.toContactPoint(), RoadspaceIdentifier(it.first)) }
        val predecessorJunctionId = road.link
            .flatMap { it.predecessor }
            .flatMap { it.getJunctionPredecessorSuccessor() }
            .map { JunctionIdentifier(it) }

        val successorRoadspaceContactPointId = road.link
            .flatMap { it.successor }
            .flatMap { it.getRoadPredecessorSuccessor() }
            .map { RoadspaceContactPointIdentifier(it.second.toContactPoint(), RoadspaceIdentifier(it.first)) }
        val successorJunctionId = road.link
            .flatMap { it.successor }
            .flatMap { it.getJunctionPredecessorSuccessor() }
            .map { JunctionIdentifier(it) }

        return RoadLinkage(
            belongsToJunctionId,
            predecessorRoadspaceContactPointId,
            predecessorJunctionId,
            successorRoadspaceContactPointId,
            successorJunctionId
        )
    }

    private fun buildAttributes(laneSection: RoadLanesLaneSection) =
        attributes("${parameters.attributesPrefix}laneSection_") {
            attribute("curvePositionStart", laneSection.laneSectionStart.curvePosition)
        }
}

fun EContactPoint.toContactPoint(): ContactPoint =
    when (this) {
        EContactPoint.START -> ContactPoint.START
        EContactPoint.END -> ContactPoint.END
    }
