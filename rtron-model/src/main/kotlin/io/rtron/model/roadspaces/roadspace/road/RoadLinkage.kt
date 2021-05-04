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

import io.rtron.model.roadspaces.junction.JunctionIdentifier
import io.rtron.model.roadspaces.roadspace.RoadspaceContactPointIdentifier
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.std.Optional
import io.rtron.std.present

/**
 * Contains the topological information about the road.
 */
data class RoadLinkage(
    val belongsToJunctionId: Optional<JunctionIdentifier>,
    val predecessorRoadspaceContactPointId: Optional<RoadspaceContactPointIdentifier>,
    val predecessorJunctionId: Optional<JunctionIdentifier>,
    val successorRoadspaceContactPointId: Optional<RoadspaceContactPointIdentifier>,
    val successorJunctionId: Optional<JunctionIdentifier>
) {
    // Properties and Initializers
    init {
        require(!(predecessorRoadspaceContactPointId.isPresent() && predecessorJunctionId.isPresent())) { "Predecessor must be either a roadspace or junction or neither." }
        require(!(successorRoadspaceContactPointId.isPresent() && successorJunctionId.isPresent())) { "Successor must be either a roadspace or junction or neither." }

        belongsToJunctionId.present {
            require(predecessorJunctionId.isEmpty()) { "If a road belongs to a junction (id=$it), a predecessing junction must not exist." }
            require(successorJunctionId.isEmpty()) { "If a road belongs to a junction (id=$it), a successing junction must not exist." }
        }
    }

    // Methods

    /** Returns a list of [RoadspaceIdentifier] of roadspaces which is referred to (predecessor and/or successor).  */
    fun getAllUsedRoadspaceIds(): List<RoadspaceIdentifier> =
        (predecessorRoadspaceContactPointId.toList() + successorRoadspaceContactPointId.toList())
            .map { it.roadspaceIdentifier }.distinct()

    /** Returns a list of [JunctionIdentifier] of junctions which is referred to (belongs to, predecessor, successor).  */
    fun getAllUsedJunctionIds(): List<JunctionIdentifier> =
        (belongsToJunctionId.toList() + predecessorJunctionId.toList() + successorJunctionId.toList()).distinct()
}
