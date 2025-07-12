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

package io.rtron.math.geometry.euclidean.threed.curve

import arrow.core.getOrElse
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.curved.threed.surface.AbstractCurveRelativeSurface3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.Range
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.range.intersectingRange

/**
 * Curve that lies on a parametric surface. This curve is parallel to the [baseSurface]'s curve but defined by a
 * laterally translated by a [lateralOffsetFunction] and vertically translated by a [heightOffsetFunction].
 *
 * If the domain of [lateralOffsetFunction] and/or [heightOffsetFunction] is not defined everywhere where the
 * [baseSurface] is defined, the [CurveOnParametricSurface3D] is only defined, where all domains overlap.
 *
 * @param baseSurface the base surface on which this curve lies
 * @param lateralOffsetFunction lateral offset to the curve of the [baseSurface]
 * @param heightOffsetFunction height offset to the curve of the [baseSurface]
 */
data class CurveOnParametricSurface3D(
    val baseSurface: AbstractCurveRelativeSurface3D,
    val lateralOffsetFunction: UnivariateFunction,
    val heightOffsetFunction: UnivariateFunction = LinearFunction.X_AXIS,
) : AbstractCurve3D() {
    // Properties and Initializers
    override val tolerance: Double get() = baseSurface.tolerance

    override val domain: Range<Double> =
        setOf(
            baseSurface.domain,
            lateralOffsetFunction.domain,
            heightOffsetFunction.domain,
        ).intersectingRange()

    init {
        require(domain.isNotEmpty()) { "Domain must not be empty." }
        require(length > tolerance) { "Length must be greater than zero as well as the tolerance threshold." }
    }

    // Methods
    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Vector3D {
        val lateralOffset =
            lateralOffsetFunction
                .valueInFuzzy(curveRelativePoint.curvePosition, tolerance)
                .getOrElse { throw it }
        val heightOffset =
            heightOffsetFunction
                .valueInFuzzy(curveRelativePoint.curvePosition, tolerance)
                .getOrElse { throw it }

        val curveRelativePoint2D = curveRelativePoint.toCurveRelative2D(lateralOffset)
        return baseSurface.calculatePointGlobalCSUnbounded(curveRelativePoint2D, heightOffset)
    }

    companion object {
        /**
         * Returns a [CurveOnParametricSurface3D]. Throws an error, if the [lateralOffsetFunction] or the
         * [heightOffsetFunction] is not defined everywhere, where the [baseSurface] is defined.
         */
        fun onCompleteSurface(
            baseSurface: AbstractCurveRelativeSurface3D,
            lateralOffsetFunction: UnivariateFunction,
            heightOffsetFunction: UnivariateFunction = LinearFunction.X_AXIS,
        ): CurveOnParametricSurface3D {
            require(lateralOffsetFunction.domain.fuzzyEncloses(baseSurface.domain, baseSurface.tolerance)) {
                "The lateral offset function must be defined everywhere where the baseSurface is also defined."
            }
            require(heightOffsetFunction.domain.fuzzyEncloses(baseSurface.domain, baseSurface.tolerance)) {
                "The height offset function must be defined everywhere where the baseSurface is also defined."
            }

            return CurveOnParametricSurface3D(baseSurface, lateralOffsetFunction, heightOffsetFunction)
        }
    }
}
