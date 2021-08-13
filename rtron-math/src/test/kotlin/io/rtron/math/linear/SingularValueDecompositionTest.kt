/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SingularValueDecompositionTest {

    @Nested
    inner class TestRank {

        @Test
        fun `matrix of two orthogonal vectors should have rank 2`() {
            val matrixValues = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 4.0))
            val matrix = RealMatrix(matrixValues)
            val singularValueDecomposition = SingularValueDecomposition(matrix)

            val actualRank = singularValueDecomposition.rank

            assertThat(actualRank).isEqualTo(2)
        }

        @Test
        fun `matrix with two colinear vectors should have rank 1`() {
            val matrixValues = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(3.0, 0.0))
            val matrix = RealMatrix(matrixValues)
            val singularValueDecomposition = SingularValueDecomposition(matrix)

            val actualRank = singularValueDecomposition.rank

            assertThat(actualRank).isEqualTo(1)
        }

        @Test
        fun `matrix of two zero vectors should have rank 0`() {
            val matrixValues = arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(0.0, 0.0))
            val matrix = RealMatrix(matrixValues)
            val singularValueDecomposition = SingularValueDecomposition(matrix)

            val actualRank = singularValueDecomposition.rank

            assertThat(actualRank).isEqualTo(0)
        }
    }

    @Nested
    inner class TestMatrixUCalculation {

        @Test
        fun `decomposition of 2x2 matrix`() {
            val matrixValues = arrayOf(doubleArrayOf(4.0, 12.0), doubleArrayOf(12.0, 11.0))
            val matrix = RealMatrix(matrixValues)
            val singularValueDecomposition = SingularValueDecomposition(matrix)
            val expectedMatrixU = RealMatrix(doubleArrayOf(0.6, 0.8, 0.8, -0.6), 2)

            val actualMatrixU = singularValueDecomposition.matrixU

            assertThat(actualMatrixU.dimension).isEqualTo(expectedMatrixU.dimension)
            assertThat(actualMatrixU.entriesFlattened)
                .containsExactly(expectedMatrixU.entriesFlattened, Offset.offset(0.01))
        }
    }

    @Nested
    inner class TestMatrixSCalculation {

        @Test
        fun `decomposition of 2x2 matrix`() {
            val matrixValues = arrayOf(doubleArrayOf(4.0, 12.0), doubleArrayOf(12.0, 11.0))
            val matrix = RealMatrix(matrixValues)
            val singularValueDecomposition = SingularValueDecomposition(matrix)
            val expectedMatrixS = RealMatrix(doubleArrayOf(20.0, 0.0, 0.0, 5.0), 2)

            val actualMatrixS = singularValueDecomposition.matrixS

            assertThat(actualMatrixS.dimension).isEqualTo(expectedMatrixS.dimension)
            assertThat(actualMatrixS.entriesFlattened)
                .containsExactly(expectedMatrixS.entriesFlattened, Offset.offset(0.01))
        }
    }

    @Nested
    inner class TestMatrixVCalculation {

        @Test
        fun `decomposition of 2x2 matrix`() {
            val matrixValues = arrayOf(doubleArrayOf(4.0, 12.0), doubleArrayOf(12.0, 11.0))
            val matrix = RealMatrix(matrixValues)
            val singularValueDecomposition = SingularValueDecomposition(matrix)
            val expectedMatrixV = RealMatrix(doubleArrayOf(0.6, 0.8, -0.8, 0.6), 2)

            val actualMatrixVT = singularValueDecomposition.matrixVT

            assertThat(actualMatrixVT.dimension).isEqualTo(expectedMatrixV.dimension)
            assertThat(actualMatrixVT.entriesFlattened)
                .containsExactly(expectedMatrixV.entriesFlattened, Offset.offset(0.01))
        }
    }
}
