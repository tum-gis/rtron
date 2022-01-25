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

package io.rtron.transformer.opendrive2roadspaces.junction

import arrow.core.getOrElse
import com.github.kittinunf.result.Result
import io.rtron.io.logging.LogManager
import io.rtron.model.opendrive.junction.JunctionConnection
import io.rtron.model.roadspaces.ModelIdentifier
import io.rtron.model.roadspaces.junction.Connection
import io.rtron.model.roadspaces.junction.ConnectionIdentifier
import io.rtron.model.roadspaces.junction.Junction
import io.rtron.model.roadspaces.junction.JunctionIdentifier
import io.rtron.model.roadspaces.roadspace.ContactPoint
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.RoadspaceContactPointIdentifier
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.std.handleEmpty
import io.rtron.std.mapAndHandleFailureOnOriginal
import io.rtron.transformer.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.opendrive2roadspaces.roadspaces.toContactPoint
import io.rtron.model.opendrive.junction.Junction as OpendriveJunction

class JunctionBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {
    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    // Methods
    fun buildJunction(id: ModelIdentifier, junction: OpendriveJunction, roadspaces: List<Roadspace>): Junction {
        val junctionId = JunctionIdentifier(junction.id, id)
        val connections = junction.connection.mapAndHandleFailureOnOriginal(
            { buildConnection(junctionId, it, roadspaces) },
            { result, original -> _reportLogger.log(result, ConnectionIdentifier(original.id, junctionId).toString(), "Ignoring connection.") }
        )

        return Junction(junctionId, connections)
    }

    private fun buildConnection(id: JunctionIdentifier, connection: JunctionConnection, roadspaces: List<Roadspace>): Result<Connection, Exception> {
        val connectionId = ConnectionIdentifier(connection.id, id)

        val incomingRoadspaceId = RoadspaceIdentifier(connection.incomingRoad, id.modelIdentifier)
        val incomingRoadspace = roadspaces.find { it.id == incomingRoadspaceId } ?: return Result.error(Exception("Incoming roadspace with $incomingRoadspaceId does not exist."))
        val incomingRoadspaceContactPointId = incomingRoadspace.road.getRoadspaceContactPointToJunction(id).handleEmpty { return Result.error(Exception("Incoming roadspace with $incomingRoadspaceId must be connected to junction ($id).")) }

        val connectingRoadspaceId = RoadspaceIdentifier(connection.connectingRoad, id.modelIdentifier)
        val connectingRoadspace = roadspaces.find { it.id == connectingRoadspaceId } ?: return Result.error(Exception("Connecting roadspace with $connectingRoadspaceId does not exist."))
        val connectingRoadspaceContactPoint = connection.contactPoint.toContactPoint().getOrElse { ContactPoint.START }
        val connectingRoadspaceContactPointId = RoadspaceContactPointIdentifier(connectingRoadspaceContactPoint, connectingRoadspaceId)

        val incomingLaneSectionId = incomingRoadspace.road.getLaneSectionIdentifier(incomingRoadspaceContactPointId)
        val connectingLaneSectionId = connectingRoadspace.road.getLaneSectionIdentifier(connectingRoadspaceContactPointId)

        val laneLinks = connection.laneLink.map { LaneIdentifier(it.from, incomingLaneSectionId) to LaneIdentifier(it.to, connectingLaneSectionId) }.toMap()
        val roadspacesConnection = Connection(connectionId, incomingRoadspaceContactPointId, connectingRoadspaceContactPointId, laneLinks)

        return Result.success(roadspacesConnection)
    }
}
