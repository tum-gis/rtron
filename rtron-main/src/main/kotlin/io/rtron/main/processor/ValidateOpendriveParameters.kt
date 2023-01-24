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

import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.readerwriter.citygml.CitygmlWriterParameters
import io.rtron.readerwriter.opendrive.OpendriveWriterParameters
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluatorParameters
import kotlinx.serialization.Serializable

@Serializable
data class ValidateOpendriveParameters(
    val tolerance: Double = Opendrive2RoadspacesParameters.DEFAULT_NUMBER_TOLERANCE,
    val discretizationStepSize: Double = Roadspaces2CitygmlParameters.DEFAULT_DISCRETIZATION_STEP_SIZE,

    val writeOpendriveFile: Boolean = true,
    val writeCitygml2File: Boolean = true,
    val writeCitygml3File: Boolean = true,

    val compressionFormat: CompressionFormat = CompressionFormat.NONE
) {

    // Methods

    fun deriveOpendriveEvaluatorParameters() = OpendriveEvaluatorParameters(
        skipRoadShapeRemoval = OpendriveEvaluatorParameters.DEFAULT_SKIP_ROAD_SHAPE_REMOVAL,
        numberTolerance = tolerance,
        planViewGeometryDistanceTolerance = OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE,
        planViewGeometryDistanceWarningTolerance = OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_WARNING_TOLERANCE,
        planViewGeometryAngleTolerance = OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE,
        planViewGeometryAngleWarningTolerance = OpendriveEvaluatorParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_WARNING_TOLERANCE,
    )

    fun deriveOpendriveWriterParameters() = OpendriveWriterParameters(
        fileCompression = compressionFormat.toOptionalCompressedFileExtension()
    )

    fun deriveOpendrive2RoadspacesParameters() = Opendrive2RoadspacesParameters(
        concurrentProcessing = false,
        numberTolerance = tolerance,
        planViewGeometryDistanceTolerance = Opendrive2RoadspacesParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE,
        planViewGeometryAngleTolerance = Opendrive2RoadspacesParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE,
        attributesPrefix = Opendrive2RoadspacesParameters.DEFAULT_ATTRIBUTES_PREFIX,
        crsEpsg = Opendrive2RoadspacesParameters.DEFAULT_CRS_EPSG,
        extrapolateLateralRoadShapes = Opendrive2RoadspacesParameters.DEFAULT_EXTRAPOLATE_LATERAL_ROAD_SHAPES
    )

    fun deriveRoadspacesEvaluatorParameters() = RoadspacesEvaluatorParameters(
        numberTolerance = tolerance,
        laneTransitionDistanceTolerance = RoadspacesEvaluatorParameters.DEFAULT_LANE_TRANSITION_DISTANCE_TOLERANCE,
    )

    fun deriveRoadspaces2Citygml2Parameters() = Roadspaces2CitygmlParameters(
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
        mappingBackwardsCompatibility = true,
    )

    fun deriveRoadspaces2Citygml3Parameters() = Roadspaces2CitygmlParameters(
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
        mappingBackwardsCompatibility = false,
    )

    fun deriveCitygml2WriterParameters() = CitygmlWriterParameters(
        versions = setOf(CitygmlVersion.V2_0),
        fileCompression = compressionFormat.toOptionalCompressedFileExtension()
    )

    fun deriveCitygml3WriterParameters() = CitygmlWriterParameters(
        versions = setOf(CitygmlVersion.V3_0),
        fileCompression = compressionFormat.toOptionalCompressedFileExtension()
    )
}
