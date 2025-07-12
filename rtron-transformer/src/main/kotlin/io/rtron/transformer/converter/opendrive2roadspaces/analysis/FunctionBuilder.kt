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

package io.rtron.transformer.converter.opendrive2roadspaces.analysis

import arrow.core.NonEmptyList
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.ConcatenatedFunction
import io.rtron.math.analysis.function.univariate.combination.SectionedUnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.StackedFunction
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.model.opendrive.lane.RoadLanesLaneOffset
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLaneWidth
import io.rtron.model.opendrive.objects.RoadObjectsObjectRepeat
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileShape
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileSuperelevation
import io.rtron.std.isStrictlySortedBy

/**
 * Builder for functions of the OpenDRIVE data model.
 */
object FunctionBuilder {
    // Methods

    /**
     * Builds a function that describes the torsion of the road reference line.
     *
     * @param superelevation entries containing coefficients for polynomial functions
     */
    fun buildCurveTorsion(superelevation: NonEmptyList<RoadLateralProfileSuperelevation>): UnivariateFunction {
        require(superelevation.isStrictlySortedBy { it.s }) { "Superelevation entries must be sorted in strict order according to s." }

        return ConcatenatedFunction.ofPolynomialFunctions(
            superelevation.map { it.s },
            superelevation.map { it.coefficients },
            prependConstant = true,
            prependConstantValue = 0.0,
        )
    }

    /**
     * Builds a function that describes one lateral entry of a road's shape.
     *
     * @param roadLateralProfileShape the cross-sectional profile of a road at a certain curve position
     */
    fun buildLateralShape(roadLateralProfileShape: NonEmptyList<RoadLateralProfileShape>): UnivariateFunction {
        require(
            roadLateralProfileShape.all {
                it.s == roadLateralProfileShape.first().s
            },
        ) { "All lateral profile shape elements must have the same curve position." }
        require(
            roadLateralProfileShape.isStrictlySortedBy {
                it.t
            },
        ) { "Lateral profile shape entries must be sorted in strict order according to t." }

        return ConcatenatedFunction.ofPolynomialFunctions(
            roadLateralProfileShape.map { it.t },
            roadLateralProfileShape.map { it.coefficients },
            prependConstant = true,
        )
    }

    /**
     * Builds a function that described the lateral lane offset to the road reference line.
     */
    fun buildLaneOffset(laneOffsets: NonEmptyList<RoadLanesLaneOffset>): UnivariateFunction {
        require(laneOffsets.isStrictlySortedBy { it.s }) { "Lane offsets entries must be sorted in strict order according to s." }

        return ConcatenatedFunction.ofPolynomialFunctions(
            laneOffsets.map { it.s },
            laneOffsets.map { it.coefficients },
            prependConstant = true,
            prependConstantValue = 0.0,
        )
    }

    /**
     * Builds a function that describes the lane width.
     *
     * @param laneWidthEntries entries containing coefficients for polynomial functions
     * @return function describing the width of a lane
     */
    fun buildLaneWidth(
        laneWidthEntries: NonEmptyList<RoadLanesLaneSectionLRLaneWidth>,
        numberTolerance: Double,
    ): UnivariateFunction {
        require(
            laneWidthEntries.isStrictlySortedBy { it.sOffset },
        ) { "Width entries of lane must be strictly sorted according to sOffset." }
        require(laneWidthEntries.head.sOffset < numberTolerance) { "First width entry must start with sOffset=0.0." }

        return ConcatenatedFunction.ofPolynomialFunctions(
            laneWidthEntries.map { it.sOffset },
            laneWidthEntries.map { it.coefficients },
            prependConstant = true,
            prependConstantValue = 0.0,
        )
    }

    /**
     * Returns the absolute height function of a [RoadObjectsObjectRepeat] object. Therefore a linear function is build
     * for the zOffsets and is added to the height function of the [roadReferenceLine].
     *
     * @param repeat object for which the height function shall be constructed
     * @param roadReferenceLine road's height
     * @return function of the object's absolute height
     */
    fun buildStackedHeightFunctionFromRepeat(
        repeat: RoadObjectsObjectRepeat,
        roadReferenceLine: Curve3D,
    ): StackedFunction {
        val heightFunctionSection =
            SectionedUnivariateFunction(
                roadReferenceLine.heightFunction,
                repeat.getRoadReferenceLineParameterSection(),
            )
        return StackedFunction.ofSum(heightFunctionSection, repeat.getHeightOffsetFunction())
    }
}
