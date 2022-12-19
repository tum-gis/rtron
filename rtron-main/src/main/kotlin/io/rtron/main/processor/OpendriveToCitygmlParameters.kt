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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.readerwriter.citygml.CitygmlWriterParameters
import io.rtron.readerwriter.opendrive.OpendriveWriterParameters
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluatorParameters
import io.rtron.transformer.modifiers.opendrive.cropper.OpendriveCropperParameters
import io.rtron.transformer.modifiers.opendrive.offset.adder.OpendriveOffsetAdderParameters
import kotlinx.serialization.Serializable

@Serializable
data class OpendriveToCitygmlParameters(
    val convertToCitygml2: Boolean = false,

    val skipRoadShapeRemoval: Boolean = OpendriveEvaluatorParameters.DEFAULT_SKIP_ROAD_SHAPE_REMOVAL,

    val tolerance: Double = Opendrive2RoadspacesParameters.DEFAULT_NUMBER_TOLERANCE,
    val planViewGeometryDistanceTolerance: Double = Opendrive2RoadspacesParameters.DEFAULT_PLAN_VIEW_GEOMETRY_DISTANCE_TOLERANCE,
    val planViewGeometryAngleTolerance: Double = Opendrive2RoadspacesParameters.DEFAULT_PLAN_VIEW_GEOMETRY_ANGLE_TOLERANCE,

    val crsEpsg: Int = Opendrive2RoadspacesParameters.DEFAULT_CRS_EPSG,
    val offsetX: Double = OpendriveOffsetAdderParameters.DEFAULT_OFFSET_X,
    val offsetY: Double = OpendriveOffsetAdderParameters.DEFAULT_OFFSET_Y,
    val offsetZ: Double = OpendriveOffsetAdderParameters.DEFAULT_OFFSET_Z,
    val cropPolygonX: List<Double> = OpendriveCropperParameters.DEFAULT_CROP_POLYGON_X,
    val cropPolygonY: List<Double> = OpendriveCropperParameters.DEFAULT_CROP_POLYGON_Y,

    val discretizationStepSize: Double = Roadspaces2CitygmlParameters.DEFAULT_DISCRETIZATION_STEP_SIZE,
    val sweepDiscretizationStepSize: Double = Roadspaces2CitygmlParameters.DEFAULT_SWEEP_DISCRETIZATION_STEP_SIZE,
    val circleSlices: Int = Roadspaces2CitygmlParameters.DEFAULT_CIRCLE_SLICES,
    val transformAdditionalRoadLines: Boolean = Roadspaces2CitygmlParameters.DEFAULT_TRANSFORM_ADDITIONAL_ROAD_LINES,

    val compressionFormat: CompressionFormat = CompressionFormat.NONE
) {
    // Methods
    fun isValid(): Either<List<String>, Unit> {
        val messages = mutableListOf<String>()
        if (cropPolygonX.size != cropPolygonY.size)
            messages += "cropPolygonX must have the same number of values as cropPolygonY"
        if (cropPolygonX.isNotEmpty() && cropPolygonX.size < 3)
            messages += "cropPolygonX must be empty or have at least three values for representing a triangle"
        if (cropPolygonY.isNotEmpty() && cropPolygonY.size < 3)
            messages += "cropPolygonX must be empty or have at least three values for representing a triangle"

        return if (messages.isEmpty())
            Unit.right()
        else
            messages.left()
    }

    fun getCitygmlWriteVersion(): CitygmlVersion = if (convertToCitygml2) CitygmlVersion.V2_0 else CitygmlVersion.V3_0

    fun deriveOpendriveEvaluatorParameters() = OpendriveEvaluatorParameters(
        skipRoadShapeRemoval = skipRoadShapeRemoval,
        numberTolerance = tolerance,

        planViewGeometryDistanceTolerance = planViewGeometryDistanceTolerance,
        planViewGeometryAngleTolerance = planViewGeometryAngleTolerance
    )

    fun deriveOpendriveOffsetAdderParameters() = OpendriveOffsetAdderParameters(
        offsetX = offsetX,
        offsetY = offsetY,
        offsetZ = offsetZ,
    )

    fun deriveOpendriveCropperParameters() = OpendriveCropperParameters(
        cropPolygonX = cropPolygonX,
        cropPolygonY = cropPolygonY,
        numberTolerance = tolerance,
    )

    fun deriveOpendriveWriterParameters() = OpendriveWriterParameters(
        fileCompression = compressionFormat.toOptionalCompressedFileExtension()
    )

    fun deriveOpendrive2RoadspacesParameters() = Opendrive2RoadspacesParameters(
        concurrentProcessing = false,

        numberTolerance = tolerance,
        planViewGeometryDistanceTolerance = planViewGeometryDistanceTolerance,
        planViewGeometryAngleTolerance = planViewGeometryAngleTolerance,
        crsEpsg = crsEpsg,
    )

    fun deriveRoadspacesEvaluatorParameters() = RoadspacesEvaluatorParameters(
        numberTolerance = tolerance,
        laneTransitionDistanceTolerance = RoadspacesEvaluatorParameters.DEFAULT_LANE_TRANSITION_DISTANCE_TOLERANCE,
    )

    fun deriveRoadspaces2CitygmlParameters() = Roadspaces2CitygmlParameters(
        concurrentProcessing = false,

        discretizationStepSize = discretizationStepSize,
        sweepDiscretizationStepSize = sweepDiscretizationStepSize,
        circleSlices = circleSlices,
        transformAdditionalRoadLines = transformAdditionalRoadLines,

        mappingBackwardsCompatibility = convertToCitygml2,
    )

    fun deriveCitygmlWriterParameters() = CitygmlWriterParameters(
        versions = setOf(this.getCitygmlWriteVersion()),
        fileCompression = compressionFormat.toOptionalCompressedFileExtension()
    )
}
