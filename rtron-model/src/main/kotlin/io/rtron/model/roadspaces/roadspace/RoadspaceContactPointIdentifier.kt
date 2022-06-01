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

package io.rtron.model.roadspaces.roadspace

import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifierInterface

enum class ContactPoint(val relativeIndex: Int) {
    START(0),
    END(-1)
}

/**
 * Identifier of a lane section containing essential meta information.
 *
 * @param roadspaceContactPoint start or end of roadspace
 * @param roadspaceIdentifier identifier of the road space
 */
data class RoadspaceContactPointIdentifier(
    val roadspaceContactPoint: ContactPoint,
    val roadspaceIdentifier: RoadspaceIdentifier
) : RoadspaceIdentifierInterface by roadspaceIdentifier {

    // Conversions
    override fun toString(): String {
        return "RoadspaceObjectIdentifier(roadspaceContactPoint=$roadspaceContactPoint, roadspaceId=$roadspaceId)"
    }
}
