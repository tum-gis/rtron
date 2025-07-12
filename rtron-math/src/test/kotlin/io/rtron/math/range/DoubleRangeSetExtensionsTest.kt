/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize

class DoubleRangeSetExtensionsTest :
    FunSpec({
        context("TestCreation") {

            test("intersecting ranges should throw an IllegalArgumentException") {
                val rangeA = Range.closed(0.0, 2.0)
                val rangeB = Range.closed(1.0, 4.0)

                shouldThrow<IllegalArgumentException> {
                    RangeSet.ofNonIntersectingRanges(rangeA, rangeB)
                }
            }

            test("connected ranges are combined to one range") {
                val rangeA = Range.closedOpen(0.0, 2.0)
                val rangeB = Range.closed(2.0, 4.0)

                val actualRangeSet = RangeSet.ofNonIntersectingRanges(rangeA, rangeB)

                actualRangeSet.asRanges() shouldHaveSize 1
            }
        }
    })
