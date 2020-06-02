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

package io.rtron.model.roadspaces.roadspace.road


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

    // Methods

    /** Returns the identifier for the adjacent lane to the left. */
    fun getAdjacentLeftLaneIdentifier(): LaneIdentifier {
        val requestedLaneId = if (laneId == -1) 1 else laneId + 1
        return LaneIdentifier(requestedLaneId, this.laneSectionIdentifier)
    }

    // Conversions
    override fun toString(): String {
        return "LaneIdentifier(laneId=$laneId, laneSectionId=$laneSectionId, roadId=$roadspaceId)"
    }

}
