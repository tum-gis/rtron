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

package io.rtron.transformer.converter.roadspaces2citygml.module

import arrow.core.Option
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.road.LaneType
import org.xmlobjects.gml.model.basictypes.Code

object CodeAdder {

    // Methods

    /**
     * Returns the classifier code of a `TrafficArea` from the [laneType] of a lane.
     */
    fun mapToTrafficAreaClassCode(laneType: LaneType): Code {

        return when (laneType) {
            LaneType.NONE -> CODE_UNKNOWN
            LaneType.DRIVING -> Code("2")
            LaneType.STOP -> CODE_UNKNOWN
            LaneType.SHOULDER -> CODE_UNKNOWN
            LaneType.BIKING -> Code("6")
            LaneType.SIDEWALK -> Code("1")
            LaneType.BORDER -> CODE_UNKNOWN
            LaneType.RESTRICTED -> CODE_UNKNOWN
            LaneType.PARKING -> CODE_UNKNOWN
            LaneType.BIDIRECTIONAL -> CODE_UNKNOWN
            LaneType.MEDIAN -> CODE_UNKNOWN
            LaneType.SPECIAL_1 -> CODE_UNKNOWN
            LaneType.SPECIAL_2 -> CODE_UNKNOWN
            LaneType.SPECIAL_3 -> CODE_UNKNOWN
            LaneType.ROAD_WORKS -> CODE_UNKNOWN
            LaneType.TRAM -> CODE_UNKNOWN
            LaneType.RAIL -> CODE_UNKNOWN
            LaneType.ENTRY -> CODE_UNKNOWN
            LaneType.EXIT -> CODE_UNKNOWN
            LaneType.OFF_RAMP -> CODE_UNKNOWN
            LaneType.ON_RAMP -> CODE_UNKNOWN
            LaneType.CONNECTING_RAMP -> CODE_UNKNOWN
            LaneType.BUS -> CODE_UNKNOWN
            LaneType.TAXI -> CODE_UNKNOWN
            LaneType.HOV -> CODE_UNKNOWN
            LaneType.MWY_ENTRY -> CODE_UNKNOWN
            LaneType.MWY_EXIT -> CODE_UNKNOWN
        }
    }

    /**
     * Returns the classifier code of a `TrafficArea` from the [roadObjectType] and [roadObjectName] of a road
     * space object.
     */
    fun mapToTrafficAreaClassCode(roadObjectType: RoadObjectType, roadObjectName: Option<String>): Code {

        return when (roadObjectType) {
            RoadObjectType.NONE -> CODE_UNKNOWN
            RoadObjectType.OBSTACLE -> CODE_UNKNOWN
            RoadObjectType.POLE -> CODE_UNKNOWN
            RoadObjectType.TREE -> CODE_UNKNOWN
            RoadObjectType.VEGETATION -> throw IllegalStateException("Must not be mapped onto Transportation module")
            RoadObjectType.BARRIER -> CODE_UNKNOWN
            RoadObjectType.BUILDING -> throw IllegalStateException("Must not be mapped onto Transportation module")
            RoadObjectType.PARKING_SPACE -> CODE_UNKNOWN
            RoadObjectType.PATCH -> CODE_UNKNOWN
            RoadObjectType.RAILING -> CODE_UNKNOWN
            RoadObjectType.TRAFFIC_ISLAND -> Code("1300")
            RoadObjectType.CROSSWALK -> Code("20")
            RoadObjectType.STREET_LAMP -> CODE_UNKNOWN
            RoadObjectType.GANTRY -> CODE_UNKNOWN
            RoadObjectType.SOUND_BARRIER -> CODE_UNKNOWN
            RoadObjectType.ROAD_MARK -> CODE_UNKNOWN
            RoadObjectType.SIGNAL -> CODE_UNKNOWN
        }
    }

    /**
     * Returns the usage code list of a `TrafficArea` from the [laneType] of a lane.
     */
    fun mapToTrafficAreaUsageCode(laneType: LaneType): List<Code> {

        return when (laneType) {
            LaneType.NONE -> emptyList()
            LaneType.DRIVING -> listOf(Code("2"))
            LaneType.STOP -> emptyList()
            LaneType.SHOULDER -> emptyList()
            LaneType.BIKING -> listOf(Code("6"))
            LaneType.SIDEWALK -> listOf(Code("1"))
            LaneType.BORDER -> emptyList()
            LaneType.RESTRICTED -> emptyList()
            LaneType.PARKING -> emptyList()
            LaneType.BIDIRECTIONAL -> emptyList()
            LaneType.MEDIAN -> emptyList()
            LaneType.SPECIAL_1 -> emptyList()
            LaneType.SPECIAL_2 -> emptyList()
            LaneType.SPECIAL_3 -> emptyList()
            LaneType.ROAD_WORKS -> emptyList()
            LaneType.TRAM -> emptyList()
            LaneType.RAIL -> emptyList()
            LaneType.ENTRY -> emptyList()
            LaneType.EXIT -> emptyList()
            LaneType.OFF_RAMP -> emptyList()
            LaneType.ON_RAMP -> emptyList()
            LaneType.CONNECTING_RAMP -> emptyList()
            LaneType.BUS -> emptyList()
            LaneType.TAXI -> emptyList()
            LaneType.HOV -> emptyList()
            LaneType.MWY_ENTRY -> emptyList()
            LaneType.MWY_EXIT -> emptyList()
        }
    }

    /**
     * Returns the function code list of a `TrafficArea` from the [laneType] of a lane.
     */
    fun mapToTrafficAreaFunctionCode(laneType: LaneType): List<Code> {
        return when (laneType) {
            LaneType.NONE -> emptyList()
            LaneType.DRIVING -> listOf(Code("1"))
            LaneType.STOP -> emptyList()
            LaneType.SHOULDER -> emptyList()
            LaneType.BIKING -> listOf(Code("2"))
            LaneType.SIDEWALK -> emptyList()
            LaneType.BORDER -> emptyList()
            LaneType.RESTRICTED -> emptyList()
            LaneType.PARKING -> emptyList()
            LaneType.BIDIRECTIONAL -> emptyList()
            LaneType.MEDIAN -> emptyList()
            LaneType.SPECIAL_1 -> emptyList()
            LaneType.SPECIAL_2 -> emptyList()
            LaneType.SPECIAL_3 -> emptyList()
            LaneType.ROAD_WORKS -> emptyList()
            LaneType.TRAM -> emptyList()
            LaneType.RAIL -> emptyList()
            LaneType.ENTRY -> emptyList()
            LaneType.EXIT -> emptyList()
            LaneType.OFF_RAMP -> emptyList()
            LaneType.ON_RAMP -> emptyList()
            LaneType.CONNECTING_RAMP -> emptyList()
            LaneType.BUS -> emptyList()
            LaneType.TAXI -> emptyList()
            LaneType.HOV -> emptyList()
            LaneType.MWY_ENTRY -> emptyList()
            LaneType.MWY_EXIT -> emptyList()
        }
    }

    val CODE_UNKNOWN = Code("9999")
}
