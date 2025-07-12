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

package io.rtron.model.roadspaces.identifier

import io.rtron.math.std.sign
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import kotlin.math.abs

/**
 * Identifier of a lane containing essential meta information.
 *
 * @param laneId id of the lane
 * @param laneSectionIdentifier identifier of the lane section
 */
data class LaneIdentifier(
    val laneId: Int,
    val laneSectionIdentifier: LaneSectionIdentifier,
) : AbstractRoadspacesIdentifier(),
    LaneSectionIdentifierInterface by laneSectionIdentifier {
    // Properties and Initializers
    val hashKey get() = "Lane_${laneId}_${laneSectionIdentifier.laneSectionId}_${laneSectionIdentifier.roadspaceIdentifier.roadspaceId}"

    // Methods
    fun getRoadSide(): RoadSide =
        when {
            laneId > 0 -> RoadSide.LEFT
            laneId == 0 -> RoadSide.CENTER
            else -> RoadSide.RIGHT
        }

    fun isLeft() = laneId > 0

    fun isCenter() = laneId == 0

    fun isRight() = laneId < 0

    /** Returns true, if lane id is in the direction of the reference line. */
    fun isForward() = isCenter() || isRight()

    /** Returns true, if lane id is in the opposite direction of the reference line. */
    fun isBackward() = isLeft()

    /** Returns the [LaneIdentifier] of the lane which is located adjacently inner (towards the reference line) to the
     * lane of this identifier.
     */
    fun getAdjacentInnerLaneIdentifier(): LaneIdentifier = LaneIdentifier(sign(laneId) * (abs(laneId) - 1), laneSectionIdentifier)

    /** Returns the [LaneIdentifier] of the lane which is located adjacently outer (in the opposite direction of the
     * reference line) to the lane of this identifier.
     */
    fun getAdjacentOuterLaneIdentifier(): LaneIdentifier = LaneIdentifier(sign(laneId) * (abs(laneId) + 1), laneSectionIdentifier)

    /** Returns the identifier for the adjacent lane to the left. */
    fun getAdjacentLeftLaneIdentifier(): LaneIdentifier = LaneIdentifier(laneId + 1, laneSectionIdentifier)

    /** Returns the identifier for the adjacent lane to the right. */
    fun getAdjacentRightLaneIdentifier(): LaneIdentifier = LaneIdentifier(laneId - 1, laneSectionIdentifier)

    /** Returns true, if the [other] lane is located within the same roadspace as the lane with this identifier. */
    fun isWithinSameRoad(other: LaneIdentifier) = toRoadspaceIdentifier() == other.toRoadspaceIdentifier()

    /** Returns true, if the [other] lane is located laterally adjacent (same lane section) to this lane. */
    fun isLaterallyAdjacent(other: LaneIdentifier): Boolean =
        getAdjacentInnerLaneIdentifier() == other || getAdjacentOuterLaneIdentifier() == other

    // Conversions
    override fun toAttributes(prefix: String): AttributeList {
        val laneIdentifier = this
        return attributes(prefix) {
            attribute("laneId", laneIdentifier.laneId)
        } + laneIdentifier.laneSectionIdentifier.toAttributes(prefix)
    }

    override fun toStringMap(): Map<String, String> = mapOf("laneId" to laneId.toString()) + laneSectionIdentifier.toStringMap()

    override fun toIdentifierText() = "LaneIdentifier(laneId=$laneId, laneSectionId=$laneSectionId, roadId=$roadspaceId)"

    fun toRoadspaceIdentifier() = laneSectionIdentifier.roadspaceIdentifier

    companion object {
        fun of(
            laneId: Int,
            laneSectionId: Int,
            roadspaceIdentifier: RoadspaceIdentifier,
        ) = LaneIdentifier(laneId, LaneSectionIdentifier(laneSectionId, roadspaceIdentifier))
    }
}
