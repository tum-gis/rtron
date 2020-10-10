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

package io.rtron.math

import com.github.kittinunf.result.Result
import io.rtron.io.csv.CSVPrinter
import io.rtron.io.files.Path
import io.rtron.math.analysis.Fresnel
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativePoint1D
import io.rtron.math.geometry.euclidean.twod.curve.Spiral2D
import io.rtron.math.geometry.euclidean.twod.curve.SpiralSegment2D
import io.rtron.math.range.Range
import io.rtron.math.range.arrange
import io.rtron.math.std.PI
import io.rtron.math.transform.AffineSequence2D
import org.junit.jupiter.api.Test


object SpiralSegment2DWriter {

    @Test
    fun writeSpiralSegment2DToCsvFile() {

        val path = Path("out/test_files/SpiralSegment2D/SpiralSegment2D-line.csv")
        val header = listOf("curvePosition", "x", "y")
        val csvPrinter = CSVPrinter(path, header)

        val curvatureStart = 5.0
        val curvatureEnd = 15.0
        val length = (curvatureEnd - curvatureStart) / PI
        val curvatureRange = LinearFunction.ofInclusiveInterceptAndPoint(5.0, 15.0, length)
        val spiralSegment = SpiralSegment2D(curvatureRange, 0.0, AffineSequence2D.EMPTY)

        for (currentPosition in curvatureRange.domain.arrange(0.1, false, 0.0)) {
            val ret = spiralSegment.calculatePoseGlobalCS(CurveRelativePoint1D(currentPosition))
            require(ret is Result.Success)
            csvPrinter.printRecord(currentPosition.toString(), ret.value.point.x.toString(), ret.value.point.y.toString())
        }

        csvPrinter.flush()
    }

    fun writeSpiral2DToCsvFile() {

        val path = Path("out/test_files/SpiralSegment2D/Spiral2D-line.csv")
        val header = listOf("type", "curvePosition", "x", "y")
        val csvPrinter = CSVPrinter(path, header)

        for (cDot in Range.closed(-1.0 * kotlin.math.PI, 1.0 * kotlin.math.PI).arrange(0.25 * kotlin.math.PI, false, 0.0)) {
            for (s in Range.closed(-100.0, 100.0).arrange(0.01, false, 0.0)) {
                val pos = Spiral2D(cDot).calculatePose(s)
                csvPrinter.printRecord("Spiral2D cDot=$cDot", s, pos.point.x, pos.point.y)
            }
        }

        csvPrinter.flush()
    }

    fun writeFresnelToCsvFile() {
        val filePath = Path("out/test_files/Fresnel/Fresnel_line.csv")
        val csvPrinter = CSVPrinter(filePath, listOf("curvePosition", "x", "y"))

        val range = Range.closed(-10000.0, 10000.0).arrange(0.01, false, 0.0)
        for (curvePosition in range) {
            val pos = Fresnel.calculatePoint(curvePosition)
            csvPrinter.printRecord(curvePosition, pos.first, pos.second)
        }

        csvPrinter.flush()
    }

}
