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

import arrow.core.toOption
import com.charleskorn.kaml.Yaml
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
import io.rtron.main.processor.CompressionFormat
import io.rtron.main.processor.OpendriveToCitygmlParameters
import io.rtron.main.processor.OpendriveToCitygmlProcessor
import io.rtron.model.opendrive.objects.EObjectType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.road.LaneType
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.modifiers.opendrive.offset.adder.OpendriveOffsetAdderParameters

class SubcommandOpendriveToCitygml :
    CliktCommand(
        name = "opendrive-to-citygml",
    ) {
    // Properties and Initializers
    override val printHelpOnEmptyArgs = true

    private val parametersPath by option(
        help = "Path to a YAML file containing the parameters of the process.",
    ).path(mustExist = true)

    private val inputPath by argument(
        help = "Path to the directory containing OpenDRIVE datasets",
    ).path(mustExist = true)
    private val outputPath by argument(
        help = "Path to the output directory into which the transformed CityGML models are written",
    ).path()

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
    private val addOffset by option(help = "Offset values by which the model is translated along the x, y, and z axis")
        .double()
        .triple()
        .default(
            Triple(
                OpendriveOffsetAdderParameters.DEFAULT_OFFSET_X,
                OpendriveOffsetAdderParameters.DEFAULT_OFFSET_Y,
                OpendriveOffsetAdderParameters.DEFAULT_OFFSET_Z,
            ),
        )
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

    override fun run() {
        val parameters =
            parametersPath.toOption().fold({
                OpendriveToCitygmlParameters(
                    convertToCitygml2 = convertToCitygml2,
                    skipRoadShapeRemoval = skipRoadShapeRemoval,
                    tolerance = tolerance,
                    planViewGeometryDistanceTolerance = planViewGeometryDistanceTolerance,
                    planViewGeometryDistanceWarningTolerance = planViewGeometryDistanceWarningTolerance,
                    planViewGeometryAngleTolerance = planViewGeometryAngleTolerance,
                    planViewGeometryAngleWarningTolerance = planViewGeometryAngleWarningTolerance,
                    reprojectModel = reprojectModel,
                    crsEpsg = crsEpsg,
                    offsetX = addOffset.first,
                    offsetY = addOffset.second,
                    offsetZ = addOffset.third,
                    cropPolygonX = cropPolygon.map { it.first },
                    cropPolygonY = cropPolygon.map { it.second },
                    removeRoadObjectsOfTypes = removeRoadObjectOfType,
                    generateRoadObjectTopSurfaceExtrusions = !skipRoadObjectTopSurfaceExtrusions,
                    roadObjectTopSurfaceExtrusionHeightPerObjectType = roadObjectTopSurfaceExtrusionHeightPerObjectType,
                    discretizationStepSize = discretizationStepSize,
                    sweepDiscretizationStepSize = sweepDiscretizationStepSize,
                    circleSlices = circleSlices,
                    generateRandomGeometryIds = generateRandomGeometryIds,
                    transformAdditionalRoadLines = transformAdditionalRoadLines,
                    generateLaneSurfaceExtrusions = !skipLaneSurfaceExtrusions,
                    laneSurfaceExtrusionHeight = laneSurfaceExtrusionHeight,
                    laneSurfaceExtrusionHeightPerLaneType = laneSurfaceExtrusionHeightPerLaneType,
                    compressionFormat = compressionFormat,
                )
            }, { parametersFilePath ->
                val parametersText = parametersFilePath.toFile().readText()

                Yaml.default.decodeFromString(OpendriveToCitygmlParameters.serializer(), parametersText)
            })

        val processor = OpendriveToCitygmlProcessor(parameters)
        processor.process(inputPath, outputPath)
    }
}
