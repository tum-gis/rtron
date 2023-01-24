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
import io.rtron.readerwriter.citygml.CitygmlFileWriter
import io.rtron.readerwriter.opendrive.OpendriveFileReader
import io.rtron.readerwriter.opendrive.OpendriveFileWriter
import io.rtron.std.handleEmpty
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesTransformer
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlTransformer
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluator
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluator
import io.rtron.transformer.modifiers.opendrive.cropper.OpendriveCropper
import io.rtron.transformer.modifiers.opendrive.offset.adder.OpendriveOffsetAdder
import io.rtron.transformer.modifiers.opendrive.offset.resolver.OpendriveOffsetResolver
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div

class OpendriveToCitygmlProcessor(
    private val parameters: OpendriveToCitygmlParameters
) {

    // Methods

    fun process(inputPath: Path, outputPath: Path) {

        val logger = KotlinLogging.logger {}

        processAllFiles(
            inputDirectoryPath = inputPath,
            withFilenameEndings = OpendriveFileReader.supportedFilenameEndings,
            outputDirectoryPath = outputPath
        ) {
            val outputSubDirectoryPath = outputDirectoryPath / "citygml_${parameters.getCitygmlWriteVersion()}"
            outputSubDirectoryPath.createDirectories()
            // check if parameters are valid
            parameters.isValid().onLeft { messages ->
                messages.forEach { logger.warn("Parameters are not valid: $it") }
                return@processAllFiles
            }
            // write the parameters as yaml file
            val parametersText = Yaml.default.encodeToString(OpendriveToCitygmlParameters.serializer(), parameters)
            (outputSubDirectoryPath / PARAMETERS_PATH).toFile().writeText(parametersText)

            // read OpenDRIVE model
            val opendriveFileReader = OpendriveFileReader.of(inputFilePath)
                .getOrElse { logger.warn(it.message); return@processAllFiles }
            val opendriveSchemaValidatorReport = opendriveFileReader.runSchemaValidation()
            opendriveSchemaValidatorReport.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH)
            if (opendriveSchemaValidatorReport.validationProcessAborted())
                return@processAllFiles
            val opendriveModel = opendriveFileReader.readModel()
                .getOrElse { logger.warn(it.message); return@processAllFiles }

            // evaluate OpenDRIVE model
            val opendriveEvaluator = OpendriveEvaluator(parameters.deriveOpendriveEvaluatorParameters())
            val opendriveEvaluatorResult = opendriveEvaluator.evaluate(opendriveModel)
            opendriveEvaluatorResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_EVALUATOR_REPORT_PATH)
            val modifiedOpendriveModel = opendriveEvaluatorResult.first.handleEmpty {
                logger.warn(opendriveEvaluatorResult.second.getTextSummary())
                return@processAllFiles
            }

            // offset OpenDRIVE model
            val opendriveOffsetAdder = OpendriveOffsetAdder(parameters.deriveOpendriveOffsetAdderParameters())
            val opendriveOffsetAddedResult = opendriveOffsetAdder.modify(modifiedOpendriveModel)
            opendriveOffsetAddedResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_OFFSET_ADDER_REPORT_PATH)

            // resolve the offset
            val opendriveOffsetResolver = OpendriveOffsetResolver()
            val opendriveOffsetResolvedResult = opendriveOffsetResolver.modify(opendriveOffsetAddedResult.first)
            opendriveOffsetResolvedResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_OFFSET_RESOLVER_REPORT_PATH)

            // crop the OpenDRIVE model
            val opendriveCropper = OpendriveCropper(parameters.deriveOpendriveCropperParameters())
            val opendriveCroppedResult = opendriveCropper.modify(opendriveOffsetAddedResult.first)
            opendriveCroppedResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_CROP_REPORT_PATH)
            val opendriveCropped = opendriveCroppedResult.first.handleEmpty {
                logger.warn("OpendriveCropper: ${opendriveCroppedResult.second.message}")
                return@processAllFiles
            }

            // write offset OpenDRIVE model
            val opendriveFileWriter = OpendriveFileWriter(parameters.deriveOpendriveWriterParameters())
            opendriveFileWriter.write(opendriveCropped, outputSubDirectoryPath)

            // transform OpenDRIVE model to Roadspaces model
            val opendrive2RoadspacesTransformer = Opendrive2RoadspacesTransformer(parameters.deriveOpendrive2RoadspacesParameters())
            val roadspacesModelResult = opendrive2RoadspacesTransformer.transform(opendriveCropped, inputFileIdentifier)
            roadspacesModelResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_TO_ROADSPACES_REPORT_PATH)
            val roadspacesModel = roadspacesModelResult.first.handleEmpty {
                logger.warn("Opendrive2RoadspacesTransformer: ${roadspacesModelResult.second.conversion.getTextSummary()}")
                return@processAllFiles
            }

            // evaluate Roadspaces model
            val roadspacesEvaluator = RoadspacesEvaluator(parameters.deriveRoadspacesEvaluatorParameters())
            val roadspacesEvaluatorResults = roadspacesEvaluator.evaluate(roadspacesModel)
            roadspacesEvaluatorResults.second.serializeToJsonFile(outputSubDirectoryPath / ROADSPACES_EVALUATOR_REPORT_PATH)

            // transform Roadspaces model to OpenDRIVE model
            val roadpaces2CitygmlTransformer = Roadspaces2CitygmlTransformer(parameters.deriveRoadspaces2CitygmlParameters())
            val citygmlModelResult = roadpaces2CitygmlTransformer.transform(roadspacesModel)
            citygmlModelResult.second.serializeToJsonFile(outputSubDirectoryPath / ROADSPACES_TO_CITYGML_REPORT_PATH)

            // write CityGML model
            val citygmlFileWriter = CitygmlFileWriter(parameters.deriveCitygmlWriterParameters())
            citygmlFileWriter.writeModel(citygmlModelResult.first, outputSubDirectoryPath, "citygml_model")
        }
    }

    companion object {
        val PARAMETERS_PATH = Path("parameters.yaml")
        val REPORTS_PATH = Path("reports")
        val OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH = REPORTS_PATH / Path("01_opendrive_schema_validator_report.json")
        val OPENDRIVE_EVALUATOR_REPORT_PATH = REPORTS_PATH / Path("02_opendrive_evaluator_report.json")
        val OPENDRIVE_OFFSET_ADDER_REPORT_PATH = REPORTS_PATH / Path("03_opendrive_offset_adder_report.json")
        val OPENDRIVE_OFFSET_RESOLVER_REPORT_PATH = REPORTS_PATH / Path("04_opendrive_offset_resolver_report.json")
        val OPENDRIVE_CROP_REPORT_PATH = REPORTS_PATH / Path("05_opendrive_crop_report.json")
        val OPENDRIVE_TO_ROADSPACES_REPORT_PATH = REPORTS_PATH / Path("06_opendrive_to_roadspaces_report.json")
        val ROADSPACES_EVALUATOR_REPORT_PATH = REPORTS_PATH / Path("07_roadspaces_evaluator_report.json")
        val ROADSPACES_TO_CITYGML_REPORT_PATH = REPORTS_PATH / Path("08_roadspaces_to_citygml_report.json")
    }
}
