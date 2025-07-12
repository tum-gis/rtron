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
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.QUARTER_PI
import kotlin.math.sqrt

class LineSegment2DTest :
    FunSpec({
        context("TestLengthCalculation") {

            test("length of line segment on axis") {
                val pointA = Vector2D(0.0, 0.0)
                val pointB = Vector2D(7.0, 0.0)
                val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)

                lineSegment.length.shouldBe(7.0 plusOrMinus DBL_EPSILON)
            }

            test("length of diagonal line segment") {
                val pointA = Vector2D(3.0, 0.0)
                val pointB = Vector2D(0.0, 4.0)
                val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)

                lineSegment.length.shouldBe(5.0 plusOrMinus DBL_EPSILON)
            }
        }

        context("TestPoseAngleCalculation") {

            test("angle of line segment on axis") {
                val pointA = Vector2D(0.0, 0.0)
                val pointB = Vector2D(0.0, 1.0)
                val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)

                val actualPose = lineSegment.calculatePoseGlobalCS(CurveRelativeVector1D.ZERO).shouldBeRight()

                actualPose.rotation.angle shouldBe HALF_PI
            }

            test("angle of diagonal line segment") {
                val pointA = Vector2D(0.0, 0.0)
                val pointB = Vector2D(1.0, 1.0)
                val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)

                val actualPose = lineSegment.calculatePoseGlobalCS(CurveRelativeVector1D.ZERO).shouldBeRight()

                actualPose.rotation.angle shouldBe QUARTER_PI
            }
        }

        context("TestPosePointCalculation") {

            test("point on line segment on axis") {
                val pointA = Vector2D(0.0, 0.0)
                val pointB = Vector2D(10.0, 0.0)
                val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)
                val curveRelativePoint = CurveRelativeVector1D(5.0)

                val actualPose = lineSegment.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

                actualPose.point shouldBe Vector2D(5.0, 0.0)
            }

            test("point on diagonal line segment on axis") {
                val pointA = Vector2D(0.0, 0.0)
                val pointB = Vector2D(1.0, 1.0)
                val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)
                val curveRelativePoint = CurveRelativeVector1D(sqrt(2.0))

                val actualPose = lineSegment.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

                actualPose.point.x.shouldBe(1.0 plusOrMinus DBL_EPSILON)
                actualPose.point.y.shouldBe(1.0 plusOrMinus DBL_EPSILON)
            }

            test("point on diagonal line segment on axis 2") {
                val pointA = Vector2D(-1.0, 0.0)
                val pointB = Vector2D(0.0, 0.0)
                val lineSegment = LineSegment2D.of(pointA, pointB, 0.0)
                val curveRelativePoint = CurveRelativeVector1D(1.0)

                val actualPose = lineSegment.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

                actualPose.point shouldBe Vector2D(0.0, 0.0)
            }
        }
    })
