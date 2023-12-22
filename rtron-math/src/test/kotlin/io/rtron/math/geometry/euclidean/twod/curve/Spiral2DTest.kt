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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.DBL_EPSILON_1
import io.rtron.math.std.DBL_EPSILON_2
import io.rtron.math.std.DBL_EPSILON_3
import io.rtron.math.std.PI
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.FileReader
import java.lang.Math.abs
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists

class Spiral2DTest : FunSpec({
    context("TestPointCalculation") {

        test("return (0,0) at l=0") {
            val spiral = Spiral2D(1.0)

            val actualPoint = spiral.calculatePoint(0.0)

            actualPoint shouldBe Vector2D.ZERO
        }

        test("return asymptotic point at l=+infinity") {
            val asymptoticPoint = Vector2D(0.5, 0.5)
            val spiral = Spiral2D(PI)

            val actualPoint = spiral.calculatePoint(Double.POSITIVE_INFINITY)

            actualPoint.x.shouldBe(asymptoticPoint.x plusOrMinus DBL_EPSILON_1)
            actualPoint.y.shouldBe(asymptoticPoint.y plusOrMinus DBL_EPSILON_1)
        }

        test("return asymptotic point at l=-infinity") {
            val asymptoticPoint = Vector2D(-0.5, -0.5)
            val spiral = Spiral2D(PI)

            val actualPoint = spiral.calculatePoint(Double.NEGATIVE_INFINITY)

            actualPoint.x.shouldBe(asymptoticPoint.x plusOrMinus DBL_EPSILON_1)
            actualPoint.y.shouldBe(asymptoticPoint.y plusOrMinus DBL_EPSILON_1)
        }
    }

    context("TestRotationCalculation") {

        test("return 0 at l=0") {
            val spiral = Spiral2D(1.0)

            val actualRotation = spiral.calculateRotation(0.0)

            actualRotation.toAngleRadians() shouldBe 0.0
        }
    }

    context("TestDatasetCalculation") {

        val logger = KotlinLogging.logger {}

        test("test point and rotation calculation against csv sample dataset") {
            val filePath = Path("src/test/cpp/spiral/build/sampled_spiral.csv").absolute()
            if (!filePath.exists()) {
                logger.warn { "Dataset does not exist at $filePath, skipping test" }
                return@test
            }
            val fileReader = FileReader(filePath.toFile())

            val records: Iterable<CSVRecord> = CSVFormat.Builder.create().setDelimiter(",").setHeader("cDot", "s", "x", "y", "t").setSkipHeaderRecord(true).build().parse(fileReader)
            for (record in records) {
                val cDot: Double = record.get("cDot").toDouble()
                val s: Double = record.get("s").toDouble()
                val x: Double = record.get("x").toDouble()
                val y: Double = record.get("y").toDouble()
                val t: Double = record.get("t").toDouble()
                // println("$l $x $y")

                val spiral = Spiral2D(cDot)
                val actualPoint = spiral.calculatePoint(s)
                val actualRotation = spiral.calculateRotation(s)

                if (abs(actualPoint.x - x) > DBL_EPSILON_1 || abs(actualPoint.y - y) > DBL_EPSILON_1) {
                    println("test")
                }
                actualPoint.x.shouldBe(x plusOrMinus DBL_EPSILON_3)
                actualPoint.y.shouldBe(y plusOrMinus DBL_EPSILON_3)
                actualRotation.angle.shouldBe(Rotation2D(t).angle plusOrMinus DBL_EPSILON_1)
            }
        }

        test("test point calculation against sample value 1") {
            val spiral = Spiral2D(-0.067773987108739761)

            val actualPoint = spiral.calculatePoint(-5330.827396000006)

            actualPoint.x.shouldBe(-3.401537830619735 plusOrMinus DBL_EPSILON)
            actualPoint.y.shouldBe(3.403385667520832 plusOrMinus DBL_EPSILON)
        }

        test("test rotation calculation against sample value 1") {
            val spiral = Spiral2D(-0.067773987108739761)

            val actualPoint = spiral.calculateRotation(-5330.827396000006)

            actualPoint.angle.shouldBe(Rotation2D(-962991.11906995473).angle plusOrMinus DBL_EPSILON)
        }

        test("test point calculation against sample value 2") {
            val spiral = Spiral2D(0.011823698552189441)

            val actualPoint = spiral.calculatePoint(38679.185313200163)

            actualPoint.x.shouldBe(8.1518659286823159 plusOrMinus DBL_EPSILON)
            actualPoint.y.shouldBe(8.1487837011384663 plusOrMinus DBL_EPSILON)
        }

        test("test rotation calculation against sample value 2") {
            val spiral = Spiral2D(0.011823698552189441)

            val actualPoint = spiral.calculateRotation(38679.185313200163)

            actualPoint.angle.shouldBe(Rotation2D(8844595.7788996678).angle plusOrMinus DBL_EPSILON)
        }

        test("test point calculation against sample value 3") {
            val spiral = Spiral2D(-0.051693646571178295)

            val actualPoint = spiral.calculatePoint(6884.6109795996472)

            // this higher offset is caused by accumulated floating point errors
            actualPoint.x.shouldBe(3.9006399958644153 plusOrMinus DBL_EPSILON_2)
            actualPoint.y.shouldBe(-3.8974453107566154 plusOrMinus DBL_EPSILON_2)
        }

        test("test rotation calculation against sample value 3") {
            val spiral = Spiral2D(-0.051693646571178295)

            val actualPoint = spiral.calculateRotation(6884.6109795996472)

            actualPoint.angle.shouldBe(Rotation2D(-1225084.3271085601).angle plusOrMinus DBL_EPSILON)
        }
    }
})
