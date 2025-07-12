/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.rtron.math.std.DBL_EPSILON

class RealMatrixTest {
    fun normalize() {
        val matrixValues = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 4.0))
        val matrix = RealMatrix(matrixValues)
        val toleratedOffset = DBL_EPSILON

        val actualMatrix =
            matrix
                .normalize(matrix.rowDimension - 1, matrix.columnDimension - 1)

        actualMatrix[0][0].shouldBe(0.25 plusOrMinus toleratedOffset)
        actualMatrix[0][1].shouldBe(0.0 plusOrMinus toleratedOffset)
        actualMatrix[1][0].shouldBe(0.0 plusOrMinus toleratedOffset)
        actualMatrix[1][1].shouldBe(1.0 plusOrMinus toleratedOffset)
    }
}
