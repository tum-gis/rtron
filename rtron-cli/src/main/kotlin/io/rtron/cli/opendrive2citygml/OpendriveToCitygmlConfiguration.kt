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

package io.rtron.cli.opendrive2citygml

import io.rtron.io.files.Path
import io.rtron.io.report.Report
import io.rtron.main.project.ProjectConfiguration
import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.readerwriter.citygml.configuration.CitygmlWriterConfiguration
import io.rtron.readerwriter.opendrive.configuration.OpendriveReaderConfiguration
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.roadspaces.configuration.RoadspacesEvaluatorConfiguration
import kotlinx.serialization.Serializable

@Serializable
data class OpendriveToCitygmlConfiguration(
    val inputPath: String = "",
    val outputPath: String = "",

    val convertToCitygml2: Boolean = false,

    val tolerance: Double = Opendrive2RoadspacesConfiguration.DEFAULT_NUMBER_TOLERANCE,
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
    fun getInputPath(): Path = Path(inputPath)
    fun getOutputPath(): Path = Path(outputPath)

    fun isValid(): Report {
        // TODO
        val report = Report()

        return report
    }

    // Methods

    fun toOpendriveReaderConfiguration(projectConfiguration: ProjectConfiguration) = OpendriveReaderConfiguration(
        projectId = projectConfiguration.projectId,
        outputSchemaValidationReportDirectoryPath = projectConfiguration.outputDirectoryPath
    )

    fun toOpendriveEvaluatorConfiguration(projectConfiguration: ProjectConfiguration) = OpendriveEvaluatorConfiguration(
        projectId = projectConfiguration.projectId,
        outputReportDirectoryPath = projectConfiguration.outputDirectoryPath,

        numberTolerance = tolerance,
    )

    fun toOpendrive2RoadspacesConfiguration(projectConfiguration: ProjectConfiguration) = Opendrive2RoadspacesConfiguration(
        projectId = projectConfiguration.projectId,
        sourceFileIdentifier = projectConfiguration.inputFileIdentifier,
        concurrentProcessing = projectConfiguration.concurrentProcessing,

        numberTolerance = tolerance,
        crsEpsg = crsEpsg,
        offsetX = offsetX,
        offsetY = offsetY,
        offsetZ = offsetZ,
    )

    fun toRoadspacesEvaluatorConfiguration(projectConfiguration: ProjectConfiguration) = RoadspacesEvaluatorConfiguration(
        projectId = projectConfiguration.projectId,
        outputReportDirectoryPath = projectConfiguration.outputDirectoryPath,

        numberTolerance = tolerance,
        distanceTolerance = Opendrive2RoadspacesConfiguration.DEFAULT_DISTANCE_TOLERANCE,
    )

    fun toRoadspaces2CitygmlConfiguration(projectConfiguration: ProjectConfiguration) = Roadspaces2CitygmlConfiguration(
        projectId = projectConfiguration.projectId,
        concurrentProcessing = projectConfiguration.concurrentProcessing,

        discretizationStepSize = discretizationStepSize,
        sweepDiscretizationStepSize = sweepDiscretizationStepSize,
        circleSlices = circleSlices,
        transformAdditionalRoadLines = transformAdditionalRoadLines,

        mappingBackwardsCompatibility = convertToCitygml2,
    )

    fun toCitygmlWriterConfiguration(projectConfiguration: ProjectConfiguration) = CitygmlWriterConfiguration(
        projectId = projectConfiguration.projectId,

        versions = if (convertToCitygml2) setOf(CitygmlVersion.V2_0) else setOf(CitygmlVersion.V3_0)
    )
}
