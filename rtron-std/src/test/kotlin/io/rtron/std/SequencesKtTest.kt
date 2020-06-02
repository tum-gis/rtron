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

package io.rtron.std

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


internal class SequencesKtTest {

    @Nested
    inner class TestZipWithNextEnclosing {

        @Test
        fun `test basic sequence`() {
            val expectedZips = listOf(Pair("a", "b"), Pair("b", "c"), Pair("c", "a"))

            val actualZips = listOf("a", "b", "c").zipWithNextEnclosing()

            assertThat(actualZips).isEqualTo(expectedZips)
        }

        @Test
        fun `empty list should return an empty list`() {
            val actualZips = emptyList<String>().zipWithNextEnclosing()

            assertThat(actualZips).isEqualTo(emptyList<String>())
        }

        @Test
        fun `list with a single element should return an empty list`() {
            val actualZips = listOf("a").zipWithNextEnclosing()

            assertThat(actualZips).isEqualTo(emptyList<String>())
        }
    }

    @Nested
    inner class TestZipWithConsecutives {

        @Test
        fun `test basic sequence`() {
            val expected = listOf(listOf("a", "a", "a"), listOf("b"), listOf("c", "c"))

            val actual = listOf("a", "a", "a", "b", "c", "c").zipWithConsecutives { it }

            assertThat(actual).isEqualTo(expected)
        }

        @Test
        fun `test basic sequence with same enclosing pair`() {
            val expected = listOf(listOf("a", "a", "a"), listOf("b"), listOf("c", "c"), listOf("a"))

            val actual = listOf("a", "a", "a", "b", "c", "c", "a").zipWithConsecutives { it }

            assertThat(actual).isEqualTo(expected)
        }


        @Test
        fun `empty list should return an empty list`() {
            val actualZips = emptyList<String>().zipWithConsecutives { it }

            assertThat(actualZips).isEqualTo(emptyList<String>())
        }

        @Test
        fun `test single element`() {
            val actualZips = listOf("a").zipWithConsecutives { it }

            assertThat(actualZips).isEqualTo(listOf(listOf("a")))
        }
    }

    @Nested
    inner class TestZipWithConsecutivesEnclosing {

        @Test
        fun `test without enclosing pair`() {
            val expected = listOf(listOf("a", "a", "a"), listOf("b"), listOf("c", "c"))

            val actual = listOf("a", "a", "a", "b", "c", "c").zipWithConsecutivesEnclosing { it }

            assertThat(actual).isEqualTo(expected)
        }

        @Test
        fun `test basic sequence with same enclosing pair`() {
            val elements = listOf("a", "a", "a", "b", "c", "c", "a")
            val expectedZips = listOf(listOf("a", "a", "a", "a"), listOf("b"), listOf("c", "c"))

            val actualZips = elements.zipWithConsecutivesEnclosing { it }

            assertThat(actualZips).isEqualTo(expectedZips)
        }

        @Test
        fun `empty list should return an empty list`() {
            val actualZips = emptyList<String>().zipWithConsecutivesEnclosing { it }

            assertThat(actualZips).isEqualTo(emptyList<String>())
        }

        @Test
        fun `test single element`() {
            val actualZips = listOf("a").zipWithConsecutivesEnclosing { it }

            assertThat(actualZips).isEqualTo(listOf(listOf("a")))
        }

        @Test
        fun `test ordering`() {
            val pair1 = Pair(1, "a")
            val pair2 = Pair(1, "b")
            val pair3 = Pair(2, "a")
            val pair4 = Pair(3, "b")
            val pair5 = Pair(3, "b")
            val pair6 = Pair(1, "b")
            val startList = listOf(pair1, pair2, pair3, pair4, pair5, pair6)
            val expectedZips = listOf(
                    listOf(pair6, pair1, pair2),
                    listOf(pair3),
                    listOf(pair4, pair5))

            val actualZips = startList.zipWithConsecutivesEnclosing { it.first }

            assertThat(actualZips).isEqualTo(expectedZips)
        }
    }

}
