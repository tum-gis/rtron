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

package io.rtron.math.geometry.curved.threed.surface

import com.github.kittinunf.result.Result
import io.rtron.math.analysis.function.bivariate.BivariateFunction
import io.rtron.math.analysis.function.bivariate.pure.PlaneFunction
import io.rtron.math.geometry.curved.twod.point.CurveRelativeVector2D
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.fuzzyEncloses
import io.rtron.std.handleFailure

/**
 * Surface which is defined along the [baseCurve]. The height of the surface id defined by means of a
 * [BivariateFunction].
 *
 * @param baseCurve the curve along which the surface is defined
 * @param heightFunction the height of the surface whereby the x axis is interpreted as curve position and the y axis
 * is interpreted as lateral offset
 */
class CurveRelativeParametricSurface3D(
    private val baseCurve: Curve3D,
    private val heightFunction: BivariateFunction = PlaneFunction.ZERO
) : AbstractCurveRelativeSurface3D() {

    // Properties and Initializers
    override val tolerance: Double get() = baseCurve.tolerance
    override val domain get() = baseCurve.domain

    init {
        require(heightFunction.domainX.fuzzyEncloses(baseCurve.domain, tolerance)) { "The height function must be defined everywhere where the referenceLine is also defined." }
        require(length > tolerance) { "Length must be greater than zero as well as the tolerance threshold." }
    }

    // Methods
    override fun calculatePointGlobalCSUnbounded(curveRelativePoint: CurveRelativeVector2D, addHeightOffset: Double):
        Result<Vector3D, Exception> {

            val affine = baseCurve.calculateAffine(curveRelativePoint.toCurveRelative1D())
                .handleFailure { throw it.error }
            val surfaceHeight = heightFunction
                .valueInFuzzy(curveRelativePoint.curvePosition, curveRelativePoint.lateralOffset, tolerance)
                .handleFailure { throw it.error }
            val offset = Vector3D(0.0, curveRelativePoint.lateralOffset, surfaceHeight + addHeightOffset)

            return Result.success(affine.transform(offset))
        }
}
