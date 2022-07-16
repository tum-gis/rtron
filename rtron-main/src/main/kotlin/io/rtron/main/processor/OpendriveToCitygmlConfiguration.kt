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

import io.rtron.io.files.FileIdentifier
import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.readerwriter.citygml.configuration.CitygmlWriterConfiguration
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.roadspaces.configuration.RoadspacesEvaluatorConfiguration
import kotlinx.serialization.Serializable

@Serializable
data class OpendriveToCitygmlConfiguration(
    val convertToCitygml2: Boolean = false,

    val tolerance: Double = Opendrive2RoadspacesConfiguration.DEFAULT_NUMBER_TOLERANCE,
    val planViewGeometryDistanceTolerance: Double = Opendrive2RoadspacesConfiguration.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE,
    val planViewGeometryAngleTolerance: Double = Opendrive2RoadspacesConfiguration.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE,

    val crsEpsg: Int = Opendrive2RoadspacesConfiguration.DEFAULT_CRS_EPSG,
    val offsetX: Double = Opendrive2RoadspacesConfiguration.DEFAULT_OFFSET_X,
    val offsetY: Double = Opendrive2RoadspacesConfiguration.DEFAULT_OFFSET_Y,
    val offsetZ: Double = Opendrive2RoadspacesConfiguration.DEFAULT_OFFSET_Z,

    val discretizationStepSize: Double = Roadspaces2CitygmlConfiguration.DEFAULT_DISCRETIZATION_STEP_SIZE,
    val sweepDiscretizationStepSize: Double = Roadspaces2CitygmlConfiguration.DEFAULT_SWEEP_DISCRETIZATION_STEP_SIZE,
    val circleSlices: Int = Roadspaces2CitygmlConfiguration.DEFAULT_CIRCLE_SLICES,
    val transformAdditionalRoadLines: Boolean = Roadspaces2CitygmlConfiguration.DEFAULT_TRANSFORM_ADDITIONAL_ROAD_LINES,
) {
    // Methods

    fun deriveOpendriveEvaluatorConfiguration() = OpendriveEvaluatorConfiguration(
        numberTolerance = tolerance,
    )

    fun deriveOpendrive2RoadspacesConfiguration(sourceFileIdentifier: FileIdentifier) = Opendrive2RoadspacesConfiguration(
        sourceFileIdentifier = sourceFileIdentifier,
        concurrentProcessing = false,

        numberTolerance = tolerance,
        planViewGeometryDistanceTolerance = planViewGeometryDistanceTolerance,
        planViewGeometryAngleTolerance = planViewGeometryAngleTolerance,

        crsEpsg = crsEpsg,
        offsetX = offsetX,
        offsetY = offsetY,
        offsetZ = offsetZ,
    )

    fun deriveRoadspacesEvaluatorConfiguration() = RoadspacesEvaluatorConfiguration(
        numberTolerance = tolerance,
        laneTransitionDistanceTolerance = RoadspacesEvaluatorConfiguration.DEFAULT_LANE_TRANSITION_DISTANCE_TOLERANCE,
    )

    fun deriveRoadspaces2CitygmlConfiguration() = Roadspaces2CitygmlConfiguration(
        concurrentProcessing = false,

        discretizationStepSize = discretizationStepSize,
        sweepDiscretizationStepSize = sweepDiscretizationStepSize,
        circleSlices = circleSlices,
        transformAdditionalRoadLines = transformAdditionalRoadLines,

        mappingBackwardsCompatibility = convertToCitygml2,
    )

    fun deriveCitygmlWriterConfiguration() = CitygmlWriterConfiguration(

        versions = if (convertToCitygml2) setOf(CitygmlVersion.V2_0) else setOf(CitygmlVersion.V3_0)
    )
}
