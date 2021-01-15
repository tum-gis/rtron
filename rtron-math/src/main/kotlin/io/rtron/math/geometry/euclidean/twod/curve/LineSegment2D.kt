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
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.BoundType
import io.rtron.math.range.Range
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import org.apache.commons.math3.geometry.euclidean.twod.Line as CMLine2D
import org.apache.commons.math3.geometry.euclidean.twod.Segment as CMSegment2D


/**
 * Line segment starting in the origin in the direction of the x axis. It has a given [length] and is moved by
 * means of the [affineSequence].
 *
 * @param length length of line segment
 */
class LineSegment2D(
        length: Double,
        override val tolerance: Double,
        override val affineSequence: AffineSequence2D = AffineSequence2D.EMPTY,
        endBoundType: BoundType = BoundType.OPEN
) : AbstractCurve2D() {

    // Properties and Initializers
    init {
        require(length.isFinite() && length > 0.0) { "Length must be finite and greater than zero." }
    }

    override val domain by lazy { Range.closedX(0.0, length, endBoundType) }

    /** end point in local coordinate system */
    private val _endPoint = Vector2D.X_AXIS.scalarMultiply(length)

    /** adapted line class of Apache Commons Math */
    private val _line = CMLine2D(Vector2D.ZERO.toVector2DCm(), _endPoint.toVector2DCm(), tolerance)

    /** adapted line segment class of Apache Commons Math */
    private val _segment2D = CMSegment2D(Vector2D.ZERO.toVector2DCm(), _endPoint.toVector2DCm(), _line)


    // Methods
    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
            Result<Vector2D, Exception> {

        val point = Vector2D(curveRelativePoint.curvePosition, 0.0)
        return Result.success(point)
    }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
            Result<Rotation2D, Exception> = Result.success(Rotation2D.ZERO)


    // Conversions

    /** Returns adapted line segment class of Commons Math. */
    fun toLineSegment2DCM() = _segment2D


    companion object {

        /**
         * Creates a [LineSegment2D] based on a [start] and a [end] point.
         *
         * @param start start of line segment
         * @param end end of line segment
         * @return returned [LineSegment2D] comprises an affine transformation matrix
         */
        fun of(start: Vector2D, end: Vector2D, tolerance: Double): LineSegment2D {
            val length = end.distance(start)
            val pose = Pose2D(start, Vector2D.X_AXIS.angle(end - start))
            val affineSequence = AffineSequence2D.of(Affine2D.of(pose))

            return LineSegment2D(length, tolerance, affineSequence)
        }
    }
}
