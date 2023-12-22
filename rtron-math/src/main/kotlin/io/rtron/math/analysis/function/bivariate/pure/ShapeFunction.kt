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

package io.rtron.math.analysis.function.bivariate.pure

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import io.rtron.math.analysis.function.bivariate.BivariateFunction
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.range.Range
import io.rtron.std.getValueEither
import java.util.SortedMap

/**
 * The bivariate shape function is defined by a list of functions that are parallel to the y axis and placed at
 * different positions on the x axis.
 *
 * @param functions univariate functions parallel to the y axis, whereas the key denotes the location on the x axis
 * @param extrapolateX if true, the last (or first) function is used when exceeding (or falling below)
 * @param extrapolateY if true, the last (or first) value is used, which is still within the domain of the respective
 * function
 */
class ShapeFunction(
    val functions: SortedMap<Double, UnivariateFunction>,
    val extrapolateX: Boolean = false,
    val extrapolateY: Boolean = false
) : BivariateFunction() {

    // Properties and Initializers
    init {
        require(functions.isNotEmpty()) { "Must contain cross-sectional functions." }
    }

    override val domainX: Range<Double> = Range.all()
    override val domainY: Range<Double> = Range.all()

    private val minimumX: Double = functions.keys.minOrNull()!!
    private val maximumX: Double = functions.keys.maxOrNull()!!

    // Methods

    override fun valueUnbounded(x: Double, y: Double): Either<Exception, Double> {
        val xAdjusted = if (extrapolateX) x.coerceIn(minimumX, maximumX) else x
        if (xAdjusted in functions) {
            return calculateZ(xAdjusted, y)
        }

        val keyBefore = getKeyBefore(x).getOrElse { throw it }
        val zBefore = calculateZ(keyBefore, y).getOrElse { throw it }
        val keyAfter = getKeyAfter(x).getOrElse { throw it }
        val zAfter = calculateZ(keyAfter, y).getOrElse { throw it }

        val linear = LinearFunction.ofInclusivePoints(keyBefore, zBefore, keyAfter, zAfter)
        return linear.valueUnbounded(x)
    }

    /**
     * Returns the key of a function, which is located before [x].
     */
    private fun getKeyBefore(x: Double): Either<Exception, Double> = functions
        .filter { it.key < x }
        .ifEmpty { return Either.Left(IllegalArgumentException("No relevant entry available.")) }
        .toSortedMap()
        .lastKey()
        .let { Either.Right(it) }

    /**
     * Returns the key of a function, which is located after [x].
     */
    private fun getKeyAfter(x: Double): Either<Exception, Double> = functions
        .filter { x < it.key }
        .ifEmpty { return Either.Left(IllegalArgumentException("No relevant entry available.")) }
        .toSortedMap()
        .firstKey()
        .let { Either.Right(it) }

    private fun calculateZ(key: Double, y: Double): Either<Exception, Double> = either {
        val selectedFunction = functions.getValueEither(key).mapLeft { it.toIllegalArgumentException() }.bind()

        val yAdjusted = if (!extrapolateY) {
            y
        } else {
            y.coerceIn(selectedFunction.domain.lowerEndpointOrNull(), selectedFunction.domain.upperEndpointOrNull())
        }

        selectedFunction.valueUnbounded(yAdjusted).bind()
    }
}
