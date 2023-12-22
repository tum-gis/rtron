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

package io.rtron.math.analysis.function.univariate.combination

import arrow.core.Either
import arrow.core.nonEmptyListOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConcatenatedFunctionTest : FunSpec({

    context("TestCreationOfLinearFunctions") {

        test("concatenated function with absolute start at 0") {
            val starts = listOf(0.0, 5.0)
            val intercepts = listOf(0.0, -5.0)
            val concatenatedFunction = ConcatenatedFunction.ofLinearFunctions(starts, intercepts)

            val actualResult1 = concatenatedFunction.valueInFuzzy(-1.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(0.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(5.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(7.0, 1E-7)

            require(actualResult1 is Either.Left)
            require(actualResult2 is Either.Right)
            actualResult2.value shouldBe 0.0
            require(actualResult3 is Either.Right)
            actualResult3.value shouldBe -5.0
            require(actualResult4 is Either.Right)
            actualResult4.value shouldBe -5.0
        }

        test("concatenated function with absolute start at -2") {
            val starts = listOf(-2.0, 3.0)
            val intercepts = listOf(0.0, -5.0)
            val concatenatedFunction = ConcatenatedFunction.ofLinearFunctions(starts, intercepts)

            val actualResult1 = concatenatedFunction.valueInFuzzy(-3.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(-2.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(3.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(5.0, 1E-7)

            require(actualResult1 is Either.Left)
            require(actualResult2 is Either.Right)
            actualResult2.value shouldBe 0.0
            require(actualResult3 is Either.Right)
            actualResult3.value shouldBe -5.0
            require(actualResult4 is Either.Right)
            actualResult4.value shouldBe -5.0
        }

        test("concatenated function with absolute start at 2") {
            val starts = listOf(2.0, 7.0)
            val intercepts = listOf(0.0, -5.0)
            val concatenatedFunction = ConcatenatedFunction.ofLinearFunctions(starts, intercepts)

            val actualResult1 = concatenatedFunction.valueInFuzzy(1.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(2.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(7.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(9.0, 1E-7)

            require(actualResult1 is Either.Left)
            require(actualResult2 is Either.Right)
            actualResult2.value shouldBe 0.0
            require(actualResult3 is Either.Right)
            actualResult3.value shouldBe -5.0
            require(actualResult4 is Either.Right)
            actualResult4.value shouldBe -5.0
        }
    }

    context("TestCreationOfPolynomialFunctions") {

        test("concatenated function with absolute start at 0") {
            val starts = nonEmptyListOf(0.0, 5.0)
            val coefficients = nonEmptyListOf(
                doubleArrayOf(2.0, 3.0, 4.0, 1.0),
                doubleArrayOf(1.0, 2.0, 3.0, 4.0)
            )
            val concatenatedFunction =
                ConcatenatedFunction.ofPolynomialFunctions(starts, coefficients)

            val actualResult1 = concatenatedFunction.valueInFuzzy(0.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(2.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(5.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(7.0, 1E-7)

            require(actualResult1 is Either.Right)
            actualResult1.value shouldBe 2.0
            require(actualResult2 is Either.Right)
            actualResult2.value shouldBe 32.0
            require(actualResult3 is Either.Right)
            actualResult3.value shouldBe 1.0
            require(actualResult4 is Either.Right)
            actualResult4.value shouldBe 49.0
        }

        test("concatenated function with absolute start at -2") {
            val starts = nonEmptyListOf(-2.0, 3.0)
            val coefficients = nonEmptyListOf(
                doubleArrayOf(2.0, 3.0, 4.0, 1.0),
                doubleArrayOf(1.0, 2.0, 3.0, 4.0)
            )
            val concatenatedFunction =
                ConcatenatedFunction.ofPolynomialFunctions(starts, coefficients)

            val actualResult1 = concatenatedFunction.valueInFuzzy(-2.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(0.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(3.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(5.0, 1E-7)

            require(actualResult1 is Either.Right)
            actualResult1.value shouldBe 2.0
            require(actualResult2 is Either.Right)
            actualResult2.value shouldBe 32.0
            require(actualResult3 is Either.Right)
            actualResult3.value shouldBe 1.0
            require(actualResult4 is Either.Right)
            actualResult4.value shouldBe 49.0
        }

        test("concatenated function with absolute start at 2") {
            val starts = nonEmptyListOf(2.0, 7.0)
            val coefficients = nonEmptyListOf(
                doubleArrayOf(2.0, 3.0, 4.0, 1.0),
                doubleArrayOf(1.0, 2.0, 3.0, 4.0)
            )
            val concatenatedFunction =
                ConcatenatedFunction.ofPolynomialFunctions(starts, coefficients)

            val actualResult1 = concatenatedFunction.valueInFuzzy(2.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(4.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(7.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(9.0, 1E-7)

            require(actualResult1 is Either.Right)
            actualResult1.value shouldBe 2.0
            require(actualResult2 is Either.Right)
            actualResult2.value shouldBe 32.0
            require(actualResult3 is Either.Right)
            actualResult3.value shouldBe 1.0
            require(actualResult4 is Either.Right)
            actualResult4.value shouldBe 49.0
        }
    }
})
