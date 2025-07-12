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

package io.rtron.main.processor

import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluatorParameters
import kotlinx.serialization.Serializable

@Serializable
data class ValidateOpendriveParameters(
    val tolerance: Double = OpendriveEvaluatorParameters.DEFAULT_NUMBER_TOLERANCE,
    val planViewGeometryDistanceTolerance: Double = OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE,
    val planViewGeometryDistanceWarningTolerance: Double =
        OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_WARNING_TOLERANCE,
    val planViewGeometryAngleTolerance: Double = OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE,
    val planViewGeometryAngleWarningTolerance: Double = OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_WARNING_TOLERANCE,
    val discretizationStepSize: Double = Roadspaces2CitygmlParameters.DEFAULT_DISCRETIZATION_STEP_SIZE,
    val writeOpendriveFile: Boolean = true,
    val writeCitygml2File: Boolean = true,
    val writeCitygml3File: Boolean = true,
    val compressionFormat: CompressionFormat = CompressionFormat.NONE,
) {
    // Methods

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
}
