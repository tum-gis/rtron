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

package io.rtron.math.range

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import io.rtron.math.std.DBL_EPSILON


internal class RangeTest {

    @Nested
    inner class UpperLowerEndpoint {

        @Test
        fun `lowerEndpoint should yield null, if no lower endpoint exists`() {
            val range: Range<Double> = Range.all()

            assertNull(range.lowerEndpointOrNull())
        }

        @Test
        fun `upperEndpoint should yield null, if no upper endpoint exists`() {
            val range: Range<Double> = Range.all()

            assertNull(range.upperEndpointOrNull())
        }
    }

    @Nested
    inner class ContainsValue {

        @Test
        fun `closed range should contain basic value`() {
            val range = Range.closed(-5.0, -2.0)

            val actualContains = range.contains(-2.0)

            assertTrue(actualContains)
        }

        @Test
        fun `closed range should contain lower value`() {
            val range = Range.closed(-5.0, -2.0)

            val actualContains = range.contains(-5.0)

            assertTrue(actualContains)
        }

        @Test
        fun `closed range should contain upper value`() {
            val range = Range.closed(-5.0, -2.0)

            val actualContains = range.contains(-2.0)

            assertTrue(actualContains)
        }

        @Test
        fun `greaterThan range contains infinity`() {
            val range = Range.greaterThan(1.0)

            val actualContains = range.contains(Double.POSITIVE_INFINITY)
            assertTrue(actualContains)
        }

        @Test
        fun `lessThan range contains negative infinity`() {
            val range = Range.lessThan(-12.0)

            val actualContains = range.contains(Double.NEGATIVE_INFINITY)

            assertTrue(actualContains)
        }

        @Test
        fun `closed range does not contain positive infinity`() {
            val range = Range.open(1.0, 555.3)

            val actualContains = range.contains(Double.POSITIVE_INFINITY)

            assertFalse(actualContains)
        }

        @Test
        fun `closed start range does not contain negative infinity`() {
            val range = Range.closed(-20.0, 0.0)

            val actualContains = range.contains(Double.NEGATIVE_INFINITY)

            assertFalse(actualContains)
        }

        @Test
        fun `closed range contains negative start limit`() {
            val actualContains = Range.closed(-3.1, 2.0).contains(-3.1)

            assertTrue(actualContains)
        }

        @Test
        fun `closed range contains positive end limit`() {
            val actualContains = Range.closed(0.0, 2.0).contains(2.0)

            assertTrue(actualContains)
        }

        @Test
        fun `closed range does not contain negative start limit`() {
            val actualContains = Range.closed(-3.1, 2.0).contains(-3.10000001)

            assertFalse(actualContains)
        }

        @Test
        fun `closed range does not contain positive start limit`() {
            val actualContains = Range.closed(0.0, 2.0).contains(2.0001)

            assertFalse(actualContains)
        }
    }

    @Nested
    inner class TestConstruction {

        @Test
        fun `negative orientation of the range throws an illegal argument exception`() {
            assertThatIllegalArgumentException().isThrownBy { Range.closed(-1.0, -1.25) }
        }

    }

    @Nested
    inner class TestIsConnected {

        @Test
        fun `is not connected`() {
            val rangeA = Range.closed(1.0, 1.0)
            val rangeB = Range.closed(2.0, 2.0)

            val actualIsConnected = rangeA.isConnected(rangeB)

            assertFalse(actualIsConnected)
        }

        @Test
        fun `is not connected with epsilon`() {
            val rangeA = Range.closed(0.0, 1.0)
            val rangeB = Range.closed((1.0 + DBL_EPSILON), 2.0)

            val actualIsConnected = rangeA.isConnected(rangeB)

            assertFalse(actualIsConnected)
        }

        @Test
        fun `is connected`() {
            val rangeA = Range.closed(0.0, 1.0)
            val rangeB = Range.closed((1.0 - DBL_EPSILON), 2.0)

            val actualIsConnected = rangeA.isConnected(rangeB)

            assertTrue(actualIsConnected)
        }

        @Test
        fun `connected range joins with positive values`() {
            val rangeA = Range.closed(0.0, 1.0)
            val rangeB = Range.closed(1.0, 2.0)

            val actualJoin = rangeA.join(rangeB)

            assertThat(actualJoin).isEqualTo(Range.closed(0.0, 2.0))
        }

        @Test
        fun `Connected joins with negative values`() {
            val rangeA = Range.closed(-1.0, -0.5)
            val rangeB = Range.closed(-0.5, -0.2)

            val actualJoin = rangeA.join(rangeB)

            assertThat(actualJoin).isEqualTo(Range.closed(-1.0, -0.2))
        }

        @Test
        fun `join throws exception if the ranges are not connected`() {
            val rangeA = Range.closed(-1.0, -0.51)
            val rangeB = Range.closed(-0.5, -0.2)

            assertThatIllegalArgumentException().isThrownBy { rangeA.join(rangeB) }
        }

    }

}
