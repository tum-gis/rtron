/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.model.roadspaces.identifier

import io.rtron.math.range.Range
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import kotlin.math.max
import kotlin.math.min

/**
 * Identifier of a range of lanes in lateral direction (within a lane section).
 *
 * @param laneIdRange range of lane ids which must have a lower and upper bound
 * @param laneSectionIdentifier identifier of the lane section
 */
data class LateralLaneRangeIdentifier(
    val laneIdRange: Range<Int>,
    val laneSectionIdentifier: LaneSectionIdentifier,
) : AbstractRoadspacesIdentifier(),
    LaneSectionIdentifierInterface by laneSectionIdentifier {
    // Properties and Initializers
    init {
        require(laneIdRange.hasLowerBound()) { "laneIdRange must have a lower bound." }
        require(laneIdRange.hasUpperBound()) { "laneIdRange must have a upper bound." }
        // require(laneIdRange.lowerEndpointOrNull()!! != laneIdRange.upperEndpointOrNull()!!) { "lowerEndpoint and upperEndpoint must be different ." }
    }

    val hashKey get() =
        "LateralLaneRange_${laneIdRange.lowerEndpointOrNull()!!}_${laneIdRange.upperEndpointOrNull()!!}_" +
            "${laneSectionIdentifier.laneSectionId}_${laneSectionIdentifier.roadspaceIdentifier.roadspaceId}"

    val lowerLaneId get() = LaneIdentifier(laneIdRange.lowerEndpointOrNull()!!, laneSectionIdentifier)
    val upperLaneId get() = LaneIdentifier(laneIdRange.upperEndpointOrNull()!!, laneSectionIdentifier)

    // Methods

    /** Returns all lane identifiers contained in this range. */
    fun getAllLeftRightLaneIdentifiers(): List<LaneIdentifier> =
        (laneIdRange.lowerEndpointOrNull()!!..laneIdRange.upperEndpointOrNull()!!)
            .filter {
                it != 0
            }.map { LaneIdentifier(it, this.laneSectionIdentifier) }

    /** Returns true, if the [laneIdentifier] is contained in this range. */
    fun contains(laneIdentifier: LaneIdentifier) =
        this.laneSectionIdentifier == laneIdentifier.laneSectionIdentifier && this.laneIdRange.contains(laneIdentifier.laneId)

    // Conversions
    override fun toAttributes(prefix: String): AttributeList {
        val lateralLaneRangeIdentifier = this
        return attributes(prefix) {
            attribute("lowerLaneId", lateralLaneRangeIdentifier.laneIdRange.lowerEndpointOrNull()!!)
            attribute("upperLaneId", lateralLaneRangeIdentifier.laneIdRange.upperEndpointOrNull()!!)
        } + lateralLaneRangeIdentifier.laneSectionIdentifier.toAttributes(prefix)
    }

    override fun toStringMap(): Map<String, String> =
        mapOf(
            "lowerLaneId" to laneIdRange.lowerEndpointOrNull()!!.toString(),
            "upperLaneId" to laneIdRange.upperEndpointOrNull()!!.toString(),
        ) + laneSectionIdentifier.toStringMap()

    override fun toIdentifierText() =
        "LateralLaneRangeIdentifier(lowerLaneId=${laneIdRange.lowerEndpointOrNull()!!}, " +
            "upperLaneId=${laneIdRange.upperEndpointOrNull()!!}, laneSectionId=$laneSectionId, roadId=$roadspaceId)"

    companion object {
        fun of(
            fromLaneId: LaneIdentifier,
            toLaneId: LaneIdentifier,
        ): LateralLaneRangeIdentifier {
            require(fromLaneId.laneSectionIdentifier == toLaneId.laneSectionIdentifier) { "Must have the same laneSectionId" }
            val lowerLaneId = min(fromLaneId.laneId, toLaneId.laneId)
            val upperLaneId = max(fromLaneId.laneId, toLaneId.laneId)

            return LateralLaneRangeIdentifier(Range.closed(lowerLaneId, upperLaneId), fromLaneId.laneSectionIdentifier)
        }
    }
}
