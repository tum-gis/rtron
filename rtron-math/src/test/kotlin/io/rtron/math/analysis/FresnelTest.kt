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

package io.rtron.math.analysis

import io.rtron.math.std.DBL_EPSILON
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.FileReader
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists

class FresnelTest {

    private val logger = KotlinLogging.logger {}

    @Nested
    inner class TestDatasetCalculation {

        @Test
        fun `test against csv sample dataset`() {
            val filePath = Path("src/test/cpp/spiral/build/sampled_fresnel_integral.csv").absolute()
            if (!filePath.exists()) {
                logger.warn { "Dataset does not exist at $filePath, skipping test" }
                return
            }
            val fileReader = FileReader(filePath.toFile())

            val records: Iterable<CSVRecord> = CSVFormat.Builder.create().setDelimiter(",").setHeader("l", "x", "y").setSkipHeaderRecord(true).build().parse(fileReader)
            for (record in records) {
                val l: Double = record.get("l").toDouble()
                val x: Double = record.get("x").toDouble()
                val y: Double = record.get("y").toDouble()
                // println("$l $x $y")

                val (actualX, actualY) = Fresnel.calculatePoint(l)

                assertThat(actualX).isCloseTo(x, Offset.offset(DBL_EPSILON))
                assertThat(actualY).isCloseTo(y, Offset.offset(DBL_EPSILON))
            }
        }

        @Test
        fun `test against sample value 1`() {
            val (actualX, actualY) = Fresnel.calculatePoint(-4.2284028867950161)

            assertThat(actualX).isCloseTo(-0.51547336206019945, Offset.offset(DBL_EPSILON))
            assertThat(actualY).isCloseTo(-0.5736113070569262, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `test against sample value 2`() {
            val (actualX, actualY) = Fresnel.calculatePoint(883.12677767970729)

            assertThat(actualX).isCloseTo(0.50035646758310326, Offset.offset(DBL_EPSILON))
            assertThat(actualY).isCloseTo(0.49994666781760994, Offset.offset(DBL_EPSILON))
        }

        @Test
        fun `test against sample value 3`() {
            val (actualX, actualY) = Fresnel.calculatePoint(-1.8154077322757265)

            assertThat(actualX).isCloseTo(-0.33992314562581244, Offset.offset(DBL_EPSILON))
            assertThat(actualY).isCloseTo(-0.43687889962705617, Offset.offset(DBL_EPSILON))
        }
    }
}
