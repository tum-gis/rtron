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

import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.ELaneType
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData


open class RoadLanesLaneSectionLRLane(
        var link: RoadLanesLaneSectionLCRLaneLink = RoadLanesLaneSectionLCRLaneLink(),
        var width: List<RoadLanesLaneSectionLRLaneWidth> = listOf(),
        var border: List<RoadLanesLaneSectionLRLaneBorder> = listOf(),
        var roadMark: List<RoadLanesLaneSectionLCRLaneRoadMark> = listOf(),
        var material: List<RoadLanesLaneSectionLRLaneMaterial> = listOf(),
        var visibility: List<RoadLanesLaneSectionLRLaneVisibility> = listOf(),
        var speed: List<RoadLanesLaneSectionLRLaneSpeed> = listOf(),
        var access: List<RoadLanesLaneSectionLRLaneAccess> = listOf(),
        var height: List<RoadLanesLaneSectionLRLaneHeight> = listOf(),
        var rule: List<RoadLanesLaneSectionLRLaneRule> = listOf(),

        var userData: List<UserData> = listOf(),
        var include: List<Include> = listOf(),
        var dataQuality: DataQuality = DataQuality(),

        var type: ELaneType = ELaneType.NONE,
        var level: Boolean = false
)
