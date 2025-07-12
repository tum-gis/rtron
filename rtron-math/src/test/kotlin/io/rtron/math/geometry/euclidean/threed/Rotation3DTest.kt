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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.DBL_EPSILON_3
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.QUARTER_PI
import io.rtron.math.std.TWO_PI

class Rotation3DTest :
    FunSpec({
        context("TestAngleAssignments") {

            test("heading assignment with half pi") {
                val actualHeading = Rotation3D(HALF_PI, 0.0, 0.0).heading

                actualHeading.shouldBe(HALF_PI plusOrMinus DBL_EPSILON)
            }

            test("heading assignment with two pi") {
                val actualHeading = Rotation3D(TWO_PI, 0.0, 0.0).heading

                actualHeading.shouldBe(0.0 plusOrMinus DBL_EPSILON_3)
            }

            test("heading assignment with two and a half pi") {
                val actualHeading = Rotation3D(HALF_PI + TWO_PI, 0.0, 0.0).heading

                actualHeading.shouldBe(HALF_PI plusOrMinus DBL_EPSILON_3)
            }

            test("pitch assignment with quarter pi") {
                val actualPitch = Rotation3D(0.0, QUARTER_PI, 0.0).pitch

                actualPitch.shouldBe(QUARTER_PI plusOrMinus DBL_EPSILON)
            }

            test("pitch assignment with two pi") {
                val actualPitch = Rotation3D(0.0, TWO_PI, 0.0).pitch

                actualPitch.shouldBe(0.0 plusOrMinus DBL_EPSILON_3)
            }

            test("pitch assignment with two and a half pi") {
                val actualPitch = Rotation3D(0.0, QUARTER_PI + TWO_PI, 0.0).pitch

                actualPitch.shouldBe(QUARTER_PI plusOrMinus DBL_EPSILON_3)
            }

            test("roll assignment with quarter pi") {
                val actualRoll = Rotation3D(0.0, 0.0, HALF_PI).roll

                actualRoll.shouldBe(HALF_PI plusOrMinus DBL_EPSILON)
            }

            test("roll assignment with two pi") {
                val actualRoll = Rotation3D(0.0, 0.0, TWO_PI).roll

                actualRoll.shouldBe(0.0 plusOrMinus DBL_EPSILON_3)
            }

            test("roll assignment with two and a half pi") {
                val actualRoll = Rotation3D(0.0, 0.0, HALF_PI + TWO_PI).roll

                actualRoll.shouldBe(HALF_PI plusOrMinus DBL_EPSILON_3)
            }
        }
    })
