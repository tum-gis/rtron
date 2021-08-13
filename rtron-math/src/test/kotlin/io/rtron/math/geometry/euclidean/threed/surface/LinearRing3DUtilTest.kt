/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.geometry.euclidean.threed.surface

import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.processing.removeConsecutiveSideDuplicates
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class LinearRing3DUtilTest {

    @Nested
    inner class TestRemoveCuts {

        @Test
        fun `remove cut with edge matching pattern`() {
            val pointA = Vector3D.ZERO
            val pointB = Vector3D(-1.0, -1.0, 0.0)
            val pointC = Vector3D.X_AXIS
            val pointD = Vector3D.Y_AXIS
            val vertices = listOf(pointA, pointB, pointA, pointC, pointD, pointB)
            val expected = listOf(pointA, pointC, pointD, pointB)

            val actualRemovedVerticesList = vertices.removeConsecutiveSideDuplicates()

            assertThat(actualRemovedVerticesList).isEqualTo(expected)
        }
    }
}
