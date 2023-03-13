/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

import arrow.optics.optics
import io.rtron.model.opendrive.core.OpendriveElement

@optics
data class RoadLanesLaneSectionLeft(
    var lane: List<RoadLanesLaneSectionLeftLane> = emptyList()
) : OpendriveElement() {

    // Methods
    fun isEmpty() = lane.isEmpty()
    fun isNotEmpty() = lane.isNotEmpty()
    fun getNumberOfLanes() = lane.size

    fun getLanes() = lane.associateBy { it.id }
    fun getLanesAscending() = lane.sortedBy { it.id }

    companion object
}
