/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.BoundType
import io.rtron.math.range.Range
import io.rtron.math.std.PI
import io.rtron.math.std.TWO_PI
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Represents an arc of a circle with a certain [curvature] which starts at the coordinates origin and continues in
 * the direction of the x axis.
 * See the wikipedia article on an [arc](https://en.wikipedia.org/wiki/Arc_(geometry)).
 *
 * @param curvature positive curvature: counter clockwise; negative curvature: clockwise
 */
class Arc2D(
    val curvature: Double,
    length: Double,
    override val tolerance: Double,
    override val affineSequence: AffineSequence2D = AffineSequence2D.EMPTY,
    endBoundType: BoundType = BoundType.OPEN
) : AbstractCurve2D() {

    // Properties and Initializers
    init {
        require(curvature.isFinite()) { "Curvature must be finite." }
        require(curvature != 0.0) { "Curvature must not be zero (use a line segment instead)." }
        require(length.isFinite()) { "Length value must be finite." }
        require(length > tolerance) { "Length value must be greater than zero and the tolerance threshold." }
    }

    override val domain: Range<Double> = Range.closedX(0.0, length, endBoundType)

    /** sign of the curvature (positive: counter clockwise, negative: clockwise) */
    private val curvatureSign get() = sign(curvature)

    /** radius of the circle */
    private val radius get() = 1.0 / curvature.absoluteValue

    /** diameter of the circle the arc is located on */
    private val diameter get() = 2.0 * radius

    /** circumference of the circle the arc is located on */
    private val circumference get() = PI * diameter

    /** aperture angle of the arc */
    private val aperture get() = Rotation2D(TWO_PI * (length / circumference))

    /** the center of the arc is located on the y axis to enable an arc starting in the origin */
    val center = Vector2D(0.0, radius * curvatureSign)

    /** start angle of the arc relative to the [center] */
    val startAngle = Vector2D.X_AXIS.angle(Vector2D.ZERO - center)

    /** end angle of the arc relative to the [center] */
    val endAngle = startAngle + aperture * Rotation2D(curvatureSign)

    // Methods
    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Vector2D {
        // angle in radians between start point of the arc and given curve position
        val curvePositionAngle =
            Rotation2D(TWO_PI * (curveRelativePoint.curvePosition / circumference) * curvatureSign)

        // calculate offset to center of the arc
        val offsetToCenterVector =
            Affine2D.of(startAngle + curvePositionAngle).transform(Vector2D.X_AXIS).scalarMultiply(radius)

        return center + offsetToCenterVector
    }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Rotation2D {
        return Rotation2D(curveRelativePoint.curvePosition * curvature)
    }

    // Conversions
    override fun toString(): String {
        return "Arc2D(curvature=$curvature, domain=$domain, length=$length)"
    }
}
