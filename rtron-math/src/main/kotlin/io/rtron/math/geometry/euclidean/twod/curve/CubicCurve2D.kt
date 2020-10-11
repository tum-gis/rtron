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
import io.rtron.math.analysis.function.univariate.pure.PolynomialFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativePoint1D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.BoundType
import io.rtron.math.range.Range
import io.rtron.math.transform.AffineSequence2D
import io.rtron.std.handleFailure


/**
 * Represents a parametric cubic curve of the following form:
 * y = f(x) = c0 + c1*x + c2*x^2 + c3*x^3
 *
 * @param coefficients coefficients for f(t), whereby coefficients[0] corresponds to c0
 * @param length length of cubic curve which is used for constructing the domain
 */
class CubicCurve2D(
        val coefficients: DoubleArray,
        length: Double,
        override val tolerance: Double,
        override val affineSequence: AffineSequence2D = AffineSequence2D.EMPTY,
        endBoundType: BoundType = BoundType.OPEN
) : AbstractCurve2D() {

    // Properties and Initializers

    init {
        require(coefficients.size == 4)
        { "Requiring exactly four coefficients for building a cubic curve." }
        require(coefficients.all { it.isFinite() })
        { "All coefficients must be finite." }
        require(length.isFinite() && length > 0.0)
        { "Length must be finite and greater than zero." }
    }

    private val _polynomialFunction by lazy { PolynomialFunction(coefficients) }
    override val domain: Range<Double> = Range.closedX(0.0, length, endBoundType)

    // Methods

    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativePoint1D):
            Result<Vector2D, Exception> {

        val x = curveRelativePoint.curvePosition
        val y = _polynomialFunction.value(curveRelativePoint.curvePosition)
                .handleFailure { throw it.error }
        return Result.success(Vector2D(x, y))
    }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativePoint1D):
            Result<Rotation2D, Exception> {

        val angle = _polynomialFunction.slope(curveRelativePoint.curvePosition)
                .handleFailure { throw it.error }
        return Result.success(Rotation2D(angle))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CubicCurve2D

        if (!coefficients.contentEquals(other.coefficients)) return false
        if (affineSequence != other.affineSequence) return false
        if (domain != other.domain) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coefficients.contentHashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + affineSequence.hashCode()
        result = 31 * result + endBoundType.hashCode()
        result = 31 * result + domain.hashCode()
        return result
    }
}
