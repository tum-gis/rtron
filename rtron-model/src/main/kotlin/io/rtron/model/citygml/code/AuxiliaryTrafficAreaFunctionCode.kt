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

package io.rtron.model.citygml.code

import org.xmlobjects.gml.model.basictypes.Code

enum class AuxiliaryTrafficAreaFunctionCode(val code: Code) {
    SOFT_SHOULDER(Code("1000")),
    HARD_SHOULDER(Code("1010")),
    GREEN_AREA(Code("1020")),
    MIDDLE_LANE(Code("1030")),
    LAY_BY(Code("1040")),
    PARKING_BAY(Code("1100")),
    DITCH(Code("1200")),
    DRAINAGE(Code("1210")),
    KERBSTONE(Code("1220")),
    FLOWER_TUB(Code("1230")),
    TRAFFIC_ISLAND(Code("1300")),
    BANK(Code("1400")),
    EMBANKMENT_DIKE(Code("1410")),
    RAILROAD_EMBANKMENT(Code("1420")),
    NOISE_PROTECTION(Code("1430")),
    NOISE_PROTECTION_WALL(Code("1440")),
    NOISE_GUARD_BAR(Code("1500")),
    TOWPATH(Code("1600")),
    OTHERS(Code("1700"))
}
