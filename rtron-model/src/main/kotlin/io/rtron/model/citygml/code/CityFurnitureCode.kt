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

package io.rtron.model.citygml.code

import org.xmlobjects.gml.model.basictypes.Code

enum class CityFurnitureClassCode(
    val code: Code,
) {
    TRAFFIC(Code("1000")),
    COMMUNICATION(Code("1010")),
    SECURITY(Code("1020")),
    OTHERS(Code("1030")),
}

enum class CityFurnitureFunctionCode(
    val code: Code,
) {
    COMMUNICATION_FIXTURE(Code("1000")),
    TELEPHONE_BOX(Code("1010")),
    POSTBOX(Code("1020")),
    EMERGENCY_CALL_FIXTURE(Code("1030")),
    FIRE_DETECTOR(Code("1040")),
    POLICE_CALL_POST(Code("1050")),
    SWITCHING_UNIT(Code("1060")),
    ROAD_SIGN(Code("1070")),
    TRAFFIC_LIGHT(Code("1080")),
    FREE_STANDING_SIGN(Code("1090")),
    FREE_STANDING_WARNING_SIGN(Code("1100")),
    BUS_STOP(Code("1110")),
    MILESTONE(Code("1120")),
    RAIL_LEVEL_CROSSING(Code("1130")),
    GATE(Code("1140")),
    STREETLAMP_LANTERN_OR_CANDELABRA(Code("1150")),
    COLUMN(Code("1160")),
    LAMP_POST(Code("1170")),
    FLAGPOLE(Code("1180")),
    STREET_SINK_BOX(Code("1190")),
    RUBBISH_BIN(Code("1200")),
    CLOCK(Code("1210")),
    DIRECTIONAL_SPOT_LIGHT(Code("1220")),
    FLOODLIGHT_MAST(Code("1230")),
    WINDMILL(Code("1240")),
    SOLAR_CELL(Code("1250")),
    WATER_WHEEL(Code("1260")),
    POLE(Code("1270")),
    RADIO_MAST(Code("1280")),
    AERIAL(Code("1290")),
    RADIO_TELESCOPE(Code("1300")),
    CHIMNEY(Code("1310")),
    MARKER(Code("1320")),
    HYDRANT(Code("1330")),
    UPPER_CORRIDOR_FIRE_HYDRANT(Code("1340")),
    LOWER_FLOOR_PANEL_FIRE_HYDRANT(Code("1350")),
    SLIDEGATE_VALVE_CAP(Code("1360")),
    ENTRANCE_SHAFT(Code("1370")),
    CONVERTER(Code("1380")),
    STAIR(Code("1390")),
    OUTSIDE_STAIRCASE(Code("1400")),
    ESCALATOR(Code("1410")),
    RAMP(Code("1420")),
    PATIO(Code("1430")),
    FENCE(Code("1440")),
    MEMORIAL_MONUMENT(Code("1450")),
    WAYSIDE_SHRINE(Code("1470")),
    CROSSROADS(Code("1480")),
    CROSS_ON_THE_SUMMIT_OF_A_MOUNTAIN(Code("1490")),
    FOUNTAIN(Code("1500")),
    BLOCK_MARK(Code("1510")),
    BOUNDARY_POST(Code("1520")),
    BENCH(Code("1530")),
    OTHERS(Code("1540")),
}

enum class CityFurnitureUsageCode(
    val code: Code,
) {
    COMMUNICATION_FIXTURE(Code("1000")),
    TELEPHONE_BOX(Code("1010")),
    POSTBOX(Code("1020")),
    EMERGENCY_CALL_FIXTURE(Code("1030")),
    FIRE_DETECTOR(Code("1040")),
    POLICE_CALL_POST(Code("1050")),
    SWITCHING_UNIT(Code("1060")),
    ROAD_SIGN(Code("1070")),
    TRAFFIC_LIGHT(Code("1080")),
    FREE_STANDING_SIGN(Code("1090")),
    FREE_STANDING_WARNING_SIGN(Code("1100")),
    BUS_STOP(Code("1110")),
    MILESTONE(Code("1120")),
    RAIL_LEVEL_CROSSING(Code("1130")),
    GATE(Code("1140")),
    STREETLAMP_LANTERN_OR_CANDELABRA(Code("1150")),
    COLUMN(Code("1160")),
    LAMP_POST(Code("1170")),
    FLAGPOLE(Code("1180")),
    STREET_SINK_BOX(Code("1190")),
    RUBBISH_BIN(Code("1200")),
    CLOCK(Code("1210")),
    DIRECTIONAL_SPOT_LIGHT(Code("1220")),
    FLOODLIGHT_MAST(Code("1230")),
    WINDMILL(Code("1240")),
    SOLAR_CELL(Code("1250")),
    WATER_WHEEL(Code("1260")),
    POLE(Code("1270")),
    RADIO_MAST(Code("1280")),
    AERIAL(Code("1290")),
    RADIO_TELESCOPE(Code("1300")),
    CHIMNEY(Code("1310")),
    MARKER(Code("1320")),
    HYDRANT(Code("1330")),
    UPPER_CORRIDOR_FIRE_HYDRANT(Code("1340")),
    LOWER_FLOOR_PANEL_FIRE_HYDRANT(Code("1350")),
    SLIDEGATE_VALVE_CAP(Code("1360")),
    ENTRANCE_SHAFT(Code("1370")),
    CONVERTER(Code("1380")),
    STAIR(Code("1390")),
    OUTSIDE_STAIRCASE(Code("1400")),
    ESCALATOR(Code("1410")),
    RAMP(Code("1420")),
    PATIO(Code("1430")),
    FENCE(Code("1440")),
    MEMORIAL_MONUMENT(Code("1450")),
    WAYSIDE_SHRINE(Code("1470")),
    CROSSROADS(Code("1480")),
    CROSS_ON_THE_SUMMIT_OF_A_MOUNTAIN(Code("1490")),
    FOUNTAIN(Code("1500")),
    BLOCK_MARK(Code("1510")),
    BOUNDARY_POST(Code("1520")),
    BENCH(Code("1530")),
    OTHERS(Code("1540")),
}
