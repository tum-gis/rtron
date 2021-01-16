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

package io.rtron.math.analysis.function.univariate.combination

import com.github.kittinunf.result.Result
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class ConcatenatedFunctionTest {

    class TestCreationOfLinearFunctions {

        @Test
        fun `concatenated function with absolute start at 0`() {
            val starts = listOf(0.0, 5.0)
            val intercepts = listOf(0.0, -5.0)
            val concatenatedFunction = ConcatenatedFunction.ofLinearFunctions(starts, intercepts)

            val actualResult1 = concatenatedFunction.valueInFuzzy(-1.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(0.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(5.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(7.0, 1E-7)

            require(actualResult1 is Result.Failure)
            require(actualResult2 is Result.Success)
            Assertions.assertThat(actualResult2.value).isEqualTo(0.0)
            require(actualResult3 is Result.Success)
            Assertions.assertThat(actualResult3.value).isEqualTo(-5.0)
            require(actualResult4 is Result.Success)
            Assertions.assertThat(actualResult4.value).isEqualTo(-5.0)
        }

        @Test
        fun `concatenated function with absolute start at -2`() {
            val starts = listOf(-2.0, 3.0)
            val intercepts = listOf(0.0, -5.0)
            val concatenatedFunction = ConcatenatedFunction.ofLinearFunctions(starts, intercepts)

            val actualResult1 = concatenatedFunction.valueInFuzzy(-3.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(-2.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(3.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(5.0, 1E-7)

            require(actualResult1 is Result.Failure)
            require(actualResult2 is Result.Success)
            Assertions.assertThat(actualResult2.value).isEqualTo(0.0)
            require(actualResult3 is Result.Success)
            Assertions.assertThat(actualResult3.value).isEqualTo(-5.0)
            require(actualResult4 is Result.Success)
            Assertions.assertThat(actualResult4.value).isEqualTo(-5.0)
        }

        @Test
        fun `concatenated function with absolute start at 2`() {
            val starts = listOf(2.0, 7.0)
            val intercepts = listOf(0.0, -5.0)
            val concatenatedFunction = ConcatenatedFunction.ofLinearFunctions(starts, intercepts)

            val actualResult1 = concatenatedFunction.valueInFuzzy(1.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(2.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(7.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(9.0, 1E-7)

            require(actualResult1 is Result.Failure)
            require(actualResult2 is Result.Success)
            Assertions.assertThat(actualResult2.value).isEqualTo(0.0)
            require(actualResult3 is Result.Success)
            Assertions.assertThat(actualResult3.value).isEqualTo(-5.0)
            require(actualResult4 is Result.Success)
            Assertions.assertThat(actualResult4.value).isEqualTo(-5.0)
        }
    }

    class TestCreationOfPolynomialFunctions {

        @Test
        fun `concatenated function with absolute start at 0`() {
            val starts = listOf(0.0, 5.0)
            val coefficients = listOf(
                doubleArrayOf(2.0, 3.0, 4.0, 1.0),
                doubleArrayOf(1.0, 2.0, 3.0, 4.0)
            )
            val concatenatedFunction =
                ConcatenatedFunction.ofPolynomialFunctions(starts, coefficients)

            val actualResult1 = concatenatedFunction.valueInFuzzy(0.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(2.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(5.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(7.0, 1E-7)

            require(actualResult1 is Result.Success)
            Assertions.assertThat(actualResult1.value).isEqualTo(2.0)
            require(actualResult2 is Result.Success)
            Assertions.assertThat(actualResult2.value).isEqualTo(32.0)
            require(actualResult3 is Result.Success)
            Assertions.assertThat(actualResult3.value).isEqualTo(1.0)
            require(actualResult4 is Result.Success)
            Assertions.assertThat(actualResult4.value).isEqualTo(49.0)
        }

        @Test
        fun `concatenated function with absolute start at -2`() {
            val starts = listOf(-2.0, 3.0)
            val coefficients = listOf(
                doubleArrayOf(2.0, 3.0, 4.0, 1.0),
                doubleArrayOf(1.0, 2.0, 3.0, 4.0)
            )
            val concatenatedFunction =
                ConcatenatedFunction.ofPolynomialFunctions(starts, coefficients)

            val actualResult1 = concatenatedFunction.valueInFuzzy(-2.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(0.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(3.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(5.0, 1E-7)

            require(actualResult1 is Result.Success)
            Assertions.assertThat(actualResult1.value).isEqualTo(2.0)
            require(actualResult2 is Result.Success)
            Assertions.assertThat(actualResult2.value).isEqualTo(32.0)
            require(actualResult3 is Result.Success)
            Assertions.assertThat(actualResult3.value).isEqualTo(1.0)
            require(actualResult4 is Result.Success)
            Assertions.assertThat(actualResult4.value).isEqualTo(49.0)
        }

        @Test
        fun `concatenated function with absolute start at 2`() {
            val starts = listOf(2.0, 7.0)
            val coefficients = listOf(
                doubleArrayOf(2.0, 3.0, 4.0, 1.0),
                doubleArrayOf(1.0, 2.0, 3.0, 4.0)
            )
            val concatenatedFunction =
                ConcatenatedFunction.ofPolynomialFunctions(starts, coefficients)

            val actualResult1 = concatenatedFunction.valueInFuzzy(2.0, 1E-7)
            val actualResult2 = concatenatedFunction.valueInFuzzy(4.0, 1E-7)
            val actualResult3 = concatenatedFunction.valueInFuzzy(7.0, 1E-7)
            val actualResult4 = concatenatedFunction.valueInFuzzy(9.0, 1E-7)

            require(actualResult1 is Result.Success)
            Assertions.assertThat(actualResult1.value).isEqualTo(2.0)
            require(actualResult2 is Result.Success)
            Assertions.assertThat(actualResult2.value).isEqualTo(32.0)
            require(actualResult3 is Result.Success)
            Assertions.assertThat(actualResult3.value).isEqualTo(1.0)
            require(actualResult4 is Result.Success)
            Assertions.assertThat(actualResult4.value).isEqualTo(49.0)
        }
    }
}
