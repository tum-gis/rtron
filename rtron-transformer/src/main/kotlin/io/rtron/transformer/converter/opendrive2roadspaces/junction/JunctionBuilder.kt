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

package io.rtron.transformer.converter.opendrive2roadspaces.junction

import arrow.core.getOrElse
import io.rtron.model.opendrive.junction.EContactPoint
import io.rtron.model.opendrive.junction.JunctionConnection
import io.rtron.model.roadspaces.identifier.ConnectionIdentifier
import io.rtron.model.roadspaces.identifier.JunctionIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.junction.Connection
import io.rtron.model.roadspaces.junction.Junction
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.RoadspaceContactPointIdentifier
import io.rtron.std.handleEmpty
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.opendrive2roadspaces.roadspaces.toContactPoint
import io.rtron.model.opendrive.junction.Junction as OpendriveJunction

class JunctionBuilder(
    private val parameters: Opendrive2RoadspacesParameters,
) {
    // Methods
    fun buildDefaultJunction(
        junction: OpendriveJunction,
        roadspaces: List<Roadspace>,
    ): Junction {
        val junctionId = JunctionIdentifier(junction.id)
        val connections = junction.connection.map { buildConnection(junctionId, it, roadspaces) }
        return Junction(junctionId, connections)
    }

    private fun buildConnection(
        id: JunctionIdentifier,
        connection: JunctionConnection,
        roadspaces: List<Roadspace>,
    ): Connection {
        val connectionId = ConnectionIdentifier(connection.id, id)
        val incomingRoadspaceId = RoadspaceIdentifier.of(connection.incomingRoad)
        val connectingRoadspaceId = RoadspaceIdentifier.of(connection.connectingRoad)

        check(roadspaces.count { it.id == incomingRoadspaceId } == 1) { "Incoming roadspace with $incomingRoadspaceId does not exist." }
        val incomingRoadspace = roadspaces.find { it.id == incomingRoadspaceId }!!
        val incomingRoadspaceContactPointId =
            incomingRoadspace.road.getRoadspaceContactPointToJunction(id)
                .handleEmpty { throw Exception("Incoming roadspace with $incomingRoadspaceId must be connected to junction ($id).") }

        check(
            roadspaces.count { it.id == connectingRoadspaceId } == 1,
        ) { "Connecting roadspace with $connectingRoadspaceId does not exist." }
        val connectingRoadspace = roadspaces.find { it.id == connectingRoadspaceId }!!
        val connectingRoadspaceContactPoint = connection.contactPoint.getOrElse { EContactPoint.START }.toContactPoint()
        val connectingRoadspaceContactPointId =
            RoadspaceContactPointIdentifier(connectingRoadspaceContactPoint, connectingRoadspaceId)

        val incomingLaneSectionId = incomingRoadspace.road.getLaneSectionIdentifier(incomingRoadspaceContactPointId)
        val connectingLaneSectionId =
            connectingRoadspace.road.getLaneSectionIdentifier(connectingRoadspaceContactPointId)

        val laneLinks =
            connection.laneLink.associate {
                LaneIdentifier(it.from, incomingLaneSectionId) to LaneIdentifier(it.to, connectingLaneSectionId)
            }

        return Connection(connectionId, incomingRoadspaceContactPointId, connectingRoadspaceContactPointId, laneLinks)
    }
}
