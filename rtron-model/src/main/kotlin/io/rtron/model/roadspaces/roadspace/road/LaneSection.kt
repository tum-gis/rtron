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

package io.rtron.model.roadspaces.roadspace.road

import arrow.core.Either
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.StackedFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.range.Range
import io.rtron.math.std.sign
import io.rtron.std.getValueResult
import io.rtron.std.handleFailure
import io.rtron.std.toEither
import io.rtron.std.toResult
import kotlin.math.abs

/**
 * Represents the section of a road in which the number of lanes and their attributes do not change.
 *
 * @param id identifier of the lane section
 * @param curvePositionDomain domain of the curve position of this lane section in comparison of the road
 * @param lanes lanes collection whereby the lane id is used as the map's key
 * @param centerLane center lane of the lane section, which has no width
 */
data class LaneSection(
    val id: LaneSectionIdentifier,
    val curvePositionDomain: Range<Double>,
    val lanes: Map<Int, Lane>,
    val centerLane: CenterLane
) {

    // Properties and Initializers
    init {
        require(curvePositionDomain.hasLowerBound()) { "Curve position domain must have a lower bound" }
        require(lanes.isNotEmpty()) { "LaneSection must contain lanes." }
        require(lanes.all { it.key == it.value.id.laneId }) { "Lane elements must be positioned according to their lane id on the map." }

        val expectedLaneIds = (lanes.keys.minOrNull()!!..lanes.keys.maxOrNull()!!)
            .toMutableList()
            .also { it.remove(0) }
        require(lanes.keys.containsAll(expectedLaneIds)) { "There must be no gaps within the given laneIds." }
    }

    val curvePositionStart get() = CurveRelativeVector1D(curvePositionDomain.lowerEndpointOrNull()!!)
    val laneList get() = lanes.toList().sortedBy { it.first }.map { it.second }

    // Secondary Constructors
    constructor(
        id: LaneSectionIdentifier,
        curvePositionDomain: Range<Double>,
        lanes: List<Lane>,
        centerLane: CenterLane
    ) :
        this(id, curvePositionDomain, lanes.associateBy { it.id.laneId }, centerLane)

    // Methods
    fun getLane(laneId: Int): Either<IllegalArgumentException, Lane> = lanes.getValueResult(laneId).toEither()
    fun getLane(laneIdentifier: LaneIdentifier): Either<IllegalArgumentException, Lane> =
        lanes.getValueResult(laneIdentifier.laneId).toEither()

    /**
     * Returns the lateral offset function located on a lane with [laneId].
     *
     * @param laneId id of requested lane
     * @param factor if the [factor] is 0.0 the inner lane boundary is returned. If the [factor] is 1.0 the outer lane
     * boundary is returned. An offset function within the middle of the lane is achieved by a [factor] of 0.5.
     */
    fun getLateralLaneOffset(laneId: Int, factor: Double): Either<Exception, UnivariateFunction> {
        val selectedLanes = (1..abs(laneId)).toList()
            .map { sign(laneId) * it }
            .map { getLane(it).toResult() }
            .handleFailure { return Either.Left(it.error) }

        val currentLane = selectedLanes.last()
        val innerLaneBoundaryOffset = selectedLanes.dropLast(1)
            .map { it.width }
            .let { if (it.isEmpty()) LinearFunction.X_AXIS else StackedFunction.ofSum(it) }

        return StackedFunction(
            listOf(innerLaneBoundaryOffset, currentLane.width),
            { sign(laneId) * (it[0] + factor * it[1]) }
        )
            .let { Either.Right(it) }
    }

    /**
     * Returns the height offset function located on lane with [laneIdentifier].
     *
     * @param laneIdentifier id of requested lane
     * @param factor If the [factor] is 0.0 the height offset of the inner lane boundary is returned. If the [factor]
     * is 1.0 the height offset of the outer lane boundary is returned. A height offset function within the middle
     * of the lane is achieved by a [factor] of 0.5.
     */
    fun getLaneHeightOffset(laneIdentifier: LaneIdentifier, factor: Double):
        Either<IllegalArgumentException, UnivariateFunction> {

        val inner = getInnerLaneHeightOffset(laneIdentifier).toResult().handleFailure { return Either.Left(it.error) }
        val outer = getOuterLaneHeightOffset(laneIdentifier).toResult().handleFailure { return Either.Left(it.error) }
        val laneHeightOffset = StackedFunction(listOf(inner, outer), { it[0] * (1.0 - factor) + it[1] * factor })
        return Either.Right(laneHeightOffset)
    }

    private fun getOuterLaneHeightOffset(laneIdentifier: LaneIdentifier):
        Either<IllegalArgumentException, UnivariateFunction> =
        getLane(laneIdentifier).map { it.outerHeightOffset }

    private fun getInnerLaneHeightOffset(laneIdentifier: LaneIdentifier):
        Either<IllegalArgumentException, UnivariateFunction> =
        getLane(laneIdentifier).map { it.innerHeightOffset }
}
