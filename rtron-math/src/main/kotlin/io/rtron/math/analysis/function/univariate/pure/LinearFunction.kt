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

package io.rtron.math.analysis.function.univariate.pure

import arrow.core.Either
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.range.Range
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * Linear function of the form f(x) = [slope] * x + [intercept].
 *
 * @param slope slope of linear function
 * @param intercept [intercept] at f(0)
 * @param domain defined domain for the function
 */
data class LinearFunction(
    val slope: Double,
    val intercept: Double = 0.0,
    override val domain: Range<Double> = Range.all()
) : UnivariateFunction() {

    // Methods
    override fun valueUnbounded(x: Double): Either<IllegalArgumentException, Double> =
        Either.Right(slope * x + intercept)

    override fun slopeUnbounded(x: Double): Either<IllegalArgumentException, Double> = Either.Right(slope)

    companion object {

        /**
         * Linear function representing the x axis.
         */
        val X_AXIS = LinearFunction(0.0, 0.0, Range.all())

        /**
         * Returns a linear function starting at (0.0, [intercept]) and stopping at ([pointY] - [intercept] , [pointY])
         * with a slope of 1.0 and a closed parameter range.
         *
         * @param intercept linear function starting at (0.0, [intercept])
         * @param pointY linear function stopping at ([pointY] - [intercept] , [pointY])
         */
        fun ofInclusiveYValuesAndUnitSlope(intercept: Double, pointY: Double): LinearFunction {
            require(intercept.isFinite()) { "Intercept must be a finite value." }
            require(pointY.isFinite()) { "PointY must be a finite value." }

            val slope = sign(pointY - intercept)
            val domain = Range.closed(0.0, abs(pointY - intercept))
            return LinearFunction(slope, intercept, domain)
        }

        /**
         * Returns a linear function constructed by (0.0, [intercept]) and ([pointX], [pointY]) within a closed
         * parameter range.
         *
         * @param intercept linear function starting at (0.0, [intercept])
         * @param pointX linear function stopping at ([pointX], [pointY])
         * @param pointY linear function stopping at ([pointX], [pointY])
         */
        fun ofInclusiveInterceptAndPoint(intercept: Double, pointX: Double, pointY: Double): LinearFunction {
            require(pointX.isFinite() && pointX != 0.0) { "point must not be located on the y axis." }

            val slope = (pointY - intercept) / pointX
            val domain = Range.closed(min(0.0, pointX), max(0.0, pointX))

            return LinearFunction(slope, intercept, domain)
        }

        /**
         * Returns a linear function by (0.0, [intercept]) and ([pointX], [pointY]) within a closed parameter range.
         * If the [intercept] or the [pointY] is not finite (e.g. NaN), the respective other value is used.
         *
         * @param intercept linear function starting at (0.0, [intercept])
         * @param pointX linear function stopping at ([pointX], [pointY])
         * @param pointY linear function stopping at ([pointX], [pointY])
         */
        fun ofInclusiveInterceptAndPointWithoutNaN(intercept: Double, pointX: Double, pointY: Double): LinearFunction {
            require(intercept.isFinite() || pointY.isFinite()) { "Either intercept or pointY must be finite." }
            require(pointX.isFinite()) { "X must be finite." }

            val adjustedIntercept = if (intercept.isFinite()) intercept else pointY
            val adjustedPointY = if (pointY.isFinite()) pointY else intercept
            return ofInclusiveInterceptAndPoint(adjustedIntercept, pointX, adjustedPointY)
        }

        /**
         * Returns a linear function constructed by ([point1X], [point1Y]) and ([point2X], [point2Y]) within a closed
         * parameter range.
         *
         * @param point1X x value of starting point
         * @param point1Y y value of starting point
         * @param point2X x value of stopping point
         * @param point2Y y value of stopping point
         *
         * @return linear function with inclusive starting and stopping points
         */
        fun ofInclusivePoints(point1X: Double, point1Y: Double, point2X: Double, point2Y: Double): LinearFunction {
            require(!(point1X == point2X && point1Y == point2Y)) { "Points are required to be different." }

            val slope = (point2Y - point1Y) / (point2X - point1X)
            val intercept = point1Y - slope * point1X
            val domain = Range.closed(min(point1X, point2X), max(point1X, point2X))

            return LinearFunction(slope, intercept, domain)
        }
    }
}
