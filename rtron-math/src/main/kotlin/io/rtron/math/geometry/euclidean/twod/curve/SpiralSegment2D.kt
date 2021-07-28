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

package io.rtron.math.geometry.euclidean.twod.curve

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.BoundType
import io.rtron.math.range.Range
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import io.rtron.std.handleFailure

/**
 * Spiral curve segment within a defined [domain] that is given by the [curvatureFunction].
 * See wikipedia article on [Euler spiral](https://en.wikipedia.org/wiki/Euler_spiral).
 *
 * @param curvatureFunction describes the curvature as a function of the curvePosition
 * @param endBoundType bound type of the curve segment's end
 *
 */
class SpiralSegment2D(
    private val curvatureFunction: LinearFunction,
    override val tolerance: Double,
    override val affineSequence: AffineSequence2D = AffineSequence2D.EMPTY,
    endBoundType: BoundType = BoundType.OPEN
) : AbstractCurve2D() {

    // Properties and Initializers

    private val lowerDomainEndpoint =
        curvatureFunction.domain.lowerEndpointResult().handleFailure { throw it.error }
    private val upperDomainEndpoint =
        curvatureFunction.domain.upperEndpointResult().handleFailure { throw it.error }
    override val domain: Range<Double> =
        Range.closedX(lowerDomainEndpoint, upperDomainEndpoint, endBoundType)

    init {
        require(lowerDomainEndpoint == 0.0) { "Lower endpoint of domain must be zero (for moving the spiral segment, the affine sequence is preferred)." }
        require(length.isFinite()) { "Length value must be finite." }
        require(length > tolerance) { "Length value must be greater than zero and the tolerance threshold." }
        require(curvatureFunction.slope.isFinite()) { "Curvature slope must be finite." }
        require(curvatureFunction.slope != 0.0) { "Curvature slope must not be zero (if it's zero use a line or an arc segment)." }
    }

    private val _spiral = Spiral2D(curvatureFunction.slope)
    private val _lengthStart =
        curvatureFunction.startValue.handleFailure { throw it.error } / curvatureFunction.slope
    private val _spiralPoseStart = _spiral.calculatePose(_lengthStart)

    // Methods

    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Result<Vector2D, Exception> = calculatePoseLocalCS(curveRelativePoint).map { it.point }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Result<Rotation2D, Exception> = calculatePoseLocalCS(curveRelativePoint).map { it.rotation }

    private fun calculatePoseLocalCS(curveRelativePoint: CurveRelativeVector1D):
        Result<Pose2D, IllegalArgumentException> {

        val poseOnUnitSpiral = _spiral.calculatePose(_lengthStart + curveRelativePoint.curvePosition)
        val poseOnUnitSpiralStartingAtOrigin = Affine2D.of(_spiralPoseStart).inverseTransform(poseOnUnitSpiral)

        return Result.success(poseOnUnitSpiralStartingAtOrigin)
    }
}

fun LinearFunction.Companion.ofSpiralCurvature(curvatureStart: Double, curvatureEnd: Double, length: Double): LinearFunction =
    ofInclusiveInterceptAndPoint(curvatureStart, length, curvatureEnd)
