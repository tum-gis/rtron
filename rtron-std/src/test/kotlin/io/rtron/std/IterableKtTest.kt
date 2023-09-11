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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class IterableKtTest : FunSpec({
    context("TestIsSorted") {

        test("isSorted() returns true if integer list is sorted ascending") {
            val sortedList = listOf(1, 7, 7, 12, 13)

            sortedList.isSorted().shouldBeTrue()
        }

        test("isSorted() returns false if integer list is not sorted ascending") {
            val sortedList = listOf(1, 7, 3, 12, 13)

            sortedList.isSorted().shouldBeFalse()
        }

        test("isSortedDescending() returns true if integer list is sorted descending") {
            val sortedList = listOf(13, 7, 7, 1, -1)

            sortedList.isSortedDescending().shouldBeTrue()
        }

        test("isSortedDescending() returns false if integer list is not sorted descending") {
            val sortedList = listOf(13, 7, 7, 1, -1, 0)

            sortedList.isSortedDescending().shouldBeFalse()
        }
    }
})
