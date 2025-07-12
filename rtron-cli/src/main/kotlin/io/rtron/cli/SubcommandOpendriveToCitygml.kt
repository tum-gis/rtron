/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.pair
import com.github.ajalt.clikt.parameters.options.triple
import com.github.ajalt.clikt.parameters.options.unique
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.cli.utility.CompressionFormat
import io.rtron.cli.utility.processAllFiles
import io.rtron.cli.utility.toFileExtension
import io.rtron.io.issues.getTextSummary
import io.rtron.io.serialization.serializeToJsonFile
import io.rtron.model.opendrive.objects.EObjectType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.road.LaneType
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
import io.rtron.transformer.modifiers.opendrive.applier.OpendriveApplier
import io.rtron.transformer.modifiers.opendrive.applier.OpendriveApplierParameters
import io.rtron.transformer.modifiers.opendrive.applier.OpendriveApplierRules
import io.rtron.transformer.modifiers.opendrive.cropper.OpendriveCropper
import io.rtron.transformer.modifiers.opendrive.cropper.OpendriveCropperParameters
import io.rtron.transformer.modifiers.opendrive.offset.adder.OpendriveOffsetAdder
import io.rtron.transformer.modifiers.opendrive.offset.adder.OpendriveOffsetAdderParameters
import io.rtron.transformer.modifiers.opendrive.offset.resolver.OpendriveOffsetResolver
import io.rtron.transformer.modifiers.opendrive.remover.OpendriveObjectRemover
import io.rtron.transformer.modifiers.opendrive.remover.OpendriveObjectRemoverParameters
import io.rtron.transformer.modifiers.opendrive.reprojector.OpendriveReprojector
import io.rtron.transformer.modifiers.opendrive.reprojector.OpendriveReprojectorParameters
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.readText

class SubcommandOpendriveToCitygml :
    CliktCommand(
        name = "opendrive-to-citygml",
    ) {
    // Properties and Initializers
    val logger = KotlinLogging.logger {}

    override val printHelpOnEmptyArgs = true

    private val inputPath by argument(
        help = "Path to the directory containing OpenDRIVE datasets",
    ).path(mustExist = true)
    private val outputPath by argument(
        help = "Path to the output directory into which the transformed CityGML models are written",
    ).path()

    private val applyRulesPath by option(
        help = "Path to the a JSON file containing a list of rules for modifying the OpenDRIVE dataset",
    ).path(mustExist = true)

    private val skipRoadShapeRemoval by option(
        help = "Skip the removal of the road shape, if a lateral lane offset exists (not compliant to standard)",
    ).flag()

    private val convertToCitygml2 by option(help = "Convert to CityGML 2.0 (otherwise CityGML 3.0)").flag()

    private val tolerance by option(help = "Allowed tolerance when comparing double values")
        .double()
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
    private val reprojectModel by option(help = "Reproject the geometries into a different geospatial coordinate reference system").flag()
    private val crsEpsg by option(help = "EPSG code of the coordinate reference system used in the OpenDRIVE datasets")
        .int()
        .default(Opendrive2RoadspacesParameters.DEFAULT_CRS_EPSG)
    private val addOffset by option(help = "Offset values by which the model is translated along the x, y, and z axis").double().triple()

    private val cropPolygon by option(help = "2D polygon outline for cropping the OpenDRIVE dataset").double().pair().multiple()
    private val removeRoadObjectOfType by option(help = "Remove road object of a specific type").enum<EObjectType>().multiple().unique()
    private val skipRoadObjectTopSurfaceExtrusions by option(
        help = "Skip extruding the top surfaces of road objects for traffic space solids",
    ).flag()
    private val roadObjectTopSurfaceExtrusionHeightPerObjectType: Map<RoadObjectType, Double> by option(
        help = "Comma-separated list of enum=value pairs, e.g. PARKING_SPACE=4.5,CROSSWALK=2.5",
    ).convert { input ->
        val resultMap = Opendrive2RoadspacesParameters.DEFAULT_ROAD_OBJECT_TOP_SURFACE_EXTRUSION_HEIGHT_PER_OBJECT_TYPE.toMutableMap()

        input.split(",").forEach { entry ->
            if (entry.isNotBlank()) {
                val (key, value) = entry.split("=")
                val roadObjectType =
                    try {
                        RoadObjectType.valueOf(key.uppercase())
                    } catch (e: IllegalArgumentException) {
                        fail("Invalid OpenDRIVE road object type: $key")
                    }
                val doubleValue =
                    value.toDoubleOrNull()
                        ?: fail("Invalid value for $key: $value is not a number")
                resultMap[roadObjectType] = doubleValue
            }
        }
        resultMap.toMap()
    }.default(Opendrive2RoadspacesParameters.DEFAULT_ROAD_OBJECT_TOP_SURFACE_EXTRUSION_HEIGHT_PER_OBJECT_TYPE)

    private val discretizationStepSize by option(help = "Distance between each discretization step for curves and surfaces")
        .double()
        .default(Roadspaces2CitygmlParameters.DEFAULT_DISCRETIZATION_STEP_SIZE)
    private val sweepDiscretizationStepSize by option(
        help = "Distance between each discretization step for solid geometries of ParametricSweep3D",
    ).double()
        .default(Roadspaces2CitygmlParameters.DEFAULT_SWEEP_DISCRETIZATION_STEP_SIZE)
    private val circleSlices by option(help = "Number of discretization points for a circle or cylinder")
        .int()
        .default(Roadspaces2CitygmlParameters.DEFAULT_CIRCLE_SLICES)
    private val generateRandomGeometryIds by option(help = "True, if random ids shall be generated for the gml geometries").flag()
    private val transformAdditionalRoadLines by option(
        help = "If true, additional road lines, such as the reference line, lane boundaries, etc., are also transformed",
    ).flag()

    private val skipLaneSurfaceExtrusions by option(
        help = "Skip extruding lane surfaces for traffic space solids",
    ).flag()
    private val laneSurfaceExtrusionHeight by option(help = "Default extrusion height for traffic space solids (in meters)")
        .double()
        .default(Roadspaces2CitygmlParameters.DEFAULT_LANE_SURFACE_EXTRUSION_HEIGHT)

    private val laneSurfaceExtrusionHeightPerLaneType: Map<LaneType, Double> by option(
        help = "Comma-separated list of enum=value pairs, e.g. DRIVING=4.5,SIDEWALK=2.5",
    ).convert { input ->
        val resultMap = Roadspaces2CitygmlParameters.DEFAULT_LANE_SURFACE_EXTRUSION_HEIGHT_PER_LANE_TYPE.toMutableMap()

        input.split(",").forEach { entry ->
            if (entry.isNotBlank()) {
                val (key, value) = entry.split("=")
                val laneType =
                    try {
                        LaneType.valueOf(key.uppercase())
                    } catch (e: IllegalArgumentException) {
                        fail("Invalid OpenDRIVE lane type: $key")
                    }
                val doubleValue =
                    value.toDoubleOrNull()
                        ?: fail("Invalid value for $key: $value is not a number")
                resultMap[laneType] = doubleValue
            }
        }
        resultMap.toMap()
    }.default(Roadspaces2CitygmlParameters.DEFAULT_LANE_SURFACE_EXTRUSION_HEIGHT_PER_LANE_TYPE)

    private val compressionFormat: CompressionFormat by option(
        help = "Compress the output files with the respective compression format",
    ).enum<CompressionFormat>()
        .default(CompressionFormat.GZ)

    // Methods
    override fun help(context: Context) = "Transform OpenDRIVE datasets to CityGML"

    fun isValid(): Either<List<String>, Unit> {
        val issues = mutableListOf<String>()
        if (cropPolygon.isNotEmpty() && cropPolygon.size < 3) {
            issues += "cropPolygon must be empty or have at least three values for representing a triangle"
        }

        return if (issues.isEmpty()) {
            Unit.right()
        } else {
            issues.left()
        }
    }

    fun getCitygmlWriteVersion(): CitygmlVersion = if (convertToCitygml2) CitygmlVersion.V2_0 else CitygmlVersion.V3_0

    fun deriveOpendriveEvaluatorParameters() =
        OpendriveEvaluatorParameters(
            skipRoadShapeRemoval = skipRoadShapeRemoval,
            numberTolerance = tolerance,
            planViewGeometryDistanceTolerance = planViewGeometryDistanceTolerance,
            planViewGeometryDistanceWarningTolerance = planViewGeometryDistanceWarningTolerance,
            planViewGeometryAngleTolerance = planViewGeometryAngleTolerance,
            planViewGeometryAngleWarningTolerance = planViewGeometryAngleWarningTolerance,
        )

    fun deriveOpendriveObjectRemoverParameters() =
        OpendriveObjectRemoverParameters(
            removeRoadObjectsWithoutType = OpendriveObjectRemoverParameters.DEFAULT_REMOVE_ROAD_OBJECTS_WITHOUT_TYPE,
            removeRoadObjectsOfTypes = removeRoadObjectOfType,
        )

    fun deriveOpendriveApplierParameters() =
        OpendriveApplierParameters(
            fillEmptyRoadNameWithIds = OpendriveApplierParameters.DEFAULT_FILL_EMPTY_ROAD_NAMES_WITH_ID,
        )

    fun deriveOpendriveOffsetAdderParameters() =
        OpendriveOffsetAdderParameters(
            offsetX = addOffset.toOption().fold({ OpendriveOffsetAdderParameters.DEFAULT_OFFSET_X }, { it.first }),
            offsetY = addOffset.toOption().fold({ OpendriveOffsetAdderParameters.DEFAULT_OFFSET_Y }, { it.second }),
            offsetZ = addOffset.toOption().fold({ OpendriveOffsetAdderParameters.DEFAULT_OFFSET_Z }, { it.third }),
            offsetHeading = OpendriveOffsetAdderParameters.DEFAULT_OFFSET_HEADING,
        )

    fun deriveOpendriveReprojectorParameters() =
        OpendriveReprojectorParameters(
            reprojectModel = reprojectModel,
            targetCrsEpsg = crsEpsg,
            deviationWarningTolerance = OpendriveReprojectorParameters.DEFAULT_DEVIATION_WARNING_TOLERANCE,
        )

    fun deriveOpendriveCropperParameters() =
        OpendriveCropperParameters(
            numberTolerance = tolerance,
            cropPolygonX = cropPolygon.map { it.first },
            cropPolygonY = cropPolygon.map { it.second },
        )

    fun deriveOpendrive2RoadspacesParameters() =
        Opendrive2RoadspacesParameters(
            concurrentProcessing = false,
            numberTolerance = tolerance,
            planViewGeometryDistanceTolerance = planViewGeometryDistanceTolerance,
            planViewGeometryAngleTolerance = planViewGeometryAngleTolerance,
            attributesPrefix = Opendrive2RoadspacesParameters.DEFAULT_ATTRIBUTES_PREFIX,
            deriveCrsEpsgAutomatically = true,
            crsEpsg = crsEpsg,
            extrapolateLateralRoadShapes =
                Opendrive2RoadspacesParameters.DEFAULT_EXTRAPOLATE_LATERAL_ROAD_SHAPES,
            generateRoadObjectTopSurfaceExtrusions = !skipRoadObjectTopSurfaceExtrusions,
            roadObjectTopSurfaceExtrusionHeightPerObjectType = roadObjectTopSurfaceExtrusionHeightPerObjectType,
        )

    fun deriveRoadspacesEvaluatorParameters() =
        RoadspacesEvaluatorParameters(
            numberTolerance = tolerance,
            laneTransitionDistanceTolerance = RoadspacesEvaluatorParameters.DEFAULT_LANE_TRANSITION_DISTANCE_TOLERANCE,
        )

    fun deriveRoadspaces2CitygmlParameters() =
        Roadspaces2CitygmlParameters(
            concurrentProcessing = false,
            gmlIdPrefix = Roadspaces2CitygmlParameters.DEFAULT_GML_ID_PREFIX,
            xlinkPrefix = Roadspaces2CitygmlParameters.DEFAULT_XLINK_PREFIX,
            identifierAttributesPrefix = Roadspaces2CitygmlParameters.DEFAULT_IDENTIFIER_ATTRIBUTES_PREFIX,
            geometryAttributesPrefix = Roadspaces2CitygmlParameters.DEFAULT_GEOMETRY_ATTRIBUTES_PREFIX,
            flattenGenericAttributeSets = Roadspaces2CitygmlParameters.DEFAULT_FLATTEN_GENERIC_ATTRIBUTE_SETS,
            discretizationStepSize = discretizationStepSize,
            sweepDiscretizationStepSize = sweepDiscretizationStepSize,
            circleSlices = circleSlices,
            generateRandomGeometryIds = generateRandomGeometryIds,
            transformAdditionalRoadLines = transformAdditionalRoadLines,
            generateLongitudinalFillerSurfaces = Roadspaces2CitygmlParameters.DEFAULT_GENERATE_LONGITUDINAL_FILLER_SURFACES,
            generateLaneSurfaceExtrusions = !skipLaneSurfaceExtrusions,
            laneSurfaceExtrusionHeight = laneSurfaceExtrusionHeight,
            laneSurfaceExtrusionHeightPerLaneType = laneSurfaceExtrusionHeightPerLaneType,
            mappingBackwardsCompatibility = convertToCitygml2,
        )

    override fun run() {
        isValid().onLeft { issues ->
            issues.forEach { logger.warn { "Parameters are not valid: $it" } }
            return
        }

        processAllFiles(
            inputDirectoryPath = inputPath,
            withFilenameEndings = OpendriveReader.supportedFilenameEndings,
            outputDirectoryPath = outputPath,
        ) {
            val outputSubDirectoryPath = outputDirectoryPath / "citygml_${getCitygmlWriteVersion()}"
            outputSubDirectoryPath.createDirectories()
            var processStep = 1

            // validate schema of OpenDRIVE model
            val opendriveSchemaValidatorReport =
                OpendriveValidator.validateFromFile(inputFilePath).getOrElse {
                    logger.warn { it.message }
                    return@processAllFiles
                }
            opendriveSchemaValidatorReport.serializeToJsonFile(
                outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH),
            )
            if (opendriveSchemaValidatorReport.validationProcessAborted()) {
                return@processAllFiles
            }

            // read of OpenDRIVE model
            val opendriveModel =
                OpendriveReader
                    .readFromFile(inputFilePath)
                    .getOrElse {
                        logger.warn { it.message }
                        return@processAllFiles
                    }

            // evaluate OpenDRIVE model
            processStep++
            val opendriveEvaluator =
                OpendriveEvaluator(
                    deriveOpendriveEvaluatorParameters(),
                )
            val opendriveEvaluatorResult = opendriveEvaluator.evaluate(opendriveModel)
            opendriveEvaluatorResult.second.serializeToJsonFile(
                outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_EVALUATOR_REPORT_PATH),
            )
            var modifiedOpendriveModel =
                opendriveEvaluatorResult.first.handleEmpty {
                    logger.warn { opendriveEvaluatorResult.second.getTextSummary() }
                    return@processAllFiles
                }

            // apply predefined rules
            applyRulesPath.toOption().onSome {
                processStep++
                val opendriveApplierRules: OpendriveApplierRules = Json.decodeFromString<OpendriveApplierRules>(it.readText())
                val opendriveApplier = OpendriveApplier(deriveOpendriveApplierParameters())
                val opendriveApplierResult = opendriveApplier.modify(modifiedOpendriveModel, opendriveApplierRules)
                opendriveApplierResult.second.serializeToJsonFile(
                    outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_APPLIER_REPORT_PATH),
                )
                modifiedOpendriveModel = opendriveApplierResult.first
            }

            // reproject OpenDRIVE model
            if (reprojectModel) {
                processStep++
                val opendriveReprojector = OpendriveReprojector(deriveOpendriveReprojectorParameters())
                val opendriveReprojectorResult = opendriveReprojector.modify(modifiedOpendriveModel)
                opendriveReprojectorResult.second.serializeToJsonFile(
                    outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_REPROJECTOR_REPORT_PATH),
                )
                modifiedOpendriveModel =
                    opendriveReprojectorResult.first.handleEmpty {
                        logger.warn { "OpendriveReprojector: ${opendriveReprojectorResult.second.message}" }
                        return@processAllFiles
                    }
            }

            // offset OpenDRIVE model
            if (addOffset.toOption().isSome()) {
                processStep++
                val opendriveOffsetAdder = OpendriveOffsetAdder(deriveOpendriveOffsetAdderParameters())
                val opendriveOffsetAddedResult = opendriveOffsetAdder.modify(modifiedOpendriveModel)
                opendriveOffsetAddedResult.second.serializeToJsonFile(
                    outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_OFFSET_ADDER_REPORT_PATH),
                )
                modifiedOpendriveModel = opendriveOffsetAddedResult.first
            }

            // resolve the offset
            if (modifiedOpendriveModel.header.offset.isSome()) {
                processStep++
                val opendriveOffsetResolver = OpendriveOffsetResolver()
                val opendriveOffsetResolvedResult = opendriveOffsetResolver.modify(modifiedOpendriveModel)
                opendriveOffsetResolvedResult.second.serializeToJsonFile(
                    outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_OFFSET_RESOLVER_REPORT_PATH),
                )
                modifiedOpendriveModel = opendriveOffsetResolvedResult.first
            }

            // crop the OpenDRIVE model
            if (cropPolygon.isNotEmpty()) {
                processStep++
                val opendriveCropper = OpendriveCropper(deriveOpendriveCropperParameters())
                val opendriveCroppedResult = opendriveCropper.modify(modifiedOpendriveModel)
                opendriveCroppedResult.second.serializeToJsonFile(
                    outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_CROP_REPORT_PATH),
                )
                modifiedOpendriveModel =
                    opendriveCroppedResult.first.handleEmpty {
                        logger.warn { "OpendriveCropper: ${opendriveCroppedResult.second.message}" }
                        return@processAllFiles
                    }
            }

            // remove objects from OpenDRIVE model
            if (removeRoadObjectOfType.isNotEmpty()) {
                processStep++
                val opendriveObjectRemover = OpendriveObjectRemover(deriveOpendriveObjectRemoverParameters())
                val opendriveRemovedObjectResult = opendriveObjectRemover.modify(modifiedOpendriveModel)
                opendriveRemovedObjectResult.second.serializeToJsonFile(
                    outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_OBJECT_REMOVER_REPORT_PATH),
                )
                modifiedOpendriveModel = opendriveRemovedObjectResult.first
            }

            // write the modified OpenDRIVE model
            val opendriveFilePath = outputSubDirectoryPath / ("opendrive.xodr" + compressionFormat.toFileExtension())
            OpendriveWriter.writeToFile(modifiedOpendriveModel, opendriveFilePath)

            // transform OpenDRIVE model to Roadspaces model
            processStep++
            val opendrive2RoadspacesTransformer = Opendrive2RoadspacesTransformer(deriveOpendrive2RoadspacesParameters())
            val roadspacesModelResult = opendrive2RoadspacesTransformer.transform(modifiedOpendriveModel)
            roadspacesModelResult.second.serializeToJsonFile(
                outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + OPENDRIVE_TO_ROADSPACES_REPORT_PATH),
            )
            val roadspacesModel =
                roadspacesModelResult.first.handleEmpty {
                    logger.warn { "Opendrive2RoadspacesTransformer: ${roadspacesModelResult.second.conversion.getTextSummary()}" }
                    return@processAllFiles
                }

            // evaluate Roadspaces model
            processStep++
            val roadspacesEvaluator = RoadspacesEvaluator(deriveRoadspacesEvaluatorParameters())
            val roadspacesEvaluatorResults = roadspacesEvaluator.evaluate(roadspacesModel)
            roadspacesEvaluatorResults.second.serializeToJsonFile(
                outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + ROADSPACES_EVALUATOR_REPORT_PATH),
            )

            // transform Roadspaces model to OpenDRIVE model
            processStep++
            val roadpaces2CitygmlTransformer = Roadspaces2CitygmlTransformer(deriveRoadspaces2CitygmlParameters())
            val citygmlModelResult = roadpaces2CitygmlTransformer.transform(roadspacesModel)
            citygmlModelResult.second.serializeToJsonFile(
                outputSubDirectoryPath / REPORTS_PATH / (processStep.toString() + ROADSPACES_TO_CITYGML_REPORT_PATH),
            )

            // write CityGML model
            CitygmlWriter.writeToFile(
                citygmlModelResult.first,
                getCitygmlWriteVersion(),
                outputSubDirectoryPath / ("citygml_model.gml" + compressionFormat.toFileExtension()),
            )
        }
    }

    companion object {
        val REPORTS_PATH = Path("reports")
        val OPENDRIVE_SCHEMA_VALIDATOR_REPORT_PATH = "_opendrive_schema_validator_report.json"
        val OPENDRIVE_EVALUATOR_REPORT_PATH = "_opendrive_evaluator_report.json"
        val OPENDRIVE_APPLIER_REPORT_PATH = "_opendrive_applier_report.json"
        val OPENDRIVE_REPROJECTOR_REPORT_PATH = "_opendrive_reprojector_report.json"
        val OPENDRIVE_OFFSET_ADDER_REPORT_PATH = "_opendrive_offset_adder_report.json"
        val OPENDRIVE_OFFSET_RESOLVER_REPORT_PATH = "_opendrive_offset_resolver_report.json"
        val OPENDRIVE_CROP_REPORT_PATH = "_opendrive_crop_report.json"
        val OPENDRIVE_OBJECT_REMOVER_REPORT_PATH = "_opendrive_object_remover_report.json"
        val OPENDRIVE_TO_ROADSPACES_REPORT_PATH = "_opendrive_to_roadspaces_report.json"
        val ROADSPACES_EVALUATOR_REPORT_PATH = "_roadspaces_evaluator_report.json"
        val ROADSPACES_TO_CITYGML_REPORT_PATH = "_roadspaces_to_citygml_report.json"
    }
}
