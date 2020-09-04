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

package io.rtron.transformer.opendrive2roadspaces.topology

import io.rtron.model.opendrive.junction.JunctionConnection
import io.rtron.model.roadspaces.ModelIdentifier
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.road.ContactPoint
import io.rtron.model.roadspaces.topology.junction.Connection
import io.rtron.model.roadspaces.topology.junction.ConnectionIdentifier
import io.rtron.model.roadspaces.topology.junction.Junction
import io.rtron.model.roadspaces.topology.junction.JunctionIdentifier
import io.rtron.std.getOrElse
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.opendrive2roadspaces.roadspaces.toContactPoint
import io.rtron.model.opendrive.junction.Junction as OpendriveJunction


class TopologyBuilder(
        private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Methods
    fun buildJunction(id: ModelIdentifier, srcJunction: OpendriveJunction): Junction {
        val junctionId = JunctionIdentifier(srcJunction.id, id)
        val connections = srcJunction.connection.map { buildConnection(junctionId, it) }

        return Junction(junctionId, connections)
    }

    private fun buildConnection(id: JunctionIdentifier, srcConnection: JunctionConnection): Connection {
        val connectionId = ConnectionIdentifier(srcConnection.id, id)
        val incomingRoadspaceId = RoadspaceIdentifier(srcConnection.incomingRoad, id.modelIdentifier)
        val connectingRoadspaceId = RoadspaceIdentifier(srcConnection.connectingRoad, id.modelIdentifier)
        val contactPoint = srcConnection.contactPoint.toContactPoint().getOrElse(ContactPoint.START)

        val laneLinks = srcConnection.laneLink.map { it.from to it.to }.toMap()

        return Connection(connectionId, incomingRoadspaceId, connectingRoadspaceId, contactPoint, laneLinks)
    }
}
