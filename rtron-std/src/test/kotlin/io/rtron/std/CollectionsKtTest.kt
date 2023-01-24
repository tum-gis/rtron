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

package io.rtron.std

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CollectionsKtTest {

    @Nested
    inner class TestDistinctConsecutive {

        @Test
        fun `test basic list with consecutive duplicate at the beginning`() {
            val expectedValues = listOf("a", "b", "c")

            val actualValues = listOf("a", "a", "b", "c").distinctConsecutiveBy { it }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test basic list with same enclosing pair`() {
            val expectedValues = listOf("a", "b", "c", "a")

            val actualValues = listOf("a", "a", "b", "c", "a").distinctConsecutiveBy { it }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test empty list`() {
            val actualValues = emptyList<String>().distinctConsecutiveBy { it }

            assertThat(actualValues).isEqualTo(emptyList<String>())
        }

        @Test
        fun `test list with a single element`() {
            val actualValues = listOf("a").distinctConsecutiveBy { it }

            assertThat(actualValues).isEqualTo(listOf("a"))
        }
    }

    @Nested
    inner class TestDistinctConsecutiveEnclosing {

        @Test
        fun `test basic list with consecutive duplicate at the beginning`() {
            val expectedValues = listOf("a", "b", "c")

            val actualValues = listOf("a", "a", "b", "c").distinctConsecutiveEnclosingBy { it }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test basic list with same enclosing pair`() {
            val expectedValues = listOf("a", "b", "c")

            val actualValues = listOf("a", "a", "b", "c", "a").distinctConsecutiveEnclosingBy { it }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test removal of multiple consecutive objects`() {
            val expectedValues = listOf("a", "b", "c")

            val actualValues = listOf("a", "b", "b", "b", "c").distinctConsecutiveEnclosingBy { it }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test empty list`() {
            val actualValues = emptyList<String>().distinctConsecutiveEnclosingBy { it }

            assertThat(actualValues).isEqualTo(emptyList<String>())
        }

        @Test
        fun `test list with a single element`() {
            val actualValues = listOf("a").distinctConsecutiveEnclosingBy { it }

            assertThat(actualValues).isEqualTo(listOf("a"))
        }
    }

    @Nested
    inner class TestFilterToSorting {
        @Test
        fun `test basic list with consecutive duplicate at the beginning`() {
            val expectedValues = listOf(1, 2, 3)

            val actualValues = listOf(1, 1, 2, 3).filterToSorting { first, second -> first < second }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test unsorted list`() {
            val expectedValues = listOf(3, 4, 12)

            val actualValues = listOf(3, 1, 2, 4, 12, 5, 3).filterToSorting { first, second -> first < second }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test empty list`() {
            val actualValues = emptyList<String>().filterToSorting { first, second -> first < second }

            assertThat(actualValues).isEqualTo(emptyList<String>())
        }

        @Test
        fun `test list with a single element`() {
            val actualValues = listOf("a").filterToSorting { first, second -> first < second }

            assertThat(actualValues).isEqualTo(listOf("a"))
        }
    }

    @Nested
    inner class TestWindowedEnclosing {

        @Test
        fun `test basic enclosed windowing of character sequence`() {
            val baseSequence = sequenceOf("a", "b", "c", "d")
            val expectedSequence = sequenceOf(
                listOf("a", "b", "c"),
                listOf("b", "c", "d"),
                listOf("c", "d", "a"),
                listOf("d", "a", "b")
            )

            val actualSequence = baseSequence.windowedEnclosing(3)

            assertThat(actualSequence.toList()).isEqualTo(expectedSequence.toList())
        }

        @Test
        fun `test basic enclosed windowing of integer sequence`() {
            val baseSequence = sequenceOf(1, 2, 3, 4, 5, 6)
            val expectedSequence = sequenceOf(
                listOf(1, 2, 3),
                listOf(2, 3, 4),
                listOf(3, 4, 5),
                listOf(4, 5, 6),
                listOf(5, 6, 1),
                listOf(6, 1, 2)
            )

            val actualValues = baseSequence.windowedEnclosing(3)

            assertThat(actualValues.toList()).isEqualTo(expectedSequence.toList())
        }
    }

    @Nested
    inner class TestFilterWithNext {

        @Test
        fun `test basic enclosed windowing of character sequence`() {
            val expectedValues = listOf("a", "b", "c")

            val actualValues = listOf("a", "a", "b", "c").filterWithNext { a, b -> a != b }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test basic list with same enclosing pair`() {
            val expectedValues = listOf("a", "b", "c", "a")

            val actualValues = listOf("a", "a", "b", "c", "a").filterWithNext { a, b -> a != b }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test list with three consecutively following duplicates`() {
            val expectedValues = listOf("a", "b", "c", "a")

            val actualValues = listOf("a", "a", "a", "b", "b", "c", "a").filterWithNext { a, b -> a != b }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test empty list`() {
            val actualValues = emptyList<String>().filterWithNext { a, b -> a != b }

            assertThat(actualValues).isEqualTo(emptyList<String>())
        }

        @Test
        fun `test list with a single element`() {
            val actualValues = listOf("a").filterWithNext { a, b -> a != b }

            assertThat(actualValues).isEqualTo(listOf("a"))
        }
    }

    @Nested
    inner class TestFilterWithNextEnclosing {

        @Test
        fun `test basic list with consecutive duplicate at the beginning`() {
            val expectedValues = listOf("a", "b", "c")

            val actualValues = listOf("a", "a", "b", "c").filterWithNextEnclosing { a, b -> a != b }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test basic list with same enclosing pair`() {
            val expectedValues = listOf("a", "b", "c")

            val actualValues = listOf("a", "a", "b", "c", "a").filterWithNextEnclosing { a, b -> a != b }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test removal of multiple consecutive objects`() {
            val expectedValues = listOf("a", "b", "c")

            val actualValues = listOf("a", "b", "b", "b", "c").filterWithNextEnclosing { a, b -> a != b }

            assertThat(actualValues).isEqualTo(expectedValues)
        }

        @Test
        fun `test empty list`() {
            val actualValues = emptyList<String>().filterWithNextEnclosing { a, b -> a != b }

            assertThat(actualValues).isEqualTo(emptyList<String>())
        }

        @Test
        fun `test list with a single element`() {
            val actualValues = listOf("a").filterWithNextEnclosing { a, b -> a != b }

            assertThat(actualValues).isEqualTo(listOf("a"))
        }
    }
}
