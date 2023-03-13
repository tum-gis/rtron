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

package io.rtron.main.processor

import arrow.core.getOrElse
import com.charleskorn.kaml.Yaml
import io.rtron.io.messages.getTextSummary
import io.rtron.io.serialization.serializeToJsonFile
import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlWriter
import io.rtron.readerwriter.opendrive.OpendriveFileReader
import io.rtron.readerwriter.opendrive.OpendriveFileWriter
import io.rtron.readerwriter.opendrive.OpendriveValidator
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
    private val parameters: ValidateOpendriveParameters
) {

    // Methods

    fun process(inputPath: Path, outputPath: Path) {
        val logger = KotlinLogging.logger {}

        processAllFiles(
            inputDirectoryPath = inputPath,
            withFilenameEndings = OpendriveFileReader.supportedFilenameEndings,
            outputDirectoryPath = outputPath
        ) {
            // write the parameters as yaml file
            val parametersText = Yaml.default.encodeToString(ValidateOpendriveParameters.serializer(), parameters)
            (outputDirectoryPath / PARAMETERS_PATH).toFile().writeText(parametersText)

            // validate schema of OpenDRIVE model
            val opendriveSchemaValidatorReport = OpendriveValidator.validateFromFile(inputFilePath).getOrElse { logger.warn(it.message); return@processAllFiles }
            opendriveSchemaValidatorReport.serializeToJsonFile(outputDirectoryPath / OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH)
            if (opendriveSchemaValidatorReport.validationProcessAborted()) {
                return@processAllFiles
            }
            // read of OpenDRIVE model
            val opendriveModel = OpendriveFileReader.readFromFile(inputFilePath)
                .getOrElse { logger.warn(it.message); return@processAllFiles }

            // evaluate OpenDRIVE model
            val opendriveEvaluator = OpendriveEvaluator(parameters.deriveOpendriveEvaluatorParameters())
            val opendriveEvaluatorResult = opendriveEvaluator.evaluate(opendriveModel)
            opendriveEvaluatorResult.second.serializeToJsonFile(outputDirectoryPath / OPENDRIVE_EVALUATOR_REPORT_PATH)
            if (opendriveEvaluatorResult.second.containsFatalErrors()) {
                logger.warn(opendriveEvaluatorResult.second.getTextSummary())
                return@processAllFiles
            }
            val modifiedOpendriveModel = opendriveEvaluatorResult.first.handleEmpty {
                logger.warn(opendriveEvaluatorResult.second.getTextSummary())
                return@processAllFiles
            }

            // write modified OpenDRIVE model
            if (parameters.writeOpendriveFile) {
                val opendriveFileWriter = OpendriveFileWriter(parameters.deriveOpendriveWriterParameters())
                opendriveFileWriter.write(modifiedOpendriveModel, outputDirectoryPath)
            }

            // transform OpenDRIVE model to Roadspaces model
            val opendrive2RoadspacesTransformer = Opendrive2RoadspacesTransformer(parameters.deriveOpendrive2RoadspacesParameters())
            val roadspacesModelResult = opendrive2RoadspacesTransformer.transform(modifiedOpendriveModel, inputFileIdentifier)
            roadspacesModelResult.second.serializeToJsonFile(outputDirectoryPath / OPENDRIVE_TO_ROADSPACES_REPORT_PATH)
            val roadspacesModel = roadspacesModelResult.first.handleEmpty {
                logger.warn(roadspacesModelResult.second.conversion.getTextSummary())
                return@processAllFiles
            }

            // evaluate Roadspaces model
            val roadspacesEvaluator = RoadspacesEvaluator(parameters.deriveRoadspacesEvaluatorParameters())
            val roadspacesEvaluatorResults = roadspacesEvaluator.evaluate(roadspacesModel)
            roadspacesEvaluatorResults.second.serializeToJsonFile(outputDirectoryPath / ROADSPACES_EVALUATOR_REPORT_PATH)

            // transform Roadspaces model to CityGML2 model
            val roadspaces2Citygml2Transformer = Roadspaces2CitygmlTransformer(parameters.deriveRoadspaces2Citygml2Parameters())
            val citygml2ModelResult = roadspaces2Citygml2Transformer.transform(roadspacesModel)
            citygml2ModelResult.second.serializeToJsonFile(outputDirectoryPath / ROADSPACES_TO_CITYGML2_REPORT_PATH)

            // write CityGML 2 model
            if (parameters.writeCitygml2File) {
                CitygmlWriter.writeModel(citygml2ModelResult.first, outputDirectoryPath, "citygml2_model", parameters.deriveCitygml2WriterParameters())
            }

            // transform Roadspaces model to CityGML3 model
            val roadspaces2Citygml3Transformer = Roadspaces2CitygmlTransformer(parameters.deriveRoadspaces2Citygml3Parameters())
            val citygml3ModelResult = roadspaces2Citygml3Transformer.transform(roadspacesModel)
            citygml3ModelResult.second.serializeToJsonFile(outputDirectoryPath / ROADSPACES_TO_CITYGML3_REPORT_PATH)

            // write CityGML3 model
            if (parameters.writeCitygml3File) {
                CitygmlWriter.writeModel(citygml3ModelResult.first, outputDirectoryPath, "citygml3_model", parameters.deriveCitygml3WriterParameters())
            }
        }
    }

    companion object {
        val PARAMETERS_PATH = Path("parameters.yaml")
        val REPORTS_PATH = Path("reports")
        val OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH = REPORTS_PATH / Path("01_opendrive_schema_validator_report.json")
        val OPENDRIVE_EVALUATOR_REPORT_PATH = REPORTS_PATH / Path("02_opendrive_evaluator_report.json")
        val OPENDRIVE_TO_ROADSPACES_REPORT_PATH = REPORTS_PATH / Path("03_opendrive_to_roadspaces_report.json")
        val ROADSPACES_EVALUATOR_REPORT_PATH = REPORTS_PATH / Path("04_roadspaces_evaluator_report.json")
        val ROADSPACES_TO_CITYGML2_REPORT_PATH = REPORTS_PATH / Path("05_roadspaces_to_citygml2_report.json")
        val ROADSPACES_TO_CITYGML3_REPORT_PATH = REPORTS_PATH / Path("06_roadspaces_to_citygml3_report.json")
    }
}
