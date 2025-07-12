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

package io.rtron.model.opendrive.road

enum class EDirection { SAME, OPPOSITE }

enum class EMaxSpeedString { NO_LIMIT, UNDEFINED }

enum class ParamPoly3PRange { ARC_LENGTH, NORMALIZED }

enum class ERoadLinkElementType { ROAD, JUNCTION }

enum class ERoadType {
    UNKNOWN,
    RURAL,
    MOTORWAY,
    TOWN,
    LOW_SPEED,
    PEDESTRIAN,
    BICYCLE,
    TOWN_EXPRESSWAY,
    TOWN_COLLECTOR,
    TOWN_ARTERIAL,
    TOWN_PRIVATE,
    TOWN_LOCAL,
    TOWN_PLAY_STREET,
}

enum class ETrafficRule { RIGHT_HAND_TRAFFIC, LEFT_HAND_TRAFFIC }
