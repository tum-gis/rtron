/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.analysis.function.univariate

import arrow.core.Either
import arrow.core.continuations.either
import io.rtron.math.analysis.function.univariate.combination.StackedFunction
import io.rtron.math.range.DefinableDomain
import io.rtron.math.range.fuzzyContains

/**
 * Function with exactly one parameter of the form z = f(x).
 */
abstract class UnivariateFunction : DefinableDomain<Double> {

    // Properties and Initializers
    /**
     * [startValue] = f(lowest endpoint of [domain]).
     */
    val startValue: Either<Exception, Double> by lazy { valueUnbounded(domain.lowerEndpointOrNull() ?: Double.NEGATIVE_INFINITY) }

    /**
     * [endValue] = f(upper endpoint of [domain]).
     */
    val endValue: Either<Exception, Double> by lazy { valueUnbounded(domain.upperEndpointOrNull() ?: Double.POSITIVE_INFINITY) }

    // Operators
    operator fun plus(other: UnivariateFunction) = StackedFunction.ofSum(this, other)
    operator fun unaryMinus() = StackedFunction(this, { -it[0] })
    operator fun times(m: Double) = StackedFunction(this, { it[0] * m })
    operator fun div(m: Double) = StackedFunction(this, { it[0] / m })

    // Methods
    /**
     * Evaluation of z = f(x) without checking whether x is within the function's [domain].
     *
     * @param x parameter [x] of function
     * @return Result.Success(z) = f(x), if evaluation was successful
     */
    internal abstract fun valueUnbounded(x: Double): Either<Exception, Double>

    /**
     * Evaluation of z = f(x) with strict checking whether x is within the function's [domain].
     *
     * @param x parameter [x] of function
     * @return returns Result.Success(z) = f(x), if [x] is strictly contained in [domain] and evaluation was successful
     */
    fun value(x: Double): Either<Exception, Double> = either.eager {
        domain.containsResult(x).bind()
        valueUnbounded(x).bind()
    }

    /**
     * Evaluation of z = f(x) with fuzzy checking whether x is within the function's [domain].
     *
     * @param x parameter [x] of function
     * @param tolerance allowed tolerance for fuzzy checking
     * @return returns Result.Success(z) = f(x), if [x] is fuzzily contained in [domain] and evaluation was successful
     */
    open fun valueInFuzzy(x: Double, tolerance: Double): Either<Exception, Double> {
        return if (!domain.fuzzyContains(x, tolerance))
            Either.Left(IllegalArgumentException("Value x=$x must be within in the defined $domain."))
        else valueUnbounded(x)
    }

    /**
     * Evaluation of the slope = f'(x) without checking whether x is within the function's [domain].
     *
     * @param x parameter [x] of function
     * @return Result.Success(slope) = f(x), if evaluation was successful
     */
    internal abstract fun slopeUnbounded(x: Double): Either<Exception, Double>

    /**
     * Evaluation of the slope = f'(x) with strict checking whether x is within the function's [domain].
     *
     * @param x parameter [x] of function
     * @return returns Result.Success(slope) = f(x), if [x] is strictly contained in [domain] and evaluation was
     * successful
     */
    fun slope(x: Double): Either<Exception, Double> =
        when (x) {
            in domain -> slopeUnbounded(x)
            else -> Either.Left(IllegalArgumentException("Value x=$x must be within in the defined $domain."))
        }

    /**
     * Evaluation of the slope = f(x) with fuzzy checking whether x is within the function's [domain].
     *
     * @param x parameter [x] of function
     * @param tolerance allowed tolerance for fuzzy checking
     * @return returns Result.Success(slope) = f(x), if [x] is fuzzily contained in [domain] and evaluation was
     * successful
     */
    fun slopeInFuzzy(x: Double, tolerance: Double): Either<Exception, Double> {
        return if (!domain.fuzzyContains(x, tolerance))
            Either.Left(IllegalArgumentException("Value x=$x must be within in the defined $domain."))
        else slopeUnbounded(x)
    }
}
