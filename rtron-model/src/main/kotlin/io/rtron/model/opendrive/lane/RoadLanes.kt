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

package io.rtron.model.opendrive.lane

import arrow.core.NonEmptyList
import arrow.core.Validated
import arrow.core.computations.ResultEffect.bind
import io.rtron.math.range.Range
import io.rtron.math.range.length
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.additions.exceptions.toIllegalStateException
import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.std.isSortedBy
import io.rtron.std.toValidated

data class RoadLanes(
    var laneOffset: List<RoadLanesLaneOffset> = emptyList(),
    var laneSection: List<RoadLanesLaneSection> = emptyList(),
) : OpendriveElement() {

    // Validation Properties
    val laneSectionValidated: Validated<OpendriveException.EmptyList, NonEmptyList<RoadLanesLaneSection>>
        get() = NonEmptyList.fromList(laneSection).toValidated { OpendriveException.EmptyList("laneSection") }

    // Validation Methods
    fun getSevereViolations(): List<OpendriveException> = laneSectionValidated.fold({ listOf(it) }, { emptyList() })

    fun healMinorViolations(): List<OpendriveException> {
        val healedViolations = mutableListOf<OpendriveException>()

        if (!laneSection.isSortedBy { it.s }) {
            healedViolations += OpendriveException.NonSortedList("laneSection")
            laneSection = laneSection.sortedBy { it.s }
        }

        return healedViolations
    }

    // Methods
    fun containsLaneOffset() = laneOffset.isNotEmpty()

    fun getLaneSectionRanges(lastLaneSectionEnd: Double): NonEmptyList<Range<Double>> {
        require(laneSection.all { it.s < lastLaneSectionEnd }) {
            "The curve relative starts of all lane section must be below the " +
                "provided lastLaneSectionEnd ($lastLaneSectionEnd)."
        }
        val adjustedLaneSections = laneSectionValidated.toEither().mapLeft { it.toIllegalStateException() }.bind()

        val laneSectionRanges = adjustedLaneSections.zipWithNext().map { Range.closed(it.first.s, it.second.s) }
        val lastLaneSectionRange = Range.closed(adjustedLaneSections.last().s, lastLaneSectionEnd)

        return NonEmptyList.fromListUnsafe(laneSectionRanges + lastLaneSectionRange)
    }

    fun getLaneSectionLengths(lastLaneSectionEnd: Double): NonEmptyList<Double> =
        getLaneSectionRanges(lastLaneSectionEnd).map { it.length }

    fun getLaneSectionsWithRanges(lastLaneSectionEnd: Double): NonEmptyList<Pair<Range<Double>, RoadLanesLaneSection>> =
        NonEmptyList.fromListUnsafe(getLaneSectionRanges(lastLaneSectionEnd).zip(laneSection))
}
