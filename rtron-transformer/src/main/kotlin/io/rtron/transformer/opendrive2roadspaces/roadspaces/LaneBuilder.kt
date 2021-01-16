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

import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.ConcatenatedFunction
import io.rtron.math.analysis.function.univariate.pure.ConstantFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.range.Range
import io.rtron.math.std.fuzzyEquals
import io.rtron.model.opendrive.common.ERoadMarkType
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionLCRLaneRoadMark
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionLRLane
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionLRLaneHeight
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.road.*
import io.rtron.std.filterToStrictSortingBy
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
     * @param srcLane lane object of the OpenDRIVE data model
     * @param baseAttributes attributes attached to the transformed [Lane] object
     */
    fun buildLane(
        id: LaneIdentifier,
        curvePositionDomain: Range<Double>,
        srcLane: RoadLanesLaneSectionLRLane,
        baseAttributes: AttributeList
    ): Lane {

        // build lane geometry
        val width = _functionBuilder.buildLaneWidth(id, srcLane.width)
        val laneHeightOffsets = buildLaneHeightOffset(id, srcLane.height)

        // build road markings
        val roadMarkings = buildRoadMarkings(id, curvePositionDomain, srcLane.roadMark)

        // lane topology
        val predecessors = srcLane.link.predecessor.map { it.id }
        val successors = srcLane.link.successor.map { it.id }

        // build lane attributes
        val attributes = baseAttributes + buildAttributes(srcLane)

        // build up lane object
        return Lane(
            id, width, laneHeightOffsets.inner, laneHeightOffsets.outer, srcLane.level, roadMarkings,
            predecessors, successors, attributes
        )
    }

    /**
     * Builds a center lane of a lane section.
     *
     * @param id identifier of the center lane
     * @param curvePositionDomain curve position domain (relative to the lane section) where the lane is defined
     * @param srcLanes center lane object of the OpenDRIVE data model
     * @param baseAttributes attributes attached to the transformed [Lane] object
     */
    fun buildCenterLane(
        id: LaneSectionIdentifier,
        curvePositionDomain: Range<Double>,
        srcLanes: List<RoadLanesLaneSectionCenterLane>,
        baseAttributes: AttributeList
    ): CenterLane {
        val laneIdentifier = LaneIdentifier(0, id)

        if (srcLanes.isEmpty()) {
            _reportLogger.info("Lane section contains no center lane.", id.toString())
            return CenterLane(laneIdentifier)
        }
        if (srcLanes.size > 1)
            _reportLogger.info(
                "Lane section contains multiple center lanes, " +
                    "but should contain only one.",
                id.toString()
            )

        val srcLane = srcLanes.first()
        if (srcLane.id != 0)
            _reportLogger.info("Center lane should have id 0, but has ${srcLane.id}.", id.toString())

        val roadMarkings = buildRoadMarkings(laneIdentifier, curvePositionDomain, srcLane.roadMark)
        val attributes = baseAttributes + buildAttributes(srcLane)
        return CenterLane(laneIdentifier, srcLane.level, roadMarkings, attributes)
    }

    /**
     * Small helper class containing the height offset functions of the inner and outer lane boundary.
     */
    private data class LaneHeightOffset(val inner: UnivariateFunction, val outer: UnivariateFunction)

    /**
     * Builds up the height offset function for the inner and outer lane boundary.
     *
     * @param srcLaneHeights lane height entries of the OpenDRIVE data model
     */
    private fun buildLaneHeightOffset(id: LaneIdentifier, srcLaneHeights: List<RoadLanesLaneSectionLRLaneHeight>):
        LaneHeightOffset {

            // remove consecutively duplicated height entries
            val heightEntriesDistinct = srcLaneHeights.filterToStrictSortingBy { it.sOffset }
            if (heightEntriesDistinct.size < srcLaneHeights.size)
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
                heightEntriesAdjusted.map { it.inner }
            )

            val outer = if (heightEntriesAdjusted.isEmpty()) LinearFunction.X_AXIS
            else ConcatenatedFunction.ofLinearFunctions(
                heightEntriesAdjusted.map { it.sOffset },
                heightEntriesAdjusted.map { it.outer }
            )

            return LaneHeightOffset(inner, outer)
        }

    /**
     * Builds a list of road markings ([srcRoadMark]).
     *
     * @param id identifier of the lane for which the road markings are built
     * @param curvePositionDomain curve position domain (relative to the lane section) where the road markings is defined
     * @param srcRoadMark road marking entries of the OpenDRIVE data model
     */
    private fun buildRoadMarkings(
        id: LaneIdentifier,
        curvePositionDomain: Range<Double>,
        srcRoadMark: List<RoadLanesLaneSectionLCRLaneRoadMark>
    ): List<RoadMarking> {

        val curvePositionDomainEnd = curvePositionDomain.upperEndpointOrNull()!!
        val adjustedSrcRoadMark = srcRoadMark
            .filter { it.sOffset in curvePositionDomain }
            .filter { !fuzzyEquals(it.sOffset, curvePositionDomainEnd, configuration.parameters.tolerance) }
        if (adjustedSrcRoadMark.size < srcRoadMark.size)
            _reportLogger.warn(
                "Road mark entries have been removed, as the sOffset is not located within " +
                    "the local curve position domain ($curvePositionDomain) of the lane section.",
                id.toString()
            )

        if (adjustedSrcRoadMark.isEmpty()) return emptyList()

        return adjustedSrcRoadMark.zipWithNext()
            .filter { it.first.typeAttribute != ERoadMarkType.NONE }
            .map { buildRoadMarking(it.first, it.second.sOffset) } +
            if (adjustedSrcRoadMark.last().typeAttribute != ERoadMarkType.NONE)
                listOf(buildRoadMarking(adjustedSrcRoadMark.last())) else emptyList()
    }

    /**
     * Builds an individual road marking [srcRoadMark].
     *
     * @param srcRoadMark road mark entry of the OpenDRIVE data model
     * @param domainEndpoint upper domain endpoint for the domain of the road mark
     */
    private fun buildRoadMarking(srcRoadMark: RoadLanesLaneSectionLCRLaneRoadMark, domainEndpoint: Double = Double.NaN):
        RoadMarking {

            val domain = if (domainEndpoint.isNaN()) Range.atLeast(srcRoadMark.sOffset)
            else Range.closed(srcRoadMark.sOffset, domainEndpoint)

            val width = ConstantFunction(srcRoadMark.width, domain)

            val attributes = attributes("${configuration.parameters.attributesPrefix}roadMarking") {
                attribute("_curvePositionStart", srcRoadMark.sOffset)
                attribute("_width", srcRoadMark.width)
                attribute("_type", srcRoadMark.typeAttribute.toString())
                attribute("_weight", srcRoadMark.weight.toString())
                attribute("_color", srcRoadMark.color.toString())
                attribute("_material", srcRoadMark.material)
            }

            return RoadMarking(width, attributes)
        }

    private fun buildAttributes(srcCenterLane: RoadLanesLaneSectionCenterLane) =
        attributes("${configuration.parameters.attributesPrefix}lane_") {
            attribute("type", srcCenterLane.type.toString())
            attribute("level", srcCenterLane.level)

            attributes("predecessor_lane") {
                srcCenterLane.link.predecessor.forEachIndexed { i, element ->
                    attribute("_$i", element.id)
                }
            }
            attributes("successor_lane") {
                srcCenterLane.link.successor.forEachIndexed { i, element ->
                    attribute("_$i", element.id)
                }
            }
        }

    private fun buildAttributes(srcLeftRightLane: RoadLanesLaneSectionLRLane) =
        attributes("${configuration.parameters.attributesPrefix}lane_") {
            attribute("type", srcLeftRightLane.type.toString())
            attribute("level", srcLeftRightLane.level)

            attributes("predecessor_lane") {
                srcLeftRightLane.link.predecessor.forEachIndexed { i, element ->
                    attribute("_$i", element.id)
                }
            }
            attributes("successor_lane") {
                srcLeftRightLane.link.successor.forEachIndexed { i, element ->
                    attribute("_$i", element.id)
                }
            }

            attributes("material") {
                srcLeftRightLane.material.forEachIndexed { i, element ->
                    attribute("_curvePositionStart_$i", element.sOffset)
                    attribute("_surface_$i", element.surface)
                    attribute("_friction_$i", element.friction)
                    attribute("_roughness_$i", element.roughness)
                }
            }

            attributes("speed") {
                srcLeftRightLane.speed.forEachIndexed { i, element ->
                    attribute("_curvePositionStart_$i", element.sOffset)
                    attribute("_max_$i", element.max, element.unit.toUnitOfMeasure())
                }
            }

            attributes("heightOffset") {
                srcLeftRightLane.height.forEachIndexed { i, element ->
                    attribute("_curvePositionStart_$i", element.sOffset)
                    attribute("_inner_$i", element.inner)
                    attribute("_outer_$i", element.outer)
                }
            }
        }
}
