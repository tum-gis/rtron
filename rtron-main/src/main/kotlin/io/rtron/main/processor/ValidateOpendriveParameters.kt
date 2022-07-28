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

package io.rtron.main.processor

import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.readerwriter.citygml.CitygmlWriterParameters
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluatorParameters
import kotlinx.serialization.Serializable

@Serializable
data class ValidateOpendriveParameters(
    val tolerance: Double = Opendrive2RoadspacesParameters.DEFAULT_NUMBER_TOLERANCE,
    val discretizationStepSize: Double = Roadspaces2CitygmlParameters.DEFAULT_DISCRETIZATION_STEP_SIZE
) {

    // Methods

    fun deriveOpendriveEvaluatorParameters() = OpendriveEvaluatorParameters(
        numberTolerance = tolerance,
    )

    fun deriveOpendrive2RoadspacesParameters() = Opendrive2RoadspacesParameters(
        concurrentProcessing = false,

        numberTolerance = tolerance,
        planViewGeometryDistanceTolerance = Opendrive2RoadspacesParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE,
        planViewGeometryAngleTolerance = Opendrive2RoadspacesParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE,
    )

    fun deriveRoadspacesEvaluatorParameters() = RoadspacesEvaluatorParameters(
        numberTolerance = tolerance,
        laneTransitionDistanceTolerance = RoadspacesEvaluatorParameters.DEFAULT_LANE_TRANSITION_DISTANCE_TOLERANCE,
    )

    fun deriveRoadspaces2CitygmlParameters() = Roadspaces2CitygmlParameters(
        concurrentProcessing = false,

        discretizationStepSize = discretizationStepSize,

        transformAdditionalRoadLines = true,

        generateLongitudinalFillerSurfaces = false,
        mappingBackwardsCompatibility = true,
        // mappingBackwardsCompatibility = false
    )

    fun deriveCitygmlWriterParameters() = CitygmlWriterParameters(

        versions = setOf(CitygmlVersion.V2_0)
        // versions = setOf(CitygmlVersion.V3_0)
    )
}
