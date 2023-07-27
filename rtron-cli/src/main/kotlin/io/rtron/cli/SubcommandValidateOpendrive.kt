/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.cli

import arrow.core.toOption
import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.path
import io.rtron.main.processor.CompressionFormat
import io.rtron.main.processor.ValidateOpendriveParameters
import io.rtron.main.processor.ValidateOpendriveProcessor
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters

class SubcommandValidateOpendrive : CliktCommand(name = "validate-opendrive", help = "Validate OpenDRIVE datasets.", printHelpOnEmptyArgs = true) {

    // Properties and Initializers
    private val parametersPath by option(
        help = "Path to a YAML file containing the parameters of the process."
    ).path(mustExist = true)

    private val inputPath by argument(
        help = "Path to the directory containing OpenDRIVE datasets"
    ).path(mustExist = true)
    private val outputPath by argument(
        help = "Path to the output directory into which the reports are written"
    ).path()

    private val tolerance by option(help = "allowed tolerance when comparing double values").double()
        .default(Opendrive2RoadspacesParameters.DEFAULT_NUMBER_TOLERANCE)
    private val planViewGeometryDistanceTolerance by option(help = "allowed distance tolerance between two geometry elements in the plan view").double()
        .default(OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE)
    private val planViewGeometryDistanceWarningTolerance by option(help = "warning distance tolerance between two geometry elements in the plan view").double()
        .default(OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_WARNING_TOLERANCE)
    private val planViewGeometryAngleTolerance by option(help = "allowed angle tolerance between two geometry elements in the plan view").double()
        .default(OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE)
    private val planViewGeometryAngleWarningTolerance by option(help = "warning angle tolerance between two geometry elements in the plan view").double()
        .default(OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_WARNING_TOLERANCE)

    private val discretizationStepSize by option(help = "distance between each discretization step for curves and surfaces").double()
        .default(Roadspaces2CitygmlParameters.DEFAULT_DISCRETIZATION_STEP_SIZE)

    private val skipOpendriveExport by option(help = "skip the export of the adjusted OpenDRIVE dataset").flag()
    private val skipCitygmlExport by option(help = "skip the export of the CityGML dataset for visual inspection purposes").flag()

    private val compressionFormat: CompressionFormat by option(help = "compress the output files with the respective compression format").enum<CompressionFormat>()
        .default(CompressionFormat.NONE)

    // Methods
    override fun run() {
        val parameters = parametersPath.toOption().fold({
            ValidateOpendriveParameters(
                tolerance = tolerance,
                planViewGeometryDistanceTolerance = planViewGeometryDistanceTolerance,
                planViewGeometryDistanceWarningTolerance = planViewGeometryDistanceWarningTolerance,
                planViewGeometryAngleTolerance = planViewGeometryAngleTolerance,
                planViewGeometryAngleWarningTolerance = planViewGeometryAngleWarningTolerance,
                discretizationStepSize = discretizationStepSize,
                writeOpendriveFile = !skipOpendriveExport,
                writeCitygml2File = !skipCitygmlExport,

                compressionFormat = compressionFormat
            )
        }, { parametersFilePath ->
            val parametersText = parametersFilePath.toFile().readText()

            Yaml.default.decodeFromString(ValidateOpendriveParameters.serializer(), parametersText)
        })

        val processor = ValidateOpendriveProcessor(parameters)
        processor.process(inputPath, outputPath)
    }
}
