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

import arrow.core.Option
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes

/**
 * Road space identifier interface required for class delegation.
 */
interface RoadspaceIdentifierInterface {
    val roadspaceId: String
}

/**
 * Identifier of a road space containing essential meta information.
 *
 * @param roadspaceId id of the road space
 * @param modelIdentifier identifier of the model
 */
data class RoadspaceIdentifier(
    override val roadspaceId: String,
) : AbstractRoadspacesIdentifier(), RoadspaceIdentifierInterface {
    // Properties and Initializers
    val hashKey get() = "Roadspace_$roadspaceId"

    // Conversions
    override fun toAttributes(prefix: String): AttributeList {
        val roadspaceIdentifier = this
        return attributes(prefix) {
            attribute("roadId", roadspaceIdentifier.roadspaceId)
        }
    }

    override fun toStringMap(): Map<String, String> = mapOf("roadspaceId" to roadspaceId)

    override fun toIdentifierText(): String {
        return "RoadspaceIdentifier(roadspaceId=$roadspaceId)"
    }

    companion object {
        fun of(roadspaceId: Option<String>): RoadspaceIdentifier {
            require(roadspaceId.isSome()) { "RoadspaceId must be defined." }
            return RoadspaceIdentifier(roadspaceId.getOrNull()!!)
        }
    }
}
