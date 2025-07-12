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

package io.rtron.model.citygml.code

import org.xmlobjects.gml.model.basictypes.Code

enum class TrafficAreaFunctionCode(
    val code: Code,
) {
    DRIVING_LANE(Code("1")),
    FOOTPATH(Code("2")),
    CYCLEPATH(Code("3")),
    COMBINED_FOOT_CYCLEPATH(Code("4")),
    SQUARE(Code("5")),
    CAR_PARK(Code("6")),
    PARKING_LAY_BY(Code("7")),
    RAIL(Code("8")),
    RAIL_ROAD_COMBINED(Code("9")),
    DRAINAGE(Code("10")),
    ROAD_MARKING(Code("11")),
    ROAD_MARKING_DIRECTION(Code("12")),
    ROAD_MARKING_LANE(Code("13")),
    ROAD_MARKING_RESTRICTED(Code("14")),
    ROAD_MARKING_CROSSWALK(Code("15")),
    ROAD_MARKING_STOP(Code("16")),
    ROAD_MARKING_OTHER(Code("17")),
    OVERHEAD_WIRE_TROLLEY(Code("18")),
    TRAIN_PLATFORM(Code("19")),
    CROSSWALK(Code("20")),
    BARRIER(Code("21")),
    STAIRS(Code("22")),
    ESCALATOR(Code("23")),
    FILTERING_LANE(Code("24")),
    AIRPORT_RUNWAY(Code("25")),
    AIRPORT_TAXIWAY(Code("26")),
    AIRPORT_APRON(Code("27")),
    AIRPORT_HELIPORT(Code("28")),
    AIRPORT_RUNWAY_MARKING(Code("29")),
    GREEN_SPACES(Code("30")),
    RECREATION(Code("31")),
    BUS_LAY_BY(Code("32")),
    MOTORWAY(Code("33")),
    MOTORWAY_ENTRY(Code("34")),
    MOTORWAY_EXIT(Code("35")),
    MOTORWAY_EMERGENCY_LANE(Code("36")),
    PRIVATE_AREA(Code("37")),
    UNKNOWN(Code("9999")),
}

enum class TrafficAreaUsageCode(
    val code: Code,
) {
    PEDESTRIAN(Code("1")),
    CAR(Code("2")),
    TRUCK(Code("3")),
    BUS_TAXI(Code("4")),
    TRAIN(Code("5")),
    BICYCLE(Code("6")),
    MOTORCYCLE(Code("7")),
    TRAM_STREETCAR(Code("8")),
    BOAT_FERRY_SHIP(Code("9")),
    TELEFERIC(Code("10")),
    AEROPLANE(Code("11")),
    HELICOPTER(Code("12")),
    TAXI(Code("13")),
    HORSE(Code("14")),
    UNKNOWN(Code("9999")),
}

enum class TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode(
    val code: Code,
) {
    ASPHALT(Code("1")),
    CONCRETE(Code("2")),
    PAVEMENT(Code("3")),
    COBBLESTONE(Code("4")),
    GRAVEL(Code("5")),
    RAIL_WITH_BED(Code("6")),
    RAIL_WITHOUT_BED(Code("7")),
    SOIL(Code("8")),
    SAND(Code("9")),
    GRASS(Code("10")),
    WOOD(Code("11")),
    STEEL(Code("12")),
    MARBLE(Code("13")),
    UNKNOWN(Code("9999")),
}
