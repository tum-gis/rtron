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

package io.rtron.math.analysis.function.univariate.pure

import arrow.core.Either
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.range.Range

/**
 * Constant function of a [value] within a [domain].
 *
 * @param value constant's value
 * @param domain domain of the constant function
 */
data class ConstantFunction(
    val value: Double,
    override val domain: Range<Double> = Range.all()
) : UnivariateFunction() {

    // Properties and Initializers
    init {
        require(value.isFinite()) { "Value must be finite, but was $value." }
    }

    // Operators
    infix fun timesValue(other: Double): ConstantFunction = copy(value = this.value * other)

    // Methods
    override fun valueUnbounded(x: Double): Either<IllegalArgumentException, Double> = Either.Right(value)

    override fun slopeUnbounded(x: Double): Either<IllegalArgumentException, Double> = Either.Right(0.0)

    companion object {
        val ZERO = ConstantFunction(0.0)
    }
}
