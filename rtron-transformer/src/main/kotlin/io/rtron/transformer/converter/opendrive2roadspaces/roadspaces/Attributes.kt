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

package io.rtron.transformer.converter.opendrive2roadspaces.roadspaces

import io.rtron.model.opendrive.core.EUnitSpeed
import io.rtron.model.opendrive.lane.ELaneType
import io.rtron.model.roadspaces.roadspace.attribute.UnitOfMeasure
import io.rtron.model.roadspaces.roadspace.road.LaneType

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
        ELaneType.SHOULDER -> LaneType.SHOULDER
        ELaneType.BORDER -> LaneType.BORDER
        ELaneType.DRIVING -> LaneType.DRIVING
        ELaneType.STOP -> LaneType.STOP
        ELaneType.NONE -> LaneType.NONE
        ELaneType.RESTRICTED -> LaneType.RESTRICTED
        ELaneType.PARKING -> LaneType.PARKING
        ELaneType.MEDIAN -> LaneType.MEDIAN
        ELaneType.BIKING -> LaneType.BIKING
        ELaneType.SIDEWALK -> LaneType.SIDEWALK
        ELaneType.CURB -> LaneType.CURB
        ELaneType.BIDIRECTIONAL -> LaneType.BIDIRECTIONAL
        ELaneType.SPECIAL_1 -> LaneType.SPECIAL_1
        ELaneType.SPECIAL_2 -> LaneType.SPECIAL_2
        ELaneType.SPECIAL_3 -> LaneType.SPECIAL_3
        ELaneType.ROAD_WORKS -> LaneType.ROAD_WORKS
        ELaneType.TRAM -> LaneType.TRAM
        ELaneType.RAIL -> LaneType.RAIL
        ELaneType.ENTRY -> LaneType.ENTRY
        ELaneType.EXIT -> LaneType.EXIT
        ELaneType.OFF_RAMP -> LaneType.OFF_RAMP
        ELaneType.ON_RAMP -> LaneType.ON_RAMP
        ELaneType.CONNECTING_RAMP -> LaneType.CONNECTING_RAMP
        ELaneType.BUS -> LaneType.BUS
        ELaneType.TAXI -> LaneType.TAXI
        ELaneType.HOV -> LaneType.HOV
        ELaneType.MWY_ENTRY -> LaneType.MWY_ENTRY
        ELaneType.MWY_EXIT -> LaneType.MWY_EXIT
    }
