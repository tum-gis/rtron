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

package io.rtron.math.geometry.euclidean.threed.point

import io.rtron.math.geometry.euclidean.threed.Pose3D
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class Pose3DTest {

    @Nested
    inner class TestConversionToPose2D {

        @Test
        fun `conversion to pose2D within x-z-plane `() {
            val point = Vector3D(1.0, 2.0, 3.0)
            val rotation = Rotation3D(1.0, 2.0, 3.0)
            val pose3D = Pose3D(point, rotation)
            val expectedPose2D = Pose2D(Vector2D(1.0, 3.0), Rotation2D(2.0))

            val actualPose2D = pose3D.toPose2D(Vector3D.Y_AXIS)

            assertThat(actualPose2D).isEqualTo(expectedPose2D)
        }
    }
}
