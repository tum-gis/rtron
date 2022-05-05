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

package io.rtron.transformer.converter.opendrive2roadspaces.analysis

import arrow.core.NonEmptyList
import io.rtron.io.logging.Logger
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.ConcatenatedFunction
import io.rtron.math.analysis.function.univariate.combination.SectionedUnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.StackedFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.model.opendrive.lane.RoadLanes
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLaneWidth
import io.rtron.model.opendrive.objects.RoadObjectsObjectRepeat
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileShape
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileSuperelevation
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.std.filterToStrictSortingBy
import io.rtron.std.handleAndRemoveFailure
import io.rtron.std.isStrictlySortedBy
import io.rtron.std.toEither
import io.rtron.std.toResult
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration

/**
 * Builder for functions of the OpenDRIVE data model.
 */
class FunctionBuilder(
    private val reportLogger: Logger,
    private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Methods

    /**
     * Builds a function that describes the torsion of the road reference line.
     *
     * @param superelevation entries containing coefficients for polynomial functions
     */
    fun buildCurveTorsion(id: RoadspaceIdentifier, superelevation: NonEmptyList<RoadLateralProfileSuperelevation>):
        UnivariateFunction {
        require(superelevation.isStrictlySortedBy { it.s }) { "Superelevation entries must be sorted in strict order according to s." }

//        val superelevationEntriesAdjusted = superelevation
//            .filterToStrictSortingBy { it.s }
//        if (superelevationEntriesAdjusted.size < superelevation.size)
//            this.reportLogger.info(
//                "Ignoring superelevation entries which are not placed in strict order" +
//                    " according to s.",
//                id.toString()
//            )

        return ConcatenatedFunction.ofPolynomialFunctions(
            superelevation.map { it.s },
            superelevation.map { it.coefficients },
            prependConstant = true,
            prependConstantValue = 0.0
        )
    }

    /**
     * Builds a function that describes one lateral entry of a road's shape.
     *
     * @param roadLateralProfileShape the cross-sectional profile of a road at a certain curve position
     */
    fun buildLateralShape(id: RoadspaceIdentifier, roadLateralProfileShape: List<RoadLateralProfileShape>):
        UnivariateFunction {
        require(roadLateralProfileShape.isNotEmpty()) { "Lateral profile shape must contain elements in order to build a univariate function." }
        require(roadLateralProfileShape.all { it.s == roadLateralProfileShape.first().s }) { "All lateral profile shape elements must have the same curve position." }

        val lateralProfileEntriesAdjusted = roadLateralProfileShape
            .filterToStrictSortingBy { it.t }
        if (lateralProfileEntriesAdjusted.size < roadLateralProfileShape.size)
            this.reportLogger.info(
                "Ignoring lateral profile entries which are not placed in strict order " +
                    "according to t.",
                id.toString()
            )

        return ConcatenatedFunction.ofPolynomialFunctions(
            NonEmptyList.fromListUnsafe(lateralProfileEntriesAdjusted.map { it.t }), // TODO
            NonEmptyList.fromListUnsafe(lateralProfileEntriesAdjusted.map { it.coefficients }), // TODO
            prependConstant = true
        )
    }

    /**
     * Builds a function that described the lateral lane offset to the road reference line.
     */
    fun buildLaneOffset(id: RoadspaceIdentifier, lanes: RoadLanes): UnivariateFunction {
        if (lanes.laneOffset.isEmpty()) return LinearFunction.X_AXIS

        val laneOffsetEntriesAdjusted = lanes.laneOffset.filterToStrictSortingBy { it.s }
        if (laneOffsetEntriesAdjusted.size < lanes.laneOffset.size)
            this.reportLogger.info(
                "Ignoring lane offset entries which are not placed in strict order " +
                    "according to s.",
                id.toString()
            )

        return ConcatenatedFunction.ofPolynomialFunctions(
            NonEmptyList.fromListUnsafe(laneOffsetEntriesAdjusted.map { it.s }), // TODO
            NonEmptyList.fromListUnsafe(laneOffsetEntriesAdjusted.map { it.coefficients }), // TODO
            prependConstant = true,
            prependConstantValue = 0.0
        )
    }

    /**
     * Builds a function that describes the lane width.
     *
     * @param laneWidthEntries entries containing coefficients for polynomial functions
     * @param id identifier of the lane, required for logging output
     * @return function describing the width of a lane
     */
    fun buildLaneWidth(id: LaneIdentifier, laneWidthEntries: List<RoadLanesLaneSectionLRLaneWidth>):
        UnivariateFunction {

        require(laneWidthEntries.isStrictlySortedBy { it.sOffset }) { "Width entries of lane $id must be strictly sorted according to sOffset." }

        val widthEntriesProcessable = laneWidthEntries.map { it.getAsResult().toResult() }.handleAndRemoveFailure { reportLogger.log(it.toEither(), id.toString(), "Ignoring width entry.") }

        if (widthEntriesProcessable.isEmpty()) {
            this.reportLogger.info(
                "The lane does not contain any valid width entries. " +
                    "Continuing with a zero width.",
                id.toString()
            )
            return LinearFunction.X_AXIS
        }

        if (widthEntriesProcessable.first().sOffset > 0.0)
            this.reportLogger.info(
                "The width should be defined for the full length of the lane section and" +
                    " thus must also be defined for s=0.0. Not defined positions are interpreted with a width of 0.",
                id.toString()
            )

//        val widthEntriesAdjusted = widthEntriesProcessable
//            .filterToStrictSortingBy { it.sOffset }
//        if (widthEntriesAdjusted.size < widthEntriesProcessable.size)
//            this.reportLogger.info(
//                "Ignoring width entries which are not in strict order according to sOffset.",
//                id.toString()
//            )

        return ConcatenatedFunction.ofPolynomialFunctions(
            NonEmptyList.fromListUnsafe(widthEntriesProcessable.map { it.sOffset }), // TODO
            NonEmptyList.fromListUnsafe(widthEntriesProcessable.map { it.coefficients }), // TODO
            prependConstant = true,
            prependConstantValue = 0.0
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
    fun buildStackedHeightFunctionFromRepeat(repeat: RoadObjectsObjectRepeat, roadReferenceLine: Curve3D):
        StackedFunction {

        val heightFunctionSection = SectionedUnivariateFunction(
            roadReferenceLine.heightFunction,
            repeat.getRoadReferenceLineParameterSection()
        )
        return StackedFunction.ofSum(heightFunctionSection, repeat.getHeightOffsetFunction())
    }
}
