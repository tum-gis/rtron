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

import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes

/**
 * Lane section identifier interface required for class delegation.
 */
interface LaneSectionIdentifierInterface : RoadspaceIdentifierInterface {
    val laneSectionId: Int
}

/**
 * Identifier of a lane section containing essential meta information.
 *
 * @param laneSectionId identifier of the lane section, whereby the first lane section is referenced with 0
 * @param roadspaceIdentifier identifier of the road space
 */
data class LaneSectionIdentifier(
    override val laneSectionId: Int,
    val roadspaceIdentifier: RoadspaceIdentifier,
) : AbstractRoadspacesIdentifier(),
    LaneSectionIdentifierInterface,
    RoadspaceIdentifierInterface by roadspaceIdentifier {
    // Properties and Initializers
    init {
        require(laneSectionId >= 0) { "Lane section id must be non-negative." }
    }

    // Methods

    /** Returns the identifier for the previous lane section. */
    fun getPreviousLaneSectionIdentifier() = LaneSectionIdentifier(this.laneSectionId - 1, this.roadspaceIdentifier)

    /** Returns the identifier for the next lane section. */
    fun getNextLaneSectionIdentifier() = LaneSectionIdentifier(this.laneSectionId + 1, this.roadspaceIdentifier)

    // Conversions
    override fun toAttributes(prefix: String): AttributeList {
        val laneSectionIdentifier = this
        return attributes(prefix) {
            attribute("laneSectionId", laneSectionIdentifier.laneSectionId)
        } + laneSectionIdentifier.roadspaceIdentifier.toAttributes(prefix)
    }

    override fun toStringMap(): Map<String, String> = mapOf("laneSectionId" to laneSectionId.toString()) + roadspaceIdentifier.toStringMap()

    override fun toIdentifierText(): String = "LaneSectionIdentifier(laneSectionId=$laneSectionId, roadSpaceId=$roadspaceId)"
}
