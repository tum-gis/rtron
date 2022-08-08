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

package io.rtron.cli

import arrow.core.toOption
import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.path
import io.rtron.main.processor.ValidateOpendriveParameters
import io.rtron.main.processor.ValidateOpendriveProcessor
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters

class SubcommandValidateOpendrive : CliktCommand(name = "validate-opendrive", help = "Validate OpenDRIVE datasets.", printHelpOnEmptyArgs = true) {

    // Properties and Initializers
    init {
        context { helpFormatter = ColorFormatter() }
    }

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

    private val discretizationStepSize by option(help = "distance between each discretization step for curves and surfaces").double()
        .default(Roadspaces2CitygmlParameters.DEFAULT_DISCRETIZATION_STEP_SIZE)

    private val skipOpendriveExport by option(help = "skip the export of the adjusted OpenDRIVE dataset").flag()
    private val skipCitygmlExport by option(help = "skip the export of the CityGML dataset for visual inspection purposes").flag()

    // Methods
    override fun run() {

        val parameters = parametersPath.toOption().fold({
            ValidateOpendriveParameters(
                tolerance = tolerance,
                discretizationStepSize = discretizationStepSize,
                exportOpendriveDataset = !skipOpendriveExport,
                exportCitygml2Dataset = !skipCitygmlExport
            )
        }, { parametersFilePath ->
            val parametersText = parametersFilePath.toFile().readText()

            Yaml.default.decodeFromString(ValidateOpendriveParameters.serializer(), parametersText)
        })

        val processor = ValidateOpendriveProcessor(parameters)
        processor.process(inputPath, outputPath)
    }
}
