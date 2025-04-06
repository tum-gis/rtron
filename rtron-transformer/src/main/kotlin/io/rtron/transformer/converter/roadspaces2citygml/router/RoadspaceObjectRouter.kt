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

package io.rtron.transformer.converter.roadspaces2citygml.router

import arrow.core.some
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectBuildingSubType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.router.RoadspaceObjectRouter.CitygmlTargetFeatureType

/**
 * Feature router of [RoadspaceObject] (RoadSpace model) to the [CitygmlTargetFeatureType] (CityGML model).
 */
object RoadspaceObjectRouter {
    enum class CitygmlTargetFeatureType {
        BUILDING_BUILDING,
        CITYFURNITURE_CITYFURNITURE,
        GENERICS_GENERICOCCUPIEDSPACE,
        TRANSPORTATION_TRAFFICSPACE,
        TRANSPORTATION_AUXILIARYTRAFFICSPACE,
        TRANSPORTATION_MARKING,
        VEGETATION_SOLITARYVEGETATIONOBJECT,
    }

    /**
     * Returns the feature type [CitygmlTargetFeatureType] onto which [roadspaceObject] shall be mapped.
     */
    fun route(roadspaceObject: RoadspaceObject): CitygmlTargetFeatureType =
        when (roadspaceObject.type) {
            RoadObjectType.NONE -> CitygmlTargetFeatureType.GENERICS_GENERICOCCUPIEDSPACE
            RoadObjectType.OBSTACLE -> CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE
            RoadObjectType.POLE -> CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE
            RoadObjectType.TREE -> CitygmlTargetFeatureType.VEGETATION_SOLITARYVEGETATIONOBJECT
            RoadObjectType.VEGETATION -> CitygmlTargetFeatureType.VEGETATION_SOLITARYVEGETATIONOBJECT
            RoadObjectType.BARRIER -> CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE
            RoadObjectType.BUILDING -> {
                when (roadspaceObject.subType) {
                    RoadObjectBuildingSubType.BUS_STOP.some() -> CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE
                    else -> CitygmlTargetFeatureType.BUILDING_BUILDING
                }
            }
            RoadObjectType.PARKING_SPACE -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            RoadObjectType.TRAFFIC_ISLAND -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            RoadObjectType.CROSSWALK -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            RoadObjectType.GANTRY -> CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE
            RoadObjectType.ROAD_MARK -> CitygmlTargetFeatureType.TRANSPORTATION_MARKING
            RoadObjectType.ROAD_SURFACE -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            RoadObjectType.SIGNAL -> CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE
        }
}
