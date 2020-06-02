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
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativePoint1D
import io.rtron.math.geometry.curved.threed.surface.AbstractCurveRelativeSurface3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.Range
import io.rtron.math.range.fuzzyEncloses
import io.rtron.std.handleFailure


/**
 * Curve that lies on a parametric surface. This curve is parallel to the [baseSurface]'s curve but defined by a
 * laterally translated by a [lateralOffsetFunction] and vertically translated by a [heightOffsetFunction].
 *
 * @param baseSurface the base surface on which this curve lies
 * @param lateralOffsetFunction lateral offset to the curve of the [baseSurface]
 * @param heightOffsetFunction height offset to the curve of the [baseSurface]
 *
 */
class CurveOnParametricSurface3D(
        val baseSurface: AbstractCurveRelativeSurface3D,
        val lateralOffsetFunction: UnivariateFunction,
        val heightOffsetFunction: UnivariateFunction = LinearFunction.X_AXIS
) : AbstractCurve3D() {

    // Properties and Initializers
    init {
        require(lateralOffsetFunction.domain.fuzzyEncloses(baseSurface.domain, tolerance))
        { "The lateral offset function must be defined everywhere where the baseSurface is also defined." }
        require(heightOffsetFunction.domain.fuzzyEncloses(baseSurface.domain, tolerance))
        { "The height offset function must be defined everywhere where the baseSurface is also defined." }
    }

    override val tolerance: Double get() = baseSurface.tolerance
    override val domain: Range<Double> get() = baseSurface.domain

    // Methods
    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativePoint1D): Result<Vector3D, Exception> {

        val lateralOffset = lateralOffsetFunction.valueInFuzzy(curveRelativePoint.curvePosition, tolerance)
                .handleFailure { return it }
        val heightOffset = heightOffsetFunction.valueInFuzzy(curveRelativePoint.curvePosition, tolerance)
                .handleFailure { return it }

        val curveRelativePoint2D = curveRelativePoint.toCurveRelative2D(lateralOffset)
        return baseSurface.calculatePointGlobalCS(curveRelativePoint2D, heightOffset)
    }
}
