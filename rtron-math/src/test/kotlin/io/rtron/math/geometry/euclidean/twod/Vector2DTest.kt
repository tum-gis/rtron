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

import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.PI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

internal class Vector2DTest {

    @Test
    fun scalarMultiply() {
        val actualValue = Vector2D(1.0, 1.0).scalarMultiply(4.0)

        assertThat(actualValue).isEqualTo(Vector2D(4.0, 4.0))
    }

    @Nested
    inner class TestDistanceCalculation {
        @Test
        fun `even value distance between two point`() {
            val pointA = Vector2D(0.0, 0.0)
            val pointB = Vector2D(3.0, 0.0)

            val actualDistance = pointA.distance(pointB)

            assertThat(actualDistance).isEqualTo(3.0)
        }

        @Test
        fun `uneven value distance between two point`() {
            val pointA = Vector2D(0.0, 0.0)
            val pointB = Vector2D(1.0, 1.0)

            val actualDistance = pointA.distance(pointB)

            assertThat(actualDistance).isEqualTo(sqrt(2.0))
        }
    }

    @Nested
    inner class TestAngleCalculation {
        @Test
        fun `half pi angle between to axes`() {
            val pointA = Vector2D.X_AXIS
            val pointB = Vector2D.Y_AXIS

            val actualAngle = pointA.angle(pointB).toAngleRadians()

            assertThat(actualAngle).isEqualTo(HALF_PI)
        }

        @Test
        fun `uneven radians angle`() {
            val pointA = Vector2D.X_AXIS
            val pointB = Vector2D(1.0, -1.0)

            val actualAngle = pointA.angle(pointB).toAngleRadians()

            assertThat(actualAngle).isEqualTo(1.75 * PI)
        }

        @Test
        fun `-half pi degree angle axes`() {
            val pointA = Vector2D.X_AXIS
            val pointB = Vector2D(0.0, -1.0)

            val actualAngle = pointA.angle(pointB).toAngleRadians()

            assertThat(actualAngle).isEqualTo(1.5 * PI)
        }
    }
}
