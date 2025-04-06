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

package io.rtron.math.analysis

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.rtron.math.std.DBL_EPSILON
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.FileReader
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists

class FresnelTest : FunSpec({

    val logger = KotlinLogging.logger {}

    context("TestDatasetCalculation") {

        test("test against csv sample dataset") {
            val filePath = Path("src/test/cpp/spiral/build/sampled_fresnel_integral.csv").absolute()
            if (!filePath.exists()) {
                logger.warn { "Dataset does not exist at $filePath, skipping test" }
                return@test
            }
            val fileReader = FileReader(filePath.toFile())

            val records: Iterable<CSVRecord> =
                CSVFormat.Builder.create().setDelimiter(
                    ",",
                ).setHeader("l", "x", "y").setSkipHeaderRecord(true).get().parse(fileReader)
            for (record in records) {
                val l: Double = record.get("l").toDouble()
                val x: Double = record.get("x").toDouble()
                val y: Double = record.get("y").toDouble()
                // println("$l $x $y")

                val (actualX, actualY) = Fresnel.calculatePoint(l)

                actualX.shouldBe(x plusOrMinus DBL_EPSILON)
                actualY.shouldBe(y plusOrMinus DBL_EPSILON)
            }
        }

        test("test against sample value 1") {
            val (actualX, actualY) = Fresnel.calculatePoint(-4.2284028867950161)

            actualX.shouldBe(-0.51547336206019945 plusOrMinus DBL_EPSILON)
            actualY.shouldBe(-0.5736113070569262 plusOrMinus DBL_EPSILON)
        }

        test("test against sample value 2") {
            val (actualX, actualY) = Fresnel.calculatePoint(883.12677767970729)

            actualX.shouldBe(0.50035646758310326 plusOrMinus DBL_EPSILON)
            actualY.shouldBe(0.49994666781760994 plusOrMinus DBL_EPSILON)
        }

        test("test against sample value 3") {
            val (actualX, actualY) = Fresnel.calculatePoint(-1.8154077322757265)

            actualX.shouldBe(-0.33992314562581244 plusOrMinus DBL_EPSILON)
            actualY.shouldBe(-0.43687889962705617 plusOrMinus DBL_EPSILON)
        }
    }
})
