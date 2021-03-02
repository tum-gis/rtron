/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.opendrive2roadspaces.geometry

import com.github.kittinunf.result.Result
import io.rtron.io.logging.Logger
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.curve.AbstractCurve2D
import io.rtron.math.geometry.euclidean.twod.curve.Arc2D
import io.rtron.math.geometry.euclidean.twod.curve.CompositeCurve2D
import io.rtron.math.geometry.euclidean.twod.curve.CubicCurve2D
import io.rtron.math.geometry.euclidean.twod.curve.LateralTranslatedCurve2D
import io.rtron.math.geometry.euclidean.twod.curve.LineSegment2D
import io.rtron.math.geometry.euclidean.twod.curve.ParameterTransformedCurve2D
import io.rtron.math.geometry.euclidean.twod.curve.ParametricCubicCurve2D
import io.rtron.math.geometry.euclidean.twod.curve.SectionedCurve2D
import io.rtron.math.geometry.euclidean.twod.curve.SpiralSegment2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.BoundType
import io.rtron.math.range.Range
import io.rtron.math.range.length
import io.rtron.math.std.fuzzyEquals
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectRepeat
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometry
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesParameters

/**
 * Builder for curves in 2D from the OpenDRIVE data model.
 */
class Curve2DBuilder(
    private val reportLogger: Logger,
    private val parameters: Opendrive2RoadspacesParameters
) {

    // Methods

    /**
     * Builds a concatenated curve in 2D for the OpenDRIVE's plan view elements.
     *
     * @param srcPlanViewGeometryList source geometry curve segments of OpenDRIVE
     * @param offset applied translational offset
     */
    fun buildCurve2DFromPlanViewGeometries(
        id: RoadspaceIdentifier,
        srcPlanViewGeometryList: List<RoadPlanViewGeometry>,
        offset: Vector2D = Vector2D.ZERO
    ): Result<CompositeCurve2D, IllegalArgumentException> {

        if (srcPlanViewGeometryList.isEmpty())
            return Result.error(IllegalArgumentException("No plan view geometries available."))

        // prepare
        val srcPlanViewGeometryListAdjusted =
            srcPlanViewGeometryList.filter { it.length > parameters.tolerance }
        if (srcPlanViewGeometryListAdjusted.size < srcPlanViewGeometryList.size)
            reportLogger.warn(
                "Plan view geometry contains a length value of zero (below tolerance threshold) and " +
                    "therefore the curve element can not be constructed.",
                id.toString()
            )

        // construct composite curve
        val absoluteStarts: List<Double> = srcPlanViewGeometryListAdjusted.map { it.s }
        val absoluteDomains: List<Range<Double>> = absoluteStarts
            .zipWithNext().map { Range.closedOpen(it.first, it.second) } +
            Range.closed(absoluteStarts.last(), absoluteStarts.last() + srcPlanViewGeometryListAdjusted.last().length)
        val lengths: List<Double> = absoluteDomains.map { it.length }

        val curveMembers = srcPlanViewGeometryListAdjusted.zip(lengths).dropLast(1)
            .map { buildPlanViewGeometry(id, it.first, it.second, BoundType.OPEN, offset) } +
            buildPlanViewGeometry(id, srcPlanViewGeometryListAdjusted.last(), lengths.last(), BoundType.CLOSED, offset)

        return Result.success(CompositeCurve2D(curveMembers, absoluteDomains, absoluteStarts))
    }

    /**
     * Builds a single curve element in 2D for the OpenDRIVE's plan view element.
     *
     * @param srcGeometry source geometry element of OpenDRIVE
     * @param length length of the constructed curve element
     * @param endBoundType applied end bound type for the curve element
     * @param offset applied translational offset
     */
    private fun buildPlanViewGeometry(
        id: RoadspaceIdentifier,
        srcGeometry: RoadPlanViewGeometry,
        length: Double,
        endBoundType: BoundType = BoundType.OPEN,
        offset: Vector2D = Vector2D.ZERO
    ): AbstractCurve2D {

        if (!fuzzyEquals(srcGeometry.length, length, parameters.tolerance))
            reportLogger.warn(
                "Plan view geometry element (s=${srcGeometry.s}) contains a length value " +
                    "that does not match the start value of the next geometry element.",
                id.toString()
            )

        val startPose = Pose2D(Vector2D(srcGeometry.x, srcGeometry.y), Rotation2D(srcGeometry.hdg))
        val affineSequence = AffineSequence2D.of(Affine2D.of(offset), Affine2D.of(startPose))

        return when {
            srcGeometry.isSpiral() -> {
                val curvatureFunction = LinearFunction.ofInclusiveInterceptAndPoint(
                    srcGeometry.spiral.curvStart,
                    length,
                    srcGeometry.spiral.curvEnd
                )
                SpiralSegment2D(curvatureFunction, parameters.tolerance, affineSequence, endBoundType)
            }
            srcGeometry.isArc() -> {
                Arc2D(
                    srcGeometry.arc.curvature,
                    length,
                    parameters.tolerance,
                    affineSequence,
                    endBoundType
                )
            }
            srcGeometry.isPoly3() -> {
                CubicCurve2D(
                    srcGeometry.poly3.coefficients,
                    length,
                    parameters.tolerance,
                    affineSequence,
                    endBoundType
                )
            }
            srcGeometry.isParamPoly3() && srcGeometry.paramPoly3.isNormalized() -> {
                val parameterTransformation: (CurveRelativeVector1D) -> CurveRelativeVector1D = { it / length }
                val baseCurve = ParametricCubicCurve2D(
                    srcGeometry.paramPoly3.coefficientsU,
                    srcGeometry.paramPoly3.coefficientsV,
                    1.0,
                    parameters.tolerance,
                    affineSequence,
                    endBoundType
                )
                ParameterTransformedCurve2D(
                    baseCurve,
                    parameterTransformation,
                    Range.closedX(0.0, length, endBoundType)
                )
            }
            srcGeometry.isParamPoly3() && !srcGeometry.paramPoly3.isNormalized() -> {
                ParametricCubicCurve2D(
                    srcGeometry.paramPoly3.coefficientsU,
                    srcGeometry.paramPoly3.coefficientsV,
                    length,
                    parameters.tolerance,
                    affineSequence,
                    endBoundType
                )
            }
            else -> {
                LineSegment2D(length, parameters.tolerance, affineSequence, endBoundType)
            }
        }
    }

    /**
     * Builds the function for laterally translating the [roadReferenceLine] which is inter alia required for the
     * building of road objects.
     */
    fun buildLateralTranslatedCurve(srcRepeat: RoadObjectsObjectRepeat, roadReferenceLine: Curve3D):
        LateralTranslatedCurve2D {
            val section = SectionedCurve2D(roadReferenceLine.curveXY, srcRepeat.getRoadReferenceLineParameterSection())
            return LateralTranslatedCurve2D(section, srcRepeat.getLateralOffsetFunction(), parameters.tolerance)
        }
}
