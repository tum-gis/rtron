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

enum class EAccessRestrictionType {
    SIMULATOR, AUTONOMOUS_TRAFFIC, PEDESTRIAN, PASSENGER_CAR, BUS, DELIVERY, EMERGENCY,
    TAXI, THROUGH_TRAFFIC, TRUCK, BICYCLE, MOTORCYCLE, NONE, TRUCKS
}
enum class ELaneType {
    SHOULDER, BORDER, DRIVING, STOP, NONE, RESTRICTED, PARKING, MEDIAN, BIKING, SIDEWALK, CURB, EXIT, ENTRY, ON_RAMP,
    OFF_RAMP, CONNECTING_RAMP, BIDIRECTIONAL, SPECIAL_1, SPECIAL_2, SPECIAL_3, ROAD_WORKS, TRAM, RAIL, BUS, TAXI, HOV,
    MWY_ENTRY, MWY_EXIT
}

enum class ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange { INCREASE, DECREASE, BOTH, NONE }

enum class ERoadLanesLaneSectionLRLaneAccessRule { ALLOW, DENY }

enum class ERoadMarkColor { STANDARD, BLUE, GREEN, RED, WHITE, YELLOW, ORANGE }

enum class ERoadMarkRule { NO_PASSING, CAUTION, NONE }
enum class ERoadMarkType {
    NONE, SOLID, BROKEN, SOLID_SOLID, SOLID_BROKEN, BROKEN_SOLID, BROKEN_BROKEN,
    BOTTS_DOTS, GRASS, CURB, CUSTOM, EDGE
}

enum class ERoadMarkWeight { STANDARD, BOLD }
