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

package io.rtron.math.geometry.curved.twod.point

import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D

/**
 * Point in a curve relative coordinate system in 2D. This means that only points can be referenced which are on the
 * curve or laterally translated to the curve.
 *
 * @param curvePosition distance between the start of the curve and the point to be referenced
 * @param lateralOffset lateral offset that is perpendicular to the curve at the [curvePosition]
 */
data class CurveRelativeVector2D(
    val curvePosition: Double,
    val lateralOffset: Double = 0.0
) {

    // Properties and Initializers
    init {
        require(curvePosition.isFinite()) { "Curve position value must be finite." }
        require(lateralOffset.isFinite()) { "Lateral offset value must be finite." }
    }

    // Conversions
    fun toCurveRelative1D() = CurveRelativeVector1D(this.curvePosition)
    fun toCurveRelative3D(heightOffset: Double = 0.0) =
        CurveRelativeVector3D(this.curvePosition, this.lateralOffset, heightOffset)

    companion object {
        val ZERO = CurveRelativeVector2D(0.0, 0.0)
    }
}
