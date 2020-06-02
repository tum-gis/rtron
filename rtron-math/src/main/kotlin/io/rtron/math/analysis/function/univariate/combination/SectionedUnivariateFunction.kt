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

package io.rtron.math.analysis.function.univariate.combination

import com.github.kittinunf.result.Result
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.range.Range
import io.rtron.math.range.shiftLowerEndpointTo
import io.rtron.std.handleFailure


/**
 * Cuts out a section from the [completeFunction].
 * The resulting domain of the [SectionedUnivariateFunction] starts at 0.0 and ends at the length of the section.
 *
 * @param completeFunction complete function that is to be cut
 * @param section the range that is cut out from the [completeFunction]'s domain
 */
class SectionedUnivariateFunction(
        private val completeFunction: UnivariateFunction,
        section: Range<Double>
) : UnivariateFunction() {


    // Properties and Initializers
    override val domain: Range<Double> = section.shiftLowerEndpointTo(0.0)
    private val sectionStart = section.lowerEndpointResult().handleFailure { throw it.error }

    // Methods
    override fun valueUnbounded(x: Double): Result<Double, Exception> =
            completeFunction.valueUnbounded(sectionStart + x)

    override fun slopeUnbounded(x: Double): Result<Double, Exception> =
            completeFunction.slopeUnbounded(sectionStart + x)
}
