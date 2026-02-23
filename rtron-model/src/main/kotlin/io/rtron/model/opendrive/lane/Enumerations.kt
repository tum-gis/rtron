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

package io.rtron.model.opendrive.lane

enum class EAccessRestrictionType {
    AUTONOMOUS_TRAFFIC,
    BICYCLE,
    BUS,
    DELIVERY,
    EMERGENCY,
    HOV,
    MOTORCYCLE,
    NONE,
    PASSENGER_CAR,
    PEDESTRIAN,
    SIMULATOR,
    TAXI,
    THROUGH_TRAFFIC,
    TRUCK,
    TRUCKS,
}

enum class ELaneType {
    BIKING,
    BORDER,
    CONNECTING_RAMP,
    CURB,
    DRIVING,
    ENTRY,
    EXIT,
    MEDIAN,
    NONE,
    OFF_RAMP,
    ON_RAMP,
    PARKING,
    RAIL,
    RESTRICTED,
    SHARED,
    SHOULDER,
    SLIP_LANE,
    STOP,
    TRAM,
    WALKING,
}

enum class ELaneDirection {
    BOTH,
    REVERSED,
    STANDARD,
}

enum class ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange {
    BOTH,
    DECREASE,
    INCREASE,
    NONE,
}

enum class ERoadLanesLaneSectionLRLaneAccessRule {
    ALLOW,
    DENY,
}

enum class ERoadMarkColor {
    BLACK,
    BLUE,
    GREEN,
    ORANGE,
    RED,
    STANDARD,
    VIOLET,
    WHITE,
    YELLOW,
}

enum class ERoadMarkRule {
    NO_PASSING,
    CAUTION,
    NONE,
}

enum class ERoadMarkType {
    BOTTS_DOTS,
    BROKEN,
    BROKEN_BROKEN,
    BROKEN_SOLID,
    CURB,
    CUSTOM,
    EDGE,
    GRASS,
    NONE,
    SOLID,
    SOLID_BROKEN,
    SOLID_SOLID,
}

enum class ERoadMarkWeight {
    BOLD,
    STANDARD,
}
