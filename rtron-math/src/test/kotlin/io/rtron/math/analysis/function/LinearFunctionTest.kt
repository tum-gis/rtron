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

package io.rtron.math.analysis.function

import arrow.core.Either
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.range.Range

class LinearFunctionTest : FunSpec({
    context("TestValueCalculation") {

        test("f(3)=5 times x plus 25 should be 40") {
            val linearFunction = LinearFunction(5.0, 25.0)

            val actualResult = linearFunction.value(3.0)

            require(actualResult is Either.Right)
            actualResult.value shouldBe 40.0
        }

        test("out of range value evaluation throws an IllegalArgumentException") {
            val linearFunction = LinearFunction(-5.0, 25.0, Range.closedOpen(1.0, 3.0))

            val actualResult = linearFunction.value(3.0)

            require(actualResult is Either.Left)
            actualResult.value.shouldBeInstanceOf<IllegalArgumentException>()
        }
    }

    context("TestFactoryMethodOfInclusivePoints") {

        test("basic creation of linear function with two points") {
            val expectedLinearFunction = LinearFunction(
                -3.0 / 2.0,
                13.0 / 2.0,
                Range.closed(3.0, 7.0)
            )

            val actualLinearFunction = LinearFunction.ofInclusivePoints(3.0, 2.0, 7.0, -4.0)

            actualLinearFunction shouldBe expectedLinearFunction
        }

        test("creation of linear function with two equal points throws an IllegalArgumentException") {
            val x = 2.1
            val y = 3.4

            shouldThrow<IllegalArgumentException> {
                LinearFunction.ofInclusivePoints(x, y, x, y)
            }
        }
    }

    context("TestFactoryMethodOfInclusiveYValueAndUnitSlope") {

        test("with positive unit slope") {
            val expectedLinearFunction = LinearFunction(
                1.0,
                2.0,
                Range.closed(0.0, 2.0)
            )

            val actualLinearFunction = LinearFunction.ofInclusiveYValuesAndUnitSlope(2.0, 4.0)

            actualLinearFunction shouldBe expectedLinearFunction
        }

        test("with negative unit slope") {
            val expectedLinearFunction = LinearFunction(
                -1.0,
                17.0,
                Range.closed(0.0, 3.0)
            )

            val actualLinearFunction = LinearFunction.ofInclusiveYValuesAndUnitSlope(17.0, 14.0)

            actualLinearFunction shouldBe expectedLinearFunction
        }
    }
})
