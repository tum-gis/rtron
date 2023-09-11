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

package io.rtron.math.linear

import io.kotest.core.spec.style.FunSpec
import org.assertj.core.api.Assertions.assertThat

class MatrixUtilsTest : FunSpec({
    context("TestAppendColumn") {

        test("append column with two elements") {
            val matrixValues = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 4.0))
            val matrix = RealMatrix(matrixValues)
            val column = doubleArrayOf(2.0, 1.0)

            val actualAppendedMatrix = matrix.appendColumn(column)

            assertThat(actualAppendedMatrix.getRow(0)).isEqualTo(doubleArrayOf(1.0, 0.0, 2.0))
            assertThat(actualAppendedMatrix.getRow(1)).isEqualTo(doubleArrayOf(0.0, 4.0, 1.0))
        }
    }
})
