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

package io.rtron.model.opendrive.objects

import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.std.PI

enum class EBorderType { CONCRETE, CURB }

enum class EBridgeType { CONCRETE, STEEL, BRICK, WOOD }

enum class EObjectType {
    NONE,
    OBSTACLE,
    POLE,
    TREE,
    VEGETATION,
    BARRIER,
    BUILDING,
    PARKING_SPACE,
    PATCH,
    RAILING,
    TRAFFIC_ISLAND,
    CROSSWALK,
    STREET_LAMP,
    GANTRY,
    SOUND_BARRIER,
    ROAD_MARK,
    ROAD_SURFACE,
}
// deprecated: CAR, VAN, BUS, TRAILER, BIKE, MOTORBIKE, TRAM, TRAIN, PEDESTRIAN, WIND

enum class EOrientation {
    /** valid in positive track direction */
    PLUS,

    /** valid in negative track direction */
    MINUS,

    /** valid in both directions */
    NONE,
}

fun EOrientation.toRotation2D() =
    when (this) {
        EOrientation.MINUS -> Rotation2D(PI)
        else -> Rotation2D(0.0)
    }

enum class EOutlineFillType { GRASS, CONCRETE, COBBLE, ASPHALT, PAVEMENT, GRAVEL, SOIL, PAINT }

enum class ERoadObjectsObjectParkingSpaceAccess { ALL, CAR, WOMEN, HANDICAPPED, BUS, TRUCK, ELECTRIC, RESIDENTS }

enum class ESideType { LEFT, RIGHT, FRONT, REAR }

enum class ETunnelType { STANDARD, UNDERPASS }
