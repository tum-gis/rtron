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

package io.rtron.std

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CollectionsKtTest :
    FunSpec({
        context("TestDistinctConsecutive") {

            test("test basic list with consecutive duplicate at the beginning") {
                val expectedValues = listOf("a", "b", "c")

                val actualValues = listOf("a", "a", "b", "c").distinctConsecutiveBy { it }

                actualValues shouldBe expectedValues
            }

            test("test basic list with same enclosing pair") {
                val expectedValues = listOf("a", "b", "c", "a")

                val actualValues = listOf("a", "a", "b", "c", "a").distinctConsecutiveBy { it }

                actualValues shouldBe expectedValues
            }

            test("test empty list") {
                val actualValues = emptyList<String>().distinctConsecutiveBy { it }

                actualValues shouldBe emptyList<String>()
            }

            test("test list with a single element") {
                val actualValues = listOf("a").distinctConsecutiveBy { it }

                actualValues shouldBe listOf("a")
            }
        }

        context("TestDistinctConsecutiveEnclosing") {

            test("test basic list with consecutive duplicate at the beginning") {
                val expectedValues = listOf("a", "b", "c")

                val actualValues = listOf("a", "a", "b", "c").distinctConsecutiveEnclosingBy { it }

                actualValues shouldBe expectedValues
            }

            test("test basic list with same enclosing pair") {
                val expectedValues = listOf("a", "b", "c")

                val actualValues = listOf("a", "a", "b", "c", "a").distinctConsecutiveEnclosingBy { it }

                actualValues shouldBe expectedValues
            }

            test("test removal of multiple consecutive objects") {
                val expectedValues = listOf("a", "b", "c")

                val actualValues = listOf("a", "b", "b", "b", "c").distinctConsecutiveEnclosingBy { it }

                actualValues shouldBe expectedValues
            }

            test("test empty list") {
                val actualValues = emptyList<String>().distinctConsecutiveEnclosingBy { it }

                actualValues shouldBe emptyList<String>()
            }

            test("test list with a single element") {
                val actualValues = listOf("a").distinctConsecutiveEnclosingBy { it }

                actualValues shouldBe listOf("a")
            }
        }

        context("TestFilterToSorting") {

            test("test basic list with consecutive duplicate at the beginning") {
                val expectedValues = listOf(1, 2, 3)

                val actualValues = listOf(1, 1, 2, 3).filterToSorting { first, second -> first < second }

                actualValues shouldBe expectedValues
            }

            test("test unsorted list") {
                val expectedValues = listOf(3, 4, 12)

                val actualValues = listOf(3, 1, 2, 4, 12, 5, 3).filterToSorting { first, second -> first < second }

                actualValues shouldBe expectedValues
            }

            test("test empty list") {
                val actualValues = emptyList<String>().filterToSorting { first, second -> first < second }

                actualValues shouldBe emptyList<String>()
            }

            test("test list with a single element") {
                val actualValues = listOf("a").filterToSorting { first, second -> first < second }

                actualValues shouldBe listOf("a")
            }
        }

        context("TestWindowedEnclosing") {

            test("test basic enclosed windowing of character sequence") {
                val baseSequence = sequenceOf("a", "b", "c", "d")
                val expectedSequence =
                    sequenceOf(
                        listOf("a", "b", "c"),
                        listOf("b", "c", "d"),
                        listOf("c", "d", "a"),
                        listOf("d", "a", "b"),
                    )

                val actualSequence = baseSequence.windowedEnclosing(3)

                actualSequence.toList() shouldBe expectedSequence.toList()
            }

            test("test basic enclosed windowing of integer sequence") {
                val baseSequence = sequenceOf(1, 2, 3, 4, 5, 6)
                val expectedSequence =
                    sequenceOf(
                        listOf(1, 2, 3),
                        listOf(2, 3, 4),
                        listOf(3, 4, 5),
                        listOf(4, 5, 6),
                        listOf(5, 6, 1),
                        listOf(6, 1, 2),
                    )

                val actualValues = baseSequence.windowedEnclosing(3)

                actualValues.toList() shouldBe expectedSequence.toList()
            }
        }

        context("TestFilterWithNext") {

            test("test basic enclosed windowing of character sequence") {
                val expectedValues = listOf("a", "b", "c")

                val actualValues = listOf("a", "a", "b", "c").filterWithNext { a, b -> a != b }

                actualValues shouldBe expectedValues
            }

            test("test basic list with same enclosing pair") {
                val expectedValues = listOf("a", "b", "c", "a")

                val actualValues = listOf("a", "a", "b", "c", "a").filterWithNext { a, b -> a != b }

                actualValues shouldBe expectedValues
            }

            test("test list with three consecutively following duplicates") {
                val expectedValues = listOf("a", "b", "c", "a")

                val actualValues = listOf("a", "a", "a", "b", "b", "c", "a").filterWithNext { a, b -> a != b }

                actualValues shouldBe expectedValues
            }

            test("test empty list") {
                val actualValues = emptyList<String>().filterWithNext { a, b -> a != b }

                actualValues shouldBe emptyList<String>()
            }

            test("test list with a single element") {
                val actualValues = listOf("a").filterWithNext { a, b -> a != b }

                actualValues shouldBe listOf("a")
            }
        }

        context("TestFilterWithNextEnclosing") {

            test("test basic list with consecutive duplicate at the beginning") {
                val expectedValues = listOf("a", "b", "c")

                val actualValues = listOf("a", "a", "b", "c").filterWithNextEnclosing { a, b -> a != b }

                actualValues shouldBe expectedValues
            }

            test("test basic list with same enclosing pair") {
                val expectedValues = listOf("a", "b", "c")

                val actualValues = listOf("a", "a", "b", "c", "a").filterWithNextEnclosing { a, b -> a != b }

                actualValues shouldBe expectedValues
            }

            test("test removal of multiple consecutive objects") {
                val expectedValues = listOf("a", "b", "c")

                val actualValues = listOf("a", "b", "b", "b", "c").filterWithNextEnclosing { a, b -> a != b }

                actualValues shouldBe expectedValues
            }

            test("test empty list") {
                val actualValues = emptyList<String>().filterWithNextEnclosing { a, b -> a != b }

                actualValues shouldBe emptyList<String>()
            }

            test("test list with a single element") {
                val actualValues = listOf("a").filterWithNextEnclosing { a, b -> a != b }

                actualValues shouldBe listOf("a")
            }
        }
    })
