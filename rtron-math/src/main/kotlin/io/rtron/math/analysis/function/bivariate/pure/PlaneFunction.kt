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

package io.rtron.math.analysis.function.bivariate.pure

import arrow.core.Either
import io.rtron.math.analysis.function.bivariate.BivariateFunction
import io.rtron.math.range.Range

/**
 * Plane function of the form z = f(x, y) = [slopeX] * x + [slopeY] * y + [intercept].
 *
 * @param slopeX slope applied of x
 * @param slopeY slope applied of y
 * @param intercept [intercept] = f(0, 0)
 */
class PlaneFunction(
    val slopeX: Double,
    val slopeY: Double,
    val intercept: Double,
    override val domainX: Range<Double> = Range.all(),
    override val domainY: Range<Double> = Range.all()
) : BivariateFunction() {

    // Properties and Initializers
    init {
        require(slopeX.isFinite()) { "slopeX must be a finite value." }
        require(slopeY.isFinite()) { "slopeY must be a finite value." }
        require(intercept.isFinite()) { "intercept must be a finite value." }
    }

    // Methods
    override fun valueUnbounded(x: Double, y: Double): Either<Exception, Double> {
        return Either.Right(intercept + slopeX * x + slopeY * y)
    }

    companion object {
        val ZERO = PlaneFunction(0.0, 0.0, 0.0)
    }
}
