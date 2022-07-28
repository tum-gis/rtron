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

package io.rtron.model.roadspaces.identifier

import arrow.core.Option
import java.util.UUID

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
    val modelIdentifier: ModelIdentifier
) : AbstractRoadspacesIdentifier(), RoadspaceIdentifierInterface, ModelIdentifierInterface by modelIdentifier {

    // Properties and Initializers
    val hashKey get() = roadspaceId + '_' + modelIdentifier.fileHashSha256
    val hashedId get() = UUID.nameUUIDFromBytes(hashKey.toByteArray()).toString()

    // Conversions

    override fun toStringMap(): Map<String, String> =
        mapOf("roadspaceId" to roadspaceId) + modelIdentifier.toStringMap()

    override fun toString(): String {
        return "RoadspaceIdentifier(roadspaceId=$roadspaceId)"
    }

    companion object {
        fun of(roadspaceId: Option<String>, modelIdentifier: ModelIdentifier): RoadspaceIdentifier {
            require(roadspaceId.isDefined()) { "RoadspaceId must be defined." }
            return RoadspaceIdentifier(roadspaceId.orNull()!!, modelIdentifier)
        }
    }
}
