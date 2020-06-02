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

package io.rtron.math.range

import com.github.kittinunf.result.Result
import io.rtron.math.std.fuzzyEquals
import io.rtron.math.std.fuzzyLessThanOrEquals
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.sign


/**
 * Arranges an array of [Double] with [step] size.
 *
 * @param step size between two values
 * @param includeClosedEndPoint true, if endpoint of the [Range] is to be included
 * @param tolerance tolerance for checking whether endpoint is already included
 * @return [DoubleArray] of arranged values from lowerEndPoint to upperEndPoint of [Range]
 */
fun Range<Double>.arrange(step: Double, includeClosedEndPoint: Boolean = false, tolerance: Double = 0.0): DoubleArray {
    val lowerEndpoint = lowerEndpointOrNull()
    requireNotNull(lowerEndpoint) { "Closed lower bound type required." }
    val upperEndpoint = upperEndpointOrNull()
    requireNotNull(upperEndpoint) { "Closed upper bound type required." }

    if (includeClosedEndPoint)
        require(upperBoundType() == BoundType.CLOSED)
        { "If endpoint shall be included, the BoundType must be closed." }
    require(lowerEndpoint.isFinite() && upperEndpoint.isFinite())
    { "Finite endpoints required." }

    val numSteps = floor(length / step).toInt()
    val values = (0..numSteps).map { lowerEndpoint + sign(difference) * it * step }

    return if (includeClosedEndPoint && !fuzzyEquals(values.last(), upperEndpoint, tolerance))
        (values + upperEndpoint).toDoubleArray()
    else values.toDoubleArray()
}

/**
 * Returns true, if the [value] is fuzzily contained within this [Range] by the [tolerance].
 *
 * @param value value to be checked
 * @param tolerance allowed tolerance fuzziness
 * @return true, if [value] is contained
 */
fun Range<Double>.fuzzyContains(value: Double, tolerance: Double): Boolean = when {
    hasLowerBound() && !hasUpperBound() -> fuzzyLessThanOrEquals(lowerEndpointOrNull()!!, value, tolerance)
    !hasLowerBound() && hasUpperBound() -> fuzzyLessThanOrEquals(value, upperEndpointOrNull()!!, tolerance)
    !hasLowerBound() && !hasUpperBound() -> value in this
    else -> fuzzyLessThanOrEquals(lowerEndpointOrNull()!!, value, tolerance) && fuzzyLessThanOrEquals(value, upperEndpointOrNull()!!, tolerance)
}

/**
 * Returns [Result.Success], if the [value] is fuzzily contained within this [Range] by the [tolerance].
 *
 * @param value value to be checked
 * @param tolerance allowed tolerance fuzziness
 * @return [Result.Success], if [value] is fuzzily contained; [Result.Failure], otherwise
 */
fun Range<Double>.fuzzyContainsResult(value: Double, tolerance: Double): Result<Boolean, IllegalArgumentException> =
    when (this.fuzzyContains(value, tolerance)) {
        true -> Result.success(true)
        false -> Result.error(IllegalArgumentException("Value ($value) is not fuzzily contained in range $this."))
    }

/**
 * Widens the lower bound of the [Range] by [lowerWideningValue] and the upper bound of the
 * [Range] by [upperWideningValue].
 *
 * @param lowerWideningValue the value for widening the lower bound
 * @param upperWideningValue the value for widening the upper bound
 * @return widened [Range]
 */
fun Range<Double>.widened(lowerWideningValue: Double, upperWideningValue: Double): Range<Double> = Range.rangeOfNullable(
        lowerBoundType = lowerBoundType(),
        lowerEndpoint = lowerEndpointOrNull()?.let { it - lowerWideningValue },
        upperBoundType = upperBoundType(),
        upperEndpoint = upperEndpointOrNull()?.let { it + upperWideningValue })

/**
 * Widens the [Range] by [wideningValue] on the lower and upper bound.
 *
 * @param wideningValue value for widening the lower and upper bound
 * @return widened [Range]
 */
fun Range<Double>.widened(wideningValue: Double): Range<Double> = widened(wideningValue, wideningValue)

/**
 * Returns true, if the [other] [Range] is fuzzily enclosed with a [tolerance].
 *
 * @param other other range to be checked
 * @param tolerance allowed tolerance
 * @return true, if [other] is enclosed
 */
fun Range<Double>.fuzzyEncloses(other: Range<Double>, tolerance: Double): Boolean =
        this.widened(tolerance) encloses other

/**
 * Difference between the upper and lower bound of the [Range].
 */
val Range<Double>.difference: Double
    get() {
        val adjustedLowerEndpoint = lowerEndpointOrNull() ?: Double.NEGATIVE_INFINITY
        val adjustedUpperEndpoint = upperEndpointOrNull() ?: Double.POSITIVE_INFINITY
        return adjustedUpperEndpoint - adjustedLowerEndpoint
    }

/**
 * Absolute length between upper and lower bound.
 */
val Range<Double>.length
    get() = difference.absoluteValue

/**
 * Shifts the [Range] by a [value].
 *
 * @param value value the [Range] is to be shifted by
 * @return shifted [Range]
 */
fun Range<Double>.shift(value: Double): Range<Double> = Range.rangeOfNullable(
        lowerBoundType = this.lowerBoundType(),
        lowerEndpoint = this.lowerEndpointOrNull()?.let { it + value },
        upperBoundType = this.upperBoundType(),
        upperEndpoint = this.upperEndpointOrNull()?.let { it + value })

/**
 * Shift the [Range] so that the lower endpoint is represented by the [value].
 *
 * @param value new lower endpoint after shifting [Range]
 * @return shifted [Range]
 */
fun Range<Double>.shiftLowerEndpointTo(value: Double): Range<Double> = Range.range(
        lowerBoundType = this.lowerBoundType(),
        lowerEndpoint = value,
        upperBoundType = this.upperBoundType(),
        upperEndpoint = value + this.length)
