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

package io.rtron.readerwriter.opendrive.reader.mapper.opendrive14

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.model.opendrive.lane.EAccessRestrictionType
import io.rtron.model.opendrive.lane.ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange
import io.rtron.model.opendrive.lane.ERoadMarkRule
import io.rtron.model.opendrive.lane.ERoadMarkType
import io.rtron.model.opendrive.lane.ERoadMarkWeight
import io.rtron.model.opendrive.lane.RoadLanesLaneSection
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneLink
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMarkType
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLeft
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionRight
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import org.asam.opendrive14.CenterLane
import org.asam.opendrive14.Lane
import org.asam.opendrive14.LaneChange
import org.asam.opendrive14.OpenDRIVE
import org.asam.opendrive14.Restriction
import org.asam.opendrive14.RoadmarkType
import org.asam.opendrive14.Rule
import org.asam.opendrive14.SingleSide
import org.asam.opendrive14.Weight
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy
import org.mapstruct.ValueMapping

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = [OpendriveCommonMapper::class, Opendrive14CoreMapper::class], imports = [Option::class])
abstract class Opendrive14LaneMapper {

    //
    // Lane Section
    //
    abstract fun mapRoadLanesLaneSection(source: OpenDRIVE.Road.Lanes.LaneSection): RoadLanesLaneSection

    abstract fun mapRoadLanesLaneSectionLeft(source: OpenDRIVE.Road.Lanes.LaneSection.Left): RoadLanesLaneSectionLeft
    abstract fun mapRoadLanesLaneSectionRight(source: OpenDRIVE.Road.Lanes.LaneSection.Right): RoadLanesLaneSectionRight

    fun mapSingleSideToOption(source: SingleSide?): Option<Boolean> = source?.let { mapSingleSide(it).some() } ?: None
    fun mapSingleSide(source: SingleSide): Boolean = when (source) {
        SingleSide.TRUE -> true
        SingleSide.FALSE -> false
    }

    //
    // Lane
    //
    fun mapRoadLanesLaneSectionCenterLanes(source: CenterLane?): List<RoadLanesLaneSectionCenterLane> = source?.let { listOf(mapRoadLanesLaneSectionCenterLane(it)) } ?: emptyList()
    abstract fun mapRoadLanesLaneSectionCenterLane(source: CenterLane): RoadLanesLaneSectionCenterLane

    //
    // Lane Link
    //
    abstract fun mapCenterLaneLink(source: CenterLane.Link): RoadLanesLaneSectionLCRLaneLink

    fun mapCenterLaneLinkPredecessor(source: CenterLane.Link.Predecessor?): List<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> =
        source?.let { listOf(mapPredecessor(it)) } ?: emptyList()
    abstract fun mapPredecessor(source: CenterLane.Link.Predecessor): RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor

    fun mapCenterLaneLinkSuccessor(source: CenterLane.Link.Successor?): List<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> =
        source?.let { listOf(mapSuccessor(it)) } ?: emptyList()
    abstract fun mapSuccessor(source: CenterLane.Link.Successor): RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor

    abstract fun mapLaneLink(source: Lane.Link): RoadLanesLaneSectionLCRLaneLink

    fun mapLaneLinkPredecessor(source: Lane.Link.Predecessor?): List<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> =
        source?.let { listOf(mapPredecessor(it)) } ?: emptyList()
    abstract fun mapPredecessor(source: Lane.Link.Predecessor): RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor

    fun mapLaneLinkSuccessor(source: Lane.Link.Successor?): List<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> =
        source?.let { listOf(mapSuccessor(it)) } ?: emptyList()
    abstract fun mapSuccessor(source: Lane.Link.Successor): RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor

    //
    // Road Mark
    //
    abstract fun mapLaneRoadMarkType(source: Lane.RoadMark.Type): RoadLanesLaneSectionLCRLaneRoadMarkType
    abstract fun mapCenterLaneRoadMarkType(source: CenterLane.RoadMark.Type): RoadLanesLaneSectionLCRLaneRoadMarkType

    fun mapRoadMarkWeightToOption(source: Weight?): Option<ERoadMarkWeight> = source?.let { mapRoadMarkWeight(it).some() } ?: None
    abstract fun mapRoadMarkWeight(source: Weight): ERoadMarkWeight

    fun mapLaneChangeToOption(source: LaneChange?): Option<ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange> = source?.let { mapLaneChange(it).some() } ?: None
    abstract fun mapLaneChange(source: LaneChange): ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange

    //
    // Enumerations
    //
    @ValueMapping(source = "AUTONOMOUS___TRAFFIC", target = "AUTONOMOUS_TRAFFIC")
    abstract fun map(source: Restriction): EAccessRestrictionType

    fun mapRoadMarkTypeToOption(source: RoadmarkType?): Option<ERoadMarkType> = source?.let { mapRoadMarkType(it).some() } ?: None
    @ValueMapping(source = "SOLID___SOLID", target = "SOLID_SOLID")
    @ValueMapping(source = "SOLID___BROKEN", target = "SOLID_BROKEN")
    @ValueMapping(source = "BROKEN___SOLID", target = "BROKEN_SOLID")
    @ValueMapping(source = "BROKEN___BROKEN", target = "BROKEN_BROKEN")
    @ValueMapping(source = "BOTTS___DOTS", target = "BOTTS_DOTS")
    abstract fun mapRoadMarkType(source: RoadmarkType): ERoadMarkType

    fun mapRuleToOption(source: Rule?): Option<ERoadMarkRule> = source?.let { mapRule(it).some() } ?: None
    @ValueMapping(source = "NO___PASSING", target = "NO_PASSING")
    abstract fun mapRule(source: Rule): ERoadMarkRule
}
