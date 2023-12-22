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

package io.rtron.math.linear

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RealVectorUtilsTest : FunSpec({
    context("TestDimensionOfSpan") {

        test("two linearly independent and one dependent vector should have a span's dimension of 2") {
            val vectorA = RealVector(doubleArrayOf(1.0, -2.0, 1.0))
            val vectorB = RealVector(doubleArrayOf(2.0, 3.0, 0.0)) // 2*B = A + C
            val vectorC = RealVector(doubleArrayOf(3.0, 8.0, -1.0))
            val rowVectors = listOf(vectorA, vectorB, vectorC)

            val actualSpan = rowVectors.dimensionOfSpan()

            actualSpan shouldBe 2
        }

        test("four linearly independent vectors should have a span's dimension of 4") {
            val vectorA = RealVector(doubleArrayOf(1.0, 0.0, 0.0, 0.0))
            val vectorB = RealVector(doubleArrayOf(0.0, 1.0, 0.0, 0.0))
            val vectorC = RealVector(doubleArrayOf(0.0, 0.0, 1.0, 0.0))
            val vectorD = RealVector(doubleArrayOf(0.0, 0.0, 0.0, 1.0))
            val rowVectors = listOf(vectorA, vectorB, vectorC, vectorD)

            val actualSpan = rowVectors.dimensionOfSpan()

            actualSpan shouldBe 4
        }

        test("no vector should have a span's dimension of 0") {
            val rowVectors = emptyList<RealVector>()

            val actualSpan = rowVectors.dimensionOfSpan()

            actualSpan shouldBe 0
        }
    }
})
