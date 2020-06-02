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

package io.rtron.math.processing

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Plane3D
import io.rtron.math.std.DBL_EPSILON_2


internal class Plane3DUtilTest {

    @Nested
    inner class TestBestFittingPlaneCalculation {

        @Test
        fun `normal for clockwise point in XY-plane`() {
            val pointA = -Vector3D.X_AXIS
            val pointB = Vector3D.Y_AXIS
            val pointC = Vector3D.X_AXIS
            val expectedNormal = -Vector3D.Z_AXIS

            val actualBestFittingPlane = listOf(pointA, pointB, pointC).calculateBestFittingPlane()

            assertThat(actualBestFittingPlane.normal).isEqualTo(expectedNormal)
        }

        @Test
        fun `normal for counter clockwise point in XY-plane`() {
            val pointA = Vector3D.X_AXIS
            val pointB = Vector3D.Y_AXIS
            val pointC = -Vector3D.X_AXIS
            val expectedNormal = -Vector3D.Z_AXIS

            val actualBestFittingPlane = listOf(pointA, pointB, pointC).calculateBestFittingPlane()

            assertThat(actualBestFittingPlane.normal).isEqualTo(expectedNormal)
        }

        @Test
        fun `plane fitting with three points`() {
            val pointA = Vector3D(1.0, 0.0, 0.0)
            val pointB = Vector3D(1.0, 3.0, 0.0)
            val pointC = Vector3D(1.0, 0.0, 3.0)
            val expectedCentroid = Vector3D(1.0, 1.0, 1.0)
            val expectedNormal = -Vector3D.X_AXIS
            val expectedPlane = Plane3D(expectedCentroid, expectedNormal)

            val actualBestFittingPlane = listOf(pointA, pointB, pointC).calculateBestFittingPlane()

            assertThat(actualBestFittingPlane).isEqualTo(expectedPlane)
        }

        @Test
        fun `plane fitting with multiple points`() {
            val pointA = Vector3D(4.0, 1.0, -3.0)
            val pointB = Vector3D(2.0, 2.0, -2.0)
            val pointC = Vector3D(1.0, 1.0, 3.0)
            val pointD = Vector3D(1.0, -1.0, 9.0)
            val expectedCentroid = Vector3D(1.0, 1.0, 3.0)
            val expectedNormal = Vector3D(2.0, 3.0, 1.0).normalized()
            val expectedPlane = Plane3D(expectedCentroid, expectedNormal)

            val actualBestFittingPlane = listOf(pointA, pointB, pointC, pointD).calculateBestFittingPlane()

            assertThat(actualBestFittingPlane.normal.toDoubleArray())
                    .containsExactly(expectedPlane.normal.toDoubleArray(), Offset.offset(DBL_EPSILON_2))
            assertTrue(actualBestFittingPlane.isSimilarTo(expectedPlane))
        }

        @Test
        fun `plane fitting with five points not in the same plane`() {
            val pointA = Vector3D(1.0, 0.0, 0.0)
            val pointB = Vector3D(1.0, 3.0, 0.0)
            val pointC = Vector3D(1.0, 0.0, 3.0)
            val pointD = Vector3D(0.0, 1.0, 1.0)
            val pointE = Vector3D(2.0, 1.0, 1.0)
            val expectedCentroid = Vector3D(1.0, 1.0, 1.0)
            val expectedNormal = -Vector3D.X_AXIS
            val expectedPlane = Plane3D(expectedCentroid, expectedNormal)

            val actualBestFittingPlane = listOf(pointA, pointB, pointC, pointD, pointE)
                    .calculateBestFittingPlane()

            assertThat(actualBestFittingPlane).isEqualTo(expectedPlane)
        }

    }

}
