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

package io.rtron.math.range

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DoubleRangeExtensionsTest : FunSpec({
    context("ArrangeDoubleRange") {
        test("correct value spacing") {
            val actualValues =
                Range.closed(0.0, 1.25)
                    .arrange(0.25, false, 0.0)

            doubleArrayOf(0.0, 0.25, 0.5, 0.75, 1.0, 1.25) shouldBe actualValues
        }

        test("non zero start") {
            val actualValues =
                Range.closed(1.0, 1.25)
                    .arrange(0.25, false, 0.0)

            doubleArrayOf(1.0, 1.25) shouldBe actualValues
        }

        test("offset start") {
            val actualValues =
                Range.closed(0.51, 1.25)
                    .arrange(0.25, false, 0.0)

            doubleArrayOf(0.51, 0.76, 1.01) shouldBe actualValues
        }

        test("with epsilon offset and endpoint") {
            val actualValues =
                Range.closed(0.51, 1.25)
                    .arrange(0.25, true, 0.0)

            doubleArrayOf(0.51, 0.76, 1.01, 1.25) shouldBe actualValues
        }

        test("with endpoint") {
            val actualValues =
                Range.closed(1.0, 1.25)
                    .arrange(0.25, true, 0.0)

            doubleArrayOf(1.0, 1.25) shouldBe actualValues
        }

        test("list with length zero") {
            val actualValues =
                Range.closed(1.0, 1.0)
                    .arrange(0.25, false, 0.0)

            doubleArrayOf(1.0) shouldBe actualValues
        }

        test("list with length zero and endPoint") {
            val actualValues =
                Range.closed(1.0, 1.0)
                    .arrange(0.25, true, 0.0)

            doubleArrayOf(1.0) shouldBe actualValues
        }

        test("smaller range than step size should nevertheless contain the start") {
            val actualValues =
                Range.closed(1.0, 2.0)
                    .arrange(5.0, false, 0.0)

            doubleArrayOf(1.0) shouldBe actualValues
        }

        test("smaller range than step size should nevertheless contain the start and end") {
            val actualValues =
                Range.closed(1.0, 2.0)
                    .arrange(5.0, true, 0.0)

            doubleArrayOf(1.0, 2.0) shouldBe actualValues
        }

        test("range with length under epsilon should nevertheless contain the start and end") {
            val actualValues =
                Range.closed(0.0, 1.0E-8)
                    .arrange(0.5, true, 0.0)

            doubleArrayOf(0.0, 1.0E-8) shouldBe actualValues
        }
    }
})
