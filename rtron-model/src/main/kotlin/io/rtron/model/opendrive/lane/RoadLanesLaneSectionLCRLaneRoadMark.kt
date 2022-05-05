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

import arrow.core.None
import arrow.core.Option
import io.rtron.model.opendrive.core.OpendriveElement

data class RoadLanesLaneSectionLCRLaneRoadMark(
    var sway: List<RoadLanesLaneSectionLCRLaneRoadMarkSway> = emptyList(),
    var type: Option<RoadLanesLaneSectionLCRLaneRoadMarkType> = None,
    var explicit: Option<RoadLanesLaneSectionLCRLaneRoadMarkExplicit> = None,

    var color: ERoadMarkColor = ERoadMarkColor.STANDARD,
    var height: Option<Double> = None,
    var laneChange: Option<ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange> = None,
    var material: Option<String> = None,
    var sOffset: Double = Double.NaN,
    var typeAttribute: ERoadMarkType = ERoadMarkType.NONE,
    var weight: Option<ERoadMarkWeight> = None,
    var width: Option<Double> = None,
) : OpendriveElement()
