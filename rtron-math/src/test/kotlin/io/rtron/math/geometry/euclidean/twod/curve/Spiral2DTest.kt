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
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.FileReader
import java.lang.Math.abs
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists

internal class Spiral2DTest {

    @Nested
    inner class TestPointCalculation {

        @Test
        fun `return (0,0) at l=0`() {
            val spiral = Spiral2D(1.0)

            val actualPoint = spiral.calculatePoint(0.0)

            assertThat(actualPoint).isEqualTo(Vector2D.ZERO)
        }

        @Test
        fun `return asymptotic point at l=+infinity`() {
            val asymptoticPoint = Vector2D(0.5, 0.5)
            val spiral = Spiral2D(PI)

            val actualPoint = spiral.calculatePoint(Double.POSITIVE_INFINITY)

            assertThat(actualPoint.x).isCloseTo(asymptoticPoint.x, Offset.offset(DBL_EPSILON_1))
            assertThat(actualPoint.y).isCloseTo(asymptoticPoint.y, Offset.offset(DBL_EPSILON_1))
        }

        @Test
        fun `return asymptotic point at l=-infinity`() {
            val asymptoticPoint = Vector2D(-0.5, -0.5)
            val spiral = Spiral2D(PI)

            val actualPoint = spiral.calculatePoint(Double.NEGATIVE_INFINITY)

            assertThat(actualPoint.x).isCloseTo(asymptoticPoint.x, Offset.offset(DBL_EPSILON_1))
            assertThat(actualPoint.y).isCloseTo(asymptoticPoint.y, Offset.offset(DBL_EPSILON_1))
        }
    }

    @Nested
    inner class TestRotationCalculation {

        @Test
        fun `return 0 at l=0`() {
            val spiral = Spiral2D(1.0)

            val actualRotation = spiral.calculateRotation(0.0)

            assertThat(actualRotation.toAngleRadians()).isEqualTo(0.0)
        }
    }

    @Nested
    inner class TestDatasetCalculation {

        private val logger = KotlinLogging.logger {}

        @Test
        fun `test point and rotation calculation against csv sample dataset`() {
            val filePath = Path("src/test/cpp/spiral/build/sampled_spiral.csv").absolute()
            if (!filePath.exists()) {
                logger.warn { "Dataset does not exist at $filePath, skipping test" }
                return
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
                assertThat(actualPoint.x).isCloseTo(x, Offset.offset(DBL_EPSILON_3))
                assertThat(actualPoint.y).isCloseTo(y, Offset.offset(DBL_EPSILON_3))
                assertThat(actualRotation.angle).isCloseTo(Rotation2D(t).angle, Offset.offset(DBL_EPSILON_1))
            }
        }

        @Test
        fun `test point calculation against sample value 1`() {
            val spiral = Spiral2D(-0.067773987108739761)

            val actualPoint = spiral.calculatePoint(-5330.827396000006)

            assertThat(actualPoint.x).isCloseTo(-3.401537830619735, Offset.offset(DBL_EPSILON))
            assertThat(actualPoint.y).isCloseTo(3.403385667520832, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `test rotation calculation against sample value 1`() {
            val spiral = Spiral2D(-0.067773987108739761)

            val actualPoint = spiral.calculateRotation(-5330.827396000006)

            assertThat(actualPoint.angle).isCloseTo(Rotation2D(-962991.11906995473).angle, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `test point calculation against sample value 2`() {
            val spiral = Spiral2D(0.011823698552189441)

            val actualPoint = spiral.calculatePoint(38679.185313200163)

            assertThat(actualPoint.x).isCloseTo(8.1518659286823159, Offset.offset(DBL_EPSILON))
            assertThat(actualPoint.y).isCloseTo(8.1487837011384663, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `test rotation calculation against sample value 2`() {
            val spiral = Spiral2D(0.011823698552189441)

            val actualPoint = spiral.calculateRotation(38679.185313200163)

            assertThat(actualPoint.angle).isCloseTo(Rotation2D(8844595.7788996678).angle, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `test point calculation against sample value 3`() {
            val spiral = Spiral2D(-0.051693646571178295)

            val actualPoint = spiral.calculatePoint(6884.6109795996472)

            // this higher offset is caused by accumulated floating point errors
            assertThat(actualPoint.x).isCloseTo(3.9006399958644153, Offset.offset(DBL_EPSILON_2))
            assertThat(actualPoint.y).isCloseTo(-3.8974453107566154, Offset.offset(DBL_EPSILON_2))
        }

        @Test
        fun `test rotation calculation against sample value 3`() {
            val spiral = Spiral2D(-0.051693646571178295)

            val actualPoint = spiral.calculateRotation(6884.6109795996472)

            assertThat(actualPoint.angle).isCloseTo(Rotation2D(-1225084.3271085601).angle, Offset.offset(DBL_EPSILON))
        }
    }
}
