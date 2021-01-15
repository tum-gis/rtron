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
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.HALF_PI
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


internal class CubicCurve2DTest {

    @Nested
    inner class TestPoseCalculation {

        @Test
        fun `pose calculation of straight line`() {
            val coefficients = doubleArrayOf(0.0, 1.0, 0.0, 0.0)
            val pose = Pose2D(Vector2D(0.0, 0.0), Rotation2D(0.0))
            val affine = Affine2D.of(pose)
            val affineSequence = AffineSequence2D.of(affine)
            val curve = CubicCurve2D(coefficients, 1.0, 0.0, affineSequence)
            val curveRelativePoint = CurveRelativeVector1D(1.0)

            val actualReturn = curve.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.point.x).isCloseTo(1.0, Offset.offset(DBL_EPSILON))
            assertThat(actualReturn.value.point.y).isCloseTo(1.0, Offset.offset(DBL_EPSILON))
            assertThat(actualReturn.value.rotation.angle).isCloseTo(1.0, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `pose calculation of straight line with start pose offset`() {
            val coefficients = doubleArrayOf(0.0, 1.0, 0.0, 0.0)
            val pose = Pose2D(Vector2D(0.0, 0.0), Rotation2D(HALF_PI))
            val affine = Affine2D.of(pose)
            val affineSequence = AffineSequence2D.of(affine)
            val curve = CubicCurve2D(coefficients, 1.0, 0.0, affineSequence)
            val curveRelativePoint = CurveRelativeVector1D(1.0)

            val actualReturn = curve.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Result.Success::class.java)
            require(actualReturn is Result.Success)
            assertThat(actualReturn.value.point.x).isCloseTo(-1.0, Offset.offset(DBL_EPSILON))
            assertThat(actualReturn.value.point.y).isCloseTo(1.0, Offset.offset(DBL_EPSILON))
        }
    }

}
