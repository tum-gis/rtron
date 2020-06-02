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

package io.rtron.math.geometry.euclidean.threed

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import io.rtron.math.std.*


internal class Rotation3DTest {

    @Nested
    inner class TestAngleAssignments {

        @Test
        fun `heading assignment with half pi`() {
            val actualHeading = Rotation3D(HALF_PI, 0.0, 0.0).heading

            assertThat(actualHeading).isCloseTo(HALF_PI, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `heading assignment with two pi`() {
            val actualHeading = Rotation3D(TWO_PI, 0.0, 0.0).heading

            assertThat(actualHeading).isCloseTo(0.0, Offset.offset(DBL_EPSILON_3))
        }

        @Test
        fun `heading assignment with two and a half pi`() {
            val actualHeading = Rotation3D(HALF_PI + TWO_PI, 0.0, 0.0).heading

            assertThat(actualHeading).isCloseTo(HALF_PI, Offset.offset(DBL_EPSILON_3))
        }

        @Test
        fun `pitch assignment with quarter pi`() {
            val actualPitch = Rotation3D(0.0, QUARTER_PI, 0.0).pitch

            assertThat(actualPitch).isCloseTo(QUARTER_PI, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `pitch assignment with two pi`() {
            val actualPitch = Rotation3D(0.0, TWO_PI, 0.0).pitch

            assertThat(actualPitch).isCloseTo(0.0, Offset.offset(DBL_EPSILON_3))
        }

        @Test
        fun `pitch assignment with two and a half pi`() {
            val actualPitch = Rotation3D(0.0, QUARTER_PI + TWO_PI, 0.0).pitch

            assertThat(actualPitch).isCloseTo(QUARTER_PI, Offset.offset(DBL_EPSILON_3))
        }

        @Test
        fun `roll assignment with quarter pi`() {
            val actualRoll = Rotation3D(0.0, 0.0, HALF_PI).roll

            assertThat(actualRoll).isCloseTo(HALF_PI, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `roll assignment with two pi`() {
            val actualRoll = Rotation3D(0.0, 0.0, TWO_PI).roll

            assertThat(actualRoll).isCloseTo(0.0, Offset.offset(DBL_EPSILON_3))
        }

        @Test
        fun `roll assignment with two and a half pi`() {
            val actualRoll = Rotation3D(0.0, 0.0, HALF_PI + TWO_PI).roll

            assertThat(actualRoll).isCloseTo(HALF_PI, Offset.offset(DBL_EPSILON_3))
        }
    }

}
