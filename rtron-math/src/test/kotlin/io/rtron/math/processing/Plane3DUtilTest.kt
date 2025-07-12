/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Plane3D
import io.rtron.math.std.DBL_EPSILON_1
import io.rtron.math.std.DBL_EPSILON_2

class Plane3DUtilTest :
    FunSpec({
        context("TestBestFittingPlaneCalculation") {

            test("normal for clockwise point in XY-plane") {
                val pointA = -Vector3D.X_AXIS
                val pointB = Vector3D.Y_AXIS
                val pointC = Vector3D.X_AXIS
                val expectedNormal = -Vector3D.Z_AXIS

                val actualBestFittingPlane = listOf(pointA, pointB, pointC).calculateBestFittingPlane(DBL_EPSILON_1)

                actualBestFittingPlane.normal shouldBe expectedNormal
            }

            test("normal for counter clockwise point in XY-plane") {
                val pointA = Vector3D.X_AXIS
                val pointB = Vector3D.Y_AXIS
                val pointC = -Vector3D.X_AXIS
                val expectedNormal = -Vector3D.Z_AXIS

                val actualBestFittingPlane = listOf(pointA, pointB, pointC).calculateBestFittingPlane(DBL_EPSILON_1)

                actualBestFittingPlane.normal shouldBe expectedNormal
            }

            test("plane fitting with three points") {
                val pointA = Vector3D(1.0, 0.0, 0.0)
                val pointB = Vector3D(1.0, 3.0, 0.0)
                val pointC = Vector3D(1.0, 0.0, 3.0)
                val expectedCentroid = Vector3D(1.0, 1.0, 1.0)
                val expectedNormal = -Vector3D.X_AXIS
                val expectedPlane = Plane3D(expectedCentroid, expectedNormal, DBL_EPSILON_1)

                val actualBestFittingPlane = listOf(pointA, pointB, pointC).calculateBestFittingPlane(DBL_EPSILON_1)

                actualBestFittingPlane shouldBe expectedPlane
            }

            test("plane fitting with multiple points") {
                val pointA = Vector3D(4.0, 1.0, -3.0)
                val pointB = Vector3D(2.0, 2.0, -2.0)
                val pointC = Vector3D(1.0, 1.0, 3.0)
                val pointD = Vector3D(1.0, -1.0, 9.0)
                val expectedCentroid = Vector3D(1.0, 1.0, 3.0)
                val expectedNormal = Vector3D(2.0, 3.0, 1.0).normalized()
                val expectedPlane = Plane3D(expectedCentroid, expectedNormal, 0.0)

                val actualBestFittingPlane =
                    listOf(pointA, pointB, pointC, pointD)
                        .calculateBestFittingPlane(DBL_EPSILON_2)

                actualBestFittingPlane.normal.toDoubleArray().zip(expectedPlane.normal.toDoubleArray()).forEach {
                    it.first.shouldBe(it.second plusOrMinus DBL_EPSILON_2)
                }
                actualBestFittingPlane.isSimilarTo(expectedPlane).shouldBeTrue()
            }

            test("plane fitting with five points not in the same plane") {
                val pointA = Vector3D(1.0, 0.0, 0.0)
                val pointB = Vector3D(1.0, 3.0, 0.0)
                val pointC = Vector3D(1.0, 0.0, 3.0)
                val pointD = Vector3D(0.0, 1.0, 1.0)
                val pointE = Vector3D(2.0, 1.0, 1.0)
                val expectedCentroid = Vector3D(1.0, 1.0, 1.0)
                val expectedNormal = -Vector3D.X_AXIS
                val expectedPlane = Plane3D(expectedCentroid, expectedNormal, DBL_EPSILON_1)
                val actualBestFittingPlane =
                    listOf(pointA, pointB, pointC, pointD, pointE)
                        .calculateBestFittingPlane(DBL_EPSILON_1)

                actualBestFittingPlane shouldBe expectedPlane
            }
        }
    })
