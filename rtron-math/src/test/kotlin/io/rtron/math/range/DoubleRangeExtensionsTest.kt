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

package io.rtron.math.range

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DoubleRangeExtensionsTest {

    @Nested
    inner class ArrangeDoubleRange {
        @Test
        fun `correct value spacing`() {
            val actualValues = Range.closed(0.0, 1.25)
                .arrange(0.25, false, 0.0)

            assertThat(doubleArrayOf(0.0, 0.25, 0.5, 0.75, 1.0, 1.25)).isEqualTo(actualValues)
        }

        @Test
        fun `non zero start`() {
            val actualValues = Range.closed(1.0, 1.25)
                .arrange(0.25, false, 0.0)

            assertThat(doubleArrayOf(1.0, 1.25)).isEqualTo(actualValues)
        }

        @Test
        fun `offset start`() {
            val actualValues = Range.closed(0.51, 1.25)
                .arrange(0.25, false, 0.0)

            assertThat(doubleArrayOf(0.51, 0.76, 1.01)).isEqualTo(actualValues)
        }

        @Test
        fun `with epsilon offset and endpoint`() {
            val actualValues = Range.closed(0.51, 1.25)
                .arrange(0.25, true, 0.0)

            assertThat(doubleArrayOf(0.51, 0.76, 1.01, 1.25)).isEqualTo(actualValues)
        }

        @Test
        fun `with endpoint`() {
            val actualValues = Range.closed(1.0, 1.25)
                .arrange(0.25, true, 0.0)

            assertThat(doubleArrayOf(1.0, 1.25)).isEqualTo(actualValues)
        }

        @Test
        fun `list with length zero`() {
            val actualValues = Range.closed(1.0, 1.0)
                .arrange(0.25, false, 0.0)

            assertThat(doubleArrayOf(1.0)).isEqualTo(actualValues)
        }

        @Test
        fun `list with length zero and endPoint`() {
            val actualValues = Range.closed(1.0, 1.0)
                .arrange(0.25, true, 0.0)

            assertThat(doubleArrayOf(1.0)).isEqualTo(actualValues)
        }

        @Test
        fun `smaller range than step size should nevertheless contain the start`() {
            val actualValues = Range.closed(1.0, 2.0)
                .arrange(5.0, false, 0.0)

            assertThat(doubleArrayOf(1.0)).isEqualTo(actualValues)
        }

        @Test
        fun `smaller range than step size should nevertheless contain the start and end`() {
            val actualValues = Range.closed(1.0, 2.0)
                .arrange(5.0, true, 0.0)

            assertThat(doubleArrayOf(1.0, 2.0)).isEqualTo(actualValues)
        }

        @Test
        fun `range with length under epsilon should nevertheless contain the start and end`() {
            val actualValues = Range.closed(0.0, 1.0E-8)
                .arrange(0.5, true, 0.0)

            assertThat(doubleArrayOf(0.0, 1.0E-8)).isEqualTo(actualValues)
        }
    }
}
