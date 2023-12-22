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

package io.rtron.readerwriter.opendrive.writer.mapper.opendrive17

import arrow.core.Option
import io.rtron.model.opendrive.lane.ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange
import io.rtron.model.opendrive.lane.ERoadMarkColor
import io.rtron.model.opendrive.lane.ERoadMarkRule
import io.rtron.model.opendrive.lane.ERoadMarkType
import io.rtron.model.opendrive.lane.ERoadMarkWeight
import io.rtron.model.opendrive.lane.RoadLanesLaneSection
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneLink
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMarkExplicit
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMarkType
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLaneBorder
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLaneWidth
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLeft
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLeftLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionRight
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionRightLane
import io.rtron.readerwriter.opendrive.writer.mapper.common.OpendriveCommonMapper
import org.asam.opendrive17.E_RoadMarkColor
import org.asam.opendrive17.E_RoadMarkRule
import org.asam.opendrive17.E_RoadMarkType
import org.asam.opendrive17.E_RoadMarkWeight
import org.asam.opendrive17.E_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_LaneChange
import org.asam.opendrive17.T_Road_Lanes_LaneSection
import org.asam.opendrive17.T_Road_Lanes_LaneSection_Center_Lane
import org.asam.opendrive17.T_Road_Lanes_LaneSection_Lcr_Lane_Link
import org.asam.opendrive17.T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Explicit
import org.asam.opendrive17.T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Type
import org.asam.opendrive17.T_Road_Lanes_LaneSection_Left
import org.asam.opendrive17.T_Road_Lanes_LaneSection_Left_Lane
import org.asam.opendrive17.T_Road_Lanes_LaneSection_Lr_Lane_Border
import org.asam.opendrive17.T_Road_Lanes_LaneSection_Lr_Lane_Width
import org.asam.opendrive17.T_Road_Lanes_LaneSection_Right
import org.asam.opendrive17.T_Road_Lanes_LaneSection_Right_Lane
import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueCheckStrategy
import org.mapstruct.ValueMapping

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = [OpendriveCommonMapper::class, Opendrive17CoreMapper::class], imports = [Option::class])
abstract class Opendrive17LaneMapper {
    //
    // Lane Section
    //
    abstract fun mapRoadLanesLaneSection(source: RoadLanesLaneSection): T_Road_Lanes_LaneSection

    fun mapRoadLanesLaneSectionLeft(source: Option<RoadLanesLaneSectionLeft>): T_Road_Lanes_LaneSection_Left? =
        source.fold({ null }, { mapRoadLanesLaneSectionLeft(it) })
    abstract fun mapRoadLanesLaneSectionLeft(source: RoadLanesLaneSectionLeft): T_Road_Lanes_LaneSection_Left

    fun mapOptionRoadLanesLaneSectionRight(source: Option<RoadLanesLaneSectionRight>): T_Road_Lanes_LaneSection_Right? =
        source.fold({ null }, { mapRoadLanesLaneSectionRight(it) })
    abstract fun mapRoadLanesLaneSectionRight(source: RoadLanesLaneSectionRight): T_Road_Lanes_LaneSection_Right

    //
    // Lane
    //
    @Mapping(source = "width", target = "borderOrWidth")
    abstract fun mapRoadLanesLaneSectionCenterLane(source: RoadLanesLaneSectionCenterLane): T_Road_Lanes_LaneSection_Center_Lane

    @AfterMapping
    fun afterRoadLanesLaneSectionCenterLane(source: RoadLanesLaneSectionCenterLane, @MappingTarget target: T_Road_Lanes_LaneSection_Center_Lane) {
        target.borderOrWidth += source.border.map { mapRoadLanesLaneSectionLRLaneBorder(it) }
    }

    @Mapping(source = "width", target = "borderOrWidth")
    abstract fun mapRoadLanesLaneSectionLeftLane(source: RoadLanesLaneSectionLeftLane): T_Road_Lanes_LaneSection_Left_Lane

    @AfterMapping
    fun afterRoadLanesLaneSectionLeftLane(source: RoadLanesLaneSectionLeftLane, @MappingTarget target: T_Road_Lanes_LaneSection_Left_Lane) {
        target.borderOrWidth += source.border.map { mapRoadLanesLaneSectionLRLaneBorder(it) }
    }

    @Mapping(source = "width", target = "borderOrWidth")
    abstract fun mapRoadLanesLaneSectionLeftRight(source: RoadLanesLaneSectionRightLane): T_Road_Lanes_LaneSection_Right_Lane

    @AfterMapping
    fun afterRoadLanesLaneSectionRightLane(source: RoadLanesLaneSectionRightLane, @MappingTarget target: T_Road_Lanes_LaneSection_Right_Lane) {
        target.borderOrWidth += source.border.map { mapRoadLanesLaneSectionLRLaneBorder(it) }
    }

    fun mapOptionRoadLanesLaneSectionLCRLaneLink(source: Option<RoadLanesLaneSectionLCRLaneLink>): T_Road_Lanes_LaneSection_Lcr_Lane_Link? =
        source.fold({ null }, { mapRoadLanesLaneSectionLCRLaneLink(it) })
    abstract fun mapRoadLanesLaneSectionLCRLaneLink(source: RoadLanesLaneSectionLCRLaneLink): T_Road_Lanes_LaneSection_Lcr_Lane_Link

    abstract fun mapRoadLanesLaneSectionLRLaneWidth(source: RoadLanesLaneSectionLRLaneWidth): T_Road_Lanes_LaneSection_Lr_Lane_Width
    abstract fun mapRoadLanesLaneSectionLRLaneBorder(source: RoadLanesLaneSectionLRLaneBorder): T_Road_Lanes_LaneSection_Lr_Lane_Border

    //
    // Road Mark
    //
    fun mapOptionRoadLanesLaneSectionLCRLaneRoadMarkType(source: Option<RoadLanesLaneSectionLCRLaneRoadMarkType>): T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Type? =
        source.fold({ null }, { mapRoadLanesLaneSectionLCRLaneRoadMarkType(it) })
    abstract fun mapRoadLanesLaneSectionLCRLaneRoadMarkType(source: RoadLanesLaneSectionLCRLaneRoadMarkType): T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Type

    fun mapOptionRoadMarkWeight(source: Option<ERoadMarkWeight>): E_RoadMarkWeight? =
        source.fold({ null }, { mapRoadMarkWeight(it) })
    abstract fun mapRoadMarkWeight(source: ERoadMarkWeight): E_RoadMarkWeight

    fun mapOptionRoadLanesLaneSectionLCRLaneRoadMarkLaneChange(source: Option<ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange>): E_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_LaneChange? =
        source.fold({ null }, { mapRoadLanesLaneSectionLCRLaneRoadMarkLaneChange(it) })
    abstract fun mapRoadLanesLaneSectionLCRLaneRoadMarkLaneChange(source: ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange): E_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_LaneChange

    fun mapOptionRoadLanesLaneSectionLCRLaneRoadMarkExplicit(source: Option<RoadLanesLaneSectionLCRLaneRoadMarkExplicit>): T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Explicit? =
        source.fold({ null }, { mapRoadLanesLaneSectionLCRLaneRoadMarkExplicit(it) })
    abstract fun mapRoadLanesLaneSectionLCRLaneRoadMarkExplicit(source: RoadLanesLaneSectionLCRLaneRoadMarkExplicit): T_Road_Lanes_LaneSection_Lcr_Lane_RoadMark_Explicit

    @ValueMapping(source = "SOLID_SOLID", target = "SOLID___SOLID")
    @ValueMapping(source = "SOLID_BROKEN", target = "SOLID___BROKEN")
    @ValueMapping(source = "BROKEN_SOLID", target = "BROKEN___SOLID")
    @ValueMapping(source = "BROKEN_BROKEN", target = "BROKEN___BROKEN")
    @ValueMapping(source = "BOTTS_DOTS", target = "BOTTS___DOTS")
    abstract fun mapRoadMarkType(source: ERoadMarkType): E_RoadMarkType

    fun mapOptionRoadMarkRule(source: Option<ERoadMarkRule>): E_RoadMarkRule? = source.fold({ null }, { mapRoadMarkRule(it) })

    @ValueMapping(source = "NO_PASSING", target = "NO___PASSING")
    abstract fun mapRoadMarkRule(source: ERoadMarkRule): E_RoadMarkRule

    fun mapOptionRoadMarkColor(source: Option<ERoadMarkColor>): E_RoadMarkColor? = source.fold({ null }, { mapRoadMarkColor(it) })
    abstract fun mapRoadMarkColor(source: ERoadMarkColor): E_RoadMarkColor
}
