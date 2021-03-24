/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.model.opendrive.common

import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.std.PI

/**
 * Basic rule for using the road
 */
enum class ETrafficRule { RIGHTHANDTRAFFIC, LEFTHANDTRAFFIC }

/**
 * Access definitions for the parking space (assuming that "women" and "handicapped" will drive vehicles of type
 * "car" only)
 */
enum class ERoadObjectsObjectParkingSpaceAccess {
    ALL, CAR, WOMAN, HANDICAPPED, BUS, TRUCK, ELECTRIC, RESIDENTS,
    /** [UNKNOWN] was added for indicating not set values. */
    UNKNOWN
}

// unit enumerations

// 2.2 Units
enum class EUnitDistance { METER, KILOMETER, FEET, MILE }
enum class EUnitSpeed {
    METER_PER_SECOND, MILES_PER_HOUR, KILOMETER_PER_HOUR,
    /** [UNKNOWN] was added for indicating not set values. */
    UNKNOWN
}

enum class EUnitMass { KILOGRAM, TON }
enum class EUnitSlope { PERCENT }
enum class EUnit {
    METER, KILOMETER, FEET, MILE, // EUnitDistance
    METER_PER_SECOND, MILES_PER_HOUR, KILOMETER_PER_HOUR, // EUnitSpeed
    KILOGRAM, TON, // EUnitMass
    PERCENT, // EUnit
    /** [UNKNOWN] was added for indicating not set values. */
    UNKNOWN
}

// OpenDRIVE constant enumerations

// 6.1 Road Type Information
enum class ERoadType {
    UNKNOWN, RURAL, MOTORWAY, TOWN, LOW_SPEED, PEDESTRIAN, BICYCLE, TOWN_EXPRESSWAY, TOWN_COLLECTOR,
    TOWN_ARTERIAL, TOWN_PRIVATE, TOWN_LOCAL, TOWN_PLAY_STREET
}

// 6.2 Road Mark Type Information
enum class ERoadMarkType {
    NONE, SOLID, BROKEN, SOLID_SOLID, SOLID_BROKEN, BROKEN_SOLID, BROKEN_BROKEN,
    BOTTS_DOTS, GRASS, CURB, CUSTOM, EDGE
}

// 6.3 Road Mark Weight Information
enum class ERoadMarkWeight { STANDARD, BOLD }

// 6.4 Road Mark Color Information
enum class ERoadMarkColor { STANDARD, BLUE, GREEN, RED, WHITE, YELLOW, ORANGE }

// 6.5 Lane Type Information
enum class ELaneType {
    NONE, DRIVING, STOP, SHOULDER, BIKING, SIDEWALK, BORDER, RESTRICTED, PARKING, BIDIRECTIONAL,
    MEDIAN, SPECIAL_1, SPECIAL_2, SPECIAL_3, ROAD_WORKS, TRAM, RAIL, ENTRY, EXIT, OFF_RAMP, ON_RAMP, CONNECTING_RAMP,
    BUS, TAXI, HOV, MWY_ENTRY, MWY_EXIT
}

// 6.6 Object Types
enum class EObjectType {
    NONE, OBSTACLE, POLE, TREE, VEGETATION, BARRIER, BUILDING, PARKING_SPACE, PATCH,
    RAILING, TRAFFIC_ISLAND, CROSSWALK, STREET_LAMP, GANTRY, SOUND_BARRIER, ROAD_MARK
}
// deprecated: CAR, VAN, BUS, TRAILER, BIKE, MOTORBIKE, TRAM, TRAIN, PEDESTRIAN, WIND

// 6.7 Tunnel Types
enum class ETunnelType { STANDARD, UNDERPASS }

// 6.8 Bridge Types
enum class EBridgeType { CONCRETE, STEEL, BRICK, WOOD }

// 6.9 Access Restriction Types
enum class EAccessRestrictionType {
    SIMULATOR, AUTONOMOUS_TRAFFIC, PEDESTRIAN, PASSENGER_CAR, BUS, DELIVERY, EMERGENCY,
    TAXI, THROUGH_TRAFFIC, TRUCK, BICYCLE, MOTORCYCLE, NONE, TRUCKS
}

// 6.10 Country Codes
// TODO e_countryCode
enum class ECountryCodeDeprecated { OPENDRIVE, AUSTRIA, BRAZIL, CHINA, FRANCE, GERMANY, ITALY, SWITZERLAND, USA }
// TODO e_countryCode_iso3166alpha3

// 6.12 Side Types (new in OpenDRIVE 1.5M)
enum class ESideType {
    LEFT, RIGHT, FRONT, REAR,
    /** [UNKNOWN] was added for indicating not set values. */
    UNKNOWN
}

/**
 * Type used to fill the area inside the outline (6.13 Outline Fill Types (new in OpenDRIVE 1.5M)).
 */
enum class EOutlineFillType {
    GRASS, CONCRETE, COBBLE, ASPHALT, PAVEMENT, GRAVEL, SOIL,
    /** [UNKNOWN] was added for indicating not set values. */
    UNKNOWN
}

// 6.14 Border Types (new in OpenDRIVE 1.5M)
enum class EBorderType {
    CONCRETE, CURB,
    /** [UNKNOWN] was added for indicating not set values. */
    UNKNOWN
}

// other enumerations
enum class EContactPoint { START, END, UNKNOWN }
enum class EElementDir { PLUS, MINUS, UNKNOWN }
enum class EDirection { SAME, OPPOSITE, UNKNOWN }

/**
 * Rule which is to be observed when passing the line from inside (i.e. from the lane with the lower absolute ID to
 * the lane with the higher absolute ID)
 */
enum class ERoadMarkRule { NOPASSING, CAUTION, NONE }
enum class EOrientation {
    /** valid in positive track direction */
    PLUS,
    /** valid in negative track direction */
    MINUS,
    /** valid in both directions */
    NONE
}

fun EOrientation.toRotation2D() = when (this) {
    EOrientation.MINUS -> Rotation2D(PI)
    else -> Rotation2D(0.0)
}
