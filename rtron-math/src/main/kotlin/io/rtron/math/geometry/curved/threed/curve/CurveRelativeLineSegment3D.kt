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

package io.rtron.math.geometry.curved.threed.curve

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.curved.threed.CurveRelativeAbstractGeometry3D
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.math.geometry.euclidean.threed.curve.LineSegment3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.BoundType
import io.rtron.math.range.Tolerable

/**
 * Line segment in a curve relative coordinate system in 3D.
 *
 * @param start start of the line segment
 * @param end end of the line segment
 * @param endBoundType
 * @param tolerance allowed tolerance
 */
class CurveRelativeLineSegment3D(
    val start: CurveRelativeVector3D,
    val end: CurveRelativeVector3D,
    override val tolerance: Double,
    val endBoundType: BoundType = BoundType.CLOSED
) : CurveRelativeAbstractGeometry3D(), Tolerable {

    // Properties and Initializers
    init {
        require(start != end) { "Start and end vector must not be identical." }
        require(start.fuzzyUnequals(end, tolerance)) { "Start and end vector must be different by at least the tolerance threshold." }
    }

    companion object {

        /**
         * Creates a [LineSegment3D], if [start] and [end] [Vector3D] are not fuzzily equal according to the [tolerance].
         *
         */
        fun of(
            start: CurveRelativeVector3D,
            end: CurveRelativeVector3D,
            tolerance: Double,
            endBoundType: BoundType = BoundType.CLOSED
        ): Result<CurveRelativeLineSegment3D, IllegalArgumentException> =
            if (start.fuzzyEquals(end, tolerance))
                Result.error(
                    IllegalArgumentException(
                        "Start and end vector of a line segment must be different " +
                            "according to the given tolerance."
                    )
                )
            else Result.success(CurveRelativeLineSegment3D(start, end, tolerance, endBoundType))
    }
}
