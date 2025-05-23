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

package io.rtron.cli

import arrow.core.getOrElse
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.path
import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.cli.utility.CompressionFormat
import io.rtron.cli.utility.processAllFiles
import io.rtron.cli.utility.toFileExtension
import io.rtron.io.issues.getTextSummary
import io.rtron.io.serialization.serializeToJsonFile
import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.readerwriter.citygml.CitygmlWriter
import io.rtron.readerwriter.opendrive.OpendriveReader
import io.rtron.readerwriter.opendrive.OpendriveValidator
import io.rtron.readerwriter.opendrive.OpendriveWriter
import io.rtron.std.handleEmpty
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesTransformer
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlTransformer
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluator
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluator
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluatorParameters
import kotlin.io.path.Path
import kotlin.io.path.div

class SubcommandValidateOpendrive : CliktCommand(
    name = "validate-opendrive",
) {
    // Properties and Initializers
    val logger = KotlinLogging.logger {}

    override val printHelpOnEmptyArgs = true

    private val inputPath by argument(
        help = "Path to the directory containing OpenDRIVE datasets",
    ).path(mustExist = true)
    private val outputPath by argument(
        help = "Path to the output directory into which the reports are written",
    ).path()

    private val tolerance by option(help = "Allowed tolerance when comparing double values").double()
        .default(Opendrive2RoadspacesParameters.DEFAULT_NUMBER_TOLERANCE)
    private val planViewGeometryDistanceTolerance by option(
        help = "Allowed distance tolerance between two geometry elements in the plan view",
    ).double()
        .default(OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE)
    private val planViewGeometryDistanceWarningTolerance by option(
        help = "Warning distance tolerance between two geometry elements in the plan view",
    ).double()
        .default(OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_WARNING_TOLERANCE)
    private val planViewGeometryAngleTolerance by option(
        help = "Allowed angle tolerance between two geometry elements in the plan view",
    ).double()
        .default(OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE)
    private val planViewGeometryAngleWarningTolerance by option(
        help = "Warning angle tolerance between two geometry elements in the plan view",
    ).double()
        .default(OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_WARNING_TOLERANCE)

    private val discretizationStepSize by option(help = "Distance between each discretization step for curves and surfaces").double()
        .default(Roadspaces2CitygmlParameters.DEFAULT_DISCRETIZATION_STEP_SIZE)

    private val skipOpendriveExport by option(help = "Skip the export of the adjusted OpenDRIVE dataset").flag()
    private val skipCitygmlExport by option(help = "Skip the export of the CityGML dataset for visual inspection purposes").flag()

    private val compressionFormat: CompressionFormat by option(
        help = "Compress the output files with the respective compression format",
    ).enum<CompressionFormat>().default(CompressionFormat.NONE)

    // Methods
    override fun help(context: Context) = "Validate OpenDRIVE datasets"

    fun deriveOpendriveEvaluatorParameters() =
        OpendriveEvaluatorParameters(
            skipRoadShapeRemoval = OpendriveEvaluatorParameters.DEFAULT_SKIP_ROAD_SHAPE_REMOVAL,
            numberTolerance = tolerance,
            planViewGeometryDistanceTolerance = planViewGeometryDistanceTolerance,
            planViewGeometryDistanceWarningTolerance = planViewGeometryDistanceWarningTolerance,
            planViewGeometryAngleTolerance = planViewGeometryAngleTolerance,
            planViewGeometryAngleWarningTolerance = planViewGeometryAngleWarningTolerance,
        )

    fun deriveOpendrive2RoadspacesParameters() =
        Opendrive2RoadspacesParameters(
            concurrentProcessing = false,
            numberTolerance = tolerance,
            planViewGeometryDistanceTolerance = planViewGeometryDistanceTolerance,
            planViewGeometryAngleTolerance = planViewGeometryAngleTolerance,
            attributesPrefix = Opendrive2RoadspacesParameters.DEFAULT_ATTRIBUTES_PREFIX,
            deriveCrsEpsgAutomatically = false,
            crsEpsg = Opendrive2RoadspacesParameters.DEFAULT_CRS_EPSG,
            extrapolateLateralRoadShapes = Opendrive2RoadspacesParameters.DEFAULT_EXTRAPOLATE_LATERAL_ROAD_SHAPES,
            generateRoadObjectTopSurfaceExtrusions = false,
            roadObjectTopSurfaceExtrusionHeightPerObjectType =
                Opendrive2RoadspacesParameters.DEFAULT_ROAD_OBJECT_TOP_SURFACE_EXTRUSION_HEIGHT_PER_OBJECT_TYPE,
        )

    fun deriveRoadspacesEvaluatorParameters() =
        RoadspacesEvaluatorParameters(
            numberTolerance = tolerance,
            laneTransitionDistanceTolerance = RoadspacesEvaluatorParameters.DEFAULT_LANE_TRANSITION_DISTANCE_TOLERANCE,
        )

    fun deriveRoadspaces2Citygml2Parameters() =
        Roadspaces2CitygmlParameters(
            concurrentProcessing = false,
            gmlIdPrefix = Roadspaces2CitygmlParameters.DEFAULT_GML_ID_PREFIX,
            xlinkPrefix = Roadspaces2CitygmlParameters.DEFAULT_XLINK_PREFIX,
            identifierAttributesPrefix = Roadspaces2CitygmlParameters.DEFAULT_IDENTIFIER_ATTRIBUTES_PREFIX,
            geometryAttributesPrefix = Roadspaces2CitygmlParameters.DEFAULT_GEOMETRY_ATTRIBUTES_PREFIX,
            flattenGenericAttributeSets = Roadspaces2CitygmlParameters.DEFAULT_FLATTEN_GENERIC_ATTRIBUTE_SETS,
            discretizationStepSize = discretizationStepSize,
            sweepDiscretizationStepSize = Roadspaces2CitygmlParameters.DEFAULT_SWEEP_DISCRETIZATION_STEP_SIZE,
            circleSlices = Roadspaces2CitygmlParameters.DEFAULT_CIRCLE_SLICES,
            generateRandomGeometryIds = Roadspaces2CitygmlParameters.DEFAULT_GENERATE_RANDOM_GEOMETRY_IDS,
            transformAdditionalRoadLines = true,
            generateLongitudinalFillerSurfaces = false,
            generateLaneSurfaceExtrusions = false,
            laneSurfaceExtrusionHeight = Roadspaces2CitygmlParameters.DEFAULT_LANE_SURFACE_EXTRUSION_HEIGHT,
            laneSurfaceExtrusionHeightPerLaneType = Roadspaces2CitygmlParameters.DEFAULT_LANE_SURFACE_EXTRUSION_HEIGHT_PER_LANE_TYPE,
            mappingBackwardsCompatibility = true,
        )

    fun deriveRoadspaces2Citygml3Parameters() =
        Roadspaces2CitygmlParameters(
            concurrentProcessing = false,
            gmlIdPrefix = Roadspaces2CitygmlParameters.DEFAULT_GML_ID_PREFIX,
            xlinkPrefix = Roadspaces2CitygmlParameters.DEFAULT_XLINK_PREFIX,
            identifierAttributesPrefix = Roadspaces2CitygmlParameters.DEFAULT_IDENTIFIER_ATTRIBUTES_PREFIX,
            geometryAttributesPrefix = Roadspaces2CitygmlParameters.DEFAULT_GEOMETRY_ATTRIBUTES_PREFIX,
            flattenGenericAttributeSets = Roadspaces2CitygmlParameters.DEFAULT_FLATTEN_GENERIC_ATTRIBUTE_SETS,
            discretizationStepSize = discretizationStepSize,
            sweepDiscretizationStepSize = Roadspaces2CitygmlParameters.DEFAULT_SWEEP_DISCRETIZATION_STEP_SIZE,
            circleSlices = Roadspaces2CitygmlParameters.DEFAULT_CIRCLE_SLICES,
            generateRandomGeometryIds = Roadspaces2CitygmlParameters.DEFAULT_GENERATE_RANDOM_GEOMETRY_IDS,
            transformAdditionalRoadLines = true,
            generateLongitudinalFillerSurfaces = false,
            generateLaneSurfaceExtrusions = false,
            laneSurfaceExtrusionHeight = Roadspaces2CitygmlParameters.DEFAULT_LANE_SURFACE_EXTRUSION_HEIGHT,
            laneSurfaceExtrusionHeightPerLaneType = Roadspaces2CitygmlParameters.DEFAULT_LANE_SURFACE_EXTRUSION_HEIGHT_PER_LANE_TYPE,
            mappingBackwardsCompatibility = false,
        )

    override fun run() {
        processAllFiles(
            inputDirectoryPath = inputPath,
            withFilenameEndings = OpendriveReader.supportedFilenameEndings,
            outputDirectoryPath = outputPath,
        ) {
            var processStep = 1

            // validate schema of OpenDRIVE model
            val opendriveSchemaValidatorReport =
                OpendriveValidator.validateFromFile(inputFilePath).getOrElse {
                    logger.warn { it.message }
                    return@processAllFiles
                }
            opendriveSchemaValidatorReport.serializeToJsonFile(
                outputDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH),
            )
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
            processStep++
            val opendriveEvaluator = OpendriveEvaluator(deriveOpendriveEvaluatorParameters())
            val opendriveEvaluatorResult = opendriveEvaluator.evaluate(opendriveModel)
            opendriveEvaluatorResult.second.serializeToJsonFile(
                outputDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_EVALUATOR_REPORT_PATH),
            )
            if (opendriveEvaluatorResult.second.containsFatalErrors()) {
                logger.warn { opendriveEvaluatorResult.second.getTextSummary() }
                return@processAllFiles
            }
            val modifiedOpendriveModel =
                opendriveEvaluatorResult.first.handleEmpty {
                    logger.warn { opendriveEvaluatorResult.second.getTextSummary() }
                    return@processAllFiles
                }

            // write the modified OpenDRIVE model
            if (!skipOpendriveExport) {
                val filePath = outputDirectoryPath / ("opendrive.xodr" + compressionFormat.toFileExtension())
                OpendriveWriter.writeToFile(modifiedOpendriveModel, filePath)
            }

            // transform OpenDRIVE model to Roadspaces model
            processStep++
            val opendrive2RoadspacesTransformer = Opendrive2RoadspacesTransformer(deriveOpendrive2RoadspacesParameters())
            val roadspacesModelResult = opendrive2RoadspacesTransformer.transform(modifiedOpendriveModel)
            roadspacesModelResult.second.serializeToJsonFile(
                outputDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_TO_ROADSPACES_REPORT_PATH),
            )
            val roadspacesModel =
                roadspacesModelResult.first.handleEmpty {
                    logger.warn { roadspacesModelResult.second.conversion.getTextSummary() }
                    return@processAllFiles
                }

            // evaluate Roadspaces model
            processStep++
            val roadspacesEvaluator = RoadspacesEvaluator(deriveRoadspacesEvaluatorParameters())
            val roadspacesEvaluatorResults = roadspacesEvaluator.evaluate(roadspacesModel)
            roadspacesEvaluatorResults.second.serializeToJsonFile(
                outputDirectoryPath / REPORTS_PATH / (processStep.toString() + ROADSPACES_EVALUATOR_REPORT_PATH),
            )

            // transform Roadspaces model to CityGML2 model
            processStep++
            val roadspaces2Citygml2Transformer = Roadspaces2CitygmlTransformer(deriveRoadspaces2Citygml2Parameters())
            val citygml2ModelResult = roadspaces2Citygml2Transformer.transform(roadspacesModel)
            citygml2ModelResult.second.serializeToJsonFile(
                outputDirectoryPath / REPORTS_PATH / (processStep.toString() + ROADSPACES_TO_CITYGML2_REPORT_PATH),
            )

            // write CityGML 2 model
            if (!skipCitygmlExport) {
                val filePath = outputDirectoryPath / ("citygml2_model.gml" + compressionFormat.toFileExtension())
                CitygmlWriter.writeToFile(citygml2ModelResult.first, CitygmlVersion.V2_0, filePath)
            }

            // transform Roadspaces model to CityGML3 model
            processStep++
            val roadspaces2Citygml3Transformer = Roadspaces2CitygmlTransformer(deriveRoadspaces2Citygml3Parameters())
            val citygml3ModelResult = roadspaces2Citygml3Transformer.transform(roadspacesModel)
            citygml3ModelResult.second.serializeToJsonFile(
                outputDirectoryPath / REPORTS_PATH / (processStep.toString() + ROADSPACES_TO_CITYGML3_REPORT_PATH),
            )

            // write CityGML3 model
            if (!skipCitygmlExport) {
                val filePath = outputDirectoryPath / ("citygml3_model.gml" + compressionFormat.toFileExtension())
                CitygmlWriter.writeToFile(citygml3ModelResult.first, CitygmlVersion.V3_0, filePath)
            }
        }
    }

    companion object {
        val REPORTS_PATH = Path("reports")
        val OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH = "_opendrive_schema_validator_report.json"
        val OPENDRIVE_EVALUATOR_REPORT_PATH = "_opendrive_evaluator_report.json"
        val OPENDRIVE_TO_ROADSPACES_REPORT_PATH = "_opendrive_to_roadspaces_report.json"
        val ROADSPACES_EVALUATOR_REPORT_PATH = "_roadspaces_evaluator_report.json"
        val ROADSPACES_TO_CITYGML2_REPORT_PATH = "_roadspaces_to_citygml2_report.json"
        val ROADSPACES_TO_CITYGML3_REPORT_PATH = "_roadspaces_to_citygml3_report.json"
    }
}
