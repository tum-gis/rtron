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

package io.rtron.transformer.converter.roadspaces2citygml.module

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.model.citygml.code.AuxiliaryTrafficAreaFunctionCode
import io.rtron.model.citygml.code.TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode
import io.rtron.model.citygml.code.TrafficAreaFunctionCode
import io.rtron.model.citygml.code.TrafficAreaUsageCode
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.road.LaneMaterial
import io.rtron.model.roadspaces.roadspace.road.LaneType

object CodeAdder {

    // Methods

    /**
     * Returns the [TrafficAreaFunctionCode] list of a `TrafficArea` from the [laneType] of a lane.
     */
    fun mapToTrafficAreaFunctionCodes(laneType: LaneType): List<TrafficAreaFunctionCode> =
        when (laneType) {
            LaneType.NONE -> emptyList()
            LaneType.DRIVING -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.STOP -> emptyList()
            LaneType.SHOULDER -> emptyList()
            LaneType.BIKING -> listOf(TrafficAreaFunctionCode.CYCLEPATH)
            LaneType.SIDEWALK -> listOf(TrafficAreaFunctionCode.FOOTPATH)
            LaneType.BORDER -> emptyList()
            LaneType.RESTRICTED -> emptyList()
            LaneType.PARKING -> listOf(TrafficAreaFunctionCode.PARKING_LAY_BY)
            LaneType.CURB -> emptyList()
            LaneType.BIDIRECTIONAL -> emptyList()
            LaneType.MEDIAN -> emptyList()
            LaneType.SPECIAL_1 -> emptyList()
            LaneType.SPECIAL_2 -> emptyList()
            LaneType.SPECIAL_3 -> emptyList()
            LaneType.ROAD_WORKS -> emptyList()
            LaneType.TRAM -> emptyList()
            LaneType.RAIL -> listOf(TrafficAreaFunctionCode.RAIL)
            LaneType.ENTRY -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.EXIT -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.OFF_RAMP -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.ON_RAMP -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.CONNECTING_RAMP -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.BUS -> emptyList()
            LaneType.TAXI -> emptyList()
            LaneType.HOV -> emptyList()
            LaneType.MWY_ENTRY -> listOf(TrafficAreaFunctionCode.MOTORWAY_ENTRY, TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.MWY_EXIT -> listOf(TrafficAreaFunctionCode.MOTORWAY_EXIT, TrafficAreaFunctionCode.DRIVING_LANE)
        }

    /**
     * Returns the [AuxiliaryTrafficAreaFunctionCode] list of a `AuxiliaryTrafficArea` from the [laneType] of a lane.
     */
    fun mapToAuxiliaryTrafficAreaFunctionCodes(laneType: LaneType): List<AuxiliaryTrafficAreaFunctionCode> =
        when (laneType) {
            LaneType.NONE -> emptyList()
            LaneType.DRIVING -> emptyList()
            LaneType.STOP -> emptyList()
            LaneType.SHOULDER -> emptyList()
            LaneType.BIKING -> emptyList()
            LaneType.SIDEWALK -> emptyList()
            LaneType.BORDER -> emptyList()
            LaneType.RESTRICTED -> emptyList()
            LaneType.CURB -> emptyList()
            LaneType.PARKING -> listOf(AuxiliaryTrafficAreaFunctionCode.PARKING_BAY)
            LaneType.BIDIRECTIONAL -> emptyList()
            LaneType.MEDIAN -> emptyList()
            LaneType.SPECIAL_1 -> emptyList()
            LaneType.SPECIAL_2 -> emptyList()
            LaneType.SPECIAL_3 -> emptyList()
            LaneType.ROAD_WORKS -> emptyList()
            LaneType.TRAM -> emptyList()
            LaneType.RAIL -> emptyList()
            LaneType.ENTRY -> emptyList()
            LaneType.EXIT -> emptyList()
            LaneType.OFF_RAMP -> emptyList()
            LaneType.ON_RAMP -> emptyList()
            LaneType.CONNECTING_RAMP -> emptyList()
            LaneType.BUS -> emptyList()
            LaneType.TAXI -> emptyList()
            LaneType.HOV -> emptyList()
            LaneType.MWY_ENTRY -> emptyList()
            LaneType.MWY_EXIT -> emptyList()
        }

    /**
     * Returns the [TrafficAreaUsageCode] list for a `TrafficArea` mapped from the [laneType] of a lane.
     */
    fun mapToTrafficAreaUsageCodes(laneType: LaneType): List<TrafficAreaUsageCode> =
        when (laneType) {
            LaneType.NONE -> emptyList()
            LaneType.DRIVING -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.STOP -> emptyList()
            LaneType.SHOULDER -> emptyList()
            LaneType.BIKING -> listOf(TrafficAreaUsageCode.BICYCLE)
            LaneType.SIDEWALK -> listOf(TrafficAreaUsageCode.PEDESTRIAN)
            LaneType.BORDER -> emptyList()
            LaneType.RESTRICTED -> emptyList()
            LaneType.CURB -> emptyList()
            LaneType.PARKING -> emptyList()
            LaneType.BIDIRECTIONAL -> emptyList()
            LaneType.MEDIAN -> emptyList()
            LaneType.SPECIAL_1 -> emptyList()
            LaneType.SPECIAL_2 -> emptyList()
            LaneType.SPECIAL_3 -> emptyList()
            LaneType.ROAD_WORKS -> emptyList()
            LaneType.TRAM -> emptyList()
            LaneType.RAIL -> emptyList()
            LaneType.ENTRY -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.EXIT -> emptyList()
            LaneType.OFF_RAMP -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.ON_RAMP -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.CONNECTING_RAMP -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.BUS -> listOf(TrafficAreaUsageCode.BUS_TAXI)
            LaneType.TAXI -> listOf(TrafficAreaUsageCode.TAXI)
            LaneType.HOV -> emptyList()
            LaneType.MWY_ENTRY -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.MWY_EXIT -> listOf(TrafficAreaUsageCode.CAR)
        }

    fun mapToTrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode(laneMaterial: LaneMaterial): Option<TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode> =
        when (laneMaterial.surface.uppercase()) {
            in "ASPHALT" -> TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode.ASPHALT.some()
            in "GRASS" -> TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode.GRASS.some()
            in "SPEC_CONCRETE_3D" -> TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode.GRASS.some()
            else -> None
        }

    fun mapToTrafficAreaFunctionCodes(roadObjectType: RoadObjectType): List<TrafficAreaFunctionCode> =
        when (roadObjectType) {
            RoadObjectType.NONE -> emptyList()
            RoadObjectType.OBSTACLE -> emptyList()
            RoadObjectType.POLE -> emptyList()
            RoadObjectType.TREE -> emptyList()
            RoadObjectType.VEGETATION -> emptyList()
            RoadObjectType.BARRIER -> emptyList()
            RoadObjectType.BUILDING -> emptyList()
            RoadObjectType.PARKING_SPACE -> listOf(TrafficAreaFunctionCode.PARKING_LAY_BY)
            RoadObjectType.PATCH -> emptyList()
            RoadObjectType.RAILING -> emptyList()
            RoadObjectType.TRAFFIC_ISLAND -> emptyList()
            RoadObjectType.CROSSWALK -> listOf(TrafficAreaFunctionCode.CROSSWALK)
            RoadObjectType.STREET_LAMP -> emptyList()
            RoadObjectType.GANTRY -> emptyList()
            RoadObjectType.SOUND_BARRIER -> emptyList()
            RoadObjectType.ROAD_MARK -> emptyList()
            RoadObjectType.SIGNAL -> emptyList()
        }

    fun mapToAuxiliaryTrafficAreaFunctionCodes(roadObjectType: RoadObjectType): List<AuxiliaryTrafficAreaFunctionCode> =
        when (roadObjectType) {
            RoadObjectType.NONE -> emptyList()
            RoadObjectType.OBSTACLE -> emptyList()
            RoadObjectType.POLE -> emptyList()
            RoadObjectType.TREE -> emptyList()
            RoadObjectType.VEGETATION -> emptyList()
            RoadObjectType.BARRIER -> emptyList()
            RoadObjectType.BUILDING -> emptyList()
            RoadObjectType.PARKING_SPACE -> listOf(AuxiliaryTrafficAreaFunctionCode.PARKING_BAY)
            RoadObjectType.PATCH -> emptyList()
            RoadObjectType.RAILING -> emptyList()
            RoadObjectType.TRAFFIC_ISLAND -> listOf(AuxiliaryTrafficAreaFunctionCode.TRAFFIC_ISLAND)
            RoadObjectType.CROSSWALK -> emptyList()
            RoadObjectType.STREET_LAMP -> emptyList()
            RoadObjectType.GANTRY -> emptyList()
            RoadObjectType.SOUND_BARRIER -> emptyList()
            RoadObjectType.ROAD_MARK -> emptyList()
            RoadObjectType.SIGNAL -> emptyList()
        }

    fun mapToTrafficAreaUsageCodes(roadObjectType: RoadObjectType): List<TrafficAreaUsageCode> =
        when (roadObjectType) {
            RoadObjectType.NONE -> emptyList()
            RoadObjectType.OBSTACLE -> emptyList()
            RoadObjectType.POLE -> emptyList()
            RoadObjectType.TREE -> emptyList()
            RoadObjectType.VEGETATION -> emptyList()
            RoadObjectType.BARRIER -> emptyList()
            RoadObjectType.BUILDING -> emptyList()
            RoadObjectType.PARKING_SPACE -> listOf(TrafficAreaUsageCode.CAR)
            RoadObjectType.PATCH -> emptyList()
            RoadObjectType.RAILING -> emptyList()
            RoadObjectType.TRAFFIC_ISLAND -> emptyList()
            RoadObjectType.CROSSWALK -> listOf(TrafficAreaUsageCode.PEDESTRIAN)
            RoadObjectType.STREET_LAMP -> emptyList()
            RoadObjectType.GANTRY -> emptyList()
            RoadObjectType.SOUND_BARRIER -> emptyList()
            RoadObjectType.ROAD_MARK -> emptyList()
            RoadObjectType.SIGNAL -> emptyList()
        }
}
