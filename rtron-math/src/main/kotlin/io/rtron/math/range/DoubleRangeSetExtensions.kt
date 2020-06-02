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


fun RangeSet.Companion.ofNonIntersectingRanges(ranges: Set<Range<Double>>): RangeSet<Double> {
    val rangeSets = ranges.map { of(it) }.toSet()
    require(!rangeSets.containsIntersectingRangeSets()) { "Creation of RangeSet must not contain connected ranges." }
    return rangeSets.unionRangeSets()
}

fun RangeSet.Companion.ofNonIntersectingRanges(vararg ranges: Range<Double>): RangeSet<Double>
        = ofNonIntersectingRanges(ranges.toSet())
