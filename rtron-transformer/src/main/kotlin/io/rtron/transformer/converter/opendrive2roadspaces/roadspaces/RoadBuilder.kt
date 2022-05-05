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

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.computations.either
import arrow.core.getOrElse
import arrow.core.sequenceEither
import io.rtron.io.logging.LogManager
import io.rtron.math.geometry.curved.threed.surface.CurveRelativeParametricSurface3D
import io.rtron.math.range.Range
import io.rtron.math.range.shiftLowerEndpointTo
import io.rtron.model.opendrive.junction.EContactPoint
import io.rtron.model.opendrive.lane.RoadLanesLaneSection
import io.rtron.model.roadspaces.junction.JunctionIdentifier
import io.rtron.model.roadspaces.roadspace.ContactPoint
import io.rtron.model.roadspaces.roadspace.RoadspaceContactPointIdentifier
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneSection
import io.rtron.model.roadspaces.roadspace.road.LaneSectionIdentifier
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.model.roadspaces.roadspace.road.RoadLinkage
import io.rtron.transformer.converter.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.model.opendrive.road.Road as OpendriveRoad

/**
 * Builder for [Road] objects of the RoadSpaces data model.
 */
class RoadBuilder(
    val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    private val _functionBuilder = FunctionBuilder(_reportLogger, configuration)
    private val _laneBuilder = LaneBuilder(configuration)

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
    ):
        Either<Exception, Road> = either.eager {

        require(road.lanes.getLaneSectionLengths(road.length).all { it >= configuration.numberTolerance }) { "All lane sections must have a length above the tolerance threshold." }

        val laneOffset = _functionBuilder.buildLaneOffset(id, road.lanes)
        val laneSections: NonEmptyList<LaneSection> = road.lanes.getLaneSectionsWithRanges(road.length)
            .mapIndexed { currentId, currentLaneSection ->
                buildLaneSection(
                    LaneSectionIdentifier(currentId, id),
                    currentLaneSection.first,
                    currentLaneSection.second,
                    baseAttributes
                )
            }.sequenceEither()
            .bind()
            .let { NonEmptyList.fromList(it) }
            .toEither { IllegalArgumentException("Road element contains no valid lane sections.") }
            .bind()

        val roadLinkage = buildRoadLinkage(id, road)

        val roadspaceRoad = Road(id, roadSurface, roadSurfaceWithoutTorsion, laneOffset, laneSections, roadLinkage)
        roadspaceRoad
    }

    /**
     * Builds a [LaneSection] which corresponds to OpenDRIVE's concept of lane sections.
     */
    private fun buildLaneSection(
        laneSectionIdentifier: LaneSectionIdentifier,
        curvePositionDomain: Range<Double>,
        laneSection: RoadLanesLaneSection,
        baseAttributes: AttributeList
    ):
        Either<Exception, LaneSection> = either.eager {
        require(laneSection.center.lane.size == 1) { "Lane section ($laneSectionIdentifier) must contain exactly one center lane." }
        require(laneSection.getNumberOfLeftLanes() + laneSection.getNumberOfRightLanes() >= 1) { "Lane section ($laneSectionIdentifier) must contain at least one left or right lane." }

        val localCurvePositionDomain = curvePositionDomain.shiftLowerEndpointTo(0.0)

        val laneSectionAttributes = buildAttributes(laneSection)
        val lanes = laneSection.getLeftRightLanes()
            .map { (currentLaneId, currentSrcLane) ->
                val laneIdentifier = LaneIdentifier(currentLaneId, laneSectionIdentifier)
                val attributes = baseAttributes + laneSectionAttributes
                _laneBuilder.buildLane(laneIdentifier, localCurvePositionDomain, currentSrcLane, attributes)
            }

        val centerLane = _laneBuilder.buildCenterLane(
            laneSectionIdentifier,
            localCurvePositionDomain,
            laneSection.center.lane.first(),
            baseAttributes
        )

        val roadspaceLaneSection = LaneSection(laneSectionIdentifier, curvePositionDomain, lanes, centerLane)
        roadspaceLaneSection
    }

    private fun buildRoadLinkage(id: RoadspaceIdentifier, road: OpendriveRoad): RoadLinkage {

        val belongsToJunctionId = road.getJunctionOption()
            .map { JunctionIdentifier(it, id.modelIdentifier) }

        val predecessorRoadspaceContactPointId = road.link // TODO: refactor
            .flatMap { it.predecessor }
            .flatMap { it.getRoadPredecessorSuccessor() }
            .map { RoadspaceContactPointIdentifier(it.second.toContactPoint().getOrElse { ContactPoint.START }, RoadspaceIdentifier(it.first, id.modelIdentifier)) }
        val predecessorJunctionId = road.link // TODO: refactor
            .flatMap { it.predecessor }
            .flatMap { it.getJunctionPredecessorSuccessor() }
            .map { JunctionIdentifier(it, id.modelIdentifier) }

        val successorRoadspaceContactPointId = road.link // TODO refactor
            .flatMap { it.successor }
            .flatMap { it.getRoadPredecessorSuccessor() }
            .map { RoadspaceContactPointIdentifier(it.second.toContactPoint().getOrElse { ContactPoint.START }, RoadspaceIdentifier(it.first, id.modelIdentifier)) }
        val successorJunctionId = road.link // TODO refactor
            .flatMap { it.successor }
            .flatMap { it.getJunctionPredecessorSuccessor() }
            .map { JunctionIdentifier(it, id.modelIdentifier) }

        return RoadLinkage(
            belongsToJunctionId,
            predecessorRoadspaceContactPointId,
            predecessorJunctionId,
            successorRoadspaceContactPointId,
            successorJunctionId,
        )
    }

    private fun buildAttributes(laneSection: RoadLanesLaneSection) =
        attributes("${configuration.attributesPrefix}laneSection_") {
            attribute("curvePositionStart", laneSection.laneSectionStart.curvePosition)
        }
}

fun EContactPoint.toContactPoint(default: Option<ContactPoint> = None) =
    when (this) {
        EContactPoint.START -> Some(ContactPoint.START)
        EContactPoint.END -> Some(ContactPoint.END)
    }
