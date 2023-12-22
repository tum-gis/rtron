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

package io.rtron.math.geometry.euclidean.twod.curve

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.HALF_PI
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D

class CubicCurve2DTest : FunSpec({
    context("TestPoseCalculation") {

        test("pose calculation of straight line") {
            val coefficients = doubleArrayOf(0.0, 1.0, 0.0, 0.0)
            val pose = Pose2D(Vector2D(0.0, 0.0), Rotation2D(0.0))
            val affine = Affine2D.of(pose)
            val affineSequence = AffineSequence2D.of(affine)
            val curve = CubicCurve2D(coefficients, 1.0, 0.0, affineSequence)
            val curveRelativePoint = CurveRelativeVector1D(1.0)

            val actualPose = curve.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

            actualPose.point.x.shouldBe(1.0 plusOrMinus DBL_EPSILON)
            actualPose.point.y.shouldBe(1.0 plusOrMinus DBL_EPSILON)
            actualPose.rotation.angle.shouldBe(1.0 plusOrMinus DBL_EPSILON)
        }

        test("pose calculation of straight line with start pose offset") {
            val coefficients = doubleArrayOf(0.0, 1.0, 0.0, 0.0)
            val pose = Pose2D(Vector2D(0.0, 0.0), Rotation2D(HALF_PI))
            val affine = Affine2D.of(pose)
            val affineSequence = AffineSequence2D.of(affine)
            val curve = CubicCurve2D(coefficients, 1.0, 0.0, affineSequence)
            val curveRelativePoint = CurveRelativeVector1D(1.0)

            val actualPose = curve.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

            actualPose.point.x.shouldBe(-1.0 plusOrMinus DBL_EPSILON)
            actualPose.point.y.shouldBe(1.0 plusOrMinus DBL_EPSILON)
        }

        /**
         * Cubic curve geometry from the poly3 example dataset of [ASAM](https://www.asam.net/standards/detail/opendrive/).
         * The sample dataset contains gaps between the geometry elements of the reference line.
         */

        test("cubic curve geometry from OpenDRIVE poly3 example dataset with gap to the next curve element") {
            val coefficients = doubleArrayOf(0.0, 0.0, 1.1010160043712483e-02, -4.0376563467901658e-04)
            val pose = Pose2D(Vector2D(-2.6331198952545350e+01, -7.4309373646250769e+00), Rotation2D(6.7579136200528211e-01))
            val affine = Affine2D.of(pose)
            val affineSequence = AffineSequence2D.of(affine)
            val length = 2.7431067043838230e+01
            val curve = CubicCurve2D(coefficients, length, 1E-4, affineSequence)
            val curveRelativePoint = CurveRelativeVector1D(length)

            val actualPose = curve.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

            actualPose.point.x.shouldNotBe(-5.0558344684384622e+00 plusOrMinus DBL_EPSILON) // not coincident with next curve element
            actualPose.point.y.shouldNotBe(9.6260358855640789e+00 plusOrMinus DBL_EPSILON) // not coincident with next curve element
            actualPose.rotation.angle.shouldNotBe(3.8412114603351055e-01 plusOrMinus DBL_EPSILON) // not coincident with next curve element
        }
    }
})
