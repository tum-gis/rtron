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

package io.rtron.transformer.converter.roadspaces2citygml.module

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.model.citygml.code.AuxiliaryTrafficAreaFunctionCode
import io.rtron.model.citygml.code.TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode
import io.rtron.model.citygml.code.TrafficAreaFunctionCode
import io.rtron.model.citygml.code.TrafficAreaUsageCode
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.LaneAccessRule
import io.rtron.model.roadspaces.roadspace.road.LaneMaterial
import io.rtron.model.roadspaces.roadspace.road.LaneType
import io.rtron.model.roadspaces.roadspace.road.RestrictionType

object CodeAdder {
    // Methods

    fun deriveTrafficAreaUsageCodesFromLane(lane: Lane): HashSet<TrafficAreaUsageCode> {
        val usageCodes: HashSet<TrafficAreaUsageCode> = CodeAdder.mapToTrafficAreaUsageCodes(lane.type).toHashSet()
        usageCodes.addAll(
            lane.laneAccess
                .filter {
                    it.rule == LaneAccessRule.ALLOW
                }.flatMap { CodeAdder.mapToTrafficAreaUsageCodes(it.restrictionType) }
                .toSet(),
        )
        usageCodes.removeAll(
            lane.laneAccess
                .filter { it.rule == LaneAccessRule.DENY }
                .flatMap { CodeAdder.mapToTrafficAreaUsageCodes(it.restrictionType) }
                .toSet(),
        )

        return usageCodes
    }

    /**
     * Returns the [TrafficAreaFunctionCode] list of a `TrafficArea` from the [laneType] of a lane.
     */
    fun mapToTrafficAreaFunctionCodes(laneType: LaneType): List<TrafficAreaFunctionCode> =
        when (laneType) {
            LaneType.BIKING -> listOf(TrafficAreaFunctionCode.CYCLEPATH)
            LaneType.BORDER -> emptyList()
            LaneType.CONNECTING_RAMP -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.CURB -> emptyList()
            LaneType.DRIVING -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.ENTRY -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.EXIT -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.MEDIAN -> emptyList()
            LaneType.NONE -> emptyList()
            LaneType.OFF_RAMP -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.ON_RAMP -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.PARKING -> listOf(TrafficAreaFunctionCode.PARKING_LAY_BY)
            LaneType.RAIL -> listOf(TrafficAreaFunctionCode.RAIL)
            LaneType.RESTRICTED -> emptyList()
            LaneType.SHARED ->
                listOf(
                    TrafficAreaFunctionCode.DRIVING_LANE,
                    TrafficAreaFunctionCode.FOOTPATH,
                    TrafficAreaFunctionCode.CYCLEPATH,
                )
            LaneType.SHOULDER -> emptyList()
            LaneType.SLIP_LANE -> listOf(TrafficAreaFunctionCode.DRIVING_LANE)
            LaneType.STOP -> emptyList()
            LaneType.TRAM -> emptyList()
            LaneType.WALKING -> listOf(TrafficAreaFunctionCode.FOOTPATH)
        }

    /**
     * Returns the [AuxiliaryTrafficAreaFunctionCode] list of a `AuxiliaryTrafficArea` from the [laneType] of a lane.
     */
    fun mapToAuxiliaryTrafficAreaFunctionCodes(laneType: LaneType): List<AuxiliaryTrafficAreaFunctionCode> =
        when (laneType) {
            LaneType.BIKING -> emptyList()
            LaneType.BORDER -> emptyList()
            LaneType.CONNECTING_RAMP -> emptyList()
            LaneType.CURB -> emptyList()
            LaneType.DRIVING -> emptyList()
            LaneType.ENTRY -> emptyList()
            LaneType.EXIT -> emptyList()
            LaneType.MEDIAN -> emptyList()
            LaneType.NONE -> emptyList()
            LaneType.OFF_RAMP -> emptyList()
            LaneType.ON_RAMP -> emptyList()
            LaneType.PARKING -> listOf(AuxiliaryTrafficAreaFunctionCode.PARKING_BAY)
            LaneType.RAIL -> emptyList()
            LaneType.RESTRICTED -> emptyList()
            LaneType.SHARED -> emptyList()
            LaneType.SHOULDER -> emptyList()
            LaneType.SLIP_LANE -> emptyList()
            LaneType.STOP -> emptyList()
            LaneType.TRAM -> emptyList()
            LaneType.WALKING -> emptyList()
        }

    /**
     * Returns the [TrafficAreaUsageCode] list for a `TrafficArea` mapped from the [laneType] of a lane.
     */
    fun mapToTrafficAreaUsageCodes(laneType: LaneType): List<TrafficAreaUsageCode> =
        when (laneType) {
            LaneType.BIKING -> listOf(TrafficAreaUsageCode.BICYCLE)
            LaneType.BORDER -> emptyList()
            LaneType.CONNECTING_RAMP -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.CURB -> emptyList()
            LaneType.DRIVING -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.ENTRY -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.EXIT -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.MEDIAN -> emptyList()
            LaneType.NONE -> emptyList()
            LaneType.OFF_RAMP -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.ON_RAMP -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.PARKING -> emptyList()
            LaneType.RAIL -> emptyList()
            LaneType.RESTRICTED -> emptyList()
            LaneType.SHARED -> listOf(TrafficAreaUsageCode.CAR, TrafficAreaUsageCode.BICYCLE, TrafficAreaUsageCode.PEDESTRIAN)
            LaneType.SHOULDER -> emptyList()
            LaneType.SLIP_LANE -> listOf(TrafficAreaUsageCode.CAR)
            LaneType.STOP -> emptyList()
            LaneType.TRAM -> emptyList()
            LaneType.WALKING -> listOf(TrafficAreaUsageCode.PEDESTRIAN)
        }

    fun mapToTrafficAreaUsageCodes(restrictionType: RestrictionType): List<TrafficAreaUsageCode> =
        when (restrictionType) {
            RestrictionType.AUTONOMOUS_TRAFFIC -> listOf(TrafficAreaUsageCode.CAR)
            RestrictionType.BICYCLE -> listOf(TrafficAreaUsageCode.BICYCLE)
            RestrictionType.BUS -> listOf(TrafficAreaUsageCode.BUS_TAXI)
            RestrictionType.DELIVERY -> listOf(TrafficAreaUsageCode.CAR)
            RestrictionType.EMERGENCY -> listOf(TrafficAreaUsageCode.CAR)
            RestrictionType.HOV -> listOf(TrafficAreaUsageCode.CAR)
            RestrictionType.MOTORCYCLE -> listOf(TrafficAreaUsageCode.MOTORCYCLE)
            RestrictionType.NONE -> emptyList()
            RestrictionType.PASSENGER_CAR -> listOf(TrafficAreaUsageCode.CAR)
            RestrictionType.PEDESTRIAN -> listOf(TrafficAreaUsageCode.PEDESTRIAN)
            RestrictionType.SIMULATOR -> emptyList()
            RestrictionType.TAXI -> listOf(TrafficAreaUsageCode.TAXI)
            RestrictionType.THROUGH_TRAFFIC -> emptyList()
            RestrictionType.TRUCK -> listOf(TrafficAreaUsageCode.TRUCK)
            RestrictionType.TRUCKS -> listOf(TrafficAreaUsageCode.TRUCK)
        }

    fun mapToTrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode(
        laneMaterial: LaneMaterial,
    ): Option<TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode> =
        when (laneMaterial.surface.uppercase()) {
            in "ASPHALT" -> TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode.ASPHALT.some()
            in "GRASS" -> TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode.GRASS.some()
            in "SPEC_CONCRETE_3D" -> TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode.CONCRETE.some()
            else -> None
        }

    fun mapToTrafficAreaFunctionCodes(roadObjectType: RoadObjectType): List<TrafficAreaFunctionCode> =
        when (roadObjectType) {
            RoadObjectType.BARRIER -> emptyList()
            RoadObjectType.BUILDING -> emptyList()
            RoadObjectType.CROSSWALK -> listOf(TrafficAreaFunctionCode.CROSSWALK)
            RoadObjectType.GANTRY -> emptyList()
            RoadObjectType.NONE -> emptyList()
            RoadObjectType.OBSTACLE -> emptyList()
            RoadObjectType.PARKING_SPACE -> listOf(TrafficAreaFunctionCode.PARKING_LAY_BY)
            RoadObjectType.POLE -> emptyList()
            RoadObjectType.ROAD_MARK -> emptyList()
            RoadObjectType.ROAD_SURFACE -> emptyList()
            RoadObjectType.SIGNAL -> emptyList()
            RoadObjectType.TRAFFIC_ISLAND -> emptyList()
            RoadObjectType.TREE -> emptyList()
            RoadObjectType.VEGETATION -> emptyList()
        }

    fun mapToAuxiliaryTrafficAreaFunctionCodes(roadObjectType: RoadObjectType): List<AuxiliaryTrafficAreaFunctionCode> =
        when (roadObjectType) {
            RoadObjectType.BARRIER -> emptyList()
            RoadObjectType.BUILDING -> emptyList()
            RoadObjectType.CROSSWALK -> emptyList()
            RoadObjectType.GANTRY -> emptyList()
            RoadObjectType.NONE -> emptyList()
            RoadObjectType.OBSTACLE -> emptyList()
            RoadObjectType.PARKING_SPACE -> listOf(AuxiliaryTrafficAreaFunctionCode.PARKING_BAY)
            RoadObjectType.POLE -> emptyList()
            RoadObjectType.ROAD_MARK -> emptyList()
            RoadObjectType.ROAD_SURFACE -> emptyList()
            RoadObjectType.SIGNAL -> emptyList()
            RoadObjectType.TRAFFIC_ISLAND -> listOf(AuxiliaryTrafficAreaFunctionCode.TRAFFIC_ISLAND)
            RoadObjectType.TREE -> emptyList()
            RoadObjectType.VEGETATION -> emptyList()
        }

    fun mapToTrafficAreaUsageCodes(roadObjectType: RoadObjectType): List<TrafficAreaUsageCode> =
        when (roadObjectType) {
            RoadObjectType.BARRIER -> emptyList()
            RoadObjectType.BUILDING -> emptyList()
            RoadObjectType.CROSSWALK -> listOf(TrafficAreaUsageCode.PEDESTRIAN)
            RoadObjectType.GANTRY -> emptyList()
            RoadObjectType.NONE -> emptyList()
            RoadObjectType.OBSTACLE -> emptyList()
            RoadObjectType.PARKING_SPACE -> listOf(TrafficAreaUsageCode.CAR)
            RoadObjectType.POLE -> emptyList()
            RoadObjectType.ROAD_MARK -> emptyList()
            RoadObjectType.ROAD_SURFACE -> emptyList()
            RoadObjectType.SIGNAL -> emptyList()
            RoadObjectType.TRAFFIC_ISLAND -> listOf(TrafficAreaUsageCode.PEDESTRIAN, TrafficAreaUsageCode.BICYCLE)
            RoadObjectType.TREE -> emptyList()
            RoadObjectType.VEGETATION -> emptyList()
        }
}
