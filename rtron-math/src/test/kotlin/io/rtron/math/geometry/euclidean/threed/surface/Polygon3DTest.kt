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

import arrow.core.Either
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class Polygon3DTest {

    @Nested
    inner class TestCreation {

        @Test
        fun `creation of polygon with only two points should fail`() {
            val pointA = Vector3D(0.0, 0.0, 0.0)
            val pointB = Vector3D(1.0, 1.0, 1.0)

            assertThatIllegalArgumentException().isThrownBy { Polygon3D(listOf(pointA, pointB), 0.0) }
        }

        @Test
        fun `creation of polygon with only one point should fail`() {
            val pointA = Vector3D(0.0, 0.0, 0.0)

            assertThatIllegalArgumentException().isThrownBy { Polygon3D(listOf(pointA), 0.0) }
        }

        @Test
        fun `creation of polygon with no point should fail`() {
            val pointA = Vector3D(0.0, 0.0, 0.0)

            assertThatIllegalArgumentException().isThrownBy { Polygon3D(listOf(pointA), 0.0) }
        }

        @Test
        fun `creation of polygon with consecutive point duplicates should fail`() {
            val pointA = Vector3D(0.0, 0.0, 0.0)
            val pointB = Vector3D(1.0, 1.0, 1.0)

            assertThatIllegalArgumentException()
                .isThrownBy { Polygon3D(listOf(pointA, pointB, pointA), 0.0) }
        }

        @Test
        fun `creation of polygon with three colinear points should fail`() {
            val pointA = Vector3D(1.0, 2.0, 0.0)
            val pointB = Vector3D(2.0, 3.0, 0.0)
            val pointC = Vector3D(3.0, 4.0, 0.0)

            assertThatIllegalArgumentException()
                .isThrownBy { Polygon3D(listOf(pointA, pointB, pointC), 0.0) }
        }

        @Test
        fun `creation of polygon with non-planar points should fail`() {
            val pointA = Vector3D.ZERO
            val pointB = Vector3D.X_AXIS
            val pointC = Vector3D.Y_AXIS
            val pointD = Vector3D(1.0, 1.0, 1.0)

            assertThatIllegalArgumentException()
                .isThrownBy { Polygon3D(listOf(pointA, pointB, pointC, pointD), 0.0) }
        }
    }

    @Nested
    inner class TestNormalCalculation {

        @Test
        fun `normal of triangle polygon`() {
            val pointA = Vector3D(1.0, 1.0, 1.0)
            val pointB = Vector3D(2.0, 1.0, 1.0)
            val pointC = Vector3D(2.0, 2.0, 1.0)
            val triangle = Polygon3D(listOf(pointA, pointB, pointC), 0.0)

            val actualReturn = triangle.getNormal()

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value).isEqualTo(Vector3D.Z_AXIS)
        }

        @Test
        fun `test planar quadrilateral polygon`() {
            val planarQuadrilateral = Polygon3D(
                listOf(
                    Vector3D.ZERO,
                    Vector3D.X_AXIS,
                    Vector3D(1.0, 0.0, 1.0),
                    Vector3D.Z_AXIS
                ),
                0.0
            )
            val expectedResult = Vector3D(0.0, -1.0, 0.0)

            val actualReturn = planarQuadrilateral.getNormal()

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value).isEqualTo(expectedResult)
        }
    }
}
