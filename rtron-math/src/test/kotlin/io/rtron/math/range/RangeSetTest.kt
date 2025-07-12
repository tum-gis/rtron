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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class RangeSetTest :
    FunSpec({

        context("TestContains") {

            test("contains primitive value") {
                val rangeA = Range.closedOpen(1.0, 1.3)
                val rangeB = Range.closedOpen(10.0, 12.0)
                val rangeC = Range.closed(1.3, 2.0)
                val rangeSet = RangeSet.of(rangeA, rangeB, rangeC)

                rangeSet.contains(1.3).shouldBeTrue()
            }
        }

        context("TestUnion") {

            test("simple union of two disconnected range sets") {
                val rangeA = Range.closedOpen(1.0, 1.3)
                val rangeB = Range.closed(1.4, 2.0)
                val rangeSetA = RangeSet.of(rangeA)
                val rangeSetB = RangeSet.of(rangeB)

                val actualUnion = rangeSetA.union(rangeSetB)

                actualUnion.asRanges().shouldContainExactlyInAnyOrder(rangeA, rangeB)
            }

            test("simple union of two connected range sets") {
                val rangeA = Range.closedOpen(1.0, 1.3)
                val rangeB = Range.closed(1.3, 2.0)
                val rangeSetA = RangeSet.of(rangeA)
                val rangeSetB = RangeSet.of(rangeB)
                val expectedRange = Range.closed(1.0, 2.0)

                val actualUnion = rangeSetA.union(rangeSetB)

                actualUnion.asRanges().shouldContainExactly(expectedRange)
            }
        }

        context("TestIntersection") {

            test("two disconnected range sets do not intersect") {
                val rangeA = Range.closedOpen(1.0, 1.3)
                val rangeB = Range.closed(1.4, 2.0)
                val rangeSetA = RangeSet.of(rangeA)
                val rangeSetB = RangeSet.of(rangeB)

                rangeSetA.intersects(rangeSetB).shouldBeFalse()
            }

            test("two connected range sets do not intersect") {
                val rangeA = Range.closedOpen(1.0, 1.3)
                val rangeB = Range.closed(1.3, 2.0)
                val rangeSetA = RangeSet.of(rangeA)
                val rangeSetB = RangeSet.of(rangeB)

                rangeSetA.intersects(rangeSetB).shouldBeFalse()
            }

            test("two connected and closed range sets do intersect") {
                val rangeA = Range.closed(1.0, 1.3)
                val rangeB = Range.closed(1.3, 2.0)
                val rangeSetA = RangeSet.of(rangeA)
                val rangeSetB = RangeSet.of(rangeB)

                rangeSetA.intersects(rangeSetB).shouldBeTrue()
            }
        }
    })
