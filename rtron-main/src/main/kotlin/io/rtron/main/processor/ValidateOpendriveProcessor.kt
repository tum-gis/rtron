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

import arrow.core.getOrHandle
import io.rtron.io.messages.MessageSeverity
import io.rtron.io.serialization.serializeToJsonFile
import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlWriter
import io.rtron.readerwriter.opendrive.OpendriveReader
import io.rtron.readerwriter.opendrive.OpendriveWriter
import io.rtron.std.handleEmpty
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesTransformer
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlTransformer
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluator
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluator
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

class ValidateOpendriveProcessor(
    private val configuration: ValidateOpendriveConfiguration
) {

    // Methods

    fun process(inputPath: Path, outputPath: Path) {

        val logger = KotlinLogging.logger {}

        processAllFiles(
            inputDirectoryPath = inputPath,
            withExtension = OpendriveReader.supportedFileExtensions.first(),
            outputDirectoryPath = outputPath
        ) {

            // read OpenDRIVE model
            val opendriveReader = OpendriveReader.of(inputFilePath)
                .getOrHandle { logger.warn(it.message); return@processAllFiles }
            val schemaValidationReport = opendriveReader.runSchemaValidation()
            schemaValidationReport.serializeToJsonFile(outputDirectoryPath / SCHEMA_VALIDATION_REPORT_PATH)
            if (schemaValidationReport.messages.getNumberOfMessages(MessageSeverity.FATAL_ERROR) > 0)
                return@processAllFiles
            val opendriveModel = opendriveReader.readModel()
                .getOrHandle { logger.warn(it.message); return@processAllFiles }

            // evaluate OpenDRIVE model
            val opendriveEvaluator = OpendriveEvaluator(configuration.deriveOpendriveEvaluatorConfiguration())
            val opendriveEvaluationResult = opendriveEvaluator.evaluate(opendriveModel)
            opendriveEvaluationResult.second.serializeToJsonFile(outputDirectoryPath / OPENDRIVE_EVALUATION_REPORT_PATH)
            val healedOpendriveModel = opendriveEvaluationResult.first.handleEmpty {
                logger.warn(opendriveEvaluationResult.second.getTextSummary())
                return@processAllFiles
            }

            // write healed OpenDRIVE model
            val opendriveWriter = OpendriveWriter()
            opendriveWriter.write(healedOpendriveModel, outputDirectoryPath)

            // transform OpenDRIVE model to Roadspaces model
            val opendrive2RoadspacesTransformer = Opendrive2RoadspacesTransformer(configuration.deriveOpendrive2RoadspacesConfiguration(inputFileIdentifier))
            val roadspacesModelResult = opendrive2RoadspacesTransformer.transform(healedOpendriveModel)
            roadspacesModelResult.second.serializeToJsonFile(outputDirectoryPath / OPENDRIVE_TO_ROADSPACES_REPORT_PATH)
            val roadspacesModel = roadspacesModelResult.first.handleEmpty {
                logger.warn(roadspacesModelResult.second.conversion.getTextSummary())
                return@processAllFiles
            }

            // evaluate Roadspaces model
            val roadspacesEvaluator = RoadspacesEvaluator(configuration.deriveRoadspacesEvaluatorConfiguration())
            val roadspacesEvaluationResults = roadspacesEvaluator.evaluate(roadspacesModel)
            roadspacesEvaluationResults.second.serializeToJsonFile(outputDirectoryPath / ROADSPACES_EVALUATION_REPORT_PATH)

            // transform Roadspaces model to CityGML model
            val roadspaces2CitygmlConfiguration = configuration.deriveRoadspaces2CitygmlConfiguration()
            val roadspaces2CitygmlTransformer = Roadspaces2CitygmlTransformer(roadspaces2CitygmlConfiguration)
            val citygmlModelResult = roadspaces2CitygmlTransformer.transform(roadspacesModel)
            citygmlModelResult.second.serializeToJsonFile(outputDirectoryPath / ROADSPACES_TO_CITYGML_REPORT_PATH)

            // write CityGML model
            val citygmlWriterConfiguration = configuration.deriveCitygmlWriterConfiguration()
            val citygmlWriter = CitygmlWriter(citygmlWriterConfiguration)
            citygmlWriter.writeModel(citygmlModelResult.first, outputDirectoryPath)
        }
    }

    companion object {
        val REPORTS_PATH = Path("reports")
        val SCHEMA_VALIDATION_REPORT_PATH = REPORTS_PATH / Path("01_schemaValidationReport.json")
        val OPENDRIVE_EVALUATION_REPORT_PATH = REPORTS_PATH / Path("02_opendriveEvaluationReport.json")
        val OPENDRIVE_TO_ROADSPACES_REPORT_PATH = REPORTS_PATH / Path("03_opendrive2RoadspacesReport.json")
        val ROADSPACES_EVALUATION_REPORT_PATH = REPORTS_PATH / Path("04_roadspacesEvaluationReport.json")
        val ROADSPACES_TO_CITYGML_REPORT_PATH = REPORTS_PATH / Path("05_roadspaces2CitygmlReport.json")
    }
}
