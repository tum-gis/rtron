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
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.path
import io.rtron.io.files.File
import io.rtron.io.files.Path
import io.rtron.io.files.toPath
import io.rtron.main.processor.ValidateOpendriveConfiguration
import io.rtron.main.processor.ValidateOpendriveProcessor
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration

class SubcommandValidateOpendrive : CliktCommand(name = "validate-opendrive", help = "Validate OpenDRIVE datasets.", printHelpOnEmptyArgs = true) {

    // Properties and Initializers
    init {
        context { helpFormatter = ColorFormatter() }
    }

    private val configPath by option(
        help = "Path to a YAML configuration of the process."
    ).path(mustExist = true)

    private val inputPath by argument(
        help = "Path to the directory containing OpenDRIVE datasets"
    ).path(mustExist = true)
    private val outputPath by argument(
        help = "Path to the output directory into which the reports are written"
    ).path()

    private val tolerance by option(help = "allowed tolerance when comparing double values").double()
        .default(Opendrive2RoadspacesConfiguration.DEFAULT_NUMBER_TOLERANCE)

    private val discretizationStepSize by option(help = "distance between each discretization step for curves and surfaces").double()
        .default(Roadspaces2CitygmlConfiguration.DEFAULT_DISCRETIZATION_STEP_SIZE)

    // Methods
    override fun run() {

        val configuration = configPath.toOption().fold({
            ValidateOpendriveConfiguration(
                tolerance = tolerance,
                discretizationStepSize = discretizationStepSize
            )
        }, {
            val configurationFilePath = Path.of(it)
            val configurationText = File(configurationFilePath).readText()

            Yaml.default.decodeFromString(ValidateOpendriveConfiguration.serializer(), configurationText)
        })

        val processor = ValidateOpendriveProcessor(configuration)
        processor.process(inputPath.toPath(), outputPath.toPath())
    }
}
