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

package io.rtron.math.geometry.euclidean.threed.curve

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.curved.oned.point.CurveRelativePoint1D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.BoundType
import io.rtron.math.range.Range
import org.apache.commons.math3.geometry.euclidean.threed.Segment as CMLineSegment3D


/**
 * Line segment in 3D defined by a [start] and [end] vector.
 *
 * @param start start of the line segment
 * @param end end of the line segment
 * @param endBoundType
 * @param tolerance allowed tolerance
 */
class LineSegment3D(
        val start: Vector3D,
        val end: Vector3D,
        endBoundType: BoundType = BoundType.CLOSED,
        override val tolerance: Double = 0.0
) : AbstractCurve3D() {

    // Properties and Initializers
    init {
        require(start != end) { "Start and end vector of a line must be different." }
    }

    override val domain = Range.closedX(0.0, start.distance(end), endBoundType)

    /** adapted line segment class of Apache Commons Math */
    private val _lineSegment3D by lazy {
        CMLineSegment3D(start.toVector3DCm(), end.toVector3DCm(), Line3D(start, end).toLine3DCM())
    }

    /** direction of the line segment as normalized vector */
    val direction by lazy { (end - start).normalized() }

    /** start and end vertices of the line segment as list */
    val vertices by lazy { listOf(start, end) }

    // Methods
    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativePoint1D):
            Result<Vector3D, IllegalArgumentException> {

        val point = start + (end - start).scalarMultiply(curveRelativePoint.curvePosition / length)
        return Result.success(point)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LineSegment3D

        if (start != other.start) return false
        if (end != other.end) return false
        if (length != other.length) return false
        if (domain != other.domain) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + domain.hashCode()
        return result
    }

    // Conversions

    /** Conversion to adapted Line Segment class from Apache Commons Math. */
    fun toLineSegment3DCM() = _lineSegment3D.line.toLine3D()
}
