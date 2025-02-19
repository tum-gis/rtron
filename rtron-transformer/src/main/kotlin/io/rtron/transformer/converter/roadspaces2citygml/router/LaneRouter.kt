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

import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.LaneType

/**
 * Feature router of [Lane] (RoadSpace model) to the [CitygmlTargetFeatureType] (CityGML model).
 */
object LaneRouter {
    enum class CitygmlTargetFeatureType {
        TRANSPORTATION_TRAFFICSPACE,
        TRANSPORTATION_AUXILIARYTRAFFICSPACE,
    }

    /**
     * Returns the feature type [CitygmlTargetFeatureType] onto which [lane] shall be mapped.
     */
    fun route(lane: Lane): CitygmlTargetFeatureType =
        when (lane.type) {
            LaneType.NONE -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.DRIVING -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.STOP -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.SHOULDER -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.BIKING -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.SHARED -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.SIDEWALK -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.BORDER -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.RESTRICTED -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.CURB -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.PARKING -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.BIDIRECTIONAL -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.MEDIAN -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.SPECIAL_1 -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.SPECIAL_2 -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.SPECIAL_3 -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.ROAD_WORKS -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.TRAM -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.RAIL -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.ENTRY -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.EXIT -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.OFF_RAMP -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.ON_RAMP -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.CONNECTING_RAMP -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.BUS -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.TAXI -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.HOV -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.MWY_ENTRY -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.MWY_EXIT -> CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE
            LaneType.WALKING -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
            LaneType.SLIP_LANE -> CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE
        }
}
