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

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON_11
import io.rtron.math.std.DBL_EPSILON_2
import io.rtron.math.std.DBL_EPSILON_4
import io.rtron.math.std.DBL_EPSILON_5
import io.rtron.math.std.DBL_EPSILON_6
import io.rtron.math.std.DBL_EPSILON_8
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.FileReader
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists

class SpiralSegment2DTest : FunSpec({
    context("TestPointCalculation") {

        test("first spiral of the ASAM example dataset Ex_Line-Spiral-Arc") {
            val pose = Pose2D(Vector2D(3.8003686923043311e+01, -1.8133261823256248e+00), Rotation2D(3.3186980419884304e-01))
            val affine = Affine2D.of(pose)
            val curvatureFunction = LinearFunction.ofSpiralCurvature(0.0, 1.3333327910466574e-02, 2.9999999999999996e+01)
            val curve = SpiralSegment2D(curvatureFunction, DBL_EPSILON_2, AffineSequence2D.of(affine))
            val curveRelativePoint = CurveRelativeVector1D(1.3000000000000000e+02 - 1.0000000000000000e+02)

            val actualPose = curve.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

            actualPose.point.x.shouldBe(6.5603727689096445e+01 plusOrMinus DBL_EPSILON_11)
            actualPose.point.y.shouldBe(9.8074617455403796e+00 plusOrMinus DBL_EPSILON_11)
            actualPose.rotation.angle.shouldBe(
                5.3186972285460032e-01
                    plusOrMinus DBL_EPSILON_4
            )
        }

        test("second spiral of the ASAM example dataset Ex_Line-Spiral-Arc") {
            val pose = Pose2D(Vector2D(8.7773023553010319e+01, 2.9721920045249909e+01), Rotation2D(9.3186944634590163e-01))
            val affine = Affine2D.of(pose)
            val curvatureFunction = LinearFunction.ofSpiralCurvature(1.3333327910466574e-02, 6.6666666666666671e-03, 2.0000000000000000e+01)
            val curve = SpiralSegment2D(curvatureFunction, DBL_EPSILON_2, AffineSequence2D.of(affine))
            val curveRelativePoint = CurveRelativeVector1D(1.7999999146329435e+02 - 1.5999999146329435e+02)

            val actualPose = curve.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

            actualPose.point.x.shouldBe(9.7828942354905308e+01 plusOrMinus DBL_EPSILON_11)
            actualPose.point.y.shouldBe(4.6971187858525226e+01 plusOrMinus DBL_EPSILON_11)
            actualPose.rotation.angle.shouldBe(1.1318693921172343e+00 plusOrMinus DBL_EPSILON_4)
        }

        test("first spiral of the example dataset CrossingComplex8Course") {
            val pose = Pose2D(Vector2D(4.5002666984590900e+02, 5.2071081556728734e+02), Rotation2D(4.7173401120976974e+00))
            val affine = Affine2D.of(pose)
            val curvatureFunction = LinearFunction.ofSpiralCurvature(-0.0000000000000000e+00, -1.0126582278481013e-01, 4.9620253164556960e+00)
            val curve = SpiralSegment2D(curvatureFunction, DBL_EPSILON_2, AffineSequence2D.of(affine))
            val curveRelativePoint = CurveRelativeVector1D(1.0507568316454000e+01 - 5.5455429999983039e+00)

            val actualPose = curve.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

            actualPose.point.x.shouldBe(4.4963740167278593e+02 plusOrMinus DBL_EPSILON_6)
            actualPose.point.y.shouldBe(5.1577803259440122e+02 plusOrMinus DBL_EPSILON_6)
            actualPose.rotation.angle.shouldBe(4.4660983239227257e+00 plusOrMinus DBL_EPSILON_2)
        }

        test("second spiral of the example dataset CrossingComplex8Course") {
            val pose = Pose2D(Vector2D(4.4256271579386976e+02, 5.0863294215453800e+02), Rotation2D(3.3977855734777722e+00))
            val affine = Affine2D.of(pose)
            val curvatureFunction = LinearFunction.ofSpiralCurvature(-1.0126582278481013e-01, -0.0000000000000000e+00, 4.9620253164556960e+00)
            val curve = SpiralSegment2D(curvatureFunction, DBL_EPSILON_2, AffineSequence2D.of(affine))
            val curveRelativePoint = CurveRelativeVector1D(2.6019182043553606e+01 - 2.1057156727097912e+01)

            val actualPose = curve.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

            actualPose.point.x.shouldBe(4.3763402923360974e+02 plusOrMinus DBL_EPSILON_6)
            actualPose.point.y.shouldBe(5.0819484814787387e+02 plusOrMinus DBL_EPSILON_6)
            actualPose.rotation.angle.shouldBe(3.1465437853028004e+00 plusOrMinus DBL_EPSILON_2)
        }
    }

    context("TestDatasetCalculation") {

        val logger = KotlinLogging.logger {}

        test("test point and rotation calculation against csv sample dataset") {
            val filePath = Path("src/test/datasets/spiral_test_dataset/spiral_segments.csv").absolute()
            if (!filePath.exists()) {
                logger.warn { "Dataset does not exist at $filePath, skipping test" }
                return@test
            }
            val fileReader = FileReader(filePath.toFile())

            val records: Iterable<CSVRecord> = CSVFormat.Builder.create().setDelimiter(",").setHeader("s0", "x0", "y0", "hdg0", "curv0", "curv1", "length", "s1", "x1", "y1", "hdg1").setSkipHeaderRecord(true).build().parse(fileReader)
            for (record in records) {
                val pose = Pose2D(Vector2D(record.get("x0").toDouble(), record.get("y0").toDouble()), Rotation2D(record.get("hdg0").toDouble()))
                val affine = Affine2D.of(pose)
                val curvatureFunction = LinearFunction.ofSpiralCurvature(record.get("curv0").toDouble(), record.get("curv1").toDouble(), record.get("length").toDouble())
                val curve = SpiralSegment2D(curvatureFunction, DBL_EPSILON_5, AffineSequence2D.of(affine))
                val curveRelativePoint = CurveRelativeVector1D(record.get("s1").toDouble() - record.get("s0").toDouble())

                val actualPose = curve.calculatePoseGlobalCS(curveRelativePoint).shouldBeRight()

                actualPose.point.x.shouldBe(record.get("x1").toDouble() plusOrMinus DBL_EPSILON_8)
                actualPose.point.y.shouldBe(record.get("y1").toDouble() plusOrMinus DBL_EPSILON_8)
                actualPose.rotation.angle.shouldBe(Rotation2D(record.get("hdg1").toDouble()).angle plusOrMinus DBL_EPSILON_6)
            }
        }
    }
})
