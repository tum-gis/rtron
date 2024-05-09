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

package io.rtron.math.range

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableRangeSet as GImmutableRangeSet

/** Conversion from Guava range set class. */
fun <T : Comparable<*>> GImmutableRangeSet<T>.toRangeSet() = RangeSet(this)

/**
 * An immutable set of ranges.
 *
 * @param rangeSet adapted Guava class
 */
class RangeSet<T : Comparable<*>>(
    private val rangeSet: GImmutableRangeSet<T>,
) {
    // Secondary Constructors

    constructor(ranges: Set<Range<T>>) :
        this (GImmutableRangeSet.copyOf(ImmutableList.copyOf(ranges.map { it.toRangeG() })))

    // Operators

    /**
     * Returns true, if the [value] is within the [RangeSet].
     */
    operator fun contains(value: T): Boolean = value in this.rangeSet

    // Methods

    /**
     * Returns the minimal range that encloses the range set.
     */
    fun span(): Range<T> = this.rangeSet.span().toRange()

    /**
     * Returns true, if this [RangeSet] intersects with the [otherRange].
     */
    fun intersects(otherRange: Range<T>): Boolean = this.rangeSet.intersects(otherRange.toRangeG())

    /**
     * Returns true, if this [RangeSet] intersects with the [otherRangeSet].
     */
    fun intersects(otherRangeSet: RangeSet<T>): Boolean = otherRangeSet.asRanges().any { intersects(it) }

    /**
     * Union operation of this [RangeSet] and the [other] [RangeSet].
     */
    fun union(other: RangeSet<T>): RangeSet<T> = rangeSet.union(other.toRangeSetG()).toRangeSet()

    /**
     * Returns the intersecting [RangeSet] of this range with the [other] [RangeSet].
     *
     * @param other range set that must be connected to this range set
     * @return intersecting [RangeSet]
     */
    fun intersection(other: RangeSet<T>): RangeSet<T> = rangeSet.intersection(other.toRangeSetG()).toRangeSet()

    /**
     * Returns a new [RangeSet] consisting of the difference of this range set and the [other].
     */
    fun difference(other: RangeSet<T>): RangeSet<T> = rangeSet.difference(other.toRangeSetG()).toRangeSet()

    /**
     * Returns the number of disconnected intervals of this [RangeSet].
     */
    fun numberOfDisconnectedRanges() = asRanges().size

    // Conversions

    /**
     * Conversion to Guava range set class.
     */
    fun toRangeSetG() = this.rangeSet

    /**
     * Returns all ranges individually.
     */
    fun asRanges(): Set<Range<T>> = this.rangeSet.asRanges().map { it.toRange() }.toSet()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RangeSet<*>

        if (this.rangeSet != other.rangeSet) return false

        return true
    }

    override fun hashCode(): Int {
        return this.rangeSet.hashCode()
    }

    companion object {
        /**
         * Creates an empty [RangeSet].
         */
        fun <T : Comparable<*>> empty() = RangeSet(emptySet<Range<T>>())

        /**
         * Creates a [RangeSet] that contains all values.
         */
        fun <T : Comparable<*>> all() = RangeSet(setOf<Range<T>>(Range.all()))

        /**
         * Creates a [RangeSet] containing the provided [ranges].
         *
         * @param ranges ranges which are included in the [RangeSet]
         */
        fun <T : Comparable<*>> of(vararg ranges: Range<T>): RangeSet<T> = RangeSet(ranges.toSet())
    }
}
