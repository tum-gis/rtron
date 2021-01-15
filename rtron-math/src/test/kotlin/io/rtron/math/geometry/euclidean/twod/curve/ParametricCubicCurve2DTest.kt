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

package io.rtron.math.geometry.euclidean.twod.curve

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.DBL_EPSILON_2
import io.rtron.math.std.PI
import io.rtron.math.std.QUARTER_PI
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.sqrt


internal class ParametricCubicCurve2DTest {

    @Nested
    inner class TestPoseCalculation {
        @Test
        fun `simple quadratic curve`() {
            val coefficientX = doubleArrayOf(0.0, 1.0, 0.0, 0.0)
            val coefficientY = doubleArrayOf(0.0, 0.0, 1.0, 0.0)
            val curve = ParametricCubicCurve2D(coefficientX, coefficientY, 10.0, 0.0)
            val curveRelativePoint = CurveRelativeVector1D(2.0)

            val actualReturn = curve.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.point).isEqualTo(Vector2D(2.0, 4.0))
        }

        @Test
        fun `simple linear curve`() {
            val coefficientX = doubleArrayOf(0.0, 1.0, 0.0, 0.0)
            val coefficientY = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
            val affine = Affine2D.of(Vector2D.ZERO, Rotation2D(QUARTER_PI))
            val affineSequence = AffineSequence2D.of(affine)
            val curve = ParametricCubicCurve2D(coefficientX, coefficientY, 10.0, 0.0, affineSequence)
            val curveRelativePoint = CurveRelativeVector1D(sqrt(2.0))

            val actualReturn = curve.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.point.x).isCloseTo(1.0, Offset.offset(DBL_EPSILON))
            assertThat(actualReturn.value.point.y).isCloseTo(1.0, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `simple quadratic negative curve`() {
            val coefficientX = doubleArrayOf(0.0, 1.0, 0.0, 0.0)
            val coefficientY = doubleArrayOf(0.0, 0.0, 1.0, 0.0)
            val affine = Affine2D.of(Vector2D.ZERO, Rotation2D(PI))
            val affineSequence = AffineSequence2D.of(affine)
            val curve = ParametricCubicCurve2D(coefficientX, coefficientY, 10.0, 0.0, affineSequence)
            val curveRelativePoint = CurveRelativeVector1D(2.0)

            val actualReturn = curve.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.point.x).isCloseTo(-2.0, Offset.offset(DBL_EPSILON_2))
            assertThat(actualReturn.value.point.y).isCloseTo(-4.0, Offset.offset(DBL_EPSILON_2))
        }

        @Test
        fun `quadratic curve`() {
            val coefficientX = doubleArrayOf(0.0, 1.0, 0.0, 0.0)
            val coefficientY = doubleArrayOf(0.0, 0.0, 1.0, 0.0)
            val length = 1.479
            val curveRelativePoint = CurveRelativeVector1D(length)
            val curve = ParametricCubicCurve2D(coefficientX, coefficientY, length, 0.0)

            val actualReturn = curve.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.point.x).isCloseTo(length, Offset.offset(DBL_EPSILON))
            assertThat(actualReturn.value.point.y).isCloseTo(length * length, Offset.offset(DBL_EPSILON))
        }
    }

}
