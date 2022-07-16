/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.converter.opendrive2roadspaces.configuration

import io.rtron.io.files.FileIdentifier
import io.rtron.math.geometry.euclidean.twod.point.Vector2D

/**
 * Transformation configuration for the OpenDRIVE to RoadSpace transformer.
 */
data class Opendrive2RoadspacesConfiguration(
    val sourceFileIdentifier: FileIdentifier,
    val concurrentProcessing: Boolean,

    /** allowed tolerance when comparing double values */
    val numberTolerance: Double = DEFAULT_NUMBER_TOLERANCE,
    /** distance tolerance between two geometry elements of the plan view */
    val planViewGeometryDistanceTolerance: Double = DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE,
    /** angle tolerance between two geometry elements of the plan view */
    val planViewGeometryAngleTolerance: Double = DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE,

    /** prefix of attribute names */
    val attributesPrefix: String = DEFAULT_ATTRIBUTES_PREFIX,
    /** [EPSG code](https://en.wikipedia.org/wiki/EPSG_Geodetic_Parameter_Dataset) of the coordinate reference system (obligatory for working with GIS applications) */
    val crsEpsg: Int = DEFAULT_CRS_EPSG,
    /** offset by which the model is translated along the x axis */
    val offsetX: Double = DEFAULT_OFFSET_X,
    /** offset by which the model is translated along the y axis */
    val offsetY: Double = DEFAULT_OFFSET_Y,
    /** offset by which the model is translated along the z axis */
    val offsetZ: Double = DEFAULT_OFFSET_Z,
    /** linear extrapolation of lateral road shapes if they are not defined at the position (otherwise errors are thrown) */
    val extrapolateLateralRoadShapes: Boolean = DEFAULT_EXTRAPOLATE_LATERAL_ROAD_SHAPES
) {

    /**
     * offset in the xy plane as vector
     */
    val offsetXY: Vector2D = Vector2D(offsetX, offsetY)

    companion object {
        const val DEFAULT_NUMBER_TOLERANCE = 1E-7
        const val DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE = 1E0
        const val DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE = 1E0

        const val DEFAULT_ATTRIBUTES_PREFIX = "opendrive_"
        const val DEFAULT_CRS_EPSG = 0
        const val DEFAULT_OFFSET_X = 0.0
        const val DEFAULT_OFFSET_Y = 0.0
        const val DEFAULT_OFFSET_Z = 0.0
        const val DEFAULT_EXTRAPOLATE_LATERAL_ROAD_SHAPES = false
    }
}
