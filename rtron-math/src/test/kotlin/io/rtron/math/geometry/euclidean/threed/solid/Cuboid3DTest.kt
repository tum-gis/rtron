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

package io.rtron.math.geometry.euclidean.threed.solid

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


internal class Cuboid3DTest {

    @Nested
    inner class PolygonsGeneration {

        @Test
        fun `correct number of polygons`() {
            val cuboid = Cuboid3D.UNIT

            val actualPolygonsResult = cuboid.calculatePolygonsGlobalCS()

            assertThat(actualPolygonsResult).isInstanceOf(Result.Success::class.java)
            require(actualPolygonsResult is Result.Success)
            assertThat(actualPolygonsResult.value).hasSize(6)
        }

        @Test
        fun `generated polygons list contain base polygon`() {
            val length = 6.0
            val width = 4.0
            val cuboid = Cuboid3D(length = length, width = width, height = 1.0, tolerance = 0.0)
            val vertexA = Vector3D(length / 2.0, width / 2.0, 0.0)
            val vertexB = Vector3D(length / 2.0, -width / 2.0, 0.0)
            val vertexC = Vector3D(-length / 2.0, -width / 2.0, 0.0)
            val vertexD = Vector3D(-length / 2.0, width / 2.0, 0.0)
            val expectedBasePolygon = Polygon3D.of(vertexA, vertexB, vertexC, vertexD, tolerance = 0.0)

            val actualPolygonsResult = cuboid.calculatePolygonsGlobalCS()

            assertThat(actualPolygonsResult).isInstanceOf(Result.Success::class.java)
            require(actualPolygonsResult is Result.Success)
            assertThat(actualPolygonsResult.value).contains(expectedBasePolygon)
        }

        @Test
        fun `generated polygons list contain elevated polygon`() {
            val length = 5.0
            val width = 3.0
            val height = 1.0
            val cuboid = Cuboid3D(length = length, width = width, height = height, tolerance = 0.0)
            val vertexA = Vector3D(length / 2.0, width / 2.0, height)
            val vertexB = Vector3D(-length / 2.0, width / 2.0, height)
            val vertexC = Vector3D(-length / 2.0, -width / 2.0, height)
            val vertexD = Vector3D(length / 2.0, -width / 2.0, height)
            val expectedBasePolygon = Polygon3D.of(vertexA, vertexB, vertexC, vertexD, tolerance = 0.0)

            val actualPolygonsResult = cuboid.calculatePolygonsGlobalCS()

            assertThat(actualPolygonsResult).isInstanceOf(Result.Success::class.java)
            require(actualPolygonsResult is Result.Success)
            assertThat(actualPolygonsResult.value).contains(expectedBasePolygon)
        }

        @Test
        fun `generated polygons list contain front polygon`() {
            val length = 5.0
            val width = 3.0
            val height = 1.0
            val cuboid = Cuboid3D(length = length, width = width, height = height, tolerance = 0.0)
            val vertexA = Vector3D(length / 2.0, width / 2.0, 0.0)
            val vertexB = Vector3D(length / 2.0, width / 2.0, height)
            val vertexC = Vector3D(length / 2.0, -width / 2.0, height)
            val vertexD = Vector3D(length / 2.0, -width / 2.0, 0.0)
            val expectedBasePolygon = Polygon3D.of(vertexA, vertexB, vertexC, vertexD, tolerance = 0.0)

            val actualPolygonsResult = cuboid.calculatePolygonsGlobalCS()

            assertThat(actualPolygonsResult).isInstanceOf(Result.Success::class.java)
            require(actualPolygonsResult is Result.Success)
            assertThat(actualPolygonsResult.value).contains(expectedBasePolygon)
        }

    }
}
