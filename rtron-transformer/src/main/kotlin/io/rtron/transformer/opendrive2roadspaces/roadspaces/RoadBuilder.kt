/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import io.rtron.math.geometry.curved.threed.surface.CurveRelativeParametricSurface3D
import io.rtron.math.range.Range
import io.rtron.math.range.shiftLowerEndpointTo
import io.rtron.model.opendrive.common.EContactPoint
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSection
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.road.ContactPoint
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneSection
import io.rtron.model.roadspaces.roadspace.road.LaneSectionIdentifier
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.model.roadspaces.roadspace.road.RoadLinkage
import io.rtron.model.roadspaces.topology.junction.JunctionIdentifier
import io.rtron.std.Optional
import io.rtron.std.handleFailure
import io.rtron.std.map
import io.rtron.transformer.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesConfiguration
import io.rtron.model.opendrive.road.Road as OpendriveRoad

/**
 * Builder for [Road] objects of the RoadSpaces data model.
 */
class RoadBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    private val _functionBuilder = FunctionBuilder(_reportLogger, configuration.parameters)
    private val _laneBuilder = LaneBuilder(configuration)

    // Methods

    /**
     * Builds a single road from the OpenDRIVE data model.
     *
     * @param id identifier of the road space
     * @param srcRoad source road model of OpenDRIVE
     * @param roadSurface road surface with torsion applied
     * @param roadSurfaceWithoutTorsion road surface without torsion applied (needed for lanes with true level entry)
     * @param baseAttributes attributes attached to each element of the road (e.g. lanes)
     */
    fun buildRoad(
        id: RoadspaceIdentifier,
        srcRoad: OpendriveRoad,
        roadSurface: CurveRelativeParametricSurface3D,
        roadSurfaceWithoutTorsion: CurveRelativeParametricSurface3D,
        baseAttributes: AttributeList
    ):
        Result<Road, Exception> {

            // check whether source model is processable
            srcRoad.lanes.isProcessable(configuration.parameters.tolerance)
                .map { _reportLogger.log(it, id.toString()) }
                .handleFailure { return it }

            val laneOffset = _functionBuilder.buildLaneOffset(id, srcRoad.lanes)
            val laneSections = srcRoad.lanes.getLaneSectionsWithRanges(srcRoad.length)
                .mapIndexed { currentId, currentLaneSection ->
                    buildLaneSection(
                        LaneSectionIdentifier(currentId, id),
                        currentLaneSection.first,
                        currentLaneSection.second,
                        baseAttributes
                    )
                }
                .handleFailure { return it }

            if (laneSections.isEmpty())
                return Result.error(IllegalArgumentException("Road element contains no valid lane sections."))

            val roadLinkage = buildRoadLinkage(id, srcRoad)

            val road = Road(id, roadSurface, roadSurfaceWithoutTorsion, laneOffset, laneSections, roadLinkage)
            return Result.success(road)
        }

    /**
     * Builds a [LaneSection] which corresponds to OpenDRIVE's concept of lane sections.
     */
    private fun buildLaneSection(
        laneSectionIdentifier: LaneSectionIdentifier,
        curvePositionDomain: Range<Double>,
        srcLaneSection: RoadLanesLaneSection,
        baseAttributes: AttributeList
    ):
        Result<LaneSection, Exception> {

            // check whether source model is processable
            srcLaneSection.isProcessable()
                .map { _reportLogger.log(it, laneSectionIdentifier.toString()) }
                .handleFailure { return it }

            val localCurvePositionDomain = curvePositionDomain.shiftLowerEndpointTo(0.0)

            val laneSectionAttributes = buildAttributes(srcLaneSection)
            val lanes = srcLaneSection.getLeftRightLanes()
                .map { (currentLaneId, currentSrcLane) ->
                    val laneIdentifier = LaneIdentifier(currentLaneId, laneSectionIdentifier)
                    val attributes = baseAttributes + laneSectionAttributes
                    _laneBuilder.buildLane(laneIdentifier, localCurvePositionDomain, currentSrcLane, attributes)
                }

            val centerLane = _laneBuilder.buildCenterLane(
                laneSectionIdentifier,
                localCurvePositionDomain,
                srcLaneSection.center.lane,
                baseAttributes
            )

            val laneSection = LaneSection(laneSectionIdentifier, curvePositionDomain, lanes, centerLane)
            return Result.success(laneSection)
        }

    private fun buildRoadLinkage(id: RoadspaceIdentifier, srcRoad: OpendriveRoad): RoadLinkage {

        val belongsToJunctionId = srcRoad.getJunction()
            .map { JunctionIdentifier(it, id.modelIdentifier) }

        val predecessorRoadspaceId = srcRoad.link.predecessor.getRoadPredecessorSuccessor()
            .map { RoadspaceIdentifier(it, id.modelIdentifier) }
        val predecessorJunctionId = srcRoad.link.predecessor.getJunctionPredecessorSuccessor()
            .map { JunctionIdentifier(it, id.modelIdentifier) }
        val predecessorContactPoint = srcRoad.link.predecessor
            .contactPoint.toContactPoint(default = Optional(ContactPoint.START))

        val successorRoadspaceId = srcRoad.link.successor.getRoadPredecessorSuccessor()
            .map { RoadspaceIdentifier(it, id.modelIdentifier) }
        val successorJunctionId = srcRoad.link.successor.getJunctionPredecessorSuccessor()
            .map { JunctionIdentifier(it, id.modelIdentifier) }
        val successorContactPoint = srcRoad.link.successor.contactPoint
            .toContactPoint(default = Optional(ContactPoint.START))

        return RoadLinkage(
            belongsToJunctionId,
            predecessorRoadspaceId,
            predecessorJunctionId,
            predecessorContactPoint,
            successorRoadspaceId,
            successorJunctionId,
            successorContactPoint
        )
    }

    private fun buildAttributes(srcLaneSection: RoadLanesLaneSection) =
        attributes("${configuration.parameters.attributesPrefix}laneSection_") {
            attribute("curvePositionStart", srcLaneSection.laneSectionStart.curvePosition)
        }
}

fun EContactPoint.toContactPoint(default: Optional<ContactPoint> = Optional.empty()) =
    when (this) {
        EContactPoint.START -> Optional(ContactPoint.START)
        EContactPoint.END -> Optional(ContactPoint.END)
        else -> default
    }
