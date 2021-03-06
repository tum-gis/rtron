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

package io.rtron.model.opendrive.road.lanes

import io.rtron.math.range.Range
import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData

data class RoadLanes(
    var laneOffset: List<RoadLanesLaneOffset> = listOf(),
    var laneSection: List<RoadLanesLaneSection> = listOf(),

    var userData: List<UserData> = listOf(),
    var include: List<Include> = listOf(),
    var dataQuality: DataQuality = DataQuality()
) {

    // Methods
    fun containsLaneOffset() = laneOffset.isNotEmpty()

    fun getLaneSectionsWithRanges(lastLaneSectionEnd: Double): List<Pair<Range<Double>, RoadLanesLaneSection>> {
        require(laneSection.all { it.s < lastLaneSectionEnd }) {
            "The curve relative starts of all lane section must be below the " +
                "provided lastLaneSectionEnd ($lastLaneSectionEnd)."
        }

        if (laneSection.isEmpty()) return emptyList()

        return laneSection
            .zipWithNext()
            .map { Range.closed(it.first.s, it.second.s) to it.first } +
            (Range.closed(laneSection.last().s, lastLaneSectionEnd) to laneSection.last())
    }
}
