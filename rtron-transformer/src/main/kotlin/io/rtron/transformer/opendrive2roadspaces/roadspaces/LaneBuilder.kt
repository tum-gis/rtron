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

package io.rtron.transformer.opendrive2roadspaces.roadspaces

import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.ConcatenatedFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionLRLane
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionLRLaneHeight
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.std.distinctConsecutive
import io.rtron.transformer.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesConfiguration


/**
 * Builder for [Lane] objects of the RoadSpaces data model.
 */
class LaneBuilder(
        private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()
    private val _functionBuilder = FunctionBuilder(_reportLogger, configuration.parameters)

    // Methods

    /**
     * Builds a single lane with the [id].
     *
     * @param id identifier of the lane
     * @param srcLane lane object of the OpenDRIVE data model
     * @param baseAttributes attributes attached to the transformed [Lane] object
     */
    fun buildLane(id: LaneIdentifier, srcLane: RoadLanesLaneSectionLRLane, baseAttributes: AttributeList): Lane {

        // build lane geometry
        val width = _functionBuilder.buildLaneWidth(id, srcLane.width)
        val laneHeightOffsets = buildLaneHeightOffset(id, srcLane.height)

        // build lane attributes
        val attributes = baseAttributes + buildAttributes(srcLane)

        // build up lane object
        return Lane(id, width, laneHeightOffsets.inner, laneHeightOffsets.outer, srcLane.level, attributes)
    }

    /**
     * Small helper class containing the height offset functions of the inner and outer lane boundary.
     */
    private data class LaneHeightOffset(val inner: UnivariateFunction, val outer: UnivariateFunction)

    /**
     * Builds up the height offset function for the inner and outer lane boundary.
     *
     * @param srcLaneHeights lane height entries of the OpenDRIVE data model
     */
    private fun buildLaneHeightOffset(id: LaneIdentifier, srcLaneHeights: List<RoadLanesLaneSectionLRLaneHeight>):
            LaneHeightOffset {

        // remove consecutively duplicated height entries
        val heightEntriesDistinct = srcLaneHeights.distinctConsecutive { it.sOffset }
        if (heightEntriesDistinct.size < srcLaneHeights.size)
            _reportLogger.info("Removing redundant lane height entries (equal sOffset values).", id.toString())

        // filter non-finite entries
        val heightEntriesAdjusted = heightEntriesDistinct
                .filter { it.inner.isFinite() && it.outer.isFinite() }
        if (heightEntriesAdjusted.size < heightEntriesDistinct.size)
            _reportLogger.warn("Removing at least one lane height entry as the values are not finite. This " +
                    "can also be caused by an OpenDRIVE version for which no dedicated reader is available.",
                    id.toString())

        // build the inner and outer height offset functions
        val inner = if (heightEntriesAdjusted.isEmpty()) LinearFunction.X_AXIS
        else ConcatenatedFunction.ofLinearFunctions(heightEntriesAdjusted.map { it.sOffset }, heightEntriesAdjusted.map { it.inner })

        val outer = if (heightEntriesAdjusted.isEmpty()) LinearFunction.X_AXIS
        else ConcatenatedFunction.ofLinearFunctions(heightEntriesAdjusted.map { it.sOffset }, heightEntriesAdjusted.map { it.outer })

        return LaneHeightOffset(inner, outer)
    }

    private fun buildAttributes(srcLeftRightLane: RoadLanesLaneSectionLRLane) =
            attributes("${configuration.parameters.attributesPrefix}lane_") {
                attribute("type", srcLeftRightLane.type.toString())
                attribute("level", srcLeftRightLane.level)

                attributes("material") {
                    srcLeftRightLane.material.forEachIndexed { i, element ->
                        attribute("_curvePositionStart_$i", element.sOffset)
                        attribute("_surface_$i", element.surface)
                        attribute("_friction_$i", element.friction)
                        attribute("_roughness_$i", element.roughness)
                    }
                }

                attributes("roadMark") {
                    srcLeftRightLane.roadMark.forEachIndexed { i, element ->
                        attribute("_curvePositionStart_$i", element.sOffset)
                        attribute("_type_$i", element.typeAttribute.toString())
                        attribute("_weight_$i", element.weight.toString())
                        attribute("_color_$i", element.color.toString())
                        attribute("_material_$i", element.material)
                    }
                }

                attributes("speed") {
                    srcLeftRightLane.speed.forEachIndexed { i, element ->
                        attribute("_curvePositionStart_$i", element.sOffset)
                        attribute("_max_$i", element.max, element.unit.toUnitOfMeasure())
                    }
                }

                attributes("heightOffset") {
                    srcLeftRightLane.height.forEachIndexed { i, element ->
                        attribute("_curvePositionStart_$i", element.sOffset)
                        attribute("_inner_$i", element.inner)
                        attribute("_outer_$i", element.outer)
                    }
                }

            }
}
