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

package io.rtron.model.roadspaces.junction

import io.rtron.model.roadspaces.roadspace.RoadspaceContactPointIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.std.Optional
import io.rtron.std.getValueResult

/**
 * Represents the connection of two roads, the incoming road and the connecting road.
 *
 * @param id identifier of the connection
 * @param incomingRoadspaceContactPointId identifier of the contact point of the roadspace which reaches the junction
 * @param connectingRoadspaceContactPointId identifier of the contact point of the roadspace which belongs to the junction
 * @param laneLinks links between the individual lanes
 */
data class Connection(
    val id: ConnectionIdentifier,
    val incomingRoadspaceContactPointId: RoadspaceContactPointIdentifier,
    val connectingRoadspaceContactPointId: RoadspaceContactPointIdentifier,
    val laneLinks: Map<LaneIdentifier, LaneIdentifier>
) {

    // Methods

    /**
     * Returns the lane information of the succeeding lane with [laneIdentifier], if a link exists.
     *
     * @param laneIdentifier identifier of the lane for which the linked and succeeding lane shall be found
     */
    fun getSuccessorLane(laneIdentifier: LaneIdentifier): Optional<LaneIdentifier> {
        if (incomingRoadspaceContactPointId.roadspaceIdentifier != laneIdentifier.toRoadspaceIdentifier()) return Optional.empty()

        return laneLinks.getValueResult(laneIdentifier).let { Optional.of(it) }
    }
}
