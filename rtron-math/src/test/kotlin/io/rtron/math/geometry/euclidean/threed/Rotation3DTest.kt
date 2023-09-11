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

package io.rtron.math.geometry.euclidean.threed

import io.kotest.core.spec.style.FunSpec
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.DBL_EPSILON_3
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.QUARTER_PI
import io.rtron.math.std.TWO_PI
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset

class Rotation3DTest : FunSpec({
    context("TestAngleAssignments") {

        test("heading assignment with half pi") {
            val actualHeading = Rotation3D(HALF_PI, 0.0, 0.0).heading

            assertThat(actualHeading).isCloseTo(HALF_PI, Offset.offset(DBL_EPSILON))
        }

        test("heading assignment with two pi") {
            val actualHeading = Rotation3D(TWO_PI, 0.0, 0.0).heading

            assertThat(actualHeading).isCloseTo(0.0, Offset.offset(DBL_EPSILON_3))
        }

        test("heading assignment with two and a half pi") {
            val actualHeading = Rotation3D(HALF_PI + TWO_PI, 0.0, 0.0).heading

            assertThat(actualHeading).isCloseTo(HALF_PI, Offset.offset(DBL_EPSILON_3))
        }

        test("pitch assignment with quarter pi") {
            val actualPitch = Rotation3D(0.0, QUARTER_PI, 0.0).pitch

            assertThat(actualPitch).isCloseTo(QUARTER_PI, Offset.offset(DBL_EPSILON))
        }

        test("pitch assignment with two pi") {
            val actualPitch = Rotation3D(0.0, TWO_PI, 0.0).pitch

            assertThat(actualPitch).isCloseTo(0.0, Offset.offset(DBL_EPSILON_3))
        }

        test("pitch assignment with two and a half pi") {
            val actualPitch = Rotation3D(0.0, QUARTER_PI + TWO_PI, 0.0).pitch

            assertThat(actualPitch).isCloseTo(QUARTER_PI, Offset.offset(DBL_EPSILON_3))
        }

        test("roll assignment with quarter pi") {
            val actualRoll = Rotation3D(0.0, 0.0, HALF_PI).roll

            assertThat(actualRoll).isCloseTo(HALF_PI, Offset.offset(DBL_EPSILON))
        }

        test("roll assignment with two pi") {
            val actualRoll = Rotation3D(0.0, 0.0, TWO_PI).roll

            assertThat(actualRoll).isCloseTo(0.0, Offset.offset(DBL_EPSILON_3))
        }

        test("roll assignment with two and a half pi") {
            val actualRoll = Rotation3D(0.0, 0.0, HALF_PI + TWO_PI).roll

            assertThat(actualRoll).isCloseTo(HALF_PI, Offset.offset(DBL_EPSILON_3))
        }
    }
})
