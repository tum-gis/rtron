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

package io.rtron.readerwriter.opendrive.reader.mapper.opendrive16

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.model.opendrive.lane.ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange
import io.rtron.model.opendrive.lane.ERoadMarkColor
import io.rtron.model.opendrive.lane.ERoadMarkRule
import io.rtron.model.opendrive.lane.ERoadMarkType
import io.rtron.model.opendrive.lane.ERoadMarkWeight
import io.rtron.model.opendrive.lane.RoadLanes
import io.rtron.model.opendrive.lane.RoadLanesLaneSection
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionCenter
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneLink
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMark
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMarkExplicit
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMarkType
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMarkTypeLine
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLaneBorder
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLaneWidth
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLeft
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLeftLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionRight
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionRightLane
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import org.asam.opendrive16.E_RoadMarkColor
import org.asam.opendrive16.E_RoadMarkRule
import org.asam.opendrive16.E_RoadMarkType
import org.asam.opendrive16.E_RoadMarkWeight
import org.asam.opendrive16.E_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_LaneChange
import org.asam.opendrive16.T_Bool
import org.asam.opendrive16.T_Road_Lanes
import org.asam.opendrive16.T_Road_Lanes_LaneSection
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Center
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Center_Lane
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Lcr_Lane_Link
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Explicit
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Type
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Type_Line
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Left
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Left_Lane
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Lr_Lane_Border
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Lr_Lane_Width
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Right
import org.asam.opendrive16.T_Road_Lanes_LaneSection_Right_Lane
import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueCheckStrategy
import org.mapstruct.ValueMapping

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    uses = [OpendriveCommonMapper::class, Opendrive16CoreMapper::class],
    imports = [Option::class]
)
abstract class Opendrive16LaneMapper {

    abstract fun mapRoadLanes(sources: T_Road_Lanes): RoadLanes

    //
    // Lane Section
    //
    abstract fun mapRoadLanesLaneSection(sources: T_Road_Lanes_LaneSection): RoadLanesLaneSection

    //
    // Lane
    //
    abstract fun mapRoadLanesLaneSectionCenter(source: T_Road_Lanes_LaneSection_Center): RoadLanesLaneSectionCenter
    abstract fun mapRoadLanesLaneSectionLeft(source: T_Road_Lanes_LaneSection_Left): RoadLanesLaneSectionLeft
    abstract fun mapRoadLanesLaneSectionRight(source: T_Road_Lanes_LaneSection_Right): RoadLanesLaneSectionRight

    abstract fun mapRoadLanesLaneSectionCenterLane(source: T_Road_Lanes_LaneSection_Center_Lane): RoadLanesLaneSectionCenterLane

    @AfterMapping
    open fun afterMappingRoadLanesLaneSectionCenterLane(source: T_Road_Lanes_LaneSection_Center_Lane, @MappingTarget target: RoadLanesLaneSectionCenterLane) {
        target.border = source.borderOrWidth.filterIsInstance(T_Road_Lanes_LaneSection_Lr_Lane_Border::class.java).map { mapLrLaneBorder(it) }
        target.width = source.borderOrWidth.filterIsInstance(T_Road_Lanes_LaneSection_Lr_Lane_Width::class.java).map { mapLrLaneWidth(it) }
    }

    abstract fun mapRoadLanesLaneSectionLeftLane(source: T_Road_Lanes_LaneSection_Left_Lane): RoadLanesLaneSectionLeftLane

    @AfterMapping
    open fun afterMappingRoadLanesLaneSectionLeftLane(source: T_Road_Lanes_LaneSection_Left_Lane, @MappingTarget target: RoadLanesLaneSectionLeftLane) {
        target.border = source.borderOrWidth.filterIsInstance(T_Road_Lanes_LaneSection_Lr_Lane_Border::class.java).map { mapLrLaneBorder(it) }
        target.width = source.borderOrWidth.filterIsInstance(T_Road_Lanes_LaneSection_Lr_Lane_Width::class.java).map { mapLrLaneWidth(it) }
    }

    abstract fun mapRoadLanesLaneSectionRightLane(source: T_Road_Lanes_LaneSection_Right_Lane): RoadLanesLaneSectionRightLane

    @AfterMapping
    open fun afterMappingRoadLanesLaneSectionRightLane(source: T_Road_Lanes_LaneSection_Right_Lane, @MappingTarget target: RoadLanesLaneSectionRightLane) {
        target.border = source.borderOrWidth.filterIsInstance(T_Road_Lanes_LaneSection_Lr_Lane_Border::class.java).map { mapLrLaneBorder(it) }
        target.width = source.borderOrWidth.filterIsInstance(T_Road_Lanes_LaneSection_Lr_Lane_Width::class.java).map { mapLrLaneWidth(it) }
    }

    //
    // Lane Border and Width
    //
    abstract fun mapLrLaneWidth(source: T_Road_Lanes_LaneSection_Lr_Lane_Width): RoadLanesLaneSectionLRLaneWidth
    abstract fun mapLrLaneBorder(source: T_Road_Lanes_LaneSection_Lr_Lane_Border): RoadLanesLaneSectionLRLaneBorder

    //
    // Lane Link
    //
    abstract fun mapCenterLaneLink(source: T_Road_Lanes_LaneSection_Lcr_Lane_Link): RoadLanesLaneSectionLCRLaneLink

    //
    // Road Mark
    //
    abstract fun mapLaneRoadMark(source: T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark): RoadLanesLaneSectionLCRLaneRoadMark

    abstract fun mapLaneRoadMarkType(source: T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Type): RoadLanesLaneSectionLCRLaneRoadMarkType
    abstract fun mapLaneRoadMarkTypeLine(source: T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Type_Line): RoadLanesLaneSectionLCRLaneRoadMarkTypeLine

    abstract fun mapLaneRoadMarkExplicit(source: T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Explicit): RoadLanesLaneSectionLCRLaneRoadMarkExplicit

    //
    // Enumerations
    //
    fun mapBoolToOption(source: T_Bool?): Option<Boolean> = source?.let { mapBool(it).some() } ?: None
    fun mapBool(source: T_Bool): Boolean = when (source) {
        T_Bool.TRUE -> true
        T_Bool.FALSE -> false
    }

    fun mapERoadMarkColorToOption(source: E_RoadMarkColor?): Option<ERoadMarkColor> = source?.let { mapRoadMarkColor(it).some() } ?: None
    abstract fun mapRoadMarkColor(source: E_RoadMarkColor): ERoadMarkColor

    fun mapERoadMarkRuleToOption(source: E_RoadMarkRule?): Option<ERoadMarkRule> = source?.let { mapRoadMarkRule(it).some() } ?: None

    @ValueMapping(source = "NO___PASSING", target = "NO_PASSING")
    abstract fun mapRoadMarkRule(source: E_RoadMarkRule): ERoadMarkRule

    fun mapRoadMarkLaneChangeToOption(source: E_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_LaneChange?): Option<ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange> = source?.let { mapRoadMarkLaneChange(it).some() } ?: None
    abstract fun mapRoadMarkLaneChange(source: E_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_LaneChange): ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange

    fun mapRoadMarkWeightToOption(source: E_RoadMarkWeight?): Option<ERoadMarkWeight> = source?.let { mapRoadMarkWeight(it).some() } ?: None
    abstract fun mapRoadMarkWeight(source: E_RoadMarkWeight): ERoadMarkWeight

    @ValueMapping(source = "SOLID___SOLID", target = "SOLID_SOLID")
    @ValueMapping(source = "SOLID___BROKEN", target = "SOLID_BROKEN")
    @ValueMapping(source = "BROKEN___SOLID", target = "BROKEN_SOLID")
    @ValueMapping(source = "BROKEN___BROKEN", target = "BROKEN_BROKEN")
    @ValueMapping(source = "BOTTS___DOTS", target = "BOTTS_DOTS")
    abstract fun mapRoadMarkType(source: E_RoadMarkType): ERoadMarkType
}
