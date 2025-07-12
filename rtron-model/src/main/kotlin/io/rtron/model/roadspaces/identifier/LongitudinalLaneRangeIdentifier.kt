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

import io.rtron.model.roadspaces.roadspace.attribute.AttributeList

/**
 * Identifier of a range of lanes in longitudinal direction (between lane sections or different roads).
 *
 * @param fromLaneId range of lane ids which must have a lower and upper bound
 * @param toLaneId identifier of the lane section
 */
data class LongitudinalLaneRangeIdentifier(
    val fromLaneId: LaneIdentifier,
    val toLaneId: LaneIdentifier,
) : AbstractRoadspacesIdentifier() {
    // Properties and Initializers
    init {
        require(fromLaneId != toLaneId) { "Lane identifier and lane identifier of successor must not be the same." }
    }

    val hashKey get() = "LongitudinalLaneRange_${fromLaneId.hashKey}_${toLaneId.hashKey}"

    // Methods

    /** Returns true, if the [fromLaneId] lane is located within the same roadspace as the lane with this identifier. */
    fun isWithinSameRoad() = fromLaneId.toRoadspaceIdentifier() == toLaneId.toRoadspaceIdentifier()

    // Conversions
    override fun toAttributes(prefix: String): AttributeList = fromLaneId.toAttributes("from$prefix") + toLaneId.toAttributes("to$prefix")

    override fun toStringMap(): Map<String, String> = fromLaneId.toStringMap() + toLaneId.toStringMap()

    override fun toIdentifierText() =
        "LongitudinalLaneRangeIdentifier(fromLaneId=${fromLaneId.toIdentifierText()}, toLaneId=${toLaneId.toIdentifierText()})"
}
