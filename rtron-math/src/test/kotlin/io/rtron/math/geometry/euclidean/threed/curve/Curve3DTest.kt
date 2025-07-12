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

package io.rtron.math.geometry.euclidean.threed.curve

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.twod.curve.LineSegment2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.QUARTER_PI
import kotlin.math.sqrt

class Curve3DTest :
    FunSpec({
        context("TestTorsion") {

            test("curve with zero torsion") {
                val curveXY = LineSegment2D.of(Vector2D.ZERO, Vector2D.X_AXIS, 0.0)
                val heightFunction = LinearFunction.X_AXIS
                val curve3D = Curve3D(curveXY, heightFunction)
                val affine = curve3D.calculateAffine(CurveRelativeVector1D(0.5))
                val pointLocal = Vector3D(0.0, 1.0, 0.0)

                val actualPointGlobal = affine.transform(pointLocal)

                actualPointGlobal shouldBe Vector3D(0.5, 1.0, 0.0)
            }

            test("curve with constant torsion of quarter pi") {
                val curveXY = LineSegment2D.of(Vector2D.ZERO, Vector2D.X_AXIS, 0.0)
                val heightFunction = LinearFunction.X_AXIS
                val torsionFunction = LinearFunction(0.0, QUARTER_PI)
                val curve3D = Curve3D(curveXY, heightFunction, torsionFunction)
                val affine = curve3D.calculateAffine(CurveRelativeVector1D(0.5))
                val pointLocal = Vector3D(0.0, sqrt(2.0), 0.0)

                val actualPointGlobal = affine.transform(pointLocal)

                actualPointGlobal.y.shouldBe(1.0 plusOrMinus DBL_EPSILON)
                actualPointGlobal.z.shouldBe(1.0 plusOrMinus DBL_EPSILON)
            }

            test("curve with constant torsion of half pi") {
                val curveXY = LineSegment2D.of(Vector2D.ZERO, Vector2D.X_AXIS, 0.0)
                val heightFunction = LinearFunction.X_AXIS
                val torsionFunction = LinearFunction(0.0, HALF_PI)
                val curve3D = Curve3D(curveXY, heightFunction, torsionFunction)
                val affine = curve3D.calculateAffine(CurveRelativeVector1D(0.5))
                val pointLocal = Vector3D(0.0, 1.0, 0.0)

                val actualPointGlobal = affine.transform(pointLocal)

                actualPointGlobal shouldBe Vector3D(0.5, 0.0, 1.0)
            }
        }
    })
