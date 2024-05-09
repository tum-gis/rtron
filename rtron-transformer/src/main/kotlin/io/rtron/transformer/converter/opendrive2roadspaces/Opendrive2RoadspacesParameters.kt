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

package io.rtron.transformer.converter.opendrive2roadspaces

import kotlinx.serialization.Serializable

/**
 * Transformation parameters for the OpenDRIVE to RoadSpace transformer.
 */
@Serializable
data class Opendrive2RoadspacesParameters(
    /** enable concurrency during processing */
    val concurrentProcessing: Boolean,
    /** allowed tolerance when comparing double values */
    val numberTolerance: Double,
    /** distance tolerance between two geometry elements of the plan view */
    val planViewGeometryDistanceTolerance: Double,
    /** angle tolerance between two geometry elements of the plan view */
    val planViewGeometryAngleTolerance: Double,
    /** prefix of attribute names */
    val attributesPrefix: String,
    /** [EPSG code](https://en.wikipedia.org/wiki/EPSG_Geodetic_Parameter_Dataset) of the coordinate reference system (obligatory for working with GIS applications) */
    val deriveCrsEpsgAutomatically: Boolean,
    /** [EPSG code](https://en.wikipedia.org/wiki/EPSG_Geodetic_Parameter_Dataset) of the coordinate reference system (obligatory for working with GIS applications) */
    val crsEpsg: Int,
    /** linear extrapolation of lateral road shapes if they are not defined at the position (otherwise errors are thrown) */
    val extrapolateLateralRoadShapes: Boolean,
) {
    companion object {
        const val DEFAULT_NUMBER_TOLERANCE = 1E-7
        const val DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE = 1E0
        const val DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE = 1E0

        const val DEFAULT_ATTRIBUTES_PREFIX = "opendrive_"
        const val DEFAULT_DERIVE_CRS_EPSG_AUTOMATICALLY = false
        const val DEFAULT_CRS_EPSG = 0
        const val DEFAULT_EXTRAPOLATE_LATERAL_ROAD_SHAPES = false
    }
}
