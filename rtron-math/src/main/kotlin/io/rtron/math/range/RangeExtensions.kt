package io.rtron.math.range

/**
 * Returns the intersecting [Range] of a provided set of ranges.
 *
 * @receiver provided set of ranges for which the intersecting range shall be found
 * @return maximum range that intersects all ranges within the set
 */
fun <T: Comparable<*>> Set<Range<T>>.intersectingRange(): Range<T> =
        reduce { acc, element -> acc.intersection(element) }

/**
 * Union operation of a set of [Range] to a [RangeSet].
 */
fun <T: Comparable<*>> Set<Range<T>>.unionRanges(): RangeSet<T> =
        map { RangeSet.of(it) }.toSet().unionRangeSets()

/**
 * Returns true, if the set of ranges contains intersecting [Range].
 */
fun <T: Comparable<*>> Set<Range<T>>.containsIntersectingRanges(): Boolean {
    val rangeSetsList = this.map { RangeSet.of(it) }
    return rangeSetsList.toSet().containsIntersectingRangeSets()
}
