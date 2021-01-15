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

package io.rtron.math.geometry.euclidean.twod.curve

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.QUARTER_PI
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.sqrt


internal class LineSegment2DTest {

    @Nested
    inner class TestLengthCalculation {
        @Test
        fun `length of line segment on axis`() {
            val pointA = Vector2D(0.0, 0.0)
            val pointB = Vector2D(7.0, 0.0)
            val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)

            assertThat(lineSegment.length).isCloseTo(7.0, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `length of diagonal line segment`() {
            val pointA = Vector2D(3.0, 0.0)
            val pointB = Vector2D(0.0, 4.0)
            val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)

            assertThat(lineSegment.length).isCloseTo(5.0, Offset.offset(DBL_EPSILON))
        }
    }

    @Nested
    inner class TestPoseAngleCalculation {
        @Test
        fun `angle of line segment on axis`() {
            val pointA = Vector2D(0.0, 0.0)
            val pointB = Vector2D(0.0, 1.0)
            val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)

            val actualReturn = lineSegment.calculatePoseGlobalCS(CurveRelativeVector1D.ZERO)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.rotation.angle).isEqualTo(HALF_PI)
        }

        @Test
        fun `angle of diagonal line segment`() {
            val pointA = Vector2D(0.0, 0.0)
            val pointB = Vector2D(1.0, 1.0)
            val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)

            val actualReturn = lineSegment.calculatePoseGlobalCS(CurveRelativeVector1D.ZERO)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.rotation.angle).isEqualTo(QUARTER_PI)
        }
    }

    @Nested
    inner class TestPosePointCalculation {
        @Test
        fun `point on line segment on axis`() {
            val pointA = Vector2D(0.0, 0.0)
            val pointB = Vector2D(10.0, 0.0)
            val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)
            val curveRelativePoint = CurveRelativeVector1D(5.0)


            val actualReturn = lineSegment.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.point).isEqualTo(Vector2D(5.0, 0.0))
        }

        @Test
        fun `point on diagonal line segment on axis`() {
            val pointA = Vector2D(0.0, 0.0)
            val pointB = Vector2D(1.0, 1.0)
            val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)
            val curveRelativePoint = CurveRelativeVector1D(sqrt(2.0))

            val actualReturn = lineSegment.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.point.x).isCloseTo(1.0, Offset.offset(DBL_EPSILON))
            assertThat(actualReturn.value.point.y).isCloseTo(1.0, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `point on diagonal line segment on axis 2`() {
            val pointA = Vector2D(-1.0, 0.0)
            val pointB = Vector2D(0.0, 0.0)
            val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)
            val curveRelativePoint = CurveRelativeVector1D(1.0)

            val actualReturn = lineSegment.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.point).isEqualTo(Vector2D(0.0, 0.0))
        }
    }

}
