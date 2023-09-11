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

package io.rtron.transformer.converter.opendrive2roadspaces.roadspaces

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.some
import arrow.core.toNonEmptyListOrNull
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.ConcatenatedFunction
import io.rtron.math.analysis.function.univariate.pure.ConstantFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.range.Range
import io.rtron.math.range.length
import io.rtron.math.std.fuzzyEquals
import io.rtron.model.opendrive.lane.ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMark
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLaneHeight
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLaneMaterial
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.LaneSectionIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.road.CenterLane
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.LaneChange
import io.rtron.model.roadspaces.roadspace.road.LaneMaterial
import io.rtron.model.roadspaces.roadspace.road.RoadMarking
import io.rtron.std.isStrictlySortedBy
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.messages.opendrive.of

/**
 * Builder for [Lane] objects of the RoadSpaces data model.
 */
class LaneBuilder(
    private val parameters: Opendrive2RoadspacesParameters
) {
    // Methods

    /**
     * Builds a single lane (which is either left or right to the center) with the [id].
     *
     * @param id identifier of the lane
     * @param curvePositionDomain curve position domain (relative to the lane section) where the lane is defined
     * @param lrLane lane object of the OpenDRIVE data model
     * @param baseAttributes attributes attached to the transformed [Lane] object
     */
    fun buildLane(id: LaneIdentifier, curvePositionDomain: Range<Double>, lrLane: RoadLanesLaneSectionLRLane, baseAttributes: AttributeList): ContextMessageList<Lane> {
        val messageList = DefaultMessageList()

        // build lane geometry
        val width = lrLane.getLaneWidthEntries()
            .fold({ LinearFunction.X_AXIS }, { FunctionBuilder.buildLaneWidth(it, parameters.numberTolerance) })
        val laneHeightOffsets = lrLane.getLaneHeightEntries()
            .fold({ LaneHeightOffset(LinearFunction.X_AXIS, LinearFunction.X_AXIS) }, { buildLaneHeightOffset(it) })

        // build road markings
        val roadMarkings: List<RoadMarking> =
            if (lrLane.roadMark.isEmpty()) {
                emptyList()
            } else {
                buildRoadMarkings(curvePositionDomain, lrLane.roadMark.toNonEmptyListOrNull()!!).handleMessageList { messageList += it }
            }

        // lane topology
        val predecessors = lrLane.link.fold({ emptyList() }, { link -> link.predecessor.map { it.id } })
        val successors = lrLane.link.fold({ emptyList() }, { link -> link.successor.map { it.id } })

        // build lane attributes
        val type = lrLane.type.toLaneType()
        val laneMaterial = buildLaneMaterial(lrLane.material)
        val attributes = baseAttributes + buildAttributes(lrLane)

        // build up lane object
        val lane = Lane(
            id, width, laneHeightOffsets.inner, laneHeightOffsets.outer, lrLane.getLevelWithDefault(), roadMarkings,
            predecessors, successors, type, laneMaterial, attributes
        )
        return ContextMessageList(lane, messageList)
    }

    /**
     * Builds a center lane of a lane section.
     *
     * @param id identifier of the center lane
     * @param curvePositionDomain curve position domain (relative to the lane section) where the lane is defined
     * @param centerLane center lane object of the OpenDRIVE data model
     * @param baseAttributes attributes attached to the transformed [Lane] object
     */
    fun buildCenterLane(
        id: LaneSectionIdentifier,
        curvePositionDomain: Range<Double>,
        centerLane: RoadLanesLaneSectionCenterLane,
        baseAttributes: AttributeList
    ): ContextMessageList<CenterLane> {
        require(centerLane.id == 0) { "Center lane must have id 0, but has ${centerLane.id}." }

        val laneIdentifier = LaneIdentifier(0, id)
        val messageList = DefaultMessageList()

        val roadMarkings =
            if (centerLane.roadMark.isEmpty()) {
                emptyList()
            } else {
                buildRoadMarkings(curvePositionDomain, centerLane.roadMark.toNonEmptyListOrNull()!!)
                    .handleMessageList { messageList += it }
            }

        val type = centerLane.type.toLaneType()
        val laneMaterial = buildLaneMaterial(centerLane.material)
        val attributes = baseAttributes + buildAttributes(centerLane)

        val lane = CenterLane(laneIdentifier, centerLane.getLevelWithDefault(), roadMarkings, type, laneMaterial, attributes)
        return ContextMessageList(lane, messageList)
    }

    /**
     * Small helper class containing the height offset functions of the inner and outer lane boundary.
     */
    private data class LaneHeightOffset(val inner: UnivariateFunction, val outer: UnivariateFunction)

    /**
     * Builds up the height offset function for the inner and outer lane boundary.
     *
     * @param laneHeights lane height entries of the OpenDRIVE data model
     */
    private fun buildLaneHeightOffset(laneHeights: NonEmptyList<RoadLanesLaneSectionLRLaneHeight>): LaneHeightOffset {
        require(laneHeights.isStrictlySortedBy { it.sOffset }) { "Height entries must be sorted in strict order according to sOffset." }
        require(laneHeights.all { it.inner.isFinite() && it.outer.isFinite() }) { "Inner and outer values must be finite." }

        val inner = ConcatenatedFunction.ofLinearFunctions(
            laneHeights.map { it.sOffset },
            laneHeights.map { it.inner },
            prependConstant = true
        )

        val outer = ConcatenatedFunction.ofLinearFunctions(
            laneHeights.map { it.sOffset },
            laneHeights.map { it.outer },
            prependConstant = true
        )

        return LaneHeightOffset(inner, outer)
    }

    /**
     * Builds a list of road markings ([roadMark]).
     *
     * @param curvePositionDomain curve position domain (relative to the lane section) where the road markings is defined
     * @param roadMark road marking entries of the OpenDRIVE data model
     */
    private fun buildRoadMarkings(
        curvePositionDomain: Range<Double>,
        roadMark: NonEmptyList<RoadLanesLaneSectionLCRLaneRoadMark>
    ): ContextMessageList<List<RoadMarking>> {
        require(curvePositionDomain.hasUpperBound()) { "curvePositionDomain must have an upper bound." }
        val roadMarkId = roadMark.head.additionalId.toEither { IllegalStateException("Additional outline ID must be available.") }.getOrElse { throw it }
        val messageList = DefaultMessageList()

        val curvePositionDomainEnd = curvePositionDomain.upperEndpointOrNull()!!
        val adjustedSrcRoadMark = roadMark
            .filter { it.sOffset in curvePositionDomain }
            .filter { !fuzzyEquals(it.sOffset, curvePositionDomainEnd, parameters.numberTolerance) }
        if (adjustedSrcRoadMark.size < roadMark.size) {
            messageList += DefaultMessage.of(
                "RoadMarkEntriesNotLocatedWithinSRange",
                "Road mark entries have been removed, as the sOffset is not located within " +
                    "the local curve position domain ($curvePositionDomain) of the lane section.",
                roadMarkId,
                Severity.WARNING,
                wasFixed = true
            )
        }

        if (adjustedSrcRoadMark.isEmpty()) return ContextMessageList(emptyList(), messageList)

        val roadMarkings = adjustedSrcRoadMark.zipWithNext()
            .map { buildRoadMarking(it.first, it.second.sOffset.some()) } +
            listOf(buildRoadMarking(adjustedSrcRoadMark.last()))

        return ContextMessageList(roadMarkings, messageList)
    }

    /**
     * Builds an individual road marking [roadMark].
     *
     * @param roadMark road mark entry of the OpenDRIVE data model
     * @param domainEndpoint upper domain endpoint for the domain of the road mark
     */
    private fun buildRoadMarking(roadMark: RoadLanesLaneSectionLCRLaneRoadMark, domainEndpoint: Option<Double> = None): RoadMarking {
        val domain = domainEndpoint.fold({ Range.atLeast(roadMark.sOffset) }, { Range.closed(roadMark.sOffset, it) })
        require(domain.length > parameters.numberTolerance) { "Length of road marking must be above zero and the tolerance threshold." }

        val width = roadMark.width.fold({ ConstantFunction.ZERO }, { ConstantFunction(it, domain) })

        val attributes = attributes("${parameters.attributesPrefix}roadMarking") {
            attribute("_curvePositionStart", roadMark.sOffset)
            attribute("_width", roadMark.width)
            attribute("_type", roadMark.typeAttribute.toString())
            attribute("_weight", roadMark.weight.map { it.toString() })
            attribute("_laneChange", roadMark.laneChange.map { it.toString() })
            attribute("_color", roadMark.color.toString())
            attribute("_material", roadMark.material)
        }

        val laneChange = when (roadMark.laneChange.getOrElse { ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.BOTH }) {
            ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.INCREASE -> LaneChange.INCREASE
            ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.DECREASE -> LaneChange.DECREASE
            ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.BOTH -> LaneChange.BOTH
            ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.NONE -> LaneChange.NONE
        }

        return RoadMarking(width, laneChange, attributes)
    }

    private fun buildLaneMaterial(laneMaterials: List<RoadLanesLaneSectionLRLaneMaterial>): Option<LaneMaterial> {
        if (laneMaterials.isEmpty()) {
            return None
        }
        val firstEntry = laneMaterials.first()
        // return none, if first entry does not start at the beginning (leads to gap)
        if (!fuzzyEquals(firstEntry.sOffset, 0.0, parameters.numberTolerance)) {
            return None
        }

        // return none, if different entries
        if (laneMaterials.any {
            !fuzzyEquals(it.friction, firstEntry.friction, parameters.numberTolerance) ||
                !fuzzyEquals(it.roughness, firstEntry.roughness, parameters.numberTolerance) ||
                it.surface != firstEntry.surface
        }
        ) {
            return None
        }

        val laneMaterial = LaneMaterial(firstEntry.friction, firstEntry.roughness, firstEntry.surface)
        return Some(laneMaterial)
    }

    private fun buildAttributes(leftRightLane: RoadLanesLaneSectionLRLane) =
        attributes("${parameters.attributesPrefix}lane_") {
            attribute("type", leftRightLane.type.toString())
            leftRightLane.level.tap {
                attribute("level", it)
            }

            leftRightLane.link.tap {
                attributes("predecessor_lane") {
                    it.predecessor.forEachIndexed { i, element ->
                        attribute("_$i", element.id)
                    }
                }

                attributes("successor_lane") {
                    it.successor.forEachIndexed { i, element ->
                        attribute("_$i", element.id)
                    }
                }
            }

            attributes("material") {
                leftRightLane.material.forEachIndexed { i, element ->
                    attribute("_curvePositionStart_$i", element.sOffset)
                    attribute("_surface_$i", element.surface)
                    attribute("_friction_$i", element.friction)
                    attribute("_roughness_$i", element.roughness)
                }
            }

            attributes("speed") {
                leftRightLane.speed.forEachIndexed { i, element ->
                    attribute("_curvePositionStart_$i", element.sOffset)

                    element.unit.tap {
                        attribute("_max_$i", element.max, it.toUnitOfMeasure())
                    }.tapNone {
                        attribute("_max_$i", element.max)
                    }
                }
            }

            attributes("heightOffset") {
                leftRightLane.height.forEachIndexed { i, element ->
                    attribute("_curvePositionStart_$i", element.sOffset)
                    attribute("_inner_$i", element.inner)
                    attribute("_outer_$i", element.outer)
                }
            }
        }
}
