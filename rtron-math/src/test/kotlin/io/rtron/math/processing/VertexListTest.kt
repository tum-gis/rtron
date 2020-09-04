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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import io.rtron.math.geometry.euclidean.threed.point.Vector3D


internal class VertexListTest {

    @Nested
    inner class TestRemoveLinearlyRedundantVertices {

        @Test
        fun `basic `() {
            val pointA = Vector3D.ZERO
            val pointB = Vector3D.X_AXIS
            val pointC = Vector3D.X_AXIS.scalarMultiply(2.0)
            val pointD = Vector3D(1.0, 1.0, 0.0)
            val vertices = listOf(pointA, pointB, pointC, pointD)
            val expectedVertices = listOf(pointA, pointC, pointD)

            val actualResultingVertices = vertices.removeLinearlyRedundantVertices()

            assertThat(actualResultingVertices).isEqualTo(expectedVertices)
        }
    }

    @Nested
    inner class TestListIsColinear {

        @Test
        fun `three points located on x axis should be colinear`() {
            val pointA = Vector3D.ZERO
            val pointB = Vector3D.X_AXIS
            val pointC = Vector3D.X_AXIS.scalarMultiply(2.0)
            val vertices = listOf(pointA, pointB, pointC)

            val isColinear = vertices.isColinear()

            assertTrue(isColinear)
        }
    }

    @Nested
    inner class TestTripleIsColinear {

        @Test
        fun `if first and third vector are equal, vector triple should be colinear `() {
            val pointA = Vector3D.X_AXIS
            val pointB = Vector3D(3.0, 4.0, 0.0)
            val pointC = Vector3D.X_AXIS
            val vertices = Triple(pointA, pointB, pointC)

            val isColinear = vertices.isColinear()

            assertTrue(isColinear)
        }
    }

    @Nested
    inner class TestIsPlanar {

        @Test
        fun `test triangle polygon`() {
            val points = listOf( Vector3D.ZERO, Vector3D.X_AXIS, Vector3D.Y_AXIS)

            val actual = points.isPlanar()

            assertTrue(actual)
        }

        @Test
        fun `test planar quadrilateral polygon`() {
            val pointA = Vector3D.ZERO
            val pointB = Vector3D.X_AXIS
            val pointC = Vector3D(1.0, 0.0, 1.0)
            val pointD = Vector3D.Z_AXIS
            val points = listOf(pointA, pointB, pointC, pointD)

            val actual = points.isPlanar()

            assertTrue(actual)
        }

        @Test
        fun `test non-planar quadrilateral polygon`() {
            val pointA = Vector3D.ZERO
            val pointB = Vector3D.X_AXIS
            val pointC = Vector3D.Y_AXIS
            val pointD = Vector3D(1.0, 1.0, 1.0)
            val points = listOf(pointA, pointB, pointC, pointD)

            val actual = points.isPlanar()

            assertFalse(actual)
        }

    }

    @Nested
    inner class TestCentroidCalculation {

        @Test
        fun `centroid of triangle`() {
            val pointA = Vector3D.ZERO
            val pointB = Vector3D(6.0, 0.0, 0.0)
            val pointC = Vector3D(0.0, 6.0, 0.0)
            val expectedCentroid = Vector3D(2.0, 2.0, 0.0)

            val actualCentroid = listOf(pointA, pointB, pointC).calculateCentroid()

            assertThat(actualCentroid).isEqualTo(expectedCentroid)
        }

        @Test
        fun `centroid of multiple points`() {
            val points = mutableListOf<Vector3D>()
            points += Vector3D(1.0, 2.0, 1.0)
            points += Vector3D(3.0, 2.0, 1.0)
            points += Vector3D(2.0, 1.0, 3.0)
            points += Vector3D(2.0, 3.0, 7.0)
            val expectedCentroid = Vector3D(2.0, 2.0, 3.0)

            val actualCentroid = points.calculateCentroid()

            assertThat(actualCentroid).isEqualTo(expectedCentroid)
        }
    }

}
