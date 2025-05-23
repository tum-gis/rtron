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

package io.rtron.model.roadspaces.roadspace.road

enum class LaneType {
    NONE,
    DRIVING,
    STOP,
    SHOULDER,
    BIKING,
    SHARED,
    SIDEWALK,
    BORDER,
    RESTRICTED,
    PARKING,
    CURB,
    BIDIRECTIONAL,
    MEDIAN,
    SPECIAL_1,
    SPECIAL_2,
    SPECIAL_3,
    ROAD_WORKS,
    TRAM,
    RAIL,
    ENTRY,
    EXIT,
    OFF_RAMP,
    ON_RAMP,
    CONNECTING_RAMP,
    BUS,
    TAXI,
    HOV,
    MWY_ENTRY,
    MWY_EXIT,
    WALKING,
    SLIP_LANE,
}
