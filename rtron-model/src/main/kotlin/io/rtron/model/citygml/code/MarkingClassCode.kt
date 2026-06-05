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

enum class MarkingClassCode(
    val code: Code,
) {
    ROAD_MARKING(Code("11")),
    ROAD_MARKING_DIRECTION(Code("12")),
    ROAD_MARKING_LANE(Code("13")),
    ROAD_MARKING_RESTRICTED(Code("14")),
    ROAD_MARKING_CROSSWALK(Code("15")),
    ROAD_MARKING_STOP(Code("16")),
    ARROW_RIGHT(Code("121")),
    ARROW_LEFT(Code("122")),
    ARROW_STRAIGHT(Code("123")),
    ARROW_STRAIGHT_RIGHT(Code("124")),
    ARROW_STRAIGHT_LEFT(Code("125")),
    ROAD_MARKING_LANE_BROKEN(Code("131")),
    ROAD_MARKING_LANE_SOLID(Code("132")),
    SYMBOL_BICYCLE(Code("140")),
    SYMBOL_OTHER(Code("150")),
}
