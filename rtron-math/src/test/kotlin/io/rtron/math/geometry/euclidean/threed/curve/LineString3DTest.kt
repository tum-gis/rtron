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

package io.rtron.math.geometry.euclidean.threed.curve

import arrow.core.Either
import arrow.core.nonEmptyListOf
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.std.DBL_EPSILON
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class LineString3DTest {

    @Nested
    inner class TestLengthCalculation {

        @Test
        fun `line string with two points should have a length of 1`() {
            val lineString = LineString3D(nonEmptyListOf(Vector3D.ZERO, Vector3D.X_AXIS), 0.0)

            val actualLength = lineString.length

            assertThat(actualLength).isEqualTo(1.0)
        }

        @Test
        fun `line string with multiple points should have a length of 1`() {
            val pointA = Vector3D.ZERO
            val pointB = Vector3D(1.0, 0.0, 0.0)
            val pointC = Vector3D(1.0, 1.0, 0.0)
            val pointD = Vector3D(0.0, 1.0, 0.0)
            val lineString = LineString3D(nonEmptyListOf(pointA, pointB, pointC, pointD), 0.0)

            val actualLength = lineString.length

            assertThat(actualLength).isEqualTo(3.0)
        }
    }

    @Nested
    inner class TestPointCalculation {

        @Test
        fun `line string with two points yields point in the middle`() {
            val pointA = Vector3D.ZERO
            val pointB = Vector3D(0.0, 10.0, 0.0)
            val lineString = LineString3D(nonEmptyListOf(pointA, pointB), 0.0)

            val actualReturn = lineString.calculatePointGlobalCS(CurveRelativeVector1D(5.0))

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.x).isCloseTo(0.0, Offset.offset(DBL_EPSILON))
            assertThat(actualReturn.value.y).isCloseTo(5.0, Offset.offset(DBL_EPSILON))
            assertThat(actualReturn.value.z).isCloseTo(0.0, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `line string with multiple points yields point on the top`() {
            val pointA = Vector3D.ZERO
            val pointB = Vector3D(1.0, 0.0, 0.0)
            val pointC = Vector3D(1.0, 1.0, 0.0)
            val pointD = Vector3D(0.0, 1.0, 0.0)
            val lineString = LineString3D(nonEmptyListOf(pointA, pointB, pointC, pointD), 0.0)

            val actualReturn = lineString.calculatePointGlobalCS(CurveRelativeVector1D(2.5))

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.x).isCloseTo(0.5, Offset.offset(DBL_EPSILON))
            assertThat(actualReturn.value.y).isCloseTo(1.0, Offset.offset(DBL_EPSILON))
            assertThat(actualReturn.value.z).isCloseTo(0.0, Offset.offset(DBL_EPSILON))
        }
    }
}
