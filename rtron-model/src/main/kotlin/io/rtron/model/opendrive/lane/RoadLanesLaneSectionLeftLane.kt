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
import arrow.optics.optics
import io.rtron.model.opendrive.additions.identifier.AdditionalLaneIdentifier
import io.rtron.model.opendrive.additions.identifier.LaneIdentifier

@optics
data class RoadLanesLaneSectionLeftLane(
    var id: Int = Int.MIN_VALUE,
    override var link: Option<RoadLanesLaneSectionLCRLaneLink> = None,
    override var border: List<RoadLanesLaneSectionLRLaneBorder> = emptyList(),
    override var width: List<RoadLanesLaneSectionLRLaneWidth> = emptyList(),
    override var roadMark: List<RoadLanesLaneSectionLCRLaneRoadMark> = emptyList(),
    override var material: List<RoadLanesLaneSectionLRLaneMaterial> = emptyList(),
    override var speed: List<RoadLanesLaneSectionLRLaneSpeed> = emptyList(),
    override var access: List<RoadLanesLaneSectionLRLaneAccess> = emptyList(),
    override var height: List<RoadLanesLaneSectionLRLaneHeight> = emptyList(),
    override var rule: List<RoadLanesLaneSectionLRLaneRule> = emptyList(),
    override var level: Option<Boolean> = None,
    override var type: ELaneType = ELaneType.NONE,
    override var additionalId: Option<LaneIdentifier> = None,
) : RoadLanesLaneSectionLRLane(),
    AdditionalLaneIdentifier {
    companion object
}
