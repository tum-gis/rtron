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

package io.rtron.math.geometry.euclidean.twod.curve

import arrow.core.Either
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON_11
import io.rtron.math.std.DBL_EPSILON_2
import io.rtron.math.std.DBL_EPSILON_4
import io.rtron.math.std.DBL_EPSILON_6
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import org.assertj.core.api.Assertions
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SpiralSegment2DTest {

    @Nested
    inner class TestPointCalculation {

        @Test
        fun `first spiral of the ASAM example dataset Ex_Line-Spiral-Arc`() {
            val pose = Pose2D(Vector2D(3.8003686923043311e+01, -1.8133261823256248e+00), Rotation2D(3.3186980419884304e-01))
            val affine = Affine2D.of(pose)
            val curvatureFunction = LinearFunction.ofSpiralCurvature(0.0, 1.3333327910466574e-02, 2.9999999999999996e+01)
            val curve = SpiralSegment2D(curvatureFunction, DBL_EPSILON_2, AffineSequence2D.of(affine))
            val curveRelativePoint = CurveRelativeVector1D(1.3000000000000000e+02 - 1.0000000000000000e+02)

            val actualReturn = curve.calculatePoseGlobalCS(curveRelativePoint)

            Assertions.assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            Assertions.assertThat(actualReturn.value.point.x).isCloseTo(
                6.5603727689096445e+01,
                Offset.offset(
                    DBL_EPSILON_11
                )
            )
            Assertions.assertThat(actualReturn.value.point.y).isCloseTo(9.8074617455403796e+00, Offset.offset(DBL_EPSILON_11))
            Assertions.assertThat(actualReturn.value.rotation.angle).isCloseTo(
                5.3186972285460032e-01,
                Offset.offset(
                    DBL_EPSILON_4
                )
            )
        }

        @Test
        fun `second spiral of the ASAM example dataset Ex_Line-Spiral-Arc`() {
            val pose = Pose2D(Vector2D(8.7773023553010319e+01, 2.9721920045249909e+01), Rotation2D(9.3186944634590163e-01))
            val affine = Affine2D.of(pose)
            val curvatureFunction = LinearFunction.ofSpiralCurvature(1.3333327910466574e-02, 6.6666666666666671e-03, 2.0000000000000000e+01)
            val curve = SpiralSegment2D(curvatureFunction, DBL_EPSILON_2, AffineSequence2D.of(affine))
            val curveRelativePoint = CurveRelativeVector1D(1.7999999146329435e+02 - 1.5999999146329435e+02)

            val actualReturn = curve.calculatePoseGlobalCS(curveRelativePoint)

            Assertions.assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            Assertions.assertThat(actualReturn.value.point.x).isCloseTo(
                9.7828942354905308e+01,
                Offset.offset(
                    DBL_EPSILON_11
                )
            )
            Assertions.assertThat(actualReturn.value.point.y).isCloseTo(4.6971187858525226e+01, Offset.offset(DBL_EPSILON_11))
            Assertions.assertThat(actualReturn.value.rotation.angle).isCloseTo(
                1.1318693921172343e+00,
                Offset.offset(
                    DBL_EPSILON_4
                )
            )
        }

        @Test
        fun `first spiral of the example dataset CrossingComplex8Course`() {
            val pose = Pose2D(Vector2D(4.5002666984590900e+02, 5.2071081556728734e+02), Rotation2D(4.7173401120976974e+00))
            val affine = Affine2D.of(pose)
            val curvatureFunction = LinearFunction.ofSpiralCurvature(-0.0000000000000000e+00, -1.0126582278481013e-01, 4.9620253164556960e+00)
            val curve = SpiralSegment2D(curvatureFunction, DBL_EPSILON_2, AffineSequence2D.of(affine))
            val curveRelativePoint = CurveRelativeVector1D(1.0507568316454000e+01 - 5.5455429999983039e+00)

            val actualReturn = curve.calculatePoseGlobalCS(curveRelativePoint)

            Assertions.assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            Assertions.assertThat(actualReturn.value.point.x).isCloseTo(
                4.4963740167278593e+02,
                Offset.offset(
                    DBL_EPSILON_6
                )
            )
            Assertions.assertThat(actualReturn.value.point.y).isCloseTo(5.1577803259440122e+02, Offset.offset(DBL_EPSILON_6))
            Assertions.assertThat(actualReturn.value.rotation.angle).isCloseTo(
                4.4660983239227257e+00,
                Offset.offset(
                    DBL_EPSILON_2
                )
            )
        }

        @Test
        fun `second spiral of the example dataset CrossingComplex8Course`() {
            val pose = Pose2D(Vector2D(4.4256271579386976e+02, 5.0863294215453800e+02), Rotation2D(3.3977855734777722e+00))
            val affine = Affine2D.of(pose)
            val curvatureFunction = LinearFunction.ofSpiralCurvature(-1.0126582278481013e-01, -0.0000000000000000e+00, 4.9620253164556960e+00)
            val curve = SpiralSegment2D(curvatureFunction, DBL_EPSILON_2, AffineSequence2D.of(affine))
            val curveRelativePoint = CurveRelativeVector1D(2.6019182043553606e+01 - 2.1057156727097912e+01)

            val actualReturn = curve.calculatePoseGlobalCS(curveRelativePoint)

            Assertions.assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            Assertions.assertThat(actualReturn.value.point.x).isCloseTo(
                4.3763402923360974e+02,
                Offset.offset(
                    DBL_EPSILON_6
                )
            )
            Assertions.assertThat(actualReturn.value.point.y).isCloseTo(5.0819484814787387e+02, Offset.offset(DBL_EPSILON_6))
            Assertions.assertThat(actualReturn.value.rotation.angle).isCloseTo(
                3.1465437853028004e+00,
                Offset.offset(
                    DBL_EPSILON_2
                )
            )
        }
    }
}
