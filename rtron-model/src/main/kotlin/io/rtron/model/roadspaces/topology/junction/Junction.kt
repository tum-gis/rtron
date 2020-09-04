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

package io.rtron.model.roadspaces.topology.junction

import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.RelativeLaneIdentifier
import io.rtron.std.unwrapValues


/**
 * Represents a junction which connects multiple roads and contains lane linkage information.
 *
 * @param id identifier of the [Junction]
 * @param connections list of [Connection] that connect the roads and the respective lanes
 */
data class Junction(
        val id: JunctionIdentifier,
        val connections: List<Connection>
) {

    // Methods

    /**
     * Returns the successor lane referenced by [RelativeLaneIdentifier], which follow the [laneIdentifier].
     *
     * @param laneIdentifier identifier for which the successor
     */
    fun getSuccessorLane(laneIdentifier: LaneIdentifier): List<RelativeLaneIdentifier> =
            connections.map { it.getSuccessorLane(laneIdentifier) }.unwrapValues()
}
