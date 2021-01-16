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

package io.rtron.math.geometry.euclidean.twod

import io.rtron.math.geometry.euclidean.twod.curve.Spiral2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON_1
import io.rtron.math.std.PI
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class Spiral2DTest {

    @Nested
    inner class TestPointCalculation {

        @Test
        fun `return (0,0) at l=0`() {
            val spiral = Spiral2D(1.0)

            val actualPoint = spiral.calculatePoint(0.0)

            assertThat(actualPoint).isEqualTo(Vector2D.ZERO)
        }

        @Test
        fun `return asymptotic point at l=+infinity`() {
            val asymptoticPoint = Vector2D(0.5, 0.5)
            val spiral = Spiral2D(PI)

            val actualPoint = spiral.calculatePoint(Double.POSITIVE_INFINITY)

            assertThat(actualPoint.x).isCloseTo(asymptoticPoint.x, Offset.offset(DBL_EPSILON_1))
            assertThat(actualPoint.y).isCloseTo(asymptoticPoint.y, Offset.offset(DBL_EPSILON_1))
        }

        @Test
        fun `return asymptotic point at l=-infinity`() {
            val asymptoticPoint = Vector2D(-0.5, -0.5)
            val spiral = Spiral2D(PI)

            val actualPoint = spiral.calculatePoint(Double.NEGATIVE_INFINITY)

            assertThat(actualPoint.x).isCloseTo(asymptoticPoint.x, Offset.offset(DBL_EPSILON_1))
            assertThat(actualPoint.y).isCloseTo(asymptoticPoint.y, Offset.offset(DBL_EPSILON_1))
        }
    }

    @Nested
    inner class TestRotationCalculation {

        @Test
        fun `return 0 at l=0`() {
            val spiral = Spiral2D(1.0)

            val actualRotation = spiral.calculateRotation(0.0)

            assertThat(actualRotation.toAngleRadians()).isEqualTo(0.0)
        }
    }
}
