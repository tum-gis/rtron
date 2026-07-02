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
import arrow.core.getOrElse
import arrow.core.some
import io.rtron.model.citygml.code.AuxiliaryTrafficAreaFunctionCode
import io.rtron.model.citygml.code.CityFurnitureClassCode
import io.rtron.model.citygml.code.CityFurnitureUsageCode
import io.rtron.model.citygml.code.MarkingClassCode
import io.rtron.model.citygml.code.PlantCoverClassCode
import io.rtron.model.citygml.code.PlantCoverFunctionCode
import io.rtron.model.citygml.code.PlantCoverUsageCode
import io.rtron.model.citygml.code.SolitaryVegetationObjectClassCode
import io.rtron.model.citygml.code.SolitaryVegetationObjectFunctionCode
import io.rtron.model.citygml.code.SolitaryVegetationObjectUsageCode
import io.rtron.model.citygml.code.TrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode
import io.rtron.model.citygml.code.TrafficAreaFunctionCode
import io.rtron.model.citygml.code.TrafficAreaUsageCode
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectBarrierSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectBuildingSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectCrosswalkSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectGantrySubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectObstacleSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectPoleSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectRoadMarkSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectRoadSurfaceSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectTreeSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectVegetationSubType
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.LaneAccessRule
import io.rtron.model.roadspaces.roadspace.road.LaneMaterial
import io.rtron.model.roadspaces.roadspace.road.LaneType
import io.rtron.model.roadspaces.roadspace.road.RestrictionType
import io.rtron.model.roadspaces.roadspace.road.RoadMarkType

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

    fun mapToTrafficAreaFunctionCodes(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): List<TrafficAreaFunctionCode> =
        when (objectType) {
            RoadObjectType.CROSSWALK -> listOf(TrafficAreaFunctionCode.CROSSWALK)
            RoadObjectType.PARKING_SPACE -> listOf(TrafficAreaFunctionCode.PARKING_LAY_BY)
            RoadObjectType.ROAD_SURFACE ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectRoadSurfaceSubType) {
                            RoadObjectRoadSurfaceSubType.PATCH -> emptyList<TrafficAreaFunctionCode>()
                            RoadObjectRoadSurfaceSubType.SPEED_BUMP -> emptyList<TrafficAreaFunctionCode>()
                            RoadObjectRoadSurfaceSubType.OTHER -> emptyList<TrafficAreaFunctionCode>()
                            RoadObjectRoadSurfaceSubType.MANHOLE,
                            RoadObjectRoadSurfaceSubType.POTHOLE,
                            RoadObjectRoadSurfaceSubType.DRAIN_GUTTER,
                            -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a TrafficArea.",
                            )
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.TRAFFIC_ISLAND -> emptyList()
            RoadObjectType.BARRIER,
            RoadObjectType.BUILDING,
            RoadObjectType.GANTRY,
            RoadObjectType.NONE,
            RoadObjectType.OBSTACLE,
            RoadObjectType.POLE,
            RoadObjectType.ROAD_MARK,
            RoadObjectType.SIGNAL,
            RoadObjectType.TREE,
            RoadObjectType.VEGETATION,
            -> throw IllegalArgumentException(
                "Road object of type $objectType and subtype $objectSubType is not a TrafficArea.",
            )
        }

    fun mapToAuxiliaryTrafficAreaFunctionCodes(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): List<AuxiliaryTrafficAreaFunctionCode> =
        when (objectType) {
            RoadObjectType.ROAD_SURFACE -> emptyList()
            RoadObjectType.TRAFFIC_ISLAND -> listOf(AuxiliaryTrafficAreaFunctionCode.TRAFFIC_ISLAND)
            RoadObjectType.BARRIER,
            RoadObjectType.BUILDING,
            RoadObjectType.CROSSWALK,
            RoadObjectType.GANTRY,
            RoadObjectType.NONE,
            RoadObjectType.OBSTACLE,
            RoadObjectType.PARKING_SPACE,
            RoadObjectType.POLE,
            RoadObjectType.ROAD_MARK,
            RoadObjectType.SIGNAL,
            RoadObjectType.TREE,
            RoadObjectType.VEGETATION,
            -> throw IllegalArgumentException(
                "Road object of type $objectType and subtype $objectSubType is not a TrafficArea.",
            )
        }

    fun mapToTrafficAreaUsageCodes(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): List<TrafficAreaUsageCode> =
        when (objectType) {
            RoadObjectType.CROSSWALK ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectCrosswalkSubType) {
                            RoadObjectCrosswalkSubType.BICYCLE -> listOf(TrafficAreaUsageCode.BICYCLE)
                            RoadObjectCrosswalkSubType.OTHER -> emptyList()
                            RoadObjectCrosswalkSubType.PEDESTRIAN -> listOf(TrafficAreaUsageCode.PEDESTRIAN)
                            RoadObjectCrosswalkSubType.VIRTUAL -> listOf(TrafficAreaUsageCode.PEDESTRIAN)
                            RoadObjectCrosswalkSubType.ZEBRA -> listOf(TrafficAreaUsageCode.PEDESTRIAN)
                        }
                    }.getOrElse { listOf(TrafficAreaUsageCode.PEDESTRIAN) }
            RoadObjectType.PARKING_SPACE -> listOf(TrafficAreaUsageCode.CAR, TrafficAreaUsageCode.MOTORCYCLE)
            RoadObjectType.ROAD_SURFACE -> emptyList()
            RoadObjectType.TRAFFIC_ISLAND -> listOf(TrafficAreaUsageCode.PEDESTRIAN, TrafficAreaUsageCode.BICYCLE)
            RoadObjectType.ROAD_MARK,
            RoadObjectType.OBSTACLE,
            RoadObjectType.NONE,
            RoadObjectType.BARRIER,
            RoadObjectType.BUILDING,
            RoadObjectType.GANTRY,
            RoadObjectType.POLE,
            RoadObjectType.SIGNAL,
            RoadObjectType.TREE,
            RoadObjectType.VEGETATION,
            -> throw IllegalArgumentException(
                "Road object of type $objectType and subtype $objectSubType is not a TrafficArea.",
            )
        }

    fun mapToMarkingClassCode(roadMarkType: RoadMarkType): Option<MarkingClassCode> =
        when (roadMarkType) {
            RoadMarkType.BOTTS_DOTS -> MarkingClassCode.ROAD_MARKING_LANE.some()
            RoadMarkType.BROKEN -> MarkingClassCode.ROAD_MARKING_LANE_BROKEN.some()
            RoadMarkType.BROKEN_BROKEN -> MarkingClassCode.ROAD_MARKING_LANE.some()
            RoadMarkType.BROKEN_SOLID -> MarkingClassCode.ROAD_MARKING_LANE.some()
            RoadMarkType.CURB -> None
            RoadMarkType.CUSTOM -> None
            RoadMarkType.EDGE -> None
            RoadMarkType.GRASS -> None
            RoadMarkType.NONE -> None
            RoadMarkType.SOLID -> MarkingClassCode.ROAD_MARKING_LANE_SOLID.some()
            RoadMarkType.SOLID_BROKEN -> MarkingClassCode.ROAD_MARKING_LANE.some()
            RoadMarkType.SOLID_SOLID -> MarkingClassCode.ROAD_MARKING_LANE_SOLID.some()
        }

    fun mapToMarkingClassCode(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): Option<MarkingClassCode> =
        when (objectType) {
            RoadObjectType.ROAD_MARK ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectRoadMarkSubType) {
                            RoadObjectRoadMarkSubType.ARROW_LEFT -> MarkingClassCode.ARROW_LEFT
                            RoadObjectRoadMarkSubType.ARROW_LEFT_LEFT -> MarkingClassCode.ARROW_LEFT
                            RoadObjectRoadMarkSubType.ARROW_LEFT_RIGHT -> MarkingClassCode.ROAD_MARKING_DIRECTION
                            RoadObjectRoadMarkSubType.ARROW_RIGHT -> MarkingClassCode.ARROW_RIGHT
                            RoadObjectRoadMarkSubType.ARROW_RIGHT_RIGHT -> MarkingClassCode.ARROW_RIGHT
                            RoadObjectRoadMarkSubType.ARROW_RIGHT_LEFT -> MarkingClassCode.ROAD_MARKING_DIRECTION
                            RoadObjectRoadMarkSubType.ARROW_STRAIGHT -> MarkingClassCode.ARROW_STRAIGHT
                            RoadObjectRoadMarkSubType.ARROW_STRAIGHT_LEFT -> MarkingClassCode.ARROW_STRAIGHT_LEFT
                            RoadObjectRoadMarkSubType.ARROW_STRAIGHT_RIGHT -> MarkingClassCode.ARROW_STRAIGHT_RIGHT
                            RoadObjectRoadMarkSubType.ARROW_STRAIGHT_LEFT_RIGHT -> MarkingClassCode.ROAD_MARKING_DIRECTION
                            RoadObjectRoadMarkSubType.ARROW_MERGE_LEFT -> MarkingClassCode.ROAD_MARKING_DIRECTION
                            RoadObjectRoadMarkSubType.ARROW_MERGE_RIGHT -> MarkingClassCode.ROAD_MARKING_DIRECTION
                            RoadObjectRoadMarkSubType.SIGNAL_LINES -> MarkingClassCode.ROAD_MARKING
                            RoadObjectRoadMarkSubType.TEXT -> MarkingClassCode.ROAD_MARKING
                            RoadObjectRoadMarkSubType.SYMBOL -> MarkingClassCode.SYMBOL_OTHER
                            RoadObjectRoadMarkSubType.PAINT -> MarkingClassCode.ROAD_MARKING
                            RoadObjectRoadMarkSubType.AREA -> MarkingClassCode.ROAD_MARKING_RESTRICTED
                            RoadObjectRoadMarkSubType.OTHER -> MarkingClassCode.ROAD_MARKING
                        }
                    }.getOrElse { MarkingClassCode.ROAD_MARKING }
                    .some()
            RoadObjectType.NONE,
            RoadObjectType.OBSTACLE,
            RoadObjectType.POLE,
            RoadObjectType.TREE,
            RoadObjectType.VEGETATION,
            RoadObjectType.BARRIER,
            RoadObjectType.BUILDING,
            RoadObjectType.PARKING_SPACE,
            RoadObjectType.TRAFFIC_ISLAND,
            RoadObjectType.CROSSWALK,
            RoadObjectType.GANTRY,
            RoadObjectType.ROAD_SURFACE,
            RoadObjectType.SIGNAL,
            -> throw IllegalArgumentException(
                "Road object of type $objectType and subtype $objectSubType is not a Marking.",
            )
        }

    fun mapToSolitaryVegetationObjectClassCode(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): Option<SolitaryVegetationObjectClassCode> =
        when (objectType) {
            RoadObjectType.BARRIER ->
                objectSubType.map { subType ->
                    when (subType as RoadObjectBarrierSubType) {
                        RoadObjectBarrierSubType.HEDGE -> SolitaryVegetationObjectClassCode.SHRUB
                        else -> throw IllegalArgumentException(
                            "Road object of type $objectType and subtype $objectSubType is not a SolitaryVegetationObject.",
                        )
                    }
                }
            RoadObjectType.TREE ->
                objectSubType.flatMap { subType ->
                    when (subType as RoadObjectTreeSubType) {
                        RoadObjectTreeSubType.NEEDLE -> SolitaryVegetationObjectClassCode.CONIFEROUS_TREE.some()
                        RoadObjectTreeSubType.LEAF -> SolitaryVegetationObjectClassCode.DECIDUOUS_TREE.some()
                        RoadObjectTreeSubType.PALM -> SolitaryVegetationObjectClassCode.HIGH_PLANTS.some()
                        RoadObjectTreeSubType.OTHER -> None
                    }
                }
            RoadObjectType.VEGETATION ->
                objectSubType.flatMap { subType ->
                    when (subType as RoadObjectVegetationSubType) {
                        RoadObjectVegetationSubType.BUSH -> SolitaryVegetationObjectClassCode.BUSHES.some()
                        RoadObjectVegetationSubType.FOREST -> throw IllegalArgumentException(
                            "Road object of type $objectType and subtype $objectSubType is not a SolitaryVegetationObject.",
                        )
                        RoadObjectVegetationSubType.HEDGE -> SolitaryVegetationObjectClassCode.SHRUB.some()
                        RoadObjectVegetationSubType.OTHER -> None
                    }
                }
            RoadObjectType.BUILDING,
            RoadObjectType.CROSSWALK,
            RoadObjectType.GANTRY,
            RoadObjectType.NONE,
            RoadObjectType.OBSTACLE,
            RoadObjectType.PARKING_SPACE,
            RoadObjectType.POLE,
            RoadObjectType.ROAD_MARK,
            RoadObjectType.ROAD_SURFACE,
            RoadObjectType.SIGNAL,
            RoadObjectType.TRAFFIC_ISLAND,
            -> throw IllegalArgumentException(
                "Road object of type $objectType and subtype $objectSubType is not a SolitaryVegetationObject.",
            )
        }

    fun mapToSolitaryVegetationObjectFunctionCodes(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): List<SolitaryVegetationObjectFunctionCode> =
        when (objectType) {
            RoadObjectType.BARRIER ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectBarrierSubType) {
                            RoadObjectBarrierSubType.HEDGE -> listOf(SolitaryVegetationObjectFunctionCode.SHRUB)
                            else -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a SolitaryVegetationObject.",
                            )
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.TREE ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectTreeSubType) {
                            RoadObjectTreeSubType.NEEDLE -> listOf(SolitaryVegetationObjectFunctionCode.CONIFEROUS_TREE)
                            RoadObjectTreeSubType.LEAF -> listOf(SolitaryVegetationObjectFunctionCode.DECIDUOUS_TREE)
                            RoadObjectTreeSubType.PALM -> listOf(SolitaryVegetationObjectFunctionCode.HIGH_PLANTS)
                            RoadObjectTreeSubType.OTHER -> emptyList()
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.VEGETATION ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectVegetationSubType) {
                            RoadObjectVegetationSubType.BUSH -> listOf(SolitaryVegetationObjectFunctionCode.BUSHES)
                            RoadObjectVegetationSubType.FOREST -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a SolitaryVegetationObject.",
                            )
                            RoadObjectVegetationSubType.HEDGE -> listOf(SolitaryVegetationObjectFunctionCode.SHRUB)
                            RoadObjectVegetationSubType.OTHER -> emptyList()
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.BUILDING,
            RoadObjectType.CROSSWALK,
            RoadObjectType.GANTRY,
            RoadObjectType.NONE,
            RoadObjectType.OBSTACLE,
            RoadObjectType.PARKING_SPACE,
            RoadObjectType.POLE,
            RoadObjectType.ROAD_MARK,
            RoadObjectType.ROAD_SURFACE,
            RoadObjectType.SIGNAL,
            RoadObjectType.TRAFFIC_ISLAND,
            -> throw IllegalArgumentException(
                "Road object of type $objectType and subtype $objectSubType is not a SolitaryVegetationObject.",
            )
        }

    fun mapToSolitaryVegetationObjectUsageCodes(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): List<SolitaryVegetationObjectUsageCode> =
        when (objectType) {
            RoadObjectType.BARRIER ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectBarrierSubType) {
                            RoadObjectBarrierSubType.HEDGE -> listOf(SolitaryVegetationObjectUsageCode.SHRUB)
                            else -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a SolitaryVegetationObject.",
                            )
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.TREE ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectTreeSubType) {
                            RoadObjectTreeSubType.NEEDLE -> listOf(SolitaryVegetationObjectUsageCode.CONIFEROUS_TREE)
                            RoadObjectTreeSubType.LEAF -> listOf(SolitaryVegetationObjectUsageCode.DECIDUOUS_TREE)
                            RoadObjectTreeSubType.PALM -> listOf(SolitaryVegetationObjectUsageCode.HIGH_PLANTS)
                            RoadObjectTreeSubType.OTHER -> emptyList()
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.VEGETATION ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectVegetationSubType) {
                            RoadObjectVegetationSubType.BUSH -> listOf(SolitaryVegetationObjectUsageCode.BUSHES)
                            RoadObjectVegetationSubType.FOREST -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a SolitaryVegetationObject.",
                            )
                            RoadObjectVegetationSubType.HEDGE -> listOf(SolitaryVegetationObjectUsageCode.SHRUB)
                            RoadObjectVegetationSubType.OTHER -> emptyList()
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.BUILDING,
            RoadObjectType.CROSSWALK,
            RoadObjectType.GANTRY,
            RoadObjectType.NONE,
            RoadObjectType.OBSTACLE,
            RoadObjectType.PARKING_SPACE,
            RoadObjectType.POLE,
            RoadObjectType.ROAD_MARK,
            RoadObjectType.ROAD_SURFACE,
            RoadObjectType.SIGNAL,
            RoadObjectType.TRAFFIC_ISLAND,
            -> throw IllegalArgumentException(
                "Road object of type $objectType and subtype $objectSubType is not a SolitaryVegetationObject.",
            )
        }

    fun mapToPlantCoverClassCode(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): Option<PlantCoverClassCode> = None

    fun mapToPlantCoverFunctionCodes(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): List<PlantCoverFunctionCode> = emptyList()

    fun mapToPlantCoverUsageCodes(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): List<PlantCoverUsageCode> = emptyList()

    fun mapToCityFurnitureClassCode(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): Option<CityFurnitureClassCode> =
        when (objectType) {
            RoadObjectType.BARRIER ->
                objectSubType.map { subType ->
                    when (subType as RoadObjectBarrierSubType) {
                        RoadObjectBarrierSubType.HEDGE -> throw IllegalArgumentException(
                            "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                        )
                        RoadObjectBarrierSubType.GUARD_RAIL -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectBarrierSubType.JERSEY_BARRIER -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectBarrierSubType.WALL -> CityFurnitureClassCode.OTHERS
                        RoadObjectBarrierSubType.RAILING -> CityFurnitureClassCode.OTHERS
                        RoadObjectBarrierSubType.FENCE -> CityFurnitureClassCode.OTHERS
                        RoadObjectBarrierSubType.NOISE_PROTECTIONS -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectBarrierSubType.OTHER -> CityFurnitureClassCode.OTHERS
                    }
                }
            RoadObjectType.POLE ->
                objectSubType.map { subType ->
                    when (subType as RoadObjectPoleSubType) {
                        RoadObjectPoleSubType.EMERGENCY_CALL_BOX -> CityFurnitureClassCode.COMMUNICATION
                        RoadObjectPoleSubType.PERMANENT_DELINEATOR -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectPoleSubType.BOLLARD -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectPoleSubType.TRAFFIC_SIGN -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectPoleSubType.TRAFFIC_LIGHT -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectPoleSubType.POWER_POLE -> CityFurnitureClassCode.OTHERS
                        RoadObjectPoleSubType.STREET_LAMP -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectPoleSubType.WIND_TURBINE -> CityFurnitureClassCode.OTHERS
                        RoadObjectPoleSubType.OTHER -> CityFurnitureClassCode.OTHERS
                    }
                }
            RoadObjectType.OBSTACLE ->
                objectSubType.map { subType ->
                    when (subType as RoadObjectObstacleSubType) {
                        RoadObjectObstacleSubType.ADVERTISING_COLUMN -> CityFurnitureClassCode.COMMUNICATION
                        RoadObjectObstacleSubType.ART -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.SEATING -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.PICK_NICK -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.BOX -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.PHONE_BOOTH -> CityFurnitureClassCode.COMMUNICATION
                        RoadObjectObstacleSubType.CHARGING_STATION -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.DISTRIBUTION_BOX -> CityFurnitureClassCode.COMMUNICATION
                        RoadObjectObstacleSubType.CRASH_BOX -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectObstacleSubType.DUMPSTER -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.DUST_BIN -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.FOUNTAIN -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.GRIT_CONTAINER -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.HYDRANT -> CityFurnitureClassCode.SECURITY
                        RoadObjectObstacleSubType.PARKING_METER -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectObstacleSubType.PILLAR -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.PLANT_POT -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.POST_BOX -> CityFurnitureClassCode.COMMUNICATION
                        RoadObjectObstacleSubType.RAILING -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.ROCK -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.ROAD_BLOCKAGE -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectObstacleSubType.WALL -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.FENCE -> CityFurnitureClassCode.OTHERS
                        RoadObjectObstacleSubType.OTHER -> CityFurnitureClassCode.OTHERS
                    }
                }
            RoadObjectType.BUILDING ->
                objectSubType.map { subType ->
                    when (subType as RoadObjectBuildingSubType) {
                        RoadObjectBuildingSubType.BUILDING -> throw IllegalArgumentException(
                            "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                        )
                        RoadObjectBuildingSubType.BUS_STOP -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectBuildingSubType.TOLL_BOOTH -> throw IllegalArgumentException(
                            "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                        )
                        RoadObjectBuildingSubType.OTHER -> throw IllegalArgumentException(
                            "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                        )
                    }
                }
            RoadObjectType.GANTRY ->
                objectSubType.map { subType ->
                    when (subType as RoadObjectGantrySubType) {
                        RoadObjectGantrySubType.GANTRY -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectGantrySubType.GANTRY_HALF -> CityFurnitureClassCode.TRAFFIC
                        RoadObjectGantrySubType.OTHER -> CityFurnitureClassCode.TRAFFIC
                    }
                }
            RoadObjectType.SIGNAL -> CityFurnitureClassCode.TRAFFIC.some()
            RoadObjectType.CROSSWALK,
            RoadObjectType.NONE,
            RoadObjectType.PARKING_SPACE,
            RoadObjectType.ROAD_MARK,
            RoadObjectType.ROAD_SURFACE,
            RoadObjectType.TRAFFIC_ISLAND,
            RoadObjectType.TREE,
            RoadObjectType.VEGETATION,
            -> throw IllegalArgumentException(
                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
            )
        }

    fun mapToCityFurnitureFunctionCodes(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): List<CityFurnitureUsageCode> =
        when (objectType) {
            RoadObjectType.BARRIER ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectBarrierSubType) {
                            RoadObjectBarrierSubType.HEDGE -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                            )
                            RoadObjectBarrierSubType.GUARD_RAIL -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectBarrierSubType.JERSEY_BARRIER -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectBarrierSubType.WALL -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectBarrierSubType.RAILING -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectBarrierSubType.FENCE -> listOf(CityFurnitureUsageCode.FENCE)
                            RoadObjectBarrierSubType.NOISE_PROTECTIONS -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectBarrierSubType.OTHER -> listOf(CityFurnitureUsageCode.OTHERS)
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.POLE ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectPoleSubType) {
                            RoadObjectPoleSubType.EMERGENCY_CALL_BOX -> listOf(CityFurnitureUsageCode.EMERGENCY_CALL_FIXTURE)
                            RoadObjectPoleSubType.PERMANENT_DELINEATOR -> listOf(CityFurnitureUsageCode.BOUNDARY_POST)
                            RoadObjectPoleSubType.BOLLARD -> listOf(CityFurnitureUsageCode.BOUNDARY_POST)
                            RoadObjectPoleSubType.TRAFFIC_SIGN -> listOf(CityFurnitureUsageCode.ROAD_SIGN)
                            RoadObjectPoleSubType.TRAFFIC_LIGHT -> listOf(CityFurnitureUsageCode.TRAFFIC_LIGHT)
                            RoadObjectPoleSubType.POWER_POLE -> listOf(CityFurnitureUsageCode.POLE)
                            RoadObjectPoleSubType.STREET_LAMP -> listOf(CityFurnitureUsageCode.STREETLAMP_LANTERN_OR_CANDELABRA)
                            RoadObjectPoleSubType.WIND_TURBINE -> listOf(CityFurnitureUsageCode.WINDMILL)
                            RoadObjectPoleSubType.OTHER -> listOf(CityFurnitureUsageCode.OTHERS)
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.OBSTACLE ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectObstacleSubType) {
                            RoadObjectObstacleSubType.ADVERTISING_COLUMN -> listOf(CityFurnitureUsageCode.COLUMN)
                            RoadObjectObstacleSubType.ART -> listOf(CityFurnitureUsageCode.MEMORIAL_MONUMENT)
                            RoadObjectObstacleSubType.SEATING -> listOf(CityFurnitureUsageCode.BENCH)
                            RoadObjectObstacleSubType.PICK_NICK -> listOf(CityFurnitureUsageCode.BENCH)
                            RoadObjectObstacleSubType.BOX -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.PHONE_BOOTH -> listOf(CityFurnitureUsageCode.TELEPHONE_BOX)
                            RoadObjectObstacleSubType.CHARGING_STATION -> listOf(CityFurnitureUsageCode.CONVERTER)
                            RoadObjectObstacleSubType.DISTRIBUTION_BOX -> listOf(CityFurnitureUsageCode.SWITCHING_UNIT)
                            RoadObjectObstacleSubType.CRASH_BOX -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.DUMPSTER -> listOf(CityFurnitureUsageCode.RUBBISH_BIN)
                            RoadObjectObstacleSubType.DUST_BIN -> listOf(CityFurnitureUsageCode.RUBBISH_BIN)
                            RoadObjectObstacleSubType.FOUNTAIN -> listOf(CityFurnitureUsageCode.FOUNTAIN)
                            RoadObjectObstacleSubType.GRIT_CONTAINER -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.HYDRANT -> listOf(CityFurnitureUsageCode.HYDRANT)
                            RoadObjectObstacleSubType.PARKING_METER -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.PILLAR -> listOf(CityFurnitureUsageCode.COLUMN)
                            RoadObjectObstacleSubType.PLANT_POT -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.POST_BOX -> listOf(CityFurnitureUsageCode.POSTBOX)
                            RoadObjectObstacleSubType.RAILING -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.ROCK -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.ROAD_BLOCKAGE -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.WALL -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.FENCE -> listOf(CityFurnitureUsageCode.FENCE)
                            RoadObjectObstacleSubType.OTHER -> listOf(CityFurnitureUsageCode.OTHERS)
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.BUILDING ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectBuildingSubType) {
                            RoadObjectBuildingSubType.BUILDING -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                            )
                            RoadObjectBuildingSubType.BUS_STOP -> listOf(CityFurnitureUsageCode.BUS_STOP)
                            RoadObjectBuildingSubType.TOLL_BOOTH -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                            )
                            RoadObjectBuildingSubType.OTHER -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                            )
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.GANTRY ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectGantrySubType) {
                            RoadObjectGantrySubType.GANTRY -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectGantrySubType.GANTRY_HALF -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectGantrySubType.OTHER -> listOf(CityFurnitureUsageCode.OTHERS)
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.SIGNAL -> emptyList()
            RoadObjectType.CROSSWALK,
            RoadObjectType.NONE,
            RoadObjectType.PARKING_SPACE,
            RoadObjectType.ROAD_MARK,
            RoadObjectType.ROAD_SURFACE,
            RoadObjectType.TRAFFIC_ISLAND,
            RoadObjectType.TREE,
            RoadObjectType.VEGETATION,
            -> throw IllegalArgumentException(
                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
            )
        }

    fun mapToCityFurnitureUsageCodes(
        objectType: RoadObjectType,
        objectSubType: Option<RoadObjectSubType>,
    ): List<CityFurnitureUsageCode> =
        when (objectType) {
            RoadObjectType.BARRIER ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectBarrierSubType) {
                            RoadObjectBarrierSubType.HEDGE -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                            )
                            RoadObjectBarrierSubType.GUARD_RAIL -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectBarrierSubType.JERSEY_BARRIER -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectBarrierSubType.WALL -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectBarrierSubType.RAILING -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectBarrierSubType.FENCE -> listOf(CityFurnitureUsageCode.FENCE)
                            RoadObjectBarrierSubType.NOISE_PROTECTIONS -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectBarrierSubType.OTHER -> listOf(CityFurnitureUsageCode.OTHERS)
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.POLE ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectPoleSubType) {
                            RoadObjectPoleSubType.EMERGENCY_CALL_BOX -> listOf(CityFurnitureUsageCode.EMERGENCY_CALL_FIXTURE)
                            RoadObjectPoleSubType.PERMANENT_DELINEATOR -> listOf(CityFurnitureUsageCode.BOUNDARY_POST)
                            RoadObjectPoleSubType.BOLLARD -> listOf(CityFurnitureUsageCode.BOUNDARY_POST)
                            RoadObjectPoleSubType.TRAFFIC_SIGN -> listOf(CityFurnitureUsageCode.ROAD_SIGN)
                            RoadObjectPoleSubType.TRAFFIC_LIGHT -> listOf(CityFurnitureUsageCode.TRAFFIC_LIGHT)
                            RoadObjectPoleSubType.POWER_POLE -> listOf(CityFurnitureUsageCode.POLE)
                            RoadObjectPoleSubType.STREET_LAMP -> listOf(CityFurnitureUsageCode.STREETLAMP_LANTERN_OR_CANDELABRA)
                            RoadObjectPoleSubType.WIND_TURBINE -> listOf(CityFurnitureUsageCode.WINDMILL)
                            RoadObjectPoleSubType.OTHER -> listOf(CityFurnitureUsageCode.OTHERS)
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.OBSTACLE ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectObstacleSubType) {
                            RoadObjectObstacleSubType.ADVERTISING_COLUMN -> listOf(CityFurnitureUsageCode.COLUMN)
                            RoadObjectObstacleSubType.ART -> listOf(CityFurnitureUsageCode.MEMORIAL_MONUMENT)
                            RoadObjectObstacleSubType.SEATING -> listOf(CityFurnitureUsageCode.BENCH)
                            RoadObjectObstacleSubType.PICK_NICK -> listOf(CityFurnitureUsageCode.BENCH)
                            RoadObjectObstacleSubType.BOX -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.PHONE_BOOTH -> listOf(CityFurnitureUsageCode.TELEPHONE_BOX)
                            RoadObjectObstacleSubType.CHARGING_STATION -> listOf(CityFurnitureUsageCode.CONVERTER)
                            RoadObjectObstacleSubType.DISTRIBUTION_BOX -> listOf(CityFurnitureUsageCode.SWITCHING_UNIT)
                            RoadObjectObstacleSubType.CRASH_BOX -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.DUMPSTER -> listOf(CityFurnitureUsageCode.RUBBISH_BIN)
                            RoadObjectObstacleSubType.DUST_BIN -> listOf(CityFurnitureUsageCode.RUBBISH_BIN)
                            RoadObjectObstacleSubType.FOUNTAIN -> listOf(CityFurnitureUsageCode.FOUNTAIN)
                            RoadObjectObstacleSubType.GRIT_CONTAINER -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.HYDRANT -> listOf(CityFurnitureUsageCode.HYDRANT)
                            RoadObjectObstacleSubType.PARKING_METER -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.PILLAR -> listOf(CityFurnitureUsageCode.COLUMN)
                            RoadObjectObstacleSubType.PLANT_POT -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.POST_BOX -> listOf(CityFurnitureUsageCode.POSTBOX)
                            RoadObjectObstacleSubType.RAILING -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.ROCK -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.ROAD_BLOCKAGE -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.WALL -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectObstacleSubType.FENCE -> listOf(CityFurnitureUsageCode.FENCE)
                            RoadObjectObstacleSubType.OTHER -> listOf(CityFurnitureUsageCode.OTHERS)
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.BUILDING ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectBuildingSubType) {
                            RoadObjectBuildingSubType.BUILDING -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                            )
                            RoadObjectBuildingSubType.BUS_STOP -> listOf(CityFurnitureUsageCode.BUS_STOP)
                            RoadObjectBuildingSubType.TOLL_BOOTH -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                            )
                            RoadObjectBuildingSubType.OTHER -> throw IllegalArgumentException(
                                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
                            )
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.GANTRY ->
                objectSubType
                    .map { subType ->
                        when (subType as RoadObjectGantrySubType) {
                            RoadObjectGantrySubType.GANTRY -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectGantrySubType.GANTRY_HALF -> listOf(CityFurnitureUsageCode.OTHERS)
                            RoadObjectGantrySubType.OTHER -> listOf(CityFurnitureUsageCode.OTHERS)
                        }
                    }.getOrElse { emptyList() }
            RoadObjectType.SIGNAL -> emptyList()
            RoadObjectType.CROSSWALK,
            RoadObjectType.NONE,
            RoadObjectType.PARKING_SPACE,
            RoadObjectType.ROAD_MARK,
            RoadObjectType.ROAD_SURFACE,
            RoadObjectType.TRAFFIC_ISLAND,
            RoadObjectType.TREE,
            RoadObjectType.VEGETATION,
            -> throw IllegalArgumentException(
                "Road object of type $objectType and subtype $objectSubType is not a CityFurniture.",
            )
        }
}
