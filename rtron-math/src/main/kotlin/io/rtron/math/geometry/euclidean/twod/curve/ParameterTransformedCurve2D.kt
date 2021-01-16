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
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.Range
import io.rtron.math.range.fuzzyEncloses

/**
 * Transforms the parameter (curveRelativePoint) by means of the [transformationFunction] function, before calculating
 * the cartesian coordinates of the [baseCurve].
 *
 * The parameters of a curve can e.g. be normalized (domain: [0.0, 1.0]) or reflect the actual curve length
 * (domain: [0.0, length]). To harmonise these differences, this class allows to rescale the parameters
 * of an [AbstractCurve2D].
 *
 * @param baseCurve base curve which parameter is to be transformed
 * @param transformationFunction function which is applied to the provided parameter
 *
 */
data class ParameterTransformedCurve2D(
    private val baseCurve: AbstractCurve2D,
    private val transformationFunction: (CurveRelativeVector1D) -> CurveRelativeVector1D,
    override val domain: Range<Double>
) : AbstractCurve2D() {

    // Properties and Initializers

    init {
        require(!domain.isEmpty()) { "Domain must not be empty." }
        require(domain.hasLowerBound() && domain.hasUpperBound()) { "Domain must have lower and upper bound." }

        val lowerEndpoint = transformationFunction(CurveRelativeVector1D(domain.lowerEndpointOrNull()!!))
        val upperEndpoint = transformationFunction(CurveRelativeVector1D(domain.upperEndpointOrNull()!!))
        val transformedDomain = Range.range(
            domain.lowerBoundType(),
            lowerEndpoint.curvePosition,
            domain.upperBoundType(),
            upperEndpoint.curvePosition
        )
        require(baseCurve.domain.fuzzyEncloses(transformedDomain, tolerance)) { "The base curve must be defined everywhere where the parameter transformed curve is also defined." }
    }

    override val tolerance: Double get() = baseCurve.tolerance

    // Methods

    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Result<Vector2D, Exception> {
            val transformedPoint = transformationFunction(curveRelativePoint)
            return baseCurve.calculatePointGlobalCS(transformedPoint)
        }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Result<Rotation2D, Exception> {
            val transformedPoint = transformationFunction(curveRelativePoint)
            return baseCurve.calculateRotationGlobalCS(transformedPoint)
        }
}
