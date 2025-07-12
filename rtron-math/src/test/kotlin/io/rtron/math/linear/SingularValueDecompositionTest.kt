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
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class SingularValueDecompositionTest :
    FunSpec({
        context("TestRank") {

            test("matrix of two orthogonal vectors should have rank 2") {
                val matrixValues = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 4.0))
                val matrix = RealMatrix(matrixValues)
                val singularValueDecomposition = SingularValueDecomposition(matrix)

                val actualRank = singularValueDecomposition.rank

                actualRank shouldBe 2
            }

            test("matrix with two colinear vectors should have rank 1") {
                val matrixValues = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(3.0, 0.0))
                val matrix = RealMatrix(matrixValues)
                val singularValueDecomposition = SingularValueDecomposition(matrix)

                val actualRank = singularValueDecomposition.rank

                actualRank shouldBe 1
            }

            test("matrix of two zero vectors should have rank 0") {
                val matrixValues = arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(0.0, 0.0))
                val matrix = RealMatrix(matrixValues)
                val singularValueDecomposition = SingularValueDecomposition(matrix)

                val actualRank = singularValueDecomposition.rank

                actualRank shouldBe 0
            }
        }

        context("TestMatrixUCalculation") {

            test("decomposition of 2x2 matrix") {
                val matrixValues = arrayOf(doubleArrayOf(4.0, 12.0), doubleArrayOf(12.0, 11.0))
                val matrix = RealMatrix(matrixValues)
                val singularValueDecomposition = SingularValueDecomposition(matrix)
                val expectedMatrixU = RealMatrix(doubleArrayOf(0.6, 0.8, 0.8, -0.6), 2)

                val actualMatrixU = singularValueDecomposition.matrixU

                actualMatrixU.dimension shouldBe expectedMatrixU.dimension
                actualMatrixU.entriesFlattened
                    .zip(expectedMatrixU.entriesFlattened)
                    .forEach { it.first.shouldBe(it.second plusOrMinus 0.01) }
            }
        }

        context("TestMatrixSCalculation") {

            test("decomposition of 2x2 matrix") {
                val matrixValues = arrayOf(doubleArrayOf(4.0, 12.0), doubleArrayOf(12.0, 11.0))
                val matrix = RealMatrix(matrixValues)
                val singularValueDecomposition = SingularValueDecomposition(matrix)
                val expectedMatrixS = RealMatrix(doubleArrayOf(20.0, 0.0, 0.0, 5.0), 2)

                val actualMatrixS = singularValueDecomposition.matrixS

                actualMatrixS.dimension shouldBe expectedMatrixS.dimension
                actualMatrixS.entriesFlattened
                    .zip(expectedMatrixS.entriesFlattened)
                    .forEach { it.first.shouldBe(it.second plusOrMinus 0.01) }
            }
        }

        context("TestMatrixVCalculation") {

            test("decomposition of 2x2 matrix") {
                val matrixValues = arrayOf(doubleArrayOf(4.0, 12.0), doubleArrayOf(12.0, 11.0))
                val matrix = RealMatrix(matrixValues)
                val singularValueDecomposition = SingularValueDecomposition(matrix)
                val expectedMatrixV = RealMatrix(doubleArrayOf(0.6, 0.8, -0.8, 0.6), 2)

                val actualMatrixVT = singularValueDecomposition.matrixVT

                actualMatrixVT.dimension shouldBe expectedMatrixV.dimension
                actualMatrixVT.entriesFlattened
                    .zip(expectedMatrixV.entriesFlattened)
                    .forEach { it.first.shouldBe(it.second plusOrMinus 0.01) }
            }
        }
    })
