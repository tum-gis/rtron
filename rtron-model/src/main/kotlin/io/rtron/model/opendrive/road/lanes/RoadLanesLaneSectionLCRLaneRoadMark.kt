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

import io.rtron.model.opendrive.common.*


data class RoadLanesLaneSectionLCRLaneRoadMark(
        var sway: List<RoadLanesLaneSectionLCRLaneRoadMarkSway> = listOf(),
        var type: RoadLanesLaneSectionLCRLaneRoadMarkType = RoadLanesLaneSectionLCRLaneRoadMarkType(),
        var explicit: RoadLanesLaneSectionLCRLaneRoadMarkExplicit = RoadLanesLaneSectionLCRLaneRoadMarkExplicit(),

        var userData: List<UserData> = listOf(),
        var include: List<Include> = listOf(),
        var dataQuality: DataQuality = DataQuality(),

        var sOffset: Double = Double.NaN,
        var typeAttribute: ERoadMarkType = ERoadMarkType.NONE,
        var weight: ERoadMarkWeight = ERoadMarkWeight.STANDARD,
        var color: ERoadMarkColor = ERoadMarkColor.STANDARD,
        var material: String = "",
        var width: Double = 0.0,
        var laneChange: ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange = ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.NONE,
        var height: Double = Double.NaN
)
