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
 * Junction identifier interface required for class delegation.
 */
interface JunctionIdentifierInterface {
    val junctionId: String
}

/**
 * Identifier of a [Junction].
 *
 * @param junctionId id of the junction
 */
data class JunctionIdentifier(
    override val junctionId: String,
) : AbstractRoadspacesIdentifier(), JunctionIdentifierInterface {
    // Properties and Initializers
    val hashKey get() = "Junction_$junctionId"

    // Conversions
    override fun toAttributes(prefix: String): AttributeList {
        val junctionIdentifier = this
        return attributes(prefix) {
            attribute("junctionId", junctionIdentifier.junctionId)
        }
    }

    override fun toStringMap(): Map<String, String> = mapOf("junctionId" to junctionId)

    override fun toIdentifierText(): String {
        return "JunctionIdentifier(junctionId=$junctionId)"
    }
}
