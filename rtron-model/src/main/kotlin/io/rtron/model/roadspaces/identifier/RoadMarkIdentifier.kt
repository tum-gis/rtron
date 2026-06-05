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
import io.rtron.model.roadspaces.roadspace.attribute.attributes

class RoadMarkIdentifier(
    val roadMarkId: Int,
    val laneIdentifier: LaneIdentifier,
) : AbstractRoadspacesIdentifier(),
    LaneIdentifierInterface by laneIdentifier {
    // Properties and Initializers
    val hashKey get() = "RoadMark_${roadMarkId}_Lane_${laneId}_${laneIdentifier.laneSectionId}_${laneIdentifier.roadspaceId}"

    // Conversions
    override fun toAttributes(prefix: String): AttributeList {
        val roadMarkIdentifier = this
        return attributes(prefix) {
            attribute("roadMarkId", roadMarkIdentifier.roadMarkId)
        } + roadMarkIdentifier.laneIdentifier.toAttributes(prefix)
    }

    override fun toStringMap(): Map<String, String> = mapOf("roadMarkId" to roadMarkId.toString()) + laneIdentifier.toStringMap()

    override fun toIdentifierText() =
        "RoadMarkIdentifier(roadMarkId=$roadMarkId, laneIdentifier=$laneIdentifier, laneId=$laneId, laneSectionId=$laneSectionId, roadId=$roadspaceId)"
}
