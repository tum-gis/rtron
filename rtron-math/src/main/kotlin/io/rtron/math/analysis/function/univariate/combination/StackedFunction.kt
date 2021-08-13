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

package io.rtron.math.analysis.function.univariate.combination

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getOrElse
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.range.Range
import io.rtron.math.range.intersectingRange
import io.rtron.math.range.unionRanges

/**
 * Stacks multiple functions and outputs the value according to the defined [operation].
 *
 * @param memberFunctions functions to be stacked together
 * @param operation operation which combines the values of each member function
 * @param defaultValue the default value, if one of the member functions is not defined at the requested parameter
 */
class StackedFunction(
    private val memberFunctions: List<UnivariateFunction>,
    private val operation: (operands: List<Double>) -> Double,
    private val defaultValue: Double = Double.NaN
) : UnivariateFunction() {

    // Properties and Initializers
    init {
        require(memberFunctions.isNotEmpty()) { "Must contain member functions." }
    }

    override val domain: Range<Double> =
        if (defaultValue.isFinite()) memberFunctions.map { it.domain }.toSet().unionRanges().span()
        else memberFunctions.map { it.domain }.toSet().intersectingRange()

    // Secondary Constructors
    constructor(
        memberFunction: UnivariateFunction,
        operation: (operands: List<Double>) -> Double,
        defaultValue: Double = Double.NaN
    ) :
        this(listOf(memberFunction), operation, defaultValue)

    // Methods
    override fun valueUnbounded(x: Double): Result<Double, IllegalArgumentException> {
        val individualValues = memberFunctions.map { it.valueUnbounded(x) getOrElse { defaultValue } }
        return Result.success(operation(individualValues))
    }

    override fun slopeUnbounded(x: Double): Result<Double, IllegalArgumentException> {
        val individualValues = memberFunctions.map { it.slopeUnbounded(x) getOrElse { 0.0 } }
        return Result.success(operation(individualValues))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as StackedFunction

        if (memberFunctions != other.memberFunctions) return false
        if (operation != other.operation) return false
        if (defaultValue != other.defaultValue) return false
        if (domain != other.domain) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + memberFunctions.hashCode()
        result = 31 * result + operation.hashCode()
        result = 31 * result + defaultValue.hashCode()
        result = 31 * result + domain.hashCode()
        return result
    }

    companion object {

        /**
         * Creates a [StackedFunction] which serves sum of each [memberFunctions].
         *
         * @param memberFunctions member functions to be summed
         * @param defaultValue used value if one of the [memberFunctions] is not defined at the requested parameter
         */
        fun ofSum(memberFunctions: List<UnivariateFunction>, defaultValue: Double = Double.NaN): StackedFunction =
            StackedFunction(memberFunctions, { it.sum() }, defaultValue)

        /**
         * Creates a [StackedFunction] which serves sum of each [memberFunctions].
         *
         * @param memberFunctions member functions to be summed
         * @param defaultValue used value if one of the [memberFunctions] is not defined at the requested parameter
         */
        fun ofSum(vararg memberFunctions: UnivariateFunction, defaultValue: Double = Double.NaN): StackedFunction =
            StackedFunction(memberFunctions.toList(), { it.sum() }, defaultValue)
    }
}
