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

package io.rtron.math.geometry.euclidean.twod.curve

import com.github.kittinunf.result.Result
import io.rtron.math.analysis.function.univariate.pure.PolynomialFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.BoundType
import io.rtron.math.range.Range
import io.rtron.math.transform.AffineSequence2D
import io.rtron.std.handleFailure

/**
 * Represents a parametric cubic curve of the following form:
 * x = fx(t) = x0 + x1*t + x2*t^2 + x3*t^3
 * y = fy(t) = y0 + y1*t + y2*t^2 + y3*t^3
 *
 * @param coefficientsX coefficients for fx(t), whereby coefficientsX[0] corresponds to x0
 * @param coefficientsY coefficients for fy(t), whereby coefficientsY[0] corresponds to y0
 * @param length length of parametric curve which is used for constructing the domain
 */
class ParametricCubicCurve2D(
    private val coefficientsX: DoubleArray,
    private val coefficientsY: DoubleArray,
    length: Double,
    override val tolerance: Double,
    override val affineSequence: AffineSequence2D = AffineSequence2D.EMPTY,
    endBoundType: BoundType = BoundType.OPEN
) : AbstractCurve2D() {

    // Properties and Initializers
    init {
        require(coefficientsX.size == 4) { "Requiring exactly four x coefficients for building a cubic curve." }
        require(coefficientsY.size == 4) { "Requiring exactly four y coefficients for building a cubic curve." }
        require(coefficientsX.all { it.isFinite() }) { "All x coefficients must be finite." }
        require(coefficientsY.all { it.isFinite() }) { "All y coefficients must be finite." }
        require(length.isFinite()) { "Length value must be finite." }
        require(length > tolerance) { "Length value must be greater than zero and the tolerance threshold." }
    }

    private val _polynomialFunctionX by lazy { PolynomialFunction(coefficientsX) }
    private val _polynomialFunctionY by lazy { PolynomialFunction(coefficientsY) }
    override val domain: Range<Double> = Range.closedX(0.0, length, endBoundType)

    // Methods
    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Result<Vector2D, Exception> {

        val x = _polynomialFunctionX.value(curveRelativePoint.curvePosition)
            .handleFailure { throw it.error }
        val y = _polynomialFunctionY.value(curveRelativePoint.curvePosition)
            .handleFailure { throw it.error }
        return Result.success(Vector2D(x, y))
    }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Result<Rotation2D, Exception> {

        val x = _polynomialFunctionX.slope(curveRelativePoint.curvePosition)
            .handleFailure { throw it.error }
        val y = _polynomialFunctionY.slope(curveRelativePoint.curvePosition)
            .handleFailure { throw it.error }
        val rotation = Rotation2D.of(Vector2D(x, y))
        return Result.success(rotation)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParametricCubicCurve2D

        if (!coefficientsX.contentEquals(other.coefficientsX)) return false
        if (!coefficientsY.contentEquals(other.coefficientsY)) return false
        if (affineSequence != other.affineSequence) return false
        if (domain != other.domain) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coefficientsX.contentHashCode()
        result = 31 * result + coefficientsY.contentHashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + affineSequence.hashCode()
        result = 31 * result + endBoundType.hashCode()
        result = 31 * result + domain.hashCode()
        return result
    }
}
