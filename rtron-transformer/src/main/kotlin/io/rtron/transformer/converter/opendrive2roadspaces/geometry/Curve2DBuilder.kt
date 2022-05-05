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

package io.rtron.transformer.converter.opendrive2roadspaces.geometry

import arrow.core.Either
import arrow.core.NonEmptyList
import io.rtron.io.logging.Logger
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.GeometryException
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
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.range.length
import io.rtron.math.std.fuzzyEquals
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import io.rtron.model.opendrive.objects.RoadObjectsObjectRepeat
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometry
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration

/**
 * Builder for curves in 2D from the OpenDRIVE data model.
 */
class Curve2DBuilder(
    private val reportLogger: Logger,
    private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Methods

    /**
     * Builds a concatenated curve in 2D for the OpenDRIVE's plan view elements.
     *
     * @param planViewGeometryList source geometry curve segments of OpenDRIVE
     * @param offset applied translational offset
     */
    fun buildCurve2DFromPlanViewGeometries(
        id: RoadspaceIdentifier,
        planViewGeometryList: NonEmptyList<RoadPlanViewGeometry>,
        offset: Vector2D = Vector2D.ZERO
    ): Either<GeometryException, CompositeCurve2D> {

        require(planViewGeometryList.all { it.length > configuration.numberTolerance }) { "All plan view geometry elements must have a length greater than zero (above the tolerance threshold)." }

        // construct composite curve
        val absoluteStarts: List<Double> = planViewGeometryList.map { it.s }
        val absoluteDomains: List<Range<Double>> = absoluteStarts
            .zipWithNext().map { Range.closedOpen(it.first, it.second) } +
            Range.closed(absoluteStarts.last(), absoluteStarts.last() + planViewGeometryList.last().length)
        val lengths: List<Double> = absoluteDomains.map { it.length }

        val curveMembers = planViewGeometryList.zip(lengths).dropLast(1)
            .map { buildPlanViewGeometry(id, it.first, it.second, BoundType.OPEN, offset) } +
            buildPlanViewGeometry(id, planViewGeometryList.last(), lengths.last(), BoundType.CLOSED, offset)

        return CompositeCurve2D.of(curveMembers, absoluteDomains, absoluteStarts, configuration.distanceTolerance, configuration.angleTolerance)
    }

    /**
     * Builds a single curve element in 2D for the OpenDRIVE's plan view element.
     *
     * @param geometry source geometry element of OpenDRIVE
     * @param length length of the constructed curve element
     * @param endBoundType applied end bound type for the curve element
     * @param offset applied translational offset
     */
    private fun buildPlanViewGeometry(
        id: RoadspaceIdentifier,
        geometry: RoadPlanViewGeometry,
        length: Double,
        endBoundType: BoundType = BoundType.OPEN,
        offset: Vector2D = Vector2D.ZERO
    ): AbstractCurve2D {

        if (!fuzzyEquals(geometry.length, length, configuration.numberTolerance))
            reportLogger.warn(
                "Plan view geometry element (s=${geometry.s}) contains a length value " +
                    "that does not match the start value of the next geometry element.",
                id.toString()
            )

        val startPose = Pose2D(Vector2D(geometry.x, geometry.y), Rotation2D(geometry.hdg))
        val affineSequence = AffineSequence2D.of(Affine2D.of(offset), Affine2D.of(startPose))

        geometry.spiral.tap {
            val curvatureFunction = LinearFunction.ofInclusiveInterceptAndPoint(
                it.curvStart,
                length,
                it.curvEnd
            )
            return SpiralSegment2D(curvatureFunction, configuration.numberTolerance, affineSequence, endBoundType)
        }

        geometry.arc.tap {
            return Arc2D(
                it.curvature,
                length,
                configuration.numberTolerance,
                affineSequence,
                endBoundType
            )
        }

        geometry.poly3.tap {
            return CubicCurve2D(
                it.coefficients,
                length,
                configuration.numberTolerance,
                affineSequence,
                endBoundType
            )
        }

        geometry.paramPoly3.tap {
            return if (it.isNormalized()) {
                val parameterTransformation: (CurveRelativeVector1D) -> CurveRelativeVector1D = { curveRelativePoint -> curveRelativePoint / length }
                val baseCurve = ParametricCubicCurve2D(
                    it.coefficientsU,
                    it.coefficientsV,
                    1.0,
                    configuration.numberTolerance,
                    affineSequence,
                    endBoundType
                )
                ParameterTransformedCurve2D(
                    baseCurve,
                    parameterTransformation,
                    Range.closedX(0.0, length, endBoundType)
                )
            } else {
                ParametricCubicCurve2D(
                    it.coefficientsU,
                    it.coefficientsV,
                    length,
                    configuration.numberTolerance,
                    affineSequence,
                    endBoundType
                )
            }
        }

        return LineSegment2D(length, configuration.numberTolerance, affineSequence, endBoundType)
    }

    /**
     * Builds the function for laterally translating the [roadReferenceLine] which is inter alia required for the
     * building of road objects.
     */
    fun buildLateralTranslatedCurve(repeat: RoadObjectsObjectRepeat, roadReferenceLine: Curve3D):
        Either<IllegalArgumentException, LateralTranslatedCurve2D> {
        val repeatObjectDomain = repeat.getRoadReferenceLineParameterSection()

        if (!roadReferenceLine.curveXY.domain.fuzzyEncloses(repeatObjectDomain, configuration.numberTolerance))
            return Either.Left(IllegalArgumentException("Domain of repeat road object ($repeatObjectDomain) is not enclosed by the domain of the reference line (${roadReferenceLine.curveXY.domain}) according to the tolerance."))

        val section = SectionedCurve2D(roadReferenceLine.curveXY, repeatObjectDomain)
        val lateralTranslatedCurve = LateralTranslatedCurve2D(section, repeat.getLateralOffsetFunction(), configuration.numberTolerance)
        return Either.Right(lateralTranslatedCurve)
    }
}
