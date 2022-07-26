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
import io.rtron.io.messages.getTextSummary
import io.rtron.io.serialization.serializeToJsonFile
import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlWriter
import io.rtron.readerwriter.opendrive.OpendriveReader
import io.rtron.std.handleEmpty
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesTransformer
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlTransformer
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluator
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluator
import io.rtron.transformer.modifiers.opendrive.shifter.OpendriveShifter
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

class OpendriveToCitygmlProcessor(
    private val parameters: OpendriveToCitygmlParameters
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
            val opendriveSchemaValidatorReport = opendriveReader.runSchemaValidation()
            opendriveSchemaValidatorReport.serializeToJsonFile(outputDirectoryPath / OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH)
            if (opendriveSchemaValidatorReport.validationProcessAborted())
                return@processAllFiles
            val opendriveModel = opendriveReader.readModel()
                .getOrHandle { logger.warn(it.message); return@processAllFiles }

            // evaluate OpenDRIVE model
            val opendriveEvaluator = OpendriveEvaluator(parameters.deriveOpendriveEvaluatorParameters())
            val opendriveEvaluatorResult = opendriveEvaluator.evaluate(opendriveModel)
            opendriveEvaluatorResult.second.serializeToJsonFile(outputDirectoryPath / OPENDRIVE_EVALUATOR_REPORT_PATH)
            val healedOpendriveModel = opendriveEvaluatorResult.first.handleEmpty {
                logger.warn(opendriveEvaluatorResult.second.getTextSummary())
                return@processAllFiles
            }

            // shift OpenDRIVE model
            val opendriveShifter = OpendriveShifter(parameters.deriveOpendriveShifterParameters())
            val opendriveShifterResult = opendriveShifter.modify(healedOpendriveModel)
            opendriveShifterResult.second.serializeToJsonFile(outputDirectoryPath / OPENDRIVE_SHIFTER_REPORT_PATH)

            // transform OpenDRIVE model to Roadspaces model
            val opendrive2RoadspacesTransformer = Opendrive2RoadspacesTransformer(parameters.deriveOpendrive2RoadspacesParameters())
            val roadspacesModelResult = opendrive2RoadspacesTransformer.transform(opendriveShifterResult.first, inputFileIdentifier)
            roadspacesModelResult.second.serializeToJsonFile(outputDirectoryPath / OPENDRIVE_TO_ROADSPACES_REPORT_PATH)
            val roadspacesModel = roadspacesModelResult.first.handleEmpty {
                logger.warn("Opendrive2RoadspacesTransformer: ${roadspacesModelResult.second.conversion.getTextSummary()}")
                return@processAllFiles
            }

            // evaluate Roadspaces model
            val roadspacesEvaluator = RoadspacesEvaluator(parameters.deriveRoadspacesEvaluatorParameters())
            val roadspacesEvaluatorResults = roadspacesEvaluator.evaluate(roadspacesModel)
            roadspacesEvaluatorResults.second.serializeToJsonFile(outputDirectoryPath / ROADSPACES_EVALUATOR_REPORT_PATH)

            // transform Roadspaces model to OpenDRIVE model
            val roadpaces2CitygmlTransformer = Roadspaces2CitygmlTransformer(parameters.deriveRoadspaces2CitygmlParameters())
            val citygmlModelResult = roadpaces2CitygmlTransformer.transform(roadspacesModel)
            citygmlModelResult.second.serializeToJsonFile(outputDirectoryPath / ROADSPACES_TO_CITYGML_REPORT_PATH)

            // write OpenDRIVE model
            val citygmlWriter = CitygmlWriter(parameters.deriveCitygmlWriterParameters())
            citygmlWriter.writeModel(citygmlModelResult.first, outputDirectoryPath)
        }
    }

    companion object {
        val REPORTS_PATH = Path("reports")
        val OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH = REPORTS_PATH / Path("01_opendrive_schema_validator_report.json")
        val OPENDRIVE_EVALUATOR_REPORT_PATH = REPORTS_PATH / Path("02_opendrive_evaluator_report.json")
        val OPENDRIVE_SHIFTER_REPORT_PATH = REPORTS_PATH / Path("03_opendrive_shifter_report.json")
        val OPENDRIVE_TO_ROADSPACES_REPORT_PATH = REPORTS_PATH / Path("04_opendrive_to_roadspaces_report.json")
        val ROADSPACES_EVALUATOR_REPORT_PATH = REPORTS_PATH / Path("05_roadspaces_evaluator_report.json")
        val ROADSPACES_TO_CITYGML_REPORT_PATH = REPORTS_PATH / Path("06_roadspaces_to_citygml_report.json")
    }
}
