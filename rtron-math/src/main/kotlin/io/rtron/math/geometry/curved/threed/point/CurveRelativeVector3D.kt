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

package io.rtron.math.geometry.curved.threed.point

import arrow.core.Either
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.curved.threed.CurveRelativeAbstractGeometry3D
import io.rtron.math.geometry.curved.twod.point.CurveRelativeVector2D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.std.fuzzyEquals as doubleFuzzyEquals

/**
 * Represents a vector in a curve relative coordinate system in 3D. This means that only points can be referenced which
 * are not before the curve's start or after the curve's end within the three-dimensional space.
 *
 * @param curvePosition distance between the start of the curve and the point to be referenced
 * @param lateralOffset lateral offset that is perpendicular to the curve at the [curvePosition]
 * @param heightOffset additional height offset to the curve's height
 */
data class CurveRelativeVector3D(
    val curvePosition: Double,
    val lateralOffset: Double = 0.0,
    val heightOffset: Double = 0.0
) : CurveRelativeAbstractGeometry3D() {

    // Properties and Initializers
    init {
        require(curvePosition.isFinite()) { "Curve position value must be finite." }
        require(lateralOffset.isFinite()) { "Lateral offset value must be finite." }
        require(heightOffset.isFinite()) { "Height offset value must be finite." }
    }

    // Operators

    /**
     * Returns true, if [curvePosition], [lateralOffset] and [heightOffset] are all fuzzily equal with a tolerance
     * of [tolerance].
     */
    fun fuzzyEquals(o: CurveRelativeVector3D, tolerance: Double) =
        doubleFuzzyEquals(this.curvePosition, o.curvePosition, tolerance) &&
            doubleFuzzyEquals(this.lateralOffset, o.lateralOffset, tolerance) &&
            doubleFuzzyEquals(this.heightOffset, o.heightOffset, tolerance)

    fun fuzzyUnequals(o: CurveRelativeVector3D, tolerance: Double) = !fuzzyEquals(o, tolerance)

    // Methods
    fun getCartesianCurveOffset() = Vector3D(0.0, lateralOffset, heightOffset)

    // Conversions
    fun toCurveRelative1D() = CurveRelativeVector1D(curvePosition)
    fun toCurveRelative2D() = CurveRelativeVector2D(curvePosition, lateralOffset)

    companion object {
        val ZERO = CurveRelativeVector3D(0.0, 0.0, 0.0)

        /**
         * Creates a [CurveRelativeVector3D] by a [curvePosition], [lateralOffset] and [heightOffset]. If one of the
         * values is not finite, an error is returned.
         */
        fun of(curvePosition: Double, lateralOffset: Double, heightOffset: Double):
            Either<IllegalArgumentException, CurveRelativeVector3D> =
            if (!curvePosition.isFinite() || !lateralOffset.isFinite() || !heightOffset.isFinite())
                Either.Left(IllegalArgumentException("CurvePosition, lateralOffset, heightOffset must be finite."))
            else Either.Right(CurveRelativeVector3D(curvePosition, lateralOffset, heightOffset))
    }
}
