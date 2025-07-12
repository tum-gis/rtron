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

package io.rtron.math.geometry.euclidean.threed

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.PI

class Vector3DTest :
    FunSpec({
        context("Addition") {

            test("adding points with positive coordinates") {
                val pointA = Vector3D(1.0, 1.0, 1.0)
                val pointB = Vector3D(1.0, 1.0, 1.0)

                val actualSum = pointA + pointB

                actualSum shouldBe Vector3D(2.0, 2.0, 2.0)
            }

            test("adding points with negative coordinates") {
                val pointA = Vector3D(-10.0, -5.0, -1.0)
                val pointB = Vector3D(10.0, 5.0, 1.0)

                val actualSum = pointA + pointB

                actualSum shouldBe Vector3D(0.0, 0.0, 0.0)
            }
        }

        context("Subtraction") {

            test("subtracting points with positive coordinates") {
                val pointA = Vector3D(4.0, 3.0, 2.0)
                val pointB = Vector3D(2.0, 1.0, 0.0)

                val actualDifference = pointA - pointB

                actualDifference shouldBe Vector3D(2.0, 2.0, 2.0)
            }
        }

        context("ScalarMultiplication") {

            test("scalar multiplication of basic point (1,1,1)") {
                val actualResult = Vector3D(1.0, 1.0, 1.0).scalarMultiply(4.0)

                actualResult shouldBe Vector3D(4.0, 4.0, 4.0)
            }
        }

        context("ScalarDivision") {

            test("scalar division of basic point") {
                val actualResult = Vector3D(2.0, 4.0, 1.0).scalarDivide(4.0)

                actualResult shouldBe Vector3D(0.5, 1.0, 0.25)
            }

            test("scalar division by zero throws error") {
                shouldThrow<IllegalArgumentException> {
                    Vector3D(2.0, 4.0, 1.0).scalarDivide(0.0)
                }
            }
        }

        context("Normalize") {

            test("normalized does not violate immutability principle") {
                val point = Vector3D(2.0, 4.0, 1.0)

                point.normalized()

                point shouldBe Vector3D(2.0, 4.0, 1.0)
            }
        }

        context("Angle") {

            test("half pi angle between axes") {
                val pointA = Vector3D(1.0, 0.0, 0.0)
                val pointB = Vector3D(0.0, 1.0, 0.0)

                val actualSum = pointA.angle(pointB)

                actualSum shouldBe HALF_PI
            }

            test("adding points with positive coordinates") {
                val pointA = Vector3D(1.0, 1.0, 1.0)
                val pointB = Vector3D(-1.0, -1.0, -1.0)

                val actualSum = pointA.angle(pointB)

                actualSum shouldBe PI
            }
        }

        context("Equality") {

            test("zero point equals zero point") {
                val pointA = Vector3D(0.0, 0.0, 0.0)
                val pointB = Vector3D(0.0, 0.0, 0.0)

                (pointA == pointB).shouldBeTrue()
            }

            test("positive point equals positive point") {
                val pointA = Vector3D(4.0, 0.0, 2.0)
                val pointB = Vector3D(4.0, 0.0, 2.0)

                (pointA == pointB).shouldBeTrue()
            }

            test("positive point unequals positive point") {
                val pointA = Vector3D(2.0, 0.0, 2.0)
                val pointB = Vector3D(4.0, 0.0, 2.0)

                (pointA == pointB).shouldBeFalse()
            }

            test("positive point unequals positive point with epsilon") {
                val pointA = Vector3D(2.0, 0.0, 2.0)
                val pointB = Vector3D(0.0, 0.0, 1.99999)

                (pointA == pointB).shouldBeFalse()
            }
        }
    })
