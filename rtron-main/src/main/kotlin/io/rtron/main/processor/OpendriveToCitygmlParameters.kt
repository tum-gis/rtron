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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.rtron.model.opendrive.objects.EObjectType
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.road.LaneType
import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluatorParameters
import io.rtron.transformer.modifiers.opendrive.cropper.OpendriveCropperParameters
import io.rtron.transformer.modifiers.opendrive.offset.adder.OpendriveOffsetAdderParameters
import io.rtron.transformer.modifiers.opendrive.remover.OpendriveObjectRemoverParameters
import io.rtron.transformer.modifiers.opendrive.reprojector.OpendriveReprojectorParameters
import kotlinx.serialization.Serializable

@Serializable
data class OpendriveToCitygmlParameters(
    val convertToCitygml2: Boolean = false,
    val skipRoadShapeRemoval: Boolean = OpendriveEvaluatorParameters.DEFAULT_SKIP_ROAD_SHAPE_REMOVAL,
    val tolerance: Double = Opendrive2RoadspacesParameters.DEFAULT_NUMBER_TOLERANCE,
    val planViewGeometryDistanceTolerance: Double = OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE,
    val planViewGeometryDistanceWarningTolerance: Double =
        OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_WARNING_TOLERANCE,
    val planViewGeometryAngleTolerance: Double =
        OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE,
    val planViewGeometryAngleWarningTolerance: Double =
        OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_WARNING_TOLERANCE,
    val reprojectModel: Boolean = OpendriveReprojectorParameters.DEFAULT_REPROJECT_MODEL,
    val crsEpsg: Int = Opendrive2RoadspacesParameters.DEFAULT_CRS_EPSG,
    val offsetX: Double = OpendriveOffsetAdderParameters.DEFAULT_OFFSET_X,
    val offsetY: Double = OpendriveOffsetAdderParameters.DEFAULT_OFFSET_Y,
    val offsetZ: Double = OpendriveOffsetAdderParameters.DEFAULT_OFFSET_Z,
    val cropPolygonX: List<Double> = OpendriveCropperParameters.DEFAULT_CROP_POLYGON_X,
    val cropPolygonY: List<Double> = OpendriveCropperParameters.DEFAULT_CROP_POLYGON_Y,
    val removeRoadObjectsOfTypes: Set<EObjectType> = OpendriveObjectRemoverParameters.DEFAULT_REMOVE_ROAD_OBJECTS_OF_TYPES,
    val generateRoadObjectTopSurfaceExtrusions: Boolean =
        Opendrive2RoadspacesParameters.DEFAULT_GENERATE_ROAD_OBJECT_TOP_SURFACE_EXTRUSIONS,
    val roadObjectTopSurfaceExtrusionHeightPerObjectType: Map<RoadObjectType, Double> =
        Opendrive2RoadspacesParameters.DEFAULT_ROAD_OBJECT_TOP_SURFACE_EXTRUSION_HEIGHT_PER_OBJECT_TYPE,
    val discretizationStepSize: Double = Roadspaces2CitygmlParameters.DEFAULT_DISCRETIZATION_STEP_SIZE,
    val sweepDiscretizationStepSize: Double = Roadspaces2CitygmlParameters.DEFAULT_SWEEP_DISCRETIZATION_STEP_SIZE,
    val circleSlices: Int = Roadspaces2CitygmlParameters.DEFAULT_CIRCLE_SLICES,
    val generateRandomGeometryIds: Boolean = Roadspaces2CitygmlParameters.DEFAULT_GENERATE_RANDOM_GEOMETRY_IDS,
    val transformAdditionalRoadLines: Boolean = Roadspaces2CitygmlParameters.DEFAULT_TRANSFORM_ADDITIONAL_ROAD_LINES,
    val generateLaneSurfaceExtrusions: Boolean = Roadspaces2CitygmlParameters.DEFAULT_GENERATE_LANE_SURFACE_EXTRUSIONS,
    val laneSurfaceExtrusionHeight: Double = Roadspaces2CitygmlParameters.DEFAULT_LANE_SURFACE_EXTRUSION_HEIGHT,
    val laneSurfaceExtrusionHeightPerLaneType: Map<LaneType, Double> =
        Roadspaces2CitygmlParameters.DEFAULT_LANE_SURFACE_EXTRUSION_HEIGHT_PER_LANE_TYPE,
    val compressionFormat: CompressionFormat = CompressionFormat.NONE,
) {
    // Methods
    fun isValid(): Either<List<String>, Unit> {
        val issues = mutableListOf<String>()
        if (cropPolygonX.size != cropPolygonY.size) {
            issues += "cropPolygonX must have the same number of values as cropPolygonY"
        }
        if (cropPolygonX.isNotEmpty() && cropPolygonX.size < 3) {
            issues += "cropPolygonX must be empty or have at least three values for representing a triangle"
        }
        if (cropPolygonY.isNotEmpty() && cropPolygonY.size < 3) {
            issues += "cropPolygonX must be empty or have at least three values for representing a triangle"
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
            removeRoadObjectsOfTypes = removeRoadObjectsOfTypes,
        )

    fun deriveOpendriveOffsetAdderParameters() =
        OpendriveOffsetAdderParameters(
            offsetX = offsetX,
            offsetY = offsetY,
            offsetZ = offsetZ,
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
            cropPolygonX = cropPolygonX,
            cropPolygonY = cropPolygonY,
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
            generateRoadObjectTopSurfaceExtrusions = generateRoadObjectTopSurfaceExtrusions,
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
            generateLaneSurfaceExtrusions = generateLaneSurfaceExtrusions,
            laneSurfaceExtrusionHeight = laneSurfaceExtrusionHeight,
            laneSurfaceExtrusionHeightPerLaneType = laneSurfaceExtrusionHeightPerLaneType,
            mappingBackwardsCompatibility = convertToCitygml2,
        )
}
