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
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.Range
import io.rtron.math.std.DBL_EPSILON_7
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import io.rtron.std.cumulativeSum
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


internal class CompositeCurve2DTest {

    @Nested
    inner class TestPoseCalculation {

        @Test
        fun `calculating last point on curve is not NaN`() {
            val tolerance = DBL_EPSILON_7
            // example from Crossing8Course road with id=515

            val point1 = Vector2D(-7.0710678119936246e+00, -7.0710678117566195e+00)
            val rotation1 = Rotation2D(7.8539816339160284e-01)
            val affine1 = Affine2D.of(point1, rotation1)
            val affineSequence1 = AffineSequence2D.of(affine1)
            val curveMember1 = LineSegment2D(4.8660000002386461e-01, tolerance, affineSequence1)

            val point2 = Vector2D(-6.7269896522493644e+00, -6.7269896520163819e+00)
            val rotation2 = Rotation2D(7.8539816339001800e-01)
            val affine2 = Affine2D.of(point2, rotation2)
            val affineSequence2 = AffineSequence2D.of(affine2)
            val curveMember2 = SpiralSegment2D(
                    LinearFunction.ofSpiralCurvature(-0.0000000000000000e+00,
                            -1.2698412698412698e-01,
                            3.1746031746031744e+00),
                    tolerance, affineSequence2)

            val point3 = Vector2D(-4.3409250448547327e+00, -4.6416930098216129e+00)
            val rotation3 = Rotation2D(5.8383605706600694e-01)
            val affine3 = Affine2D.of(point3, rotation3)
            val affineSequence3 = AffineSequence2D.of(affine3)
            val curveMember3 = Arc2D(-1.2698412698412698e-01, 9.1954178989066371e+00, tolerance,
                    affineSequence3)

            val point4 = Vector2D(4.3409256447834164e+00, -4.6416930099218154e+00)
            val rotation4 = Rotation2D(-5.8383605708086783e-01)
            val affine4 = Affine2D.of(point4, rotation4)
            val affineSequence4 = AffineSequence2D.of(affine4)
            val curveMember4 = SpiralSegment2D(
                    LinearFunction.ofSpiralCurvature(-1.2698412698412698e-01,
                            -0.0000000000000000e+00,
                            3.1746031746031744e+00),
                    tolerance, affineSequence4)

            val point5 = Vector2D(6.7269902521255664e+00, -6.7269896521471884e+00)
            val rotation5 = Rotation2D(-7.8539816341104807e-01)
            val affine5 = Affine2D.of(point5, rotation5)
            val affineSequence5 = AffineSequence2D.of(affine5)
            val curveMember5 = LineSegment2D(4.8660000002379050e-01, tolerance, affineSequence5)

            val curveMembers = listOf(curveMember1, curveMember2, curveMember3, curveMember4, curveMember5)
            val absoluteStarts = curveMembers.map { it.length }.cumulativeSum()
            val absoluteDomains = absoluteStarts.zipWithNext().map { Range.closedOpen(it.first, it.second) }
            val compositeCurve = CompositeCurve2D(curveMembers, absoluteDomains, absoluteStarts.dropLast(1))
            val curveRelativePoint = CurveRelativeVector1D(compositeCurve.length)


            val actualPoint = compositeCurve.calculatePoseGlobalCS(curveRelativePoint)

            assertThat(actualPoint).isInstanceOf(Result.Success::class.java)
            require(actualPoint is Result.Success)
            assertThat(actualPoint.value.point).isNotEqualTo(Vector2D.ZERO)
        }
    }

}
