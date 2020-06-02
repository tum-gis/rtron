package io.rtron.math.range

import io.rtron.std.powerSet

/**
 * Unions a set of [RangeSet] to a single [RangeSet].
 */
fun <T: Comparable<*>> Set<RangeSet<T>>.unionRangeSets(): RangeSet<T> =
        reduce { acc, element -> acc.union(element) }

/**
 * Returns the intersecting [RangeSet].
 *
 * @receiver provided set of [RangeSet] for which the intersecting [RangeSet] is evaluated
 * @return minimum intersecting range set
 */
fun <T: Comparable<*>> Set<RangeSet<T>>.intersectionRangeSets(): RangeSet<T> =
        reduce { acc, element -> acc.intersection(element) }

/**
 * Returns true, if set of [RangeSet] contains intersecting [RangeSet] pairs.
 */
fun <T: Comparable<*>> Set<RangeSet<T>>.containsIntersectingRangeSets(): Boolean {
    val rangeSetPairCombinations =
            powerSet().filter { it.size == 2 }.map { Pair(it.first(), it.last()) }
    return rangeSetPairCombinations.any { it.first.intersects(it.second) }
}

/**
 * Conversion to [RangeSet].
 *
 * @receiver [Range] to be converted
 */
fun <T: Comparable<*>> Range<T>.toRangeSet(): RangeSet<T> = RangeSet.of(this)
