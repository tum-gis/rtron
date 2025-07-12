/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.linear.RealMatrix
import io.rtron.math.linear.RealVector
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.QUARTER_PI

class Affine2DTest :
    FunSpec({

        context("TestTransform") {
            test("test rotation") {
                val point = Vector2D.X_AXIS
                val rotationAngle = Rotation2D(HALF_PI)
                val affine = Affine2D.of(rotationAngle)

                val actualTransformed = affine.transform(point)

                actualTransformed shouldBe Vector2D(0.0, 1.0)
            }
        }

        context("TestInverseTransform") {
            test("inverse translation transform") {
                val point = Vector2D(10.0, 12.0)
                val translation = Vector2D(5.0, 2.0)
                val affine = Affine2D.of(translation)

                val actualTransformed = affine.inverseTransform(point)

                actualTransformed shouldBe Vector2D(5.0, 10.0)
            }
        }

        context("TestAffineMultiplication") {
            test("test appending") {
                val translation = Vector2D(1.0, 2.0)
                val affineA = Affine2D.of(translation)
                val scaling = RealVector.of(2.0, 3.0)
                val affineB = Affine2D.of(scaling)
                val expectedValues =
                    doubleArrayOf(
                        2.0,
                        0.0,
                        1.0,
                        0.0,
                        3.0,
                        2.0,
                        0.0,
                        0.0,
                        1.0,
                    )
                val expectedMatrix = RealMatrix(expectedValues, 3)

                val actualAppended = affineA.append(affineB)
                val actualMatrix = actualAppended.toMatrix()

                actualMatrix.dimension shouldBe expectedMatrix.dimension
                expectedMatrix.toDoubleArray() shouldBe actualMatrix.toDoubleArray()
            }
        }

        context("TestPoseTransforms") {
            test("test translation") {
                val point = Vector2D(5.0, 3.0)
                val pose = Pose2D(Vector2D(-10.0, -5.0), Rotation2D(0.0))
                val affineTranslation = Affine2D.of(pose.point)
                val affineRotation = Affine2D.of(pose.rotation)
                val affine = Affine2D.of(affineTranslation, affineRotation)

                val actualTransformed = affine.transform(point)

                actualTransformed shouldBe Vector2D(-5.0, -2.0)
            }

            test("inverse transform with pose in origin") {
                val point = Vector2D(5.0, 3.0)
                val pose = Pose2D(Vector2D(0.0, 0.0), Rotation2D(0.0))
                val affine = Affine2D.of(Affine2D.of(pose.point), Affine2D.of(pose.rotation))

                val actualTransformed = affine.inverseTransform(point)

                actualTransformed shouldBe Vector2D(5.0, 3.0)
            }

            test("transform with rotated pose in origin") {
                val point = Vector2D(5.0, 0.0)
                val pose = Pose2D(Vector2D(0.0, 0.0), Rotation2D(HALF_PI))
                val affine = Affine2D.of(Affine2D.of(pose.point), Affine2D.of(pose.rotation))

                val actualTransformed = affine.transform(point)

                actualTransformed shouldBe Vector2D(0.0, 5.0)
            }

            test("transform with rotated pose and offset point") {
                val point = Vector2D(2.0, 3.0)
                val pose = Pose2D(Vector2D(0.0, 0.0), Rotation2D(HALF_PI))
                val affine = Affine2D.of(Affine2D.of(pose.point), Affine2D.of(pose.rotation))

                val actualTransformed = affine.transform(point)

                actualTransformed shouldBe Vector2D(-3.0, 2.0)
            }
        }

        context("TestDecomposition") {
            test("extract translation point from basic affine") {
                val translation = Vector2D(1.0, 2.0)
                val affine = Affine2D.of(translation)

                val actual = affine.extractTranslation()

                actual shouldBe translation
            }

            test("extract rotation from basic affine") {
                val rotation = Rotation2D(QUARTER_PI)
                val affine = Affine2D.of(rotation)

                val actual = affine.extractRotation()

                actual shouldBe rotation
            }

            test("extract scale vector from basic affine") {
                val scaling = RealVector(doubleArrayOf(3.0, 2.0))
                val affine = Affine2D.of(scaling)

                val actual = affine.extractScaling()

                actual shouldBe scaling
            }

            test("extract rotation from affine with scaling, translation and rotation") {
                val scaling = RealVector(doubleArrayOf(3.0, 2.0))
                val translation = Vector2D(3.0, 2.0)
                val rotation = Rotation2D(HALF_PI)
                val affine = Affine2D.of(Affine2D.of(scaling), Affine2D.of(translation), Affine2D.of(rotation))

                val actual = affine.extractRotation()

                actual.toAngleRadians() shouldBe rotation.toAngleRadians()
            }
        }
    })
