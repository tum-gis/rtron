/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.analysis.function.bivariate.combination

import io.rtron.math.analysis.function.bivariate.BivariateFunction
import io.rtron.math.range.Range
import io.rtron.math.range.shiftLowerEndpointTo

/**
 * Cuts out a section of a [BivariateFunction]. If no cut out shall be applied for either x or y, provide either of them
 * with [Range.all].
 *
 * @param completeFunction the complete bivariate function to be cut out
 * @param sectionX section to be cut out of x
 * @param sectionY section to be cut out of y
 */
class SectionedBivariateFunction(
    private val completeFunction: BivariateFunction,
    sectionX: Range<Double>,
    sectionY: Range<Double>
) : BivariateFunction() {

    // Properties and Initializers
    override val domainX =
        if (sectionX.hasLowerBound()) sectionX.shiftLowerEndpointTo(0.0) else completeFunction.domainX

    override val domainY =
        if (sectionY.hasLowerBound()) sectionY.shiftLowerEndpointTo(0.0) else completeFunction.domainY

    private val sectionXStart = sectionX.lowerEndpointOrNull() ?: 0.0
    private val sectionYStart = sectionY.lowerEndpointOrNull() ?: 0.0

    // Methods
    override fun valueUnbounded(x: Double, y: Double) =
        completeFunction.valueUnbounded(sectionXStart + x, sectionYStart + y)
}
