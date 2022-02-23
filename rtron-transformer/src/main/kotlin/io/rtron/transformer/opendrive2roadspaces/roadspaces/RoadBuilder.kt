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

import arrow.core.Either
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.none
import com.github.kittinunf.result.map
import io.rtron.io.logging.LogManager
import io.rtron.math.geometry.curved.threed.surface.CurveRelativeParametricSurface3D
import io.rtron.math.range.Range
import io.rtron.math.range.shiftLowerEndpointTo
import io.rtron.model.opendrive.common.EContactPoint
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSection
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
import io.rtron.std.handleFailure
import io.rtron.std.toResult
import io.rtron.transformer.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
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
        Either<Exception, Road> {

        // check whether source model is processable
        road.lanes.isProcessable(configuration.tolerance)
            .map { _reportLogger.log(it, id.toString()) }
            .toResult()
            .handleFailure { return Either.Left(it.error) }

        val laneOffset = _functionBuilder.buildLaneOffset(id, road.lanes)
        val laneSections = road.lanes.getLaneSectionsWithRanges(road.length)
            .mapIndexed { currentId, currentLaneSection ->
                buildLaneSection(
                    LaneSectionIdentifier(currentId, id),
                    currentLaneSection.first,
                    currentLaneSection.second,
                    baseAttributes
                )
            }
            .map { it.toResult() }
            .handleFailure { return Either.Left(it.error) }

        if (laneSections.isEmpty())
            return Either.Left(IllegalArgumentException("Road element contains no valid lane sections."))

        val roadLinkage = buildRoadLinkage(id, road)

        val roadspaceRoad = Road(id, roadSurface, roadSurfaceWithoutTorsion, laneOffset, laneSections, roadLinkage)
        return Either.Right(roadspaceRoad)
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
        Either<Exception, LaneSection> {

        // check whether source model is processable
        laneSection.isProcessable()
            .map { _reportLogger.log(it, laneSectionIdentifier.toString()) }
            .toResult()
            .handleFailure { return Either.Left(it.error) }

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
            laneSection.center.lane,
            baseAttributes
        )

        val roadspaceLaneSection = LaneSection(laneSectionIdentifier, curvePositionDomain, lanes, centerLane)
        return Either.Right(roadspaceLaneSection)
    }

    private fun buildRoadLinkage(id: RoadspaceIdentifier, road: OpendriveRoad): RoadLinkage {

        val belongsToJunctionId = road.getJunction()
            .map { JunctionIdentifier(it, id.modelIdentifier) }

        val predecessorRoadspaceContactPointId = road.link.predecessor.getRoadPredecessorSuccessor()
            .map { RoadspaceContactPointIdentifier(it.second.toContactPoint().getOrElse { ContactPoint.START }, RoadspaceIdentifier(it.first, id.modelIdentifier)) }
        val predecessorJunctionId = road.link.predecessor.getJunctionPredecessorSuccessor()
            .map { JunctionIdentifier(it, id.modelIdentifier) }

        val successorRoadspaceContactPointId = road.link.successor.getRoadPredecessorSuccessor()
            .map { RoadspaceContactPointIdentifier(it.second.toContactPoint().getOrElse { ContactPoint.START }, RoadspaceIdentifier(it.first, id.modelIdentifier)) }
        val successorJunctionId = road.link.successor.getJunctionPredecessorSuccessor()
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

fun EContactPoint.toContactPoint(default: Option<ContactPoint> = none()) =
    when (this) {
        EContactPoint.START -> Some(ContactPoint.START)
        EContactPoint.END -> Some(ContactPoint.END)
        EContactPoint.UNKNOWN -> default
    }
