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

package io.rtron.math.transform

import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.linear.RealMatrix
import io.rtron.math.linear.RealVector
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.QUARTER_PI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class Affine2DTest {

    @Nested
    inner class TestTransform {
        @Test
        fun `test rotation`() {
            val point = Vector2D.X_AXIS
            val rotationAngle = Rotation2D(HALF_PI)
            val affine = Affine2D.of(rotationAngle)

            val actualTransformed = affine.transform(point)

            assertThat(actualTransformed).isEqualTo(Vector2D(0.0, 1.0))
        }
    }

    @Nested
    inner class TestInverseTransform {

        @Test
        fun `inverse translation transform`() {
            val point = Vector2D(10.0, 12.0)
            val translation = Vector2D(5.0, 2.0)
            val affine = Affine2D.of(translation)

            val actualTransformed = affine.inverseTransform(point)

            assertThat(actualTransformed).isEqualTo(Vector2D(5.0, 10.0))
        }
    }

    @Nested
    inner class TestAffineMultiplication {

        @Test
        fun `test appending`() {
            val translation = Vector2D(1.0, 2.0)
            val affineA = Affine2D.of(translation)
            val scaling = RealVector.of(2.0, 3.0)
            val affineB = Affine2D.of(scaling)
            val expectedValues = doubleArrayOf(
                2.0, 0.0, 0.0, 1.0,
                0.0, 3.0, 0.0, 2.0,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0
            )
            val expectedMatrix = RealMatrix(expectedValues, 4)

            val actualAppended = affineA.append(affineB)
            val actualMatrix = actualAppended.toMatrix()

            assertThat(actualMatrix.dimension).isEqualTo(expectedMatrix.dimension)
            assertArrayEquals(expectedMatrix.toDoubleArray(), actualMatrix.toDoubleArray())
        }
    }

    @Nested
    inner class TestPoseTransforms {

        @Test
        fun `test translation`() {
            val point = Vector2D(5.0, 3.0)
            val pose = Pose2D(Vector2D(-10.0, -5.0), Rotation2D(0.0))
            val affine = Affine2D.of(Affine2D.of(pose.point), Affine2D.of(pose.rotation))

            val actualTransformed = affine.transform(point)

            assertThat(actualTransformed).isEqualTo(Vector2D(-5.0, -2.0))
        }

        @Test
        fun `inverse transform with pose in origin`() {
            val point = Vector2D(5.0, 3.0)
            val pose = Pose2D(Vector2D(0.0, 0.0), Rotation2D(0.0))
            val affine = Affine2D.of(Affine2D.of(pose.point), Affine2D.of(pose.rotation))

            val actualTransformed = affine.inverseTransform(point)

            assertThat(actualTransformed).isEqualTo(Vector2D(5.0, 3.0))
        }

        @Test
        fun `transform with rotated pose in origin`() {
            val point = Vector2D(5.0, 0.0)
            val pose = Pose2D(Vector2D(0.0, 0.0), Rotation2D(HALF_PI))
            val affine = Affine2D.of(Affine2D.of(pose.point), Affine2D.of(pose.rotation))

            val actualTransformed = affine.transform(point)

            assertThat(actualTransformed).isEqualTo(Vector2D(0.0, 5.0))
        }

        @Test
        fun `transform with rotated pose and offset point`() {
            val point = Vector2D(2.0, 3.0)
            val pose = Pose2D(Vector2D(0.0, 0.0), Rotation2D(HALF_PI))
            val affine = Affine2D.of(Affine2D.of(pose.point), Affine2D.of(pose.rotation))

            val actualTransformed = affine.transform(point)

            assertThat(actualTransformed).isEqualTo(Vector2D(-3.0, 2.0))
        }
    }

    @Nested
    inner class TestDecomposition {

        @Test
        fun `extract translation point from basic affine`() {
            val translation = Vector2D(1.0, 2.0)
            val affine = Affine2D.of(translation)

            val actual = affine.extractTranslation()

            assertThat(actual).isEqualTo(translation)
        }

        @Test
        fun `extract rotation from basic affine`() {
            val rotation = Rotation2D(QUARTER_PI)
            val affine = Affine2D.of(rotation)

            val actual = affine.extractRotation()

            assertThat(actual).isEqualTo(rotation)
        }

        @Test
        fun `extract scale vector from basic affine`() {
            val scaling = RealVector(doubleArrayOf(3.0, 2.0))
            val affine = Affine2D.of(scaling)

            val actual = affine.extractScaling()

            assertThat(actual).isEqualTo(scaling)
        }

        @Test
        fun `extract rotation from affine with scaling, translation and rotation`() {
            val scaling = RealVector(doubleArrayOf(3.0, 2.0))
            val translation = Vector2D(3.0, 2.0)
            val rotation = Rotation2D(HALF_PI)
            val affine = Affine2D.of(Affine2D.of(scaling), Affine2D.of(translation), Affine2D.of(rotation))

            val actual = affine.extractRotation()

            assertThat(actual.toAngleRadians()).isEqualTo(rotation.toAngleRadians())
        }
    }
}
