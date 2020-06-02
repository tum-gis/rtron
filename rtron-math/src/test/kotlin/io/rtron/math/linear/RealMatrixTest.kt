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

package io.rtron.math.linear

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import io.rtron.math.std.DBL_EPSILON


internal class RealMatrixTest {

    @Test
    fun normalize() {
        val matrixValues = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 4.0))
        val matrix = RealMatrix(matrixValues)
        val toleratedOffset = Offset.offset(DBL_EPSILON)

        val actualMatrix = matrix
                .normalize(matrix.rowDimension - 1, matrix.columnDimension - 1)

        assertThat(actualMatrix[0][0]).isCloseTo(0.25, toleratedOffset)
        assertThat(actualMatrix[0][1]).isCloseTo(0.0, toleratedOffset)
        assertThat(actualMatrix[1][0]).isCloseTo(0.0, toleratedOffset)
        assertThat(actualMatrix[1][1]).isCloseTo(1.0, toleratedOffset)
    }
}
