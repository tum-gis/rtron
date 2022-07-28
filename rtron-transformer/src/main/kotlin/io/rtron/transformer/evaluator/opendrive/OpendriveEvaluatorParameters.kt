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

package io.rtron.transformer.evaluator.opendrive

import kotlinx.serialization.Serializable

/**
 * Parameters for the OpenDRIVE validator.
 */
@Serializable
data class OpendriveEvaluatorParameters(
    val numberTolerance: Double = DEFAULT_NUMBER_TOLERANCE,

    /** distance tolerance between two geometry elements of the plan view */
    val planViewGeometryDistanceTolerance: Double = DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE,
    /** warning tolerance for distances between two geometry elements of the plan view */
    val planViewGeometryDistanceWarningTolerance: Double = DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_WARNING_TOLERANCE,
    /** angle tolerance between two geometry elements of the plan view */
    val planViewGeometryAngleTolerance: Double = DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE,
    /** warning tolerance for angles between two geometry elements of the plan view */
    val planViewGeometryAngleWarningTolerance: Double = DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_WARNING_TOLERANCE,
) {

    init {
        require(planViewGeometryDistanceTolerance >= planViewGeometryDistanceWarningTolerance) { "Distance tolerance must be greater or equal to the warning distance tolerance." }
        require(planViewGeometryAngleTolerance >= planViewGeometryAngleWarningTolerance) { "Angle difference tolerance must be greater or equal to the warning angle difference tolerance." }
    }

    companion object {
        const val DEFAULT_NUMBER_TOLERANCE = 1E-7
        const val DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE = 1E0
        const val DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_WARNING_TOLERANCE = 1E-3

        const val DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE = 1E0
        const val DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_WARNING_TOLERANCE = 1E-3
    }
}
