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

class SequencesKtTest : FunSpec({
    context("TestZipWithNextEnclosing") {

        test("test basic sequence") {
            val expectedZips = listOf(Pair("a", "b"), Pair("b", "c"), Pair("c", "a"))

            val actualZips = listOf("a", "b", "c").zipWithNextEnclosing()

            actualZips shouldBe expectedZips
        }

        test("empty list should return an empty list") {
            val actualZips = emptyList<String>().zipWithNextEnclosing()

            actualZips shouldBe emptyList<String>()
        }

        test("list with a single element should return an empty list") {
            val actualZips = listOf("a").zipWithNextEnclosing()

            actualZips shouldBe emptyList<String>()
        }
    }

    context("TestZipWithConsecutives") {

        test("test basic sequence") {
            val expected = listOf(listOf("a", "a", "a"), listOf("b"), listOf("c", "c"))

            val actual = listOf("a", "a", "a", "b", "c", "c").zipWithConsecutives { it }

            actual shouldBe expected
        }

        test("test basic sequence with same enclosing pair") {
            val expected = listOf(listOf("a", "a", "a"), listOf("b"), listOf("c", "c"), listOf("a"))

            val actual = listOf("a", "a", "a", "b", "c", "c", "a").zipWithConsecutives { it }

            actual shouldBe expected
        }

        test("empty list should return an empty list") {
            val actualZips = emptyList<String>().zipWithConsecutives { it }

            actualZips shouldBe emptyList<String>()
        }

        test("test single element") {
            val actualZips = listOf("a").zipWithConsecutives { it }

            actualZips shouldBe listOf(listOf("a"))
        }
    }

    context("TestZipWithConsecutivesEnclosing") {

        test("test without enclosing pair") {
            val expected = listOf(listOf("a", "a", "a"), listOf("b"), listOf("c", "c"))

            val actual = listOf("a", "a", "a", "b", "c", "c").zipWithConsecutivesEnclosing { it }

            actual shouldBe expected
        }

        test("test basic sequence with same enclosing pair") {
            val elements = listOf("a", "a", "a", "b", "c", "c", "a")
            val expectedZips = listOf(listOf("a", "a", "a", "a"), listOf("b"), listOf("c", "c"))

            val actualZips = elements.zipWithConsecutivesEnclosing { it }

            actualZips shouldBe expectedZips
        }

        test("empty list should return an empty list") {
            val actualZips = emptyList<String>().zipWithConsecutivesEnclosing { it }

            actualZips shouldBe emptyList<String>()
        }

        test("test single element") {
            val actualZips = listOf("a").zipWithConsecutivesEnclosing { it }

            actualZips shouldBe listOf(listOf("a"))
        }

        test("test ordering") {
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
                listOf(pair4, pair5)
            )

            val actualZips = startList.zipWithConsecutivesEnclosing { it.first }

            actualZips shouldBe expectedZips
        }
    }
})
