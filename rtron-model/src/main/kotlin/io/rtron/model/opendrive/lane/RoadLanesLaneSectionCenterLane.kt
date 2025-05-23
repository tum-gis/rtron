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

package io.rtron.model.opendrive.lane

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.optics.optics
import io.rtron.model.opendrive.additions.identifier.AdditionalLaneIdentifier
import io.rtron.model.opendrive.additions.identifier.LaneIdentifier

@optics
data class RoadLanesLaneSectionCenterLane(
    var id: Int = 0,
    var link: Option<RoadLanesLaneSectionLCRLaneLink> = None,
    var roadMark: List<RoadLanesLaneSectionLCRLaneRoadMark> = emptyList(),
    var height: List<RoadLanesLaneSectionLRLaneHeight> = emptyList(),
    var level: Option<Boolean> = None,
    var type: ELaneType = ELaneType.NONE,
    override var additionalId: Option<LaneIdentifier> = None,
) : AdditionalLaneIdentifier {
    companion object

    fun getLevelWithDefault() = level.getOrElse { false }
}
