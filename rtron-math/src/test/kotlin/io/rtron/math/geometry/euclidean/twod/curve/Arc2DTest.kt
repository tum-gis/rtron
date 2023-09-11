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
import io.kotest.core.spec.style.FunSpec
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON_4
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.PI
import io.rtron.math.std.TWO_PI
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset

class Arc2DTest : FunSpec({
    context("TestCenterCalculation") {

        test("unit curvature with center above origin") {
            val arc = Arc2D(1.0, 2.0, 0.0)

            val actualCenter = arc.center

            assertThat(actualCenter).isEqualTo(Vector2D(0.0, 1.0))
        }

        test("unit curvature with center below origin") {
            val arc = Arc2D(-1.0, 2.0, 0.0)

            val actualCenter = arc.center

            assertThat(actualCenter).isEqualTo(Vector2D(0.0, -1.0))
        }
    }

    context("TestStartAngleCalculation") {

        test("starting angle with center above origin") {
            val arc = Arc2D(1.0, HALF_PI, 0.0)

            val actualStartAngle = arc.startAngle.toAngleRadians()

            assertThat(actualStartAngle).isEqualTo(PI + HALF_PI)
        }

        test("starting angle with center below origin") {
            val arc = Arc2D(-1.0, HALF_PI, 0.0)

            val actualStartAngle = arc.startAngle.toAngleRadians()

            assertThat(actualStartAngle).isEqualTo(HALF_PI)
        }
    }

    context("TestEndAngleCalculation") {

        test("ending angle at 0") {
            val arc = Arc2D(1.0, HALF_PI, 0.0)

            val actualEndAngle = arc.endAngle.toAngleRadians()

            assertThat(actualEndAngle).isEqualTo(0.0)
        }
    }

    context("TestLocalPoseCalculation") {

        test("calculate pose point on the start") {
            val arc = Arc2D(1.0, TWO_PI, 0.0, AffineSequence2D.EMPTY)

            val actualReturn = arc.calculatePoseGlobalCS(CurveRelativeVector1D.ZERO)

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.point).isEqualTo(Vector2D.ZERO)
        }

        test("calculate pose point on the curve") {
            val arc = Arc2D(1.0, TWO_PI, 0.0, AffineSequence2D.EMPTY)
            val curveRelativePoint = CurveRelativeVector1D(HALF_PI)

            val actualReturn = arc.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.point).isEqualTo(Vector2D(1.0, 1.0))
        }

        test("calculate pose angle on the start") {
            val arc = Arc2D(1.0, TWO_PI, 0.0, AffineSequence2D.EMPTY)

            val actualReturn = arc.calculatePoseGlobalCS(CurveRelativeVector1D.ZERO)

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.rotation.toAngleRadians()).isEqualTo(0.0)
        }

        test("calculate pose angle on the curve") {
            val arc = Arc2D(1.0, TWO_PI, 0.0, AffineSequence2D.EMPTY)
            val curveRelativePoint = CurveRelativeVector1D(HALF_PI)

            val actualReturn = arc.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.rotation.toAngleRadians()).isEqualTo(HALF_PI)
        }
    }

    context("TestGlobalPoseCalculation") {

        test("calculate pose point on the start") {
            val point = Vector2D(3.0, 5.0)
            val rotation = Rotation2D(HALF_PI)
            val affine = Affine2D.of(point, rotation)
            val affineSequence = AffineSequence2D.of(affine)
            val arc = Arc2D(1.0, TWO_PI, 0.0, affineSequence)

            val actualReturn = arc.calculatePoseGlobalCS(CurveRelativeVector1D.ZERO)

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.point).isEqualTo(Vector2D(3.0, 5.0))
        }

        test("calculate pose point on the curve") {
            val point = Vector2D(3.0, 5.0)
            val rotation = Rotation2D(HALF_PI)
            val affine = Affine2D.of(point, rotation)
            val affineSequence = AffineSequence2D.of(affine)
            val arc = Arc2D(1.0, TWO_PI, 0.0, affineSequence)
            val curveRelativePoint = CurveRelativeVector1D(HALF_PI)

            val actualReturn = arc.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.point).isEqualTo(Vector2D(2.0, 6.0))
        }

        test("calculate pose in fourth quadrant") {
            val point = Vector2D(60.0, -50.0)
            val rotation = Rotation2D(5.5)
            val affine = Affine2D.of(point, rotation)
            val affineSequence = AffineSequence2D.of(affine)
            val arc = Arc2D(0.0125, 170.0, 0.0, affineSequence)

            val actualReturn = arc.calculatePoseGlobalCS(CurveRelativeVector1D.ZERO)

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.point.x).isCloseTo(point.x, Offset.offset(DBL_EPSILON_4))
            assertThat(actualReturn.value.point.y).isCloseTo(point.y, Offset.offset(DBL_EPSILON_4))
        }

        test("calculate pose angle on the start") {
            val point = Vector2D(3.0, 5.0)
            val rotation = Rotation2D(HALF_PI)
            val affine = Affine2D.of(point, rotation)
            val affineSequence = AffineSequence2D.of(affine)
            val arc = Arc2D(1.0, TWO_PI, 0.0, affineSequence)

            val actualReturn = arc.calculatePoseGlobalCS(CurveRelativeVector1D.ZERO)

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.rotation.toAngleRadians()).isEqualTo(HALF_PI)
        }

        test("calculate pose angle on the curve") {
            val point = Vector2D(3.0, 5.0)
            val rotation = Rotation2D(HALF_PI)
            val affine = Affine2D.of(point, rotation)
            val affineSequence = AffineSequence2D.of(affine)
            val arc = Arc2D(1.0, TWO_PI, 0.0, affineSequence)
            val curveRelativePoint = CurveRelativeVector1D(HALF_PI)

            val actualReturn = arc.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.rotation.toAngleRadians()).isEqualTo(PI)
        }

        test("calculate pose angle in fourth quadrant") {
            val point = Vector2D(3.0, 5.0)
            val rotation = Rotation2D(HALF_PI)
            val affine = Affine2D.of(point, rotation)
            val affineSequence = AffineSequence2D.of(affine)
            val arc = Arc2D(1.0, TWO_PI, 0.0, affineSequence)
            val curveRelativePoint = CurveRelativeVector1D(PI)

            val actualReturn = arc.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualReturn).isInstanceOf(Either.Right::class.java)
            require(actualReturn is Either.Right)
            assertThat(actualReturn.value.rotation.toAngleRadians()).isEqualTo(PI + HALF_PI)
        }
    }
})
