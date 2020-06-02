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

package io.rtron.math.geometry.curved.oned.point

import io.rtron.math.geometry.curved.threed.point.CurveRelativePoint3D
import io.rtron.math.geometry.curved.twod.point.CurveRelativePoint2D


/**
 * Point in a curve relative coordinate system in 1D. This means that only points can be referenced which are positioned
 * on the curve.
 *
 * @param curvePosition distance between the start of the curve and the point to be referenced
 */
data class CurveRelativePoint1D(
        val curvePosition: Double
) : Comparable<CurveRelativePoint1D> {

    // Properties and Initializers
    init {
        require(curvePosition.isFinite()) { "Curve position value must be finite." }
    }

    // Operators
    operator fun plus(v: CurveRelativePoint1D) = CurveRelativePoint1D(this.curvePosition + v.curvePosition)
    operator fun minus(v: CurveRelativePoint1D) = CurveRelativePoint1D(this.curvePosition - v.curvePosition)

    override fun compareTo(other: CurveRelativePoint1D): Int = curvePosition.compareTo(other.curvePosition)

    // Conversions
    fun toCurveRelative2D(lateralOffset: Double = 0.0) =
            CurveRelativePoint2D(curvePosition, lateralOffset)

    fun toCurveRelative3D(lateralOffset: Double = 0.0, heightOffset: Double = 0.0) =
            CurveRelativePoint3D(curvePosition, lateralOffset, heightOffset)

    companion object {
        val ZERO = CurveRelativePoint1D(0.0)
    }

}
