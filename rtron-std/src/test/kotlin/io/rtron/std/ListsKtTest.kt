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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ListsKtTest : FunSpec({
    context("TestMoveWindow") {

        test("basic moving window test with differently sized lists") {
            val listA = listOf(false, false, true, false, false)
            val listB = listOf(true, true, false)
            val expectedList = listOf(false, false, true, true, false, false, false)

            val actualList = listA.moveWindow(
                listB,
                { baseElement, otherElement -> baseElement && otherElement },
                { a, b -> a || b }
            )

            actualList shouldBe expectedList
        }

        test("fail if base list is empty") {
            val listA = listOf<Boolean>()
            val listB = listOf(true, false)

            shouldThrow<IllegalArgumentException> {
                listA.moveWindow(
                    listB,
                    { baseElement, otherElement -> baseElement && otherElement },
                    { a, b -> a || b }
                )
            }
        }

        test("fail if other list is empty") {
            val listA = listOf(true, false)
            val listB = listOf<Boolean>()

            shouldThrow<IllegalArgumentException> {
                listA.moveWindow(
                    listB,
                    { baseElement, otherElement -> baseElement && otherElement },
                    { a, b -> a || b }
                )
            }
        }

        test("moving window with same type shape") {
            val listA = listOf(false, false, true, false, false)
            val listB = listOf(true, true, false)
            val expectedList = listOf(false, false, true, true, false)

            val actualList = listA.moveWindow(
                listB,
                { baseElement, otherElement -> baseElement && otherElement },
                { a, b -> a || b },
                shape = MovingWindowShape.SAME
            )

            actualList shouldBe expectedList
        }

        test("moving window with doubles") {
            val listA = listOf(0.0, 2.0, 1.0, 0.0, 0.0)
            val listB = listOf(1.0, 1.0, 0.0)
            val expectedList = listOf(0.0, 2.0, 3.0, 1.0, 0.0, 0.0, 0.0)

            val actualList = listA.moveWindow(
                listB,
                { baseElement, otherElement -> baseElement * otherElement },
                { a, b -> a + b }
            )

            actualList shouldBe expectedList
        }
    }

    context("TestSlidedWindowBoolean") {

        test("moving window with booleans and type same") {
            val listA = listOf(false, true, false, false, false, true, false)
            val listB = listOf(true, false, true)
            val expectedList = listOf(false, true, false, true, false, true, false)

            val actualList = listA.moveWindow(listB, shape = MovingWindowShape.SAME)

            actualList shouldBe expectedList
        }
    }

    context("TestFilterWindowedEnclosing") {

        test("fail if requested sublist size is greater than list size") {
            val mainList = listOf("a", "a", "a", "b", "c", "c")

            shouldThrow<IllegalArgumentException> {
                mainList.filterWindowedEnclosing(7) { it[0] == it[1] }
            }
        }

        test("test basic sublist pattern filter") {
            val mainList = listOf(1, 2, 3, 4, 4, 3, 3, 4, 4, 5, 6, 7, 8)
            val expectedList = listOf(1, 2, 3, 5, 6, 7, 8)

            val actualList = mainList.filterWindowedEnclosing(3) { it[0] == 3 && it[1] == 4 && it[2] == 4 }

            actualList shouldBe expectedList
        }

        test("sublist pattern filter with drop indices on characters") {
            val mainList = listOf("P1", "P2", "P3", "P4", "P3", "P5", "P6", "P7", "P8")
            val dropIndices = listOf(false, true, true)
            val expectedList = listOf("P1", "P2", "P3", "P5", "P6", "P7", "P8")

            val actualList = mainList.filterWindowedEnclosing(dropIndices) { it[0] == it[2] }

            actualList shouldBe expectedList
        }

        test("test removal of consecutive duplicates") {
            val mainList = listOf("A", "A", "B", "C", "D")
            val dropIndices = listOf(false, true)
            val expectedList = listOf("A", "B", "C", "D")

            val actualList = mainList.filterWindowedEnclosing(dropIndices) { it[0] == it[1] }

            actualList shouldBe expectedList
        }

        test("test removal of consecutive and enclosing duplicates by dropping the first window indices") {
            val mainList = listOf("A", "A", "B", "C", "A")
            val dropIndices = listOf(true, false)
            val expectedList = listOf("A", "B", "C")

            val actualList = mainList.filterWindowedEnclosing(dropIndices) { it[0] == it[1] }

            actualList shouldBe expectedList
        }

        test("test removal of consecutive and enclosing duplicates by dropping the second window indices") {
            val mainList = listOf("A", "A", "B", "C", "A")
            val dropIndices = listOf(false, true)
            val expectedList = listOf("A", "B", "C", "A")

            val actualList = mainList.filterWindowedEnclosing(dropIndices) { it[0] == it[1] }

            actualList shouldBe expectedList
        }
    }
})
