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

package io.rtron.math.analysis.function.bivariate

import arrow.core.Either
import io.kotest.core.spec.style.FunSpec
import io.rtron.math.analysis.function.bivariate.combination.SectionedBivariateFunction
import io.rtron.math.analysis.function.bivariate.pure.PlaneFunction
import io.rtron.math.range.Range
import org.assertj.core.api.Assertions.assertThat

class SectionedBivariateFunctionTest : FunSpec({
    context("TestCreation") {

        test("shifting with bivariate function of complete range should not throw an error") {
            val planeFunction = PlaneFunction(1.0, 1.0, 0.0, Range.all(), Range.all())
            val sectionedPlane = SectionedBivariateFunction(planeFunction, Range.closed(1.0, 3.0), Range.all())

            val actualResult = sectionedPlane.value(0.0, 0.0)

            require(actualResult is Either.Right)
            assertThat(actualResult.value).isEqualTo(1.0)
        }
    }
})
