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

import arrow.optics.optics
import io.rtron.model.opendrive.core.OpendriveElement

@optics
data class RoadLanesLaneSectionCenter(
    var lane: List<RoadLanesLaneSectionCenterLane> = emptyList(),
) : OpendriveElement() {

    // Methods
    fun getIndividualCenterLane(): RoadLanesLaneSectionCenterLane {
        check(lane.size == 1) { "Must contain exactly one center lane element." }
        return lane.first()
    }
    fun getNumberOfLanes() = lane.size

    companion object
}
