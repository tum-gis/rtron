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

import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class LineSegment3DTest {

    @Nested
    inner class TestDistance {

        @Test
        fun `point located on line segment returns zero distance`() {
            val lineSegment = LineSegment3D(Vector3D.ZERO, Vector3D.X_AXIS, 0.0)
            val point = Vector3D(0.5, 0.0, 0.0)
            val expectedDistance = 0.0

            val actualResultingVertices = lineSegment.distance(point)

            assertThat(actualResultingVertices).isEqualTo(expectedDistance)
        }

        @Test
        fun `point located outside of line segment returns the distance to closest boundary point`() {
            val lineSegment = LineSegment3D(Vector3D.ZERO, Vector3D.X_AXIS, 0.0)
            val point = Vector3D(1.5, 0.0, 0.0)
            val expectedDistance = 0.5

            val actualResultingVertices = lineSegment.distance(point)

            assertThat(actualResultingVertices).isEqualTo(expectedDistance)
        }

        @Test
        fun `point located not on line segment returns the correct distance`() {
            val lineSegment = LineSegment3D(Vector3D.ZERO, Vector3D.X_AXIS, 0.0)
            val point = Vector3D(0.5, 2.3, 0.0)
            val expectedDistance = 2.3

            val actualResultingVertices = lineSegment.distance(point)

            assertThat(actualResultingVertices).isEqualTo(expectedDistance)
        }

        @Test
        fun `boundary point of line segment returns the zero distance`() {
            val lineSegment = LineSegment3D(Vector3D.ZERO, Vector3D.X_AXIS, 0.0)
            val point = Vector3D.X_AXIS
            val expectedDistance = 0.0

            val actualResultingVertices = lineSegment.distance(point)

            assertThat(actualResultingVertices).isEqualTo(expectedDistance)
        }

        @Test
        fun `point on line but outside of line segment return distances to the shortest boundary point`() {
            val lineSegment = LineSegment3D(Vector3D.ZERO, Vector3D.X_AXIS, 0.0)
            val point = Vector3D(1.5, 0.0, 0.0)
            val expectedDistance = 0.5

            val actualResultingVertices = lineSegment.distance(point)

            assertThat(actualResultingVertices).isEqualTo(expectedDistance)
        }
    }
}
