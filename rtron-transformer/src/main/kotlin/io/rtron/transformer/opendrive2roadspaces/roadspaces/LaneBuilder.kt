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
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.ConcatenatedFunction
import io.rtron.math.analysis.function.univariate.pure.ConstantFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.range.Range
import io.rtron.math.range.length
import io.rtron.math.std.fuzzyEquals
import io.rtron.model.opendrive.common.ERoadMarkType
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionLCRLaneRoadMark
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionLRLane
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionLRLaneHeight
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.road.CenterLane
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneSectionIdentifier
import io.rtron.model.roadspaces.roadspace.road.RoadMarking
import io.rtron.std.filterToStrictSortingBy
import io.rtron.std.handleAndRemoveFailure
import io.rtron.transformer.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesConfiguration

/**
 * Builder for [Lane] objects of the RoadSpaces data model.
 */
class LaneBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()
    private val _functionBuilder = FunctionBuilder(_reportLogger, configuration.parameters)

    // Methods

    /**
     * Builds a single lane (which is either left or right to the center) with the [id].
     *
     * @param id identifier of the lane
     * @param curvePositionDomain curve position domain (relative to the lane section) where the lane is defined
     * @param lane lane object of the OpenDRIVE data model
     * @param baseAttributes attributes attached to the transformed [Lane] object
     */
    fun buildLane(
        id: LaneIdentifier,
        curvePositionDomain: Range<Double>,
        lane: RoadLanesLaneSectionLRLane,
        baseAttributes: AttributeList
    ): Lane {

        // build lane geometry
        val width = _functionBuilder.buildLaneWidth(id, lane.width)
        val laneHeightOffsets = buildLaneHeightOffset(id, lane.height)

        // build road markings
        val roadMarkings = buildRoadMarkings(id, curvePositionDomain, lane.roadMark)

        // lane topology
        val predecessors = lane.link.predecessor.map { it.id }
        val successors = lane.link.successor.map { it.id }

        // build lane attributes
        val type = lane.type.toLaneType()
        val attributes = baseAttributes + buildAttributes(lane)

        // build up lane object
        return Lane(
            id, width, laneHeightOffsets.inner, laneHeightOffsets.outer, lane.level, roadMarkings,
            predecessors, successors, type, attributes
        )
    }

    /**
     * Builds a center lane of a lane section.
     *
     * @param id identifier of the center lane
     * @param curvePositionDomain curve position domain (relative to the lane section) where the lane is defined
     * @param lanes center lane object of the OpenDRIVE data model
     * @param baseAttributes attributes attached to the transformed [Lane] object
     */
    fun buildCenterLane(
        id: LaneSectionIdentifier,
        curvePositionDomain: Range<Double>,
        lanes: List<RoadLanesLaneSectionCenterLane>,
        baseAttributes: AttributeList
    ): CenterLane {
        val laneIdentifier = LaneIdentifier(0, id)

        if (lanes.isEmpty()) {
            _reportLogger.info("Lane section contains no center lane.", id.toString())
            return CenterLane(laneIdentifier)
        }
        if (lanes.size > 1)
            _reportLogger.info(
                "Lane section contains multiple center lanes, " +
                    "but should contain only one.",
                id.toString()
            )

        val centerLane = lanes.first()
        if (centerLane.id != 0)
            _reportLogger.info("Center lane should have id 0, but has ${centerLane.id}.", id.toString())

        val roadMarkings = buildRoadMarkings(laneIdentifier, curvePositionDomain, centerLane.roadMark)
        val type = centerLane.type.toLaneType()
        val attributes = baseAttributes + buildAttributes(centerLane)
        return CenterLane(laneIdentifier, centerLane.level, roadMarkings, type, attributes)
    }

    /**
     * Small helper class containing the height offset functions of the inner and outer lane boundary.
     */
    private data class LaneHeightOffset(val inner: UnivariateFunction, val outer: UnivariateFunction)

    /**
     * Builds up the height offset function for the inner and outer lane boundary.
     *
     * @param laneHeights lane height entries of the OpenDRIVE data model
     */
    private fun buildLaneHeightOffset(id: LaneIdentifier, laneHeights: List<RoadLanesLaneSectionLRLaneHeight>):
        LaneHeightOffset {

            // remove consecutively duplicated height entries
            val heightEntriesDistinct = laneHeights.filterToStrictSortingBy { it.sOffset }
            if (heightEntriesDistinct.size < laneHeights.size)
                _reportLogger.info(
                    "Removing lane height entries which are placed not in strict order according " +
                        "to sOffset.",
                    id.toString()
                )

            // filter non-finite entries
            val heightEntriesAdjusted = heightEntriesDistinct
                .filter { it.inner.isFinite() && it.outer.isFinite() }
            if (heightEntriesAdjusted.size < heightEntriesDistinct.size)
                _reportLogger.warn(
                    "Removing at least one lane height entry, since no valid values are provided.",
                    id.toString()
                )

            // build the inner and outer height offset functions
            val inner = if (heightEntriesAdjusted.isEmpty()) LinearFunction.X_AXIS
            else ConcatenatedFunction.ofLinearFunctions(
                heightEntriesAdjusted.map { it.sOffset },
                heightEntriesAdjusted.map { it.inner },
                prependConstant = true
            )

            val outer = if (heightEntriesAdjusted.isEmpty()) LinearFunction.X_AXIS
            else ConcatenatedFunction.ofLinearFunctions(
                heightEntriesAdjusted.map { it.sOffset },
                heightEntriesAdjusted.map { it.outer },
                prependConstant = true
            )

            return LaneHeightOffset(inner, outer)
        }

    /**
     * Builds a list of road markings ([roadMark]).
     *
     * @param id identifier of the lane for which the road markings are built
     * @param curvePositionDomain curve position domain (relative to the lane section) where the road markings is defined
     * @param roadMark road marking entries of the OpenDRIVE data model
     */
    private fun buildRoadMarkings(
        id: LaneIdentifier,
        curvePositionDomain: Range<Double>,
        roadMark: List<RoadLanesLaneSectionLCRLaneRoadMark>
    ): List<RoadMarking> {

        val curvePositionDomainEnd = curvePositionDomain.upperEndpointOrNull()!!
        val adjustedSrcRoadMark = roadMark
            .filter { it.sOffset in curvePositionDomain }
            .filter { !fuzzyEquals(it.sOffset, curvePositionDomainEnd, configuration.parameters.tolerance) }
        if (adjustedSrcRoadMark.size < roadMark.size)
            _reportLogger.warn(
                "Road mark entries have been removed, as the sOffset is not located within " +
                    "the local curve position domain ($curvePositionDomain) of the lane section.",
                id.toString()
            )

        if (adjustedSrcRoadMark.isEmpty()) return emptyList()

        val roadMarkingResults = adjustedSrcRoadMark.zipWithNext()
            .filter { it.first.typeAttribute != ERoadMarkType.NONE }
            .map { buildRoadMarking(it.first, it.second.sOffset) } +
            if (adjustedSrcRoadMark.last().typeAttribute != ERoadMarkType.NONE)
                listOf(buildRoadMarking(adjustedSrcRoadMark.last())) else emptyList()

        return roadMarkingResults.handleAndRemoveFailure { _reportLogger.log(it, id.toString(), "Removing such road markings.") }
    }

    /**
     * Builds an individual road marking [roadMark].
     *
     * @param roadMark road mark entry of the OpenDRIVE data model
     * @param domainEndpoint upper domain endpoint for the domain of the road mark
     */
    private fun buildRoadMarking(roadMark: RoadLanesLaneSectionLCRLaneRoadMark, domainEndpoint: Double = Double.NaN):
        Result<RoadMarking, Exception> {

            val domain = if (domainEndpoint.isNaN()) Range.atLeast(roadMark.sOffset)
            else Range.closed(roadMark.sOffset, domainEndpoint)

            if (domain.length <= configuration.parameters.tolerance)
                return Result.error(IllegalStateException("Length of road marking is zero (or below tolerance threshold)."))

            val width = ConstantFunction(roadMark.width, domain)

            val attributes = attributes("${configuration.parameters.attributesPrefix}roadMarking") {
                attribute("_curvePositionStart", roadMark.sOffset)
                attribute("_width", roadMark.width)
                attribute("_type", roadMark.typeAttribute.toString())
                attribute("_weight", roadMark.weight.toString())
                attribute("_color", roadMark.color.toString())
                attribute("_material", roadMark.material)
            }

            val roadMarking = RoadMarking(width, attributes)
            return Result.success(roadMarking)
        }

    private fun buildAttributes(centerLane: RoadLanesLaneSectionCenterLane) =
        attributes("${configuration.parameters.attributesPrefix}lane_") {
            attribute("type", centerLane.type.toString())
            attribute("level", centerLane.level)

            attributes("predecessor_lane") {
                centerLane.link.predecessor.forEachIndexed { i, element ->
                    attribute("_$i", element.id)
                }
            }
            attributes("successor_lane") {
                centerLane.link.successor.forEachIndexed { i, element ->
                    attribute("_$i", element.id)
                }
            }
        }

    private fun buildAttributes(leftRightLane: RoadLanesLaneSectionLRLane) =
        attributes("${configuration.parameters.attributesPrefix}lane_") {
            attribute("type", leftRightLane.type.toString())
            attribute("level", leftRightLane.level)

            attributes("predecessor_lane") {
                leftRightLane.link.predecessor.forEachIndexed { i, element ->
                    attribute("_$i", element.id)
                }
            }
            attributes("successor_lane") {
                leftRightLane.link.successor.forEachIndexed { i, element ->
                    attribute("_$i", element.id)
                }
            }

            attributes("material") {
                leftRightLane.material.forEachIndexed { i, element ->
                    attribute("_curvePositionStart_$i", element.sOffset)
                    attribute("_surface_$i", element.surface)
                    attribute("_friction_$i", element.friction)
                    attribute("_roughness_$i", element.roughness)
                }
            }

            attributes("speed") {
                leftRightLane.speed.forEachIndexed { i, element ->
                    attribute("_curvePositionStart_$i", element.sOffset)
                    attribute("_max_$i", element.max, element.unit.toUnitOfMeasure())
                }
            }

            attributes("heightOffset") {
                leftRightLane.height.forEachIndexed { i, element ->
                    attribute("_curvePositionStart_$i", element.sOffset)
                    attribute("_inner_$i", element.inner)
                    attribute("_outer_$i", element.outer)
                }
            }
        }
}
