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

package io.rtron.math.std

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DoubleArrayExtensionTest : FunSpec({

    context("TestReshapeByColumnDimension") {

        test("test reshape of square matrix") {
            val expectedMatrix = arrayOf(
                doubleArrayOf(1.0, 0.0, 0.0, 1.0),
                doubleArrayOf(0.0, 1.0, 0.0, 2.0),
                doubleArrayOf(0.0, 0.0, 1.0, 3.0),
                doubleArrayOf(0.0, 0.0, 0.0, 1.0)
            )
            val matrix = doubleArrayOf(
                1.0, 0.0, 0.0, 1.0,
                0.0, 1.0, 0.0, 2.0,
                0.0, 0.0, 1.0, 3.0,
                0.0, 0.0, 0.0, 1.0
            )

            val actualReshapedMatrix = matrix.reshapeByColumnDimension(4)

            actualReshapedMatrix shouldBe expectedMatrix
        }
    }

    context("TestReshapeByRowDimension") {

        test("test fail for wrong dimension") {
            val values = doubleArrayOf(2.0, 1.0, 2.0, 1.0)

            shouldThrow<IllegalArgumentException> { values.reshapeByRowDimension(5) }
        }

        test("test reshape of square matrix") {
            val expectedMatrix = arrayOf(
                doubleArrayOf(0.0, 0.0, 1.0),
                doubleArrayOf(1.0, 0.0, 2.0),
                doubleArrayOf(0.0, 1.0, 3.0),
                doubleArrayOf(0.0, 0.0, 1.0)
            )
            val matrix = doubleArrayOf(
                0.0, 0.0, 1.0,
                1.0, 0.0, 2.0,
                0.0, 1.0, 3.0,
                0.0, 0.0, 1.0
            )

            val actualReshapedMatrix = matrix.reshapeByRowDimension(4)

            actualReshapedMatrix shouldBe expectedMatrix
        }
    }
})
