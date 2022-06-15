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

package io.rtron.cli.validate

import arrow.core.toOption
import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.path
import io.rtron.cli.ColorFormatter
import io.rtron.io.files.File
import io.rtron.io.files.Path
import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlWriter
import io.rtron.readerwriter.opendrive.OpendriveReader
import io.rtron.readerwriter.opendrive.OpendriveWriter
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesTransformer
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlTransformer
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluator
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluator

class SubcommandValidateOpendrive : CliktCommand(name = "validate-opendrive", help = "Validate OpenDRIVE datasets.", printHelpOnEmptyArgs = true) {

    // Properties and Initializers
    init {
        context { helpFormatter = ColorFormatter() }
    }

    private val configurationPath by option(
        help = "Path to the yaml configuration."
    ).path(mustExist = true)

    private val inputPath by option(
        help = "Path to the directory containing OpenDRIVE datasets"
    ).path(mustExist = true)
    private val outputPath by option(
        help = "Path to the output directory into which the transformed CityGML models are written"
    ).path()

    private val tolerance by option(help = "allowed tolerance when comparing double values").double()
        .default(Opendrive2RoadspacesConfiguration.DEFAULT_NUMBER_TOLERANCE)

    private val discretizationStepSize by option(help = "distance between each discretization step for curves and surfaces").double()
        .default(Roadspaces2CitygmlConfiguration.DEFAULT_DISCRETIZATION_STEP_SIZE)

    // Methods
    override fun run() {

        val validateOpendriveConfiguration = deriveConfiguration()

        processAllFiles(
            inputDirectoryPath = validateOpendriveConfiguration.getInputPath(),
            withExtension = OpendriveReader.supportedFileExtensions.head,
            outputDirectoryPath = validateOpendriveConfiguration.getOutputPath()
        ) {

            // read opendrive model
            val opendriveReaderConfiguration = validateOpendriveConfiguration.toOpendriveReaderConfiguration(projectConfiguration)
            val opendriveReader = OpendriveReader(opendriveReaderConfiguration)
            val opendriveModel = opendriveReader.read(projectConfiguration.inputFilePath)
                .fold({ logger.warn(it.message); return@processAllFiles }, { it })

            // evaluate opendrive model
            val opendriveEvaluatorConfiguration = validateOpendriveConfiguration.toOpendriveEvaluatorConfiguration(projectConfiguration)
            val opendriveEvaluator = OpendriveEvaluator(opendriveEvaluatorConfiguration)
            val healedOpendriveModel = opendriveEvaluator.evaluate(opendriveModel)
                .fold({ logger.warn(it.message); return@processAllFiles }, { it })

            // write healed opendrive model
            val opendriveWriterConfiguration = validateOpendriveConfiguration.toOpendriveWriterConfiguration(projectConfiguration)
            val opendriveWriter = OpendriveWriter(opendriveWriterConfiguration)
            opendriveWriter.write(healedOpendriveModel, projectConfiguration.outputDirectoryPath)

            // transform opendrive model to roadspace model
            val opendrive2RoadspacesConfiguration = validateOpendriveConfiguration.toOpendrive2RoadspacesConfiguration(projectConfiguration)
            val opendrive2RoadspacesTransformer = Opendrive2RoadspacesTransformer(opendrive2RoadspacesConfiguration)
            val roadspacesModel = opendrive2RoadspacesTransformer.transform(opendriveModel).fold({ logger.warn(it.message); return@processAllFiles }, { it })

            // evaluate roadspaces model
            val roadspacesEvaluatorConfiguration = validateOpendriveConfiguration.toRoadspacesEvaluatorConfiguration(projectConfiguration)
            val roadspacesEvaluator = RoadspacesEvaluator(roadspacesEvaluatorConfiguration)
            roadspacesEvaluator.evaluate(roadspacesModel)

            // transform roadspace model to citygml model
            val roadspaces2CitygmlConfiguration = validateOpendriveConfiguration.toRoadspaces2CitygmlConfiguration(projectConfiguration)
            val roadspaces2CitygmlTransformer = Roadspaces2CitygmlTransformer(roadspaces2CitygmlConfiguration)
            val citygmlModel = roadspaces2CitygmlTransformer.transform(roadspacesModel)

            // write citygml model
            val citygmlWriterConfiguration = validateOpendriveConfiguration.toCitygmlWriterConfiguration(projectConfiguration)
            val citygmlWriter = CitygmlWriter(citygmlWriterConfiguration)
            citygmlWriter.write(citygmlModel, projectConfiguration.outputDirectoryPath)
        }
    }

    private fun deriveConfiguration(): ValidateOpendriveConfiguration {
        val configuration = configurationPath.toOption().fold({
            ValidateOpendriveConfiguration(
                inputPath = inputPath?.toString() ?: "",
                tolerance = tolerance,
                discretizationStepSize = discretizationStepSize
            )
        }, {
            val file = File(Path.of(it))
            val text = file.readText()

            Yaml.default.decodeFromString(ValidateOpendriveConfiguration.serializer(), text)
        })

        return configuration
    }
}
