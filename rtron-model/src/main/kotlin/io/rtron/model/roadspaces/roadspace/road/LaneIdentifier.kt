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

package io.rtron.model.roadspaces.roadspace.road

import io.rtron.math.std.sign
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import java.util.UUID
import kotlin.math.abs

/**
 * Identifier of a lane containing essential meta information.
 *
 * @param laneId id of the lane
 * @param laneSectionIdentifier identifier of the lane section
 */
data class LaneIdentifier(
    val laneId: Int,
    val laneSectionIdentifier: LaneSectionIdentifier
) : LaneSectionIdentifierInterface by laneSectionIdentifier {

    // Properties and Initializers
    val hashKey get() = laneId.toString() + '_' +
        laneSectionIdentifier.laneSectionId + '_' +
        laneSectionIdentifier.roadspaceIdentifier.roadspaceId + '_' +
        laneSectionIdentifier.roadspaceIdentifier.modelIdentifier.fileHashSha256
    val hashedId get() = UUID.nameUUIDFromBytes(hashKey.toByteArray()).toString()

    // Methods

    fun isLeft() = laneId > 0
    fun isCenter() = laneId == 0
    fun isRight() = laneId < 0

    /** Returns the [LaneIdentifier] of the lane which is located adjacently inner (towards the reference line) to the
     * lane of this identifier.  */
    fun getAdjacentInnerLaneIdentifier(): LaneIdentifier =
        LaneIdentifier(sign(laneId) * (abs(laneId) - 1), laneSectionIdentifier)

    /** Returns the [LaneIdentifier] of the lane which is located adjacently outer (in the opposite direction of the
     * reference line) to the lane of this identifier.  */
    fun getAdjacentOuterLaneIdentifier(): LaneIdentifier =
        LaneIdentifier(sign(laneId) * (abs(laneId) + 1), laneSectionIdentifier)

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
    fun toStringMap(): Map<String, String> =
        mapOf("laneId" to laneId.toString()) + laneSectionIdentifier.toStringMap()

    override fun toString() = "LaneIdentifier(laneId=$laneId, laneSectionId=$laneSectionId, roadId=$roadspaceId)"
    fun toRoadspaceIdentifier() = laneSectionIdentifier.roadspaceIdentifier

    companion object {

        fun of(laneId: Int, laneSectionId: Int, roadspaceIdentifier: RoadspaceIdentifier) =
            LaneIdentifier(laneId, LaneSectionIdentifier(laneSectionId, roadspaceIdentifier))
    }
}
