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

package io.rtron.math.geometry.euclidean.twod

import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.PI
import io.rtron.math.std.TWO_PI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class Pose2DTest {

    @Nested
    inner class TestPointAssignment {

        @Test
        fun `assignment of positive point`() {
            val pose = Pose2D(Vector2D(5.0, 3.0), Rotation2D.ZERO)

            assertThat(pose.point).isEqualTo(Vector2D(5.0, 3.0))
        }

        @Test
        fun `assignment of negative point`() {
            val pose = Pose2D(Vector2D(-3.0, 1.0), Rotation2D.ZERO)

            assertThat(pose.point).isEqualTo(Vector2D(-3.0, 1.0))
        }
    }

    @Nested
    inner class TestRotationAssignment {
        @Test
        fun `assignment of two pi rotation`() {
            val pose = Pose2D(Vector2D(5.0, 3.0), Rotation2D(TWO_PI))

            assertThat(pose.rotation.toAngleRadians()).isEqualTo(0.0)
        }

        @Test
        fun `assignment of pi rotation`() {
            val pose = Pose2D(Vector2D(-3.0, 1.0), Rotation2D(PI))

            assertThat(pose.rotation.toAngleRadians()).isEqualTo(PI)
        }
    }
}
