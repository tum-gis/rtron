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

import io.rtron.main.project.ProjectConfiguration
import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.readerwriter.citygml.configuration.CitygmlWriterConfiguration
import io.rtron.readerwriter.opendrive.configuration.OpendriveReaderConfiguration
import io.rtron.readerwriter.opendrive.configuration.OpendriveWriterConfiguration
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.roadspaces.configuration.RoadspacesEvaluatorConfiguration
import kotlinx.serialization.Serializable

@Serializable
data class ValidateOpendriveConfiguration(
    val tolerance: Double = Opendrive2RoadspacesConfiguration.DEFAULT_NUMBER_TOLERANCE,
    val discretizationStepSize: Double = Roadspaces2CitygmlConfiguration.DEFAULT_DISCRETIZATION_STEP_SIZE
) {

    // Methods

    fun deriveOpendriveReaderConfiguration(projectConfiguration: ProjectConfiguration) = OpendriveReaderConfiguration(
        projectId = projectConfiguration.projectId,
        outputSchemaValidationReportDirectoryPath = projectConfiguration.outputDirectoryPath
    )

    fun deriveOpendriveEvaluatorConfiguration(projectConfiguration: ProjectConfiguration) = OpendriveEvaluatorConfiguration(
        projectId = projectConfiguration.projectId,
        outputReportDirectoryPath = projectConfiguration.outputDirectoryPath,

        numberTolerance = tolerance,
    )

    fun deriveOpendriveWriterConfiguration(projectConfiguration: ProjectConfiguration) = OpendriveWriterConfiguration(
        projectId = projectConfiguration.projectId,
    )

    fun deriveOpendrive2RoadspacesConfiguration(projectConfiguration: ProjectConfiguration) = Opendrive2RoadspacesConfiguration(
        projectId = projectConfiguration.projectId,
        sourceFileIdentifier = projectConfiguration.inputFileIdentifier,
        concurrentProcessing = projectConfiguration.concurrentProcessing,
        outputReportDirectoryPath = projectConfiguration.outputDirectoryPath,

        numberTolerance = tolerance,
        distanceTolerance = Opendrive2RoadspacesConfiguration.DEFAULT_DISTANCE_TOLERANCE,
        angleTolerance = Opendrive2RoadspacesConfiguration.DEFAULT_ANGLE_TOLERANCE,
    )

    fun deriveRoadspacesEvaluatorConfiguration(projectConfiguration: ProjectConfiguration) = RoadspacesEvaluatorConfiguration(
        projectId = projectConfiguration.projectId,
        outputReportDirectoryPath = projectConfiguration.outputDirectoryPath,

        numberTolerance = tolerance,
        distanceTolerance = Opendrive2RoadspacesConfiguration.DEFAULT_DISTANCE_TOLERANCE,
    )

    fun deriveRoadspaces2CitygmlConfiguration(projectConfiguration: ProjectConfiguration) = Roadspaces2CitygmlConfiguration(
        projectId = projectConfiguration.projectId,
        concurrentProcessing = projectConfiguration.concurrentProcessing,

        outputReportDirectoryPath = projectConfiguration.outputDirectoryPath,

        discretizationStepSize = discretizationStepSize,

        generateLongitudinalFillerSurfaces = false,
        mappingBackwardsCompatibility = true,
    )

    fun deriveCitygmlWriterConfiguration(projectConfiguration: ProjectConfiguration) = CitygmlWriterConfiguration(
        projectId = projectConfiguration.projectId,

        versions = setOf(CitygmlVersion.V2_0)
    )
}
