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

package io.rtron.math.transform

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.linear.RealMatrix
import io.rtron.math.linear.RealVector
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.HALF_PI
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import kotlin.math.cos
import kotlin.math.sin
import org.joml.Matrix4d as JOMLMatrix4d

class Affine3DTest : FunSpec({

    context("TestCreation") {

        test("last entry must be 1 when creating a translation") {
            val translation = Vector3D(1.0, 2.0, 3.0)
            val affine = Affine3D.of(translation)

            val actual = affine.toRealMatrix()[3][3]

            actual shouldBe 1.0
        }
    }

    context("TestDecomposition") {

        test("test translation from 3x3 matrix") {
            val affine = Affine3D(JOMLMatrix4d())

            val actual = affine.extractTranslation()

            actual shouldBe Vector3D(0.0, 0.0, 0.0)
        }

        test("test translation from 3x4 matrix") {
            val values = doubleArrayOf(
                1.0, 0.0, 0.0, 0.0,
                0.0, 1.0, 0.0, 3.0,
                0.0, 0.0, 1.0, 2.0,
                0.0, 0.0, 0.0, 1.0
            )
            val matrix = RealMatrix(values, 4)
            val affine = Affine3D.of(matrix)

            val actual = affine.extractTranslation()

            actual shouldBe Vector3D(0.0, 3.0, 2.0)
        }

        test("test translation") {
            val translation = Vector3D(1.0, 2.0, 3.0)
            val affine = Affine3D.of(translation)

            val actual = affine.extractTranslation()

            actual shouldBe translation
        }

        test("test scale") {
            val scaling = RealVector(doubleArrayOf(3.0, 2.0, 1.0))
            val affine = Affine3D.of(scaling)

            val actual = affine.extractScaling()

            actual shouldBe scaling
        }

        test("test rotation") {
            val scaling = RealVector(doubleArrayOf(3.0, 2.0, 1.0))
            val translation = Vector3D(3.0, 2.0, 1.0)
            val heading = HALF_PI
            val rotation = Rotation3D(heading)
            val affine = Affine3D.of(Affine3D.of(scaling), Affine3D.of(translation), Affine3D.of(rotation))

            val expectedRotationMatrix = RealMatrix(
                arrayOf(
                    doubleArrayOf(cos(heading), -sin(heading), 0.0, 0.0),
                    doubleArrayOf(sin(heading), cos(heading), 0.0, 0.0),
                    doubleArrayOf(0.0, 0.0, 1.0, 0.0),
                    doubleArrayOf(0.0, 0.0, 0.0, 1.0)
                )
            )

            val actual = affine.extractRotationAffine().toRealMatrix()

            actual.dimension shouldBe expectedRotationMatrix.dimension
            expectedRotationMatrix.toDoubleArray().zip(actual.toDoubleArray()).forEach {
                it.first.shouldBe(it.second plusOrMinus DBL_EPSILON)
            }
        }
    }

    context("TestAffineMultiplication") {

        test("test appending") {
            val translation = Vector3D(1.0, 2.0, 3.0)
            val affineA = Affine3D.of(translation)
            val scaling = RealVector.of(2.0, 3.0, 4.0)
            val affineB = Affine3D.of(scaling)
            val expectedValues = doubleArrayOf(
                2.0, 0.0, 0.0, 1.0,
                0.0, 3.0, 0.0, 2.0,
                0.0, 0.0, 4.0, 3.0,
                0.0, 0.0, 0.0, 1.0
            )
            val expectedMatrix = RealMatrix(expectedValues, 4)

            val actualAppended = affineA.append(affineB)
            val actualMatrix = actualAppended.toRealMatrix()

            actualMatrix.dimension shouldBe expectedMatrix.dimension
            expectedMatrix.toDoubleArray() shouldBe actualMatrix.toDoubleArray()
        }
    }

    context("TestTransforms") {

        test("test translation") {
            val translation = Vector3D(1.0, 2.0, 3.0)
            val affine = Affine3D.of(translation)

            val actualTranslated = affine.transform(Vector3D.ZERO)

            actualTranslated shouldBe translation
        }

        test("test inverse translation") {
            val translation = Vector3D(1.0, 2.0, 3.0)
            val affine = Affine3D.of(translation)

            val actualTranslated = affine.inverseTransform(Vector3D.ZERO)

            actualTranslated shouldBe -translation
        }
    }

    context("TestRotations") {

        test("test heading rotation") {
            val rotation = Rotation3D(HALF_PI, 0.0, 0.0)
            val affine = Affine3D.of(rotation)

            val actualRotated = affine.transform(Vector3D.X_AXIS)

            assertThat(actualRotated.x).isCloseTo(0.0, Offset.offset(DBL_EPSILON))
            assertThat(actualRotated.y).isCloseTo(1.0, Offset.offset(DBL_EPSILON))
            assertThat(actualRotated.z).isCloseTo(0.0, Offset.offset(DBL_EPSILON))
        }

        test("test pitch rotation") {
            val rotation = Rotation3D(0.0, HALF_PI, 0.0)
            val affine = Affine3D.of(rotation)

            val actualRotated = affine.transform(Vector3D.X_AXIS)

            assertThat(actualRotated.x).isCloseTo(0.0, Offset.offset(DBL_EPSILON))
            assertThat(actualRotated.y).isCloseTo(0.0, Offset.offset(DBL_EPSILON))
            assertThat(actualRotated.z).isCloseTo(-1.0, Offset.offset(DBL_EPSILON))
        }

        test("test rotation based on new standard basis") {
            val basisX = Vector3D(-1.0, 1.0, 0.0)
            val basisY = Vector3D(-1.0, 0.0, 1.0)
            val basisZ = Vector3D(1.0, 1.0, 1.0)
            val affine = Affine3D.of(basisX, basisY, basisZ)
            val expected = Vector3D(-1.0 / 3.0, -1.0 / 3.0, 1.0 / 3.0)

            val actualRotated = affine.transform(Vector3D.X_AXIS)

            assertThat(actualRotated.x).isCloseTo(expected.x, Offset.offset(DBL_EPSILON))
            assertThat(actualRotated.y).isCloseTo(expected.y, Offset.offset(DBL_EPSILON))
            assertThat(actualRotated.z).isCloseTo(expected.z, Offset.offset(DBL_EPSILON))
        }
    }

    context("TestConversions") {

        test("test to double array") {
            val translation = Vector3D(1.0, 2.0, 3.0)
            val affine = Affine3D.of(translation)
            val expectedDoubleArray = doubleArrayOf(
                1.0, 0.0, 0.0, 1.0,
                0.0, 1.0, 0.0, 2.0,
                0.0, 0.0, 1.0, 3.0,
                0.0, 0.0, 0.0, 1.0
            )

            val actualDoubleArray = affine.toDoubleArray()

            expectedDoubleArray shouldBe actualDoubleArray
        }
    }
})
