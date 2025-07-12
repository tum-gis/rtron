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

package io.rtron.math.range

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.rtron.math.std.DBL_EPSILON

class RangeTest :
    FunSpec({
        context("UpperLowerEndpoint") {

            test("lowerEndpoint should yield null, if no lower endpoint exists") {
                val range: Range<Double> = Range.all()

                range.lowerEndpointOrNull().shouldBeNull()
            }

            test("upperEndpoint should yield null, if no upper endpoint exists") {
                val range: Range<Double> = Range.all()

                range.upperEndpointOrNull().shouldBeNull()
            }
        }

        context("ContainsValue") {

            test("closed range should contain basic value") {
                val range = Range.closed(-5.0, -2.0)

                val actualContains = range.contains(-2.0)

                actualContains.shouldBeTrue()
            }

            test("closed range should contain lower value") {
                val range = Range.closed(-5.0, -2.0)

                val actualContains = range.contains(-5.0)

                actualContains.shouldBeTrue()
            }

            test("closed range should contain upper value") {
                val range = Range.closed(-5.0, -2.0)

                val actualContains = range.contains(-2.0)

                actualContains.shouldBeTrue()
            }

            test("greaterThan range contains infinity") {
                val range = Range.greaterThan(1.0)

                val actualContains = range.contains(Double.POSITIVE_INFINITY)

                actualContains.shouldBeTrue()
            }

            test("lessThan range contains negative infinity") {
                val range = Range.lessThan(-12.0)

                val actualContains = range.contains(Double.NEGATIVE_INFINITY)

                actualContains.shouldBeTrue()
            }

            test("closed range does not contain positive infinity") {
                val range = Range.open(1.0, 555.3)

                val actualContains = range.contains(Double.POSITIVE_INFINITY)

                actualContains.shouldBeFalse()
            }

            test("closed start range does not contain negative infinity") {
                val range = Range.closed(-20.0, 0.0)

                val actualContains = range.contains(Double.NEGATIVE_INFINITY)

                actualContains.shouldBeFalse()
            }

            test("closed range contains negative start limit") {
                val actualContains = Range.closed(-3.1, 2.0).contains(-3.1)

                actualContains.shouldBeTrue()
            }

            test("closed range contains positive end limit") {
                val actualContains = Range.closed(0.0, 2.0).contains(2.0)

                actualContains.shouldBeTrue()
            }

            test("closed range does not contain negative start limit") {
                val actualContains = Range.closed(-3.1, 2.0).contains(-3.10000001)

                actualContains.shouldBeFalse()
            }

            test("closed range does not contain positive start limit") {
                val actualContains = Range.closed(0.0, 2.0).contains(2.0001)

                actualContains.shouldBeFalse()
            }
        }

        context("TestConstruction") {

            test("negative orientation of the range throws an illegal argument exception") {
                shouldThrow<IllegalArgumentException> { Range.closed(-1.0, -1.25) }
            }
        }

        context("TestIsConnected") {

            test("is not connected") {
                val rangeA = Range.closed(1.0, 1.0)
                val rangeB = Range.closed(2.0, 2.0)

                val actualIsConnected = rangeA.isConnected(rangeB)

                actualIsConnected.shouldBeFalse()
            }

            test("is not connected with epsilon") {
                val rangeA = Range.closed(0.0, 1.0)
                val rangeB = Range.closed((1.0 + DBL_EPSILON), 2.0)

                val actualIsConnected = rangeA.isConnected(rangeB)

                actualIsConnected.shouldBeFalse()
            }

            test("is connected") {
                val rangeA = Range.closed(0.0, 1.0)
                val rangeB = Range.closed((1.0 - DBL_EPSILON), 2.0)

                val actualIsConnected = rangeA.isConnected(rangeB)

                actualIsConnected.shouldBeTrue()
            }

            test("connected range joins with positive values") {
                val rangeA = Range.closed(0.0, 1.0)
                val rangeB = Range.closed(1.0, 2.0)

                val actualJoin = rangeA.join(rangeB)

                actualJoin shouldBe Range.closed(0.0, 2.0)
            }

            test("Connected joins with negative values") {
                val rangeA = Range.closed(-1.0, -0.5)
                val rangeB = Range.closed(-0.5, -0.2)

                val actualJoin = rangeA.join(rangeB)

                actualJoin shouldBe Range.closed(-1.0, -0.2)
            }

            test("join throws exception if the ranges are not connected") {
                val rangeA = Range.closed(-1.0, -0.51)
                val rangeB = Range.closed(-0.5, -0.2)

                shouldThrow<IllegalArgumentException> { rangeA.join(rangeB) }
            }
        }
    })
