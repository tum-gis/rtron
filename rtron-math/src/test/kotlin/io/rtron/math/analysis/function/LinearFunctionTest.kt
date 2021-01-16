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

package io.rtron.math.analysis.function

import com.github.kittinunf.result.Result
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.range.Range
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class LinearFunctionTest {

    @Nested
    inner class TestValueCalculation {

        @Test
        fun `f(3)=5*x+25 should be 40`() {
            val linearFunction = LinearFunction(5.0, 25.0)

            val actualResult = linearFunction.value(3.0)

            require(actualResult is Result.Success)
            assertThat(actualResult.value).isEqualTo(40.0)
        }

        @Test
        fun `out of range value evaluation throws an IllegalArgumentException`() {
            val linearFunction = LinearFunction(-5.0, 25.0, Range.closedOpen(1.0, 3.0))

            val actualResult = linearFunction.value(3.0)

            require(actualResult is Result.Failure)
            assertThat(actualResult.getException()).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Nested
    inner class TestFactoryMethodOfInclusivePoints {

        @Test
        fun `basic creation of linear function with two points`() {
            val expectedLinearFunction = LinearFunction(
                -3.0 / 2.0,
                13.0 / 2.0,
                Range.closed(3.0, 7.0)
            )

            val actualLinearFunction = LinearFunction.ofInclusivePoints(3.0, 2.0, 7.0, -4.0)

            assertThat(actualLinearFunction).isEqualTo(expectedLinearFunction)
        }

        @Test
        fun `creation of linear function with two equal points throws an IllegalArgumentException`() {
            val x = 2.1
            val y = 3.4

            assertThatIllegalArgumentException().isThrownBy { LinearFunction.ofInclusivePoints(x, y, x, y) }
        }
    }

    @Nested
    inner class TestFactoryMethodOfInclusiveYValueAndUnitSlope {

        @Test
        fun `with positive unit slope`() {
            val expectedLinearFunction = LinearFunction(
                1.0,
                2.0,
                Range.closed(0.0, 2.0)
            )

            val actualLinearFunction = LinearFunction.ofInclusiveYValuesAndUnitSlope(2.0, 4.0)

            assertThat(actualLinearFunction).isEqualTo(expectedLinearFunction)
        }

        @Test
        fun `with negative unit slope`() {
            val expectedLinearFunction = LinearFunction(
                -1.0,
                17.0,
                Range.closed(0.0, 3.0)
            )

            val actualLinearFunction = LinearFunction.ofInclusiveYValuesAndUnitSlope(17.0, 14.0)

            assertThat(actualLinearFunction).isEqualTo(expectedLinearFunction)
        }
    }
}
