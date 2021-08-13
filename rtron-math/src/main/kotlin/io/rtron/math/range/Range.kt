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

package io.rtron.math.range

import com.github.kittinunf.result.Result
import com.google.common.collect.Range as GRange

/** Conversion from Guava range class. */
fun <T : Comparable<*>> GRange<T>.toRange() = Range(this)

/**
 * Represents a mathematical range.
 * See wikipedia article of [interval](https://en.wikipedia.org/wiki/Interval_(mathematics)).
 *
 * @param _range adapted guava range class
 */
class Range<T : Comparable<*>>(
    private val _range: GRange<T>
) {
    // Operators

    /**
     * Returns true, if the [value] is within this [Range] bounds.
     */
    operator fun contains(value: T): Boolean = value in _range

    // Methods

    /**
     * Returns [Result.Success], if the [value] is contained within this [Range].
     *
     * @param value value to be checked
     * @return [Result.Success], if [value] is fuzzily contained; [Result.Failure], otherwise
     */
    fun containsResult(value: T): Result<Boolean, IllegalArgumentException> =
        when (value in this) {
            true -> Result.success(true)
            false -> Result.error(IllegalArgumentException("Value ($value) is not contained in range $this."))
        }

    /** Returns true, if this range has a lower endpoint. */
    fun hasLowerBound() = _range.hasLowerBound()

    /** Returns true, if this range has an upper endpoint. */
    fun hasUpperBound() = _range.hasUpperBound()

    /** Returns the lower endpoint, if this range has one; otherwise null is returned. */
    fun lowerEndpointOrNull(): T? = if (hasLowerBound()) _range.lowerEndpoint() else null

    /** Returns the upper endpoint, if this range has one; otherwise null is returned. */
    fun upperEndpointOrNull(): T? = if (hasUpperBound()) _range.upperEndpoint() else null

    /** Returns the lower endpoint as result. */
    fun lowerEndpointResult(): Result<T, IllegalStateException> =
        if (hasLowerBound()) Result.success(_range.lowerEndpoint())
        else Result.error(IllegalStateException("No lower endpoint available."))

    /** Returns the upper endpoint as result. */
    fun upperEndpointResult(): Result<T, IllegalStateException> =
        if (hasUpperBound()) Result.success(_range.upperEndpoint())
        else Result.error(IllegalStateException("No upper endpoint available."))

    /** Returns the lower [BoundType] of this range. */
    fun lowerBoundType(): BoundType = if (hasLowerBound()) _range.lowerBoundType().toBoundType() else BoundType.NONE

    /** Returns the upper [BoundType] of this range. */
    fun upperBoundType(): BoundType = if (hasUpperBound()) _range.upperBoundType().toBoundType() else BoundType.NONE

    /**
     * Returns true, if this range has the form [v..v) or (v..v].
     */
    fun isEmpty() = _range.isEmpty

    fun isNotEmpty() = !isEmpty()

    /**
     * Returns true, if there exists a (possibly empty) range which is enclosed by both this range and [other].
     *
     * For example,
     * [1, 3) and [4, 5] are not connected
     * [1, 3) and [2, 5] are connected
     * [1, 3) and [3, 5] are connected
     *
     * @param other other range
     * @return true, if this and [other] range is connected
     */
    fun isConnected(other: Range<T>) = _range.isConnected(other._range)

    /**
     * Returns true, if the bounds of the [other] range do not extend the bounds of this range.
     *
     * @param other range to be evaluated
     * @return true if, this range encloses the [other] range
     */
    infix fun encloses(other: Range<T>) = _range.encloses(other._range)

    /**
     * Returns the intersecting range of this range with the other [connectedRange].
     *
     * @param connectedRange range that must be connected to this range
     * @return intersecting range
     */
    fun intersection(connectedRange: Range<T>): Range<T> = _range.intersection(connectedRange._range).toRange()

    /**
     * Returns the minimal range that encloses this and the [other] range.
     * For example, the span of [1, 3) and [5, 7] is [1, 7].
     *
     * @param other other range
     * @return range that encloses this and the [other] range
     */
    fun span(other: Range<T>) = _range.span(other._range).toRange()

    /**
     * Joins this and a [connectedRange] by building the span.
     *
     * @param connectedRange other range that must be connected to this range
     * @return joined [Range]
     */
    fun join(connectedRange: Range<T>): Range<T> {
        require(isConnected(connectedRange)) { "Range is not connected." }
        return span(connectedRange)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Range<*>

        if (_range != other._range) return false

        return true
    }

    override fun hashCode(): Int {
        return _range.hashCode()
    }

    // Conversions

    /** Conversion to Guava range. */
    fun toRangeG() = this._range

    /** Conversion to String. */
    override fun toString() = "Range($_range)"

    companion object {

        /**
         * Creates a [Range] based on bound type and endpoint values. If bound type is [BoundType.NONE] the
         * respective endpoint value must be null for consistency reasons.
         *
         *
         * @param lowerBoundType lower bound type which must be consistent with [lowerEndpoint]
         * @param lowerEndpoint value for lower endpoint
         * @param upperBoundType upper bound type which must be consistent with [upperEndpoint]
         * @param upperEndpoint value for upper endpoint
         * @return created [Range]
         */
        fun <T : Comparable<*>> rangeOfNullable(
            lowerBoundType: BoundType,
            lowerEndpoint: T?,
            upperBoundType: BoundType,
            upperEndpoint: T?
        ): Range<T> {
            // consistency checks
            if (lowerBoundType == BoundType.NONE)
                require(lowerEndpoint == null) { "Inconsistent lower bound parameters." }
            if (upperBoundType == BoundType.NONE)
                require(upperEndpoint == null) { "Inconsistent upper bound parameters." }
            if (lowerEndpoint == null)
                require(lowerBoundType == BoundType.NONE) { "Inconsistent lower bound parameters." }
            if (upperEndpoint == null)
                require(upperBoundType == BoundType.NONE) { "Inconsistent upper bound parameters." }

            // range function building
            if (lowerBoundType == BoundType.NONE && upperBoundType == BoundType.NONE)
                return GRange.all<T>().toRange()

            if (lowerBoundType != BoundType.NONE && upperBoundType == BoundType.NONE)
                return GRange.downTo(lowerEndpoint!!, lowerBoundType.toBoundTypeG()!!).toRange()
            if (lowerBoundType == BoundType.NONE && upperBoundType != BoundType.NONE)
                return GRange.upTo(upperEndpoint!!, upperBoundType.toBoundTypeG()!!).toRange()

            return GRange.range(
                lowerEndpoint!!,
                lowerBoundType.toBoundTypeG()!!,
                upperEndpoint!!,
                upperBoundType.toBoundTypeG()!!
            ).toRange()
        }

        /**
         * Creates a [Range].
         *
         * @param lowerBoundType type of lower bound; if [BoundType.NONE] the [lowerEndpoint] value is ignored
         * @param lowerEndpoint value for lower endpoint
         * @param upperBoundType type of lower bound; if [BoundType.NONE] the [upperEndpoint] value is ignored
         * @param upperEndpoint value for upper endpoint
         * @return created [Range]
         */
        fun <T : Comparable<*>> range(
            lowerBoundType: BoundType,
            lowerEndpoint: T,
            upperBoundType: BoundType,
            upperEndpoint: T
        ): Range<T> {
            val lowerEndpointNullable: T? = if (lowerBoundType == BoundType.NONE) null else lowerEndpoint
            val upperEndpointNullable: T? = if (upperBoundType == BoundType.NONE) null else upperEndpoint

            return rangeOfNullable(lowerBoundType, lowerEndpointNullable, upperBoundType, upperEndpointNullable)
        }

        /** Creates a [Range] of the form ([lower], [upper]). */
        fun <T : Comparable<*>> open(lower: T, upper: T): Range<T> = GRange.open(lower, upper).toRange()

        /** Creates a [Range] of the form [[lower], [upper]]. */
        fun <T : Comparable<*>> closed(lower: T, upper: T): Range<T> = GRange.closed(lower, upper).toRange()

        /** Creates a [Range] of the form ([lower], [upper]]. */
        fun <T : Comparable<*>> openClosed(lower: T, upper: T): Range<T> = GRange.openClosed(lower, upper).toRange()

        /** Creates a [Range] of the form [[lower], [upper]). */
        fun <T : Comparable<*>> closedOpen(lower: T, upper: T): Range<T> = GRange.closedOpen(lower, upper).toRange()

        /** Creates a [Range] of the form ([endpoint], ∞). */
        fun <T : Comparable<*>> greaterThan(endpoint: T): Range<T> = GRange.greaterThan(endpoint).toRange()

        /** Creates a [Range] of the form [[endpoint], ∞). */
        fun <T : Comparable<*>> atLeast(endpoint: T): Range<T> = GRange.atLeast(endpoint).toRange()

        /** Creates a [Range] of the form (-∞, [endpoint]). */
        fun <T : Comparable<*>> lessThan(endpoint: T): Range<T> = GRange.lessThan(endpoint).toRange()

        /** Creates a [Range] of the form (-∞, [endpoint]]. */
        fun <T : Comparable<*>> atMost(endpoint: T): Range<T> = GRange.atMost(endpoint).toRange()

        /** Creates a [Range] that contains every value in [T]. */
        fun <T : Comparable<*>> all(): Range<T> = GRange.all<T>().toRange()

        /**
         * Creates a [Range] of the form [[endpoint], ∞) or ([endpoint], ∞) depending on the [boundType].
         *
         * @param endpoint lower endpoint
         * @param boundType type of bound which must not be [BoundType.NONE]
         * @return created [Range]
         */
        fun <T : Comparable<*>> downTo(endpoint: T, boundType: BoundType): Range<T> {
            require(boundType != BoundType.NONE) { "Provided bound type must not be none." }
            return GRange.downTo(endpoint, boundType.toBoundTypeG()!!).toRange()
        }

        /**
         * Creates a [Range] of the form (-∞, [endpoint]) or (-∞, [endpoint]] depending on the [boundType].
         *
         * @param endpoint upper endpoint
         * @param boundType type of bound which must not be [BoundType.NONE]
         * @return created [Range]
         */
        fun <T : Comparable<*>> upTo(endpoint: T, boundType: BoundType): Range<T> {
            require(boundType != BoundType.NONE) { "Provided bound type must not be none." }
            return GRange.upTo(endpoint, boundType.toBoundTypeG()!!).toRange()
        }

        /**
         * Creates a [Range] of the form [[lower], [upper]] or [[lower], [upper]) depending on the [upperBoundType].
         *
         * @param lower lower endpoint
         * @param upper upper endpoint
         * @param upperBoundType type of upper bound which must not be [BoundType.NONE]
         * @return created [Range]
         */
        fun <T : Comparable<*>> closedX(lower: T, upper: T, upperBoundType: BoundType): Range<T> =
            when (upperBoundType) {
                BoundType.CLOSED -> closed(lower, upper)
                BoundType.OPEN -> closedOpen(lower, upper)
                BoundType.NONE -> throw IllegalArgumentException("Upper bound must exist")
            }
    }
}
