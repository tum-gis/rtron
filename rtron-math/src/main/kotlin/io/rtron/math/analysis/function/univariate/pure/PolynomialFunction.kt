/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.analysis.function.univariate.pure

import arrow.core.Either
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.range.BoundType
import io.rtron.math.range.Range
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction as CMPolynomialFunction

fun CMPolynomialFunction.toPolynomialFunction() = PolynomialFunction(this.coefficients)

/**
 * Polynomial function of form f(x) = c[0] + c[1]*x + c[2]*x^2 + ... + c\[N]*x^N.
 *
 * @param coefficients coefficients for f(x), whereby coefficients[0] corresponds to c0
 * @param domain domain for which the polynomial function is defined
 */
data class PolynomialFunction(
    val coefficients: DoubleArray,
    override val domain: Range<Double> = Range.all(),
) : UnivariateFunction() {
    // Properties and Initializers
    init {
        require(coefficients.isNotEmpty()) { "At least one coefficient must be given." }
        require(coefficients.all { it.isFinite() }) { "All coefficients must be finite." }
    }

    /** adapted polynomial of Apache Commons Math */
    private val polynomialFunction by lazy { CMPolynomialFunction(coefficients) }

    /** degree of the polynomial function */
    val degree by lazy { polynomialFunction.degree() }

    /** derivative of this polynomial function */
    private val polynomialDerivative by lazy { polynomialFunction.polynomialDerivative().toPolynomialFunction() }

    // Secondary Constructors
    constructor(coefficients: List<Double>) : this(coefficients.toDoubleArray())

    // Methods
    override fun valueUnbounded(x: Double): Either<IllegalArgumentException, Double> = Either.Right(polynomialFunction.value(x))

    override fun slopeUnbounded(x: Double): Either<IllegalArgumentException, Double> = polynomialDerivative.valueUnbounded(x)

    /**
     * Returns the calculated value of f(x), if [x] is within the function's domain. Otherwise null is returned.
     */
    fun valueOrNull(x: Double): Double? =
        when (x) {
            in domain -> polynomialFunction.value(x)
            else -> null
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PolynomialFunction

        if (!coefficients.contentEquals(other.coefficients)) return false

        return true
    }

    override fun hashCode(): Int = coefficients.contentHashCode()

    // Conversions
    override fun toString(): String = "PolynomialFunction(polynomialFunction=$polynomialFunction)"

    companion object {
        /**
         * Build a polynomial function with providing its length, which is used to construct the domain [0, [length]
         *
         * @param coefficients the polynomial's coefficients
         * @param length the length of the domain [0, length]
         * @param upperBoundType open or closed upper bound type
         */
        fun of(
            coefficients: DoubleArray,
            length: Double,
            upperBoundType: BoundType = BoundType.OPEN,
        ): PolynomialFunction {
            require(length > 0.0) { "Length must be greater than zero." }

            return when {
                length.isFinite() -> PolynomialFunction(coefficients, Range.closedX(0.0, length, upperBoundType))
                length == Double.POSITIVE_INFINITY -> PolynomialFunction(coefficients, Range.atLeast(0.0))
                else -> throw IllegalArgumentException("Unknown length state")
            }
        }
    }
}
