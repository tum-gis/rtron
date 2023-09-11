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

package io.rtron.transformer.converter.opendrive2roadspaces.geometry

import arrow.core.NonEmptyList
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
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.range.length
import io.rtron.math.std.fuzzyEquals
import io.rtron.math.transform.Affine2D
import io.rtron.math.transform.AffineSequence2D
import io.rtron.model.opendrive.objects.RoadObjectsObjectRepeat
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometry
import io.rtron.std.isStrictlySortedBy

/**
 * Builder for curves in 2D from the OpenDRIVE data model.
 */
object Curve2DBuilder {

    // Methods

    /**
     * Builds a concatenated curve in 2D for the OpenDRIVE's plan view elements.
     *
     * @param planViewGeometryList source geometry curve segments of OpenDRIVE
     */
    fun buildCurve2DFromPlanViewGeometries(
        planViewGeometryList: NonEmptyList<RoadPlanViewGeometry>,
        numberTolerance: Double,
        distanceTolerance: Double,
        angleTolerance: Double
    ): CompositeCurve2D {
        require(planViewGeometryList.all { it.length > numberTolerance }) { "All plan view geometry elements must have a length greater than zero (above the tolerance threshold)." }
        require(planViewGeometryList.isStrictlySortedBy { it.s }) { "Plan view geometry elements must be sorted in strict order according to s." }

        val (curveMembers, absoluteDomains, absoluteStarts) = prepareCurveMembers(planViewGeometryList, numberTolerance)

        // concatenate curve members
        return CompositeCurve2D.of(curveMembers, absoluteDomains, absoluteStarts, distanceTolerance, angleTolerance)
    }

    /**
     * Prepares the list of [RoadPlanViewGeometry] for constructing the composite curve.
     */
    fun prepareCurveMembers(planViewGeometryList: NonEmptyList<RoadPlanViewGeometry>, numberTolerance: Double):
        Triple<List<AbstractCurve2D>, List<Range<Double>>, List<Double>> {
        // absolute positions for each curve member
        val absoluteStarts: List<Double> = planViewGeometryList.map { it.s }
        // domains for each curve member
        val absoluteDomains: List<Range<Double>> = absoluteStarts
            .zipWithNext().map { Range.closedOpen(it.first, it.second) } +
            Range.closed(absoluteStarts.last(), absoluteStarts.last() + planViewGeometryList.last().length)
        // length derived from absolute values to increase robustness
        val lengths: List<Double> = absoluteDomains.map { it.length }

        // construct individual curve members
        val curveMembers = planViewGeometryList.zip(lengths).dropLast(1)
            .map { buildPlanViewGeometry(it.first, it.second, BoundType.OPEN, numberTolerance) } +
            buildPlanViewGeometry(planViewGeometryList.last(), lengths.last(), BoundType.CLOSED, numberTolerance)

        return Triple(curveMembers, absoluteDomains, absoluteStarts)
    }

    /**
     * Builds a single curve element in 2D for the OpenDRIVE's plan view element.
     *
     * @param geometry source geometry element of OpenDRIVE
     * @param length length of the constructed curve element
     * @param endBoundType applied end bound type for the curve element
     */
    private fun buildPlanViewGeometry(
        geometry: RoadPlanViewGeometry,
        length: Double,
        endBoundType: BoundType = BoundType.OPEN,
        numberTolerance: Double
    ): AbstractCurve2D {
        require(
            fuzzyEquals(
                geometry.length,
                length,
                numberTolerance
            )
        ) { "Plan view geometry element (s=${geometry.s}) contains a length value that does not match the start value of the next geometry element." }

        val startPose = Pose2D(Vector2D(geometry.x, geometry.y), Rotation2D(geometry.hdg))
        val affineSequence = AffineSequence2D(Affine2D.of(startPose))

        geometry.spiral.onSome {
            val curvatureFunction = LinearFunction.ofInclusiveInterceptAndPoint(
                it.curvStart,
                length,
                it.curvEnd
            )
            return SpiralSegment2D(curvatureFunction, numberTolerance, affineSequence, endBoundType)
        }

        geometry.arc.onSome {
            return Arc2D(
                it.curvature,
                length,
                numberTolerance,
                affineSequence,
                endBoundType
            )
        }

        geometry.poly3.onSome {
            return CubicCurve2D(
                it.coefficients,
                length,
                numberTolerance,
                affineSequence,
                endBoundType
            )
        }

        geometry.paramPoly3.onSome {
            return if (it.isNormalized()) {
                val parameterTransformation: (CurveRelativeVector1D) -> CurveRelativeVector1D =
                    { curveRelativePoint -> curveRelativePoint / length }
                val baseCurve = ParametricCubicCurve2D(
                    it.coefficientsU,
                    it.coefficientsV,
                    1.0,
                    numberTolerance,
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
                    numberTolerance,
                    affineSequence,
                    endBoundType
                )
            }
        }

        return LineSegment2D(length, numberTolerance, affineSequence, endBoundType)
    }

    /**
     * Builds the function for laterally translating the [roadReferenceLine] which is inter alia required for the
     * building of road objects.
     */
    fun buildLateralTranslatedCurve(
        repeat: RoadObjectsObjectRepeat,
        roadReferenceLine: Curve3D,
        numberTolerance: Double
    ): LateralTranslatedCurve2D {
        val repeatObjectDomain = repeat.getRoadReferenceLineParameterSection()
        require(
            roadReferenceLine.curveXY.domain.fuzzyEncloses(
                repeatObjectDomain,
                numberTolerance
            )
        ) { "Domain of repeat road object ($repeatObjectDomain) is not enclosed by the domain of the reference line (${roadReferenceLine.curveXY.domain}) according to the tolerance." }

        val section = SectionedCurve2D(roadReferenceLine.curveXY, repeatObjectDomain)
        return LateralTranslatedCurve2D(section, repeat.getLateralOffsetFunction(), numberTolerance)
    }
}
