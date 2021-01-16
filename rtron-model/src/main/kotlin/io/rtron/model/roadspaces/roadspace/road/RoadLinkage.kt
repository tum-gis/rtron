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
import io.rtron.model.roadspaces.topology.junction.JunctionIdentifier
import io.rtron.std.Optional

enum class ContactPoint(val relativeIndex: Int) {
    START(0),
    END(-1)
}

/**
 * Contains the topological information about the road.
 */
data class RoadLinkage(
    val belongsToJunctionId: Optional<JunctionIdentifier>,
    val predecessorRoadspaceId: Optional<RoadspaceIdentifier>,
    val predecessorJunctionId: Optional<JunctionIdentifier>,
    val predecessorContactPoint: Optional<ContactPoint>,
    val successorRoadspaceId: Optional<RoadspaceIdentifier>,
    val successorJunctionId: Optional<JunctionIdentifier>,
    val successorContactPoint: Optional<ContactPoint>
)
