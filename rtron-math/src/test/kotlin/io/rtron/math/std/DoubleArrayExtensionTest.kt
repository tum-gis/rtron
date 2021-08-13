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

package io.rtron.math.std

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DoubleArrayExtensionTest {

    @Nested
    inner class TestReshapeByColumnDimension {

        @Test
        fun `test reshape of square matrix`() {
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

            assertThat(actualReshapedMatrix).isEqualTo(expectedMatrix)
        }
    }

    @Nested
    inner class TestReshapeByRowDimension {

        @Test
        fun `test fail for wrong dimension`() {
            val values = doubleArrayOf(2.0, 1.0, 2.0, 1.0)

            assertThatIllegalArgumentException().isThrownBy { values.reshapeByRowDimension(5) }
        }

        @Test
        fun `test reshape of square matrix`() {
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

            assertThat(actualReshapedMatrix).isEqualTo(expectedMatrix)
        }
    }
}
