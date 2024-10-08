/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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
import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.io.issues.getTextSummary
import io.rtron.io.serialization.serializeToJsonFile
import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlWriter
import io.rtron.readerwriter.opendrive.OpendriveReader
import io.rtron.readerwriter.opendrive.OpendriveValidator
import io.rtron.readerwriter.opendrive.OpendriveWriter
import io.rtron.std.handleEmpty
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesTransformer
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlTransformer
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluator
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluator
import io.rtron.transformer.modifiers.opendrive.cropper.OpendriveCropper
import io.rtron.transformer.modifiers.opendrive.offset.adder.OpendriveOffsetAdder
import io.rtron.transformer.modifiers.opendrive.offset.resolver.OpendriveOffsetResolver
import io.rtron.transformer.modifiers.opendrive.remover.OpendriveObjectRemover
import io.rtron.transformer.modifiers.opendrive.reprojector.OpendriveReprojector
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div

class OpendriveToCitygmlProcessor(
    private val parameters: OpendriveToCitygmlParameters,
) {
    // Methods

    fun process(
        inputPath: Path,
        outputPath: Path,
    ) {
        val logger = KotlinLogging.logger {}

        processAllFiles(
            inputDirectoryPath = inputPath,
            withFilenameEndings = OpendriveReader.supportedFilenameEndings,
            outputDirectoryPath = outputPath,
        ) {
            val outputSubDirectoryPath = outputDirectoryPath / "citygml_${parameters.getCitygmlWriteVersion()}"
            outputSubDirectoryPath.createDirectories()
            // check if parameters are valid
            parameters.isValid().onLeft { issues ->
                issues.forEach { logger.warn { "Parameters are not valid: $it" } }
                return@processAllFiles
            }
            // write the parameters as yaml file
            val parametersText = Yaml.default.encodeToString(OpendriveToCitygmlParameters.serializer(), parameters)
            (outputSubDirectoryPath / PARAMETERS_PATH).toFile().writeText(parametersText)

            // validate schema of OpenDRIVE model
            val opendriveSchemaValidatorReport =
                OpendriveValidator.validateFromFile(inputFilePath).getOrElse {
                    logger.warn { it.message }
                    return@processAllFiles
                }
            opendriveSchemaValidatorReport.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH)
            if (opendriveSchemaValidatorReport.validationProcessAborted()) {
                return@processAllFiles
            }
            // read of OpenDRIVE model
            val opendriveModel =
                OpendriveReader.readFromFile(inputFilePath)
                    .getOrElse {
                        logger.warn { it.message }
                        return@processAllFiles
                    }

            // evaluate OpenDRIVE model
            val opendriveEvaluator = OpendriveEvaluator(parameters.deriveOpendriveEvaluatorParameters())
            val opendriveEvaluatorResult = opendriveEvaluator.evaluate(opendriveModel)
            opendriveEvaluatorResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_EVALUATOR_REPORT_PATH)
            val modifiedOpendriveModel =
                opendriveEvaluatorResult.first.handleEmpty {
                    logger.warn { opendriveEvaluatorResult.second.getTextSummary() }
                    return@processAllFiles
                }

            // reproject OpenDRIVE model
            val opendriveReprojector = OpendriveReprojector(parameters.deriveOpendriveReprojectorParameters())
            val opendriveReprojectorResult = opendriveReprojector.modify(modifiedOpendriveModel)
            opendriveReprojectorResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_REPROJECTOR_REPORT_PATH)
            val opendriveReprojected =
                opendriveReprojectorResult.first.handleEmpty {
                    logger.warn { "OpendriveReprojector: ${opendriveReprojectorResult.second.message}" }
                    return@processAllFiles
                }

            // offset OpenDRIVE model
            val opendriveOffsetAdder = OpendriveOffsetAdder(parameters.deriveOpendriveOffsetAdderParameters())
            val opendriveOffsetAddedResult = opendriveOffsetAdder.modify(opendriveReprojected)
            opendriveOffsetAddedResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_OFFSET_ADDER_REPORT_PATH)

            // resolve the offset
            val opendriveOffsetResolver = OpendriveOffsetResolver()
            val opendriveOffsetResolvedResult = opendriveOffsetResolver.modify(opendriveOffsetAddedResult.first)
            opendriveOffsetResolvedResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_OFFSET_RESOLVER_REPORT_PATH)

            // crop the OpenDRIVE model
            val opendriveCropper = OpendriveCropper(parameters.deriveOpendriveCropperParameters())
            val opendriveCroppedResult = opendriveCropper.modify(opendriveOffsetResolvedResult.first)
            opendriveCroppedResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_CROP_REPORT_PATH)
            val opendriveCropped =
                opendriveCroppedResult.first.handleEmpty {
                    logger.warn { "OpendriveCropper: ${opendriveCroppedResult.second.message}" }
                    return@processAllFiles
                }

            // remove objects from OpenDRIVE model
            val opendriveObjectRemover = OpendriveObjectRemover(parameters.deriveOpendriveObjectRemoverParameters())
            val opendriveRemovedObjectResult = opendriveObjectRemover.modify(opendriveCropped)
            opendriveRemovedObjectResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_OBJECT_REMOVER_REPORT_PATH)

            // write modified OpenDRIVE model
            val opendriveFilePath = outputSubDirectoryPath / ("opendrive.xodr" + parameters.compressionFormat.toFileExtension())
            OpendriveWriter.writeToFile(opendriveRemovedObjectResult.first, opendriveFilePath)

            // transform OpenDRIVE model to Roadspaces model
            val opendrive2RoadspacesTransformer = Opendrive2RoadspacesTransformer(parameters.deriveOpendrive2RoadspacesParameters())
            val roadspacesModelResult = opendrive2RoadspacesTransformer.transform(opendriveRemovedObjectResult.first)
            roadspacesModelResult.second.serializeToJsonFile(outputSubDirectoryPath / OPENDRIVE_TO_ROADSPACES_REPORT_PATH)
            val roadspacesModel =
                roadspacesModelResult.first.handleEmpty {
                    logger.warn { "Opendrive2RoadspacesTransformer: ${roadspacesModelResult.second.conversion.getTextSummary()}" }
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
            CitygmlWriter.writeToFile(
                citygmlModelResult.first,
                parameters.getCitygmlWriteVersion(),
                outputSubDirectoryPath / ("citygml_model.gml" + parameters.compressionFormat.toFileExtension()),
            )
        }
    }

    companion object {
        val PARAMETERS_PATH = Path("parameters.yaml")
        val REPORTS_PATH = Path("reports")
        val OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH = REPORTS_PATH / Path("01_opendrive_schema_validator_report.json")
        val OPENDRIVE_EVALUATOR_REPORT_PATH = REPORTS_PATH / Path("02_opendrive_evaluator_report.json")
        val OPENDRIVE_REPROJECTOR_REPORT_PATH = REPORTS_PATH / Path("03_opendrive_reprojector_report.json")
        val OPENDRIVE_OFFSET_ADDER_REPORT_PATH = REPORTS_PATH / Path("04_opendrive_offset_adder_report.json")
        val OPENDRIVE_OFFSET_RESOLVER_REPORT_PATH = REPORTS_PATH / Path("05_opendrive_offset_resolver_report.json")
        val OPENDRIVE_CROP_REPORT_PATH = REPORTS_PATH / Path("06_opendrive_crop_report.json")
        val OPENDRIVE_OBJECT_REMOVER_REPORT_PATH = REPORTS_PATH / Path("07_opendrive_object_remover_report.json")
        val OPENDRIVE_TO_ROADSPACES_REPORT_PATH = REPORTS_PATH / Path("08_opendrive_to_roadspaces_report.json")
        val ROADSPACES_EVALUATOR_REPORT_PATH = REPORTS_PATH / Path("09_roadspaces_evaluator_report.json")
        val ROADSPACES_TO_CITYGML_REPORT_PATH = REPORTS_PATH / Path("10_roadspaces_to_citygml_report.json")
    }
}
