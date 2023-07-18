/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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
 * Identifier of a lane section containing essential meta information.
 *
 * @param roadspaceObjectId id of the object within the road space
 * @param roadspaceObjectName name of the object within the road space
 * @param roadspaceIdentifier identifier of the road space
 */
data class RoadspaceObjectIdentifier(
    val roadspaceObjectId: String,
    val roadspaceObjectName: Option<String>,
    val roadspaceIdentifier: RoadspaceIdentifier
) : AbstractRoadspacesIdentifier(), RoadspaceIdentifierInterface by roadspaceIdentifier {

    // Properties and Initializers
    val hashKey get() = "RoadspaceObject_${roadspaceObjectId}_${roadspaceIdentifier.roadspaceId}"

    // Conversions
    override fun toAttributes(prefix: String): AttributeList {
        val roadspaceObjectIdentifier = this
        return attributes(prefix) {
            attribute("roadObjectId", roadspaceObjectIdentifier.roadspaceObjectId)
            attribute("roadObjectName", roadspaceObjectIdentifier.roadspaceObjectName)
        } + roadspaceObjectIdentifier.roadspaceIdentifier.toAttributes(prefix)
    }

    override fun toStringMap(): Map<String, String> =
        mapOf("roadspaceObjectId" to roadspaceObjectId) + roadspaceIdentifier.toStringMap()

    // Conversions
    override fun toIdentifierText(): String {
        return "RoadspaceObjectIdentifier(roadspaceObjectId=$roadspaceObjectId, roadspaceId=$roadspaceId)"
    }
}
