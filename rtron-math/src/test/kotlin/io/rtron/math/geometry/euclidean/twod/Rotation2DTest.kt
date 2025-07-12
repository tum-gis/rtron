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

package io.rtron.math.geometry.euclidean.twod

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.rtron.math.std.DBL_EPSILON_1
import io.rtron.math.std.DBL_EPSILON_2
import io.rtron.math.std.DBL_EPSILON_4
import io.rtron.math.std.TWO_PI

class Rotation2DTest :
    FunSpec({
        context("TestFuzzyEquals") {

            test("two rotations with same angle are fuzzily equal") {
                val rotationA = Rotation2D(1.3)
                val rotationB = Rotation2D(1.3)

                val actualEquality = rotationA.fuzzyEquals(rotationB, DBL_EPSILON_1)

                actualEquality.shouldBeTrue()
            }

            test("two rotations with different angle are not fuzzily equal") {
                val rotationA = Rotation2D(1.3)
                val rotationB = Rotation2D(1.4)

                val actualEquality = rotationA.fuzzyEquals(rotationB, DBL_EPSILON_1)

                actualEquality.shouldBeFalse()
            }

            test("two rotations at zero radians and two pi minus epsilon radians are fuzzily equal") {
                val rotationA = Rotation2D.ZERO
                val rotationB = Rotation2D(TWO_PI - DBL_EPSILON_2)

                val actualEquality = rotationA.fuzzyEquals(rotationB, DBL_EPSILON_2)

                actualEquality.shouldBeTrue()
            }

            test("two rotations at zero radians and two pi plus epsilon radians are fuzzily equal") {
                val rotationA = Rotation2D.ZERO
                val rotationB = Rotation2D(TWO_PI + DBL_EPSILON_2)

                val actualEquality = rotationA.fuzzyEquals(rotationB, DBL_EPSILON_2)

                actualEquality.shouldBeTrue()
            }

            test("two rotations at two pi minus epsilon radians and zero radians are fuzzily equal") {
                val rotationA = Rotation2D(TWO_PI - DBL_EPSILON_2)
                val rotationB = Rotation2D.ZERO

                val actualEquality = rotationA.fuzzyEquals(rotationB, DBL_EPSILON_2)

                actualEquality.shouldBeTrue()
            }

            test("two rotations at two pi plus epsilon radians and zero radians are fuzzily equal") {
                val rotationA = Rotation2D(TWO_PI + DBL_EPSILON_2)
                val rotationB = Rotation2D.ZERO

                val actualEquality = rotationA.fuzzyEquals(rotationB, DBL_EPSILON_2)

                actualEquality.shouldBeTrue()
            }

            test("two rotations at two pi plus larger epsilon radians and zero radians are not fuzzily equal") {
                val rotationA = Rotation2D(TWO_PI + DBL_EPSILON_4)
                val rotationB = Rotation2D.ZERO

                val actualEquality = rotationA.fuzzyEquals(rotationB, DBL_EPSILON_2)

                actualEquality.shouldBeFalse()
            }
        }
    })
