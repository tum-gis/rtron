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

import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifierInterface
import kotlin.math.abs


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
        val roadspaceIdentifier: RoadspaceIdentifier
) : LaneSectionIdentifierInterface, RoadspaceIdentifierInterface by roadspaceIdentifier {

    // Properties and Initializers
    init {
        require(laneSectionId >= 0) { "Lane section id must be non-negative." }
    }

    // Methods

    /** Returns the identifier for the next lane section. */
    fun getNextLaneSectionIdentifier() =
            LaneSectionIdentifier(this.laneSectionId + 1, this.roadspaceIdentifier)

    // Conversions
    override fun toString(): String {
        return "LaneSectionIdentifier(laneSectionId=$laneSectionId, roadSpaceId=$roadspaceId)"
    }
}


/**
 * Identifier of a lane section which allows for negative indexing.
 *
 * @param laneSectionId identifier of the lane section, whereby the first lane section is referenced with 0 and the
 * last lane section can be referenced with -1
 */
data class RelativeLaneSectionIdentifier(
        override val laneSectionId: Int,
        val roadspaceIdentifier: RoadspaceIdentifier
) : LaneSectionIdentifierInterface, RoadspaceIdentifierInterface by roadspaceIdentifier {

    // Conversions

    /**
     * Returns an absolute [LaneSectionIdentifier] and resolves the negative indices.
     *
     * @param size number of lane sections in list (last index + 1)
     */
    fun toAbsoluteLaneSectionIdentifier(size: Int): LaneSectionIdentifier {
        require(abs(laneSectionId) <= size)
        { "Lane section identifier must less or equals the given size ($size)" }

        return if (laneSectionId >= 0) LaneSectionIdentifier(laneSectionId, roadspaceIdentifier)
        else LaneSectionIdentifier(size - laneSectionId, roadspaceIdentifier)
    }
}
