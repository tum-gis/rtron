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

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toNonEmptyListOrNone
import io.rtron.model.opendrive.additions.identifier.AdditionalLaneIdentifier
import io.rtron.model.opendrive.additions.identifier.LaneIdentifier
import io.rtron.model.opendrive.core.OpendriveElement

/**
 *  [RoadLanesLaneSectionLRLane] is an abstract class, to that the data classes [RoadLanesLaneSectionLeftLane],
 *  [RoadLanesLaneSectionRightLane] and [RoadLanesLaneSectionCenterLane] can inherit from it.
 */
abstract class RoadLanesLaneSectionLRLane(
    open var link: Option<RoadLanesLaneSectionLCRLaneLink> = None,
    open var border: List<RoadLanesLaneSectionLRLaneBorder> = emptyList(),
    open var width: List<RoadLanesLaneSectionLRLaneWidth> = emptyList(),
    open var roadMark: List<RoadLanesLaneSectionLCRLaneRoadMark> = emptyList(),
    open var material: List<RoadLanesLaneSectionLRLaneMaterial> = emptyList(),
    open var speed: List<RoadLanesLaneSectionLRLaneSpeed> = emptyList(),
    open var access: List<RoadLanesLaneSectionLRLaneAccess> = emptyList(),
    open var height: List<RoadLanesLaneSectionLRLaneHeight> = emptyList(),
    open var rule: List<RoadLanesLaneSectionLRLaneRule> = emptyList(),
    open var level: Option<Boolean> = None,
    open var type: ELaneType = ELaneType.NONE,
    override var additionalId: Option<LaneIdentifier> = None,
) : OpendriveElement(),
    AdditionalLaneIdentifier {
    // Properties
    fun getLaneWidthEntries(): Option<NonEmptyList<RoadLanesLaneSectionLRLaneWidth>> = width.toNonEmptyListOrNone()

    fun getLaneHeightEntries(): Option<NonEmptyList<RoadLanesLaneSectionLRLaneHeight>> = height.toNonEmptyListOrNone()

    fun getLevelWithDefault() = level.getOrElse { false }
}
