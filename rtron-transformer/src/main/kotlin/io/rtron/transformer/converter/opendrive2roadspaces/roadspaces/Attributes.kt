/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.converter.opendrive2roadspaces.roadspaces

import io.rtron.model.opendrive.core.EUnitSpeed
import io.rtron.model.opendrive.lane.ELaneType
import io.rtron.model.opendrive.lane.ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange
import io.rtron.model.opendrive.lane.ERoadMarkType
import io.rtron.model.roadspaces.roadspace.attribute.UnitOfMeasure
import io.rtron.model.roadspaces.roadspace.road.LaneChange
import io.rtron.model.roadspaces.roadspace.road.LaneType
import io.rtron.model.roadspaces.roadspace.road.RoadMarkType

/**
 * Transforms units of the OpenDRIVE data model to units of the RoadSpaces data model.
 */
fun EUnitSpeed.toUnitOfMeasure(): UnitOfMeasure =
    when (this) {
        EUnitSpeed.METER_PER_SECOND -> UnitOfMeasure.METER_PER_SECOND
        EUnitSpeed.MILES_PER_HOUR -> UnitOfMeasure.MILES_PER_HOUR
        EUnitSpeed.KILOMETER_PER_HOUR -> UnitOfMeasure.KILOMETER_PER_HOUR
    }

/**
 * Transforms lane types of the OpenDRIVE data model to the lane types of the RoadSpaces data model.
 */
fun ELaneType.toLaneType(): LaneType =
    when (this) {
        ELaneType.BIKING -> LaneType.BIKING
        ELaneType.BORDER -> LaneType.BORDER
        ELaneType.CONNECTING_RAMP -> LaneType.CONNECTING_RAMP
        ELaneType.CURB -> LaneType.CURB
        ELaneType.DRIVING -> LaneType.DRIVING
        ELaneType.ENTRY -> LaneType.ENTRY
        ELaneType.EXIT -> LaneType.EXIT
        ELaneType.MEDIAN -> LaneType.MEDIAN
        ELaneType.NONE -> LaneType.NONE
        ELaneType.OFF_RAMP -> LaneType.OFF_RAMP
        ELaneType.ON_RAMP -> LaneType.ON_RAMP
        ELaneType.PARKING -> LaneType.PARKING
        ELaneType.RAIL -> LaneType.RAIL
        ELaneType.RESTRICTED -> LaneType.RESTRICTED
        ELaneType.SHARED -> LaneType.SHARED
        ELaneType.SHOULDER -> LaneType.SHOULDER
        ELaneType.SLIP_LANE -> LaneType.SLIP_LANE
        ELaneType.STOP -> LaneType.STOP
        ELaneType.TRAM -> LaneType.TRAM
        ELaneType.WALKING -> LaneType.WALKING
    }

/**
 * Transforms road mark types of the OpenDRIVE data model to the road mark types of the RoadSpaces data model.
 */
fun ERoadMarkType.toRoadMarkType(): RoadMarkType =
    when (this) {
        ERoadMarkType.BOTTS_DOTS -> RoadMarkType.BOTTS_DOTS
        ERoadMarkType.BROKEN -> RoadMarkType.BROKEN
        ERoadMarkType.BROKEN_BROKEN -> RoadMarkType.BROKEN_BROKEN
        ERoadMarkType.BROKEN_SOLID -> RoadMarkType.BROKEN_SOLID
        ERoadMarkType.CURB -> RoadMarkType.CURB
        ERoadMarkType.CUSTOM -> RoadMarkType.CUSTOM
        ERoadMarkType.EDGE -> RoadMarkType.EDGE
        ERoadMarkType.GRASS -> RoadMarkType.GRASS
        ERoadMarkType.NONE -> RoadMarkType.NONE
        ERoadMarkType.SOLID -> RoadMarkType.SOLID
        ERoadMarkType.SOLID_BROKEN -> RoadMarkType.SOLID_BROKEN
        ERoadMarkType.SOLID_SOLID -> RoadMarkType.SOLID_SOLID
    }

/**
 * Transforms lane change types of the OpenDRIVE data model to the lane change types of the RoadSpaces data model.
 */
fun ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.toLaneChange(): LaneChange =
    when (this) {
        ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.BOTH -> LaneChange.BOTH
        ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.DECREASE -> LaneChange.DECREASE
        ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.INCREASE -> LaneChange.INCREASE
        ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.NONE -> LaneChange.NONE
    }
