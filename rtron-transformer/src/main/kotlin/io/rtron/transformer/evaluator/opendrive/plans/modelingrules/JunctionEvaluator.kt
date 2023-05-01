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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules

import arrow.core.None
import arrow.core.flattenOption
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyJunction
import io.rtron.model.opendrive.junction.EJunctionType
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.messages.opendrive.of

object JunctionEvaluator {

    // Methods
    fun evaluate(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel = evaluateAllJunctions(modifiedOpendriveModel, parameters, messageList)
        modifiedOpendriveModel = evaluateDirectJunctions(modifiedOpendriveModel, parameters, messageList)

        return modifiedOpendriveModel
    }

    private fun evaluateAllJunctions(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel = everyJunction.modify(modifiedOpendriveModel) { currentJunction ->

            // It is deprecated to omit the <laneLink> element.
            val junctionConnectionsFiltered = currentJunction.connection.filter { it.laneLink.isNotEmpty() }
            if (currentJunction.connection.size > junctionConnectionsFiltered.size) {
                messageList += DefaultMessage.of("JunctionConnectionWithoutLaneLinks", "Junction connections (number of connections: ${currentJunction.connection.size - junctionConnectionsFiltered.size}) were removed since they did not contain any laneLinks.", currentJunction.additionalId, Severity.ERROR, wasFixed = true)
            }
            currentJunction.connection = junctionConnectionsFiltered

            // The @mainRoad, @orientation, @sStart and @sEnd attributes shall only be specified for virtual junctions.
            if (currentJunction.typeValidated != EJunctionType.VIRTUAL) {
                currentJunction.mainRoad.tap {
                    messageList += DefaultMessage.of("InvalidJunctionAttribute", "Attribute 'mainRoad' shall only be specified for virtual junctions", currentJunction.additionalId, Severity.FATAL_ERROR, wasFixed = true)
                    currentJunction.mainRoad = None
                }

                currentJunction.orientation.tap {
                    messageList += DefaultMessage.of("InvalidJunctionAttribute", "Attribute 'orientation' shall only be specified for virtual junctions", currentJunction.additionalId, Severity.FATAL_ERROR, wasFixed = true)
                    currentJunction.orientation = None
                }

                currentJunction.sStart.tap {
                    messageList += DefaultMessage.of("InvalidJunctionAttribute", "Attribute 'sStart' shall only be specified for virtual junctions", currentJunction.additionalId, Severity.FATAL_ERROR, wasFixed = true)
                    currentJunction.sStart = None
                }

                currentJunction.sEnd.tap {
                    messageList += DefaultMessage.of("InvalidJunctionAttribute", "Attribute 'sEnd' shall only be specified for virtual junctions", currentJunction.additionalId, Severity.FATAL_ERROR, wasFixed = true)
                    currentJunction.sEnd = None
                }
            }

            currentJunction
        }

        return modifiedOpendriveModel
    }

    private fun evaluateDirectJunctions(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel = everyJunction.modify(modifiedOpendriveModel) { currentJunction ->
            if (currentJunction.typeValidated != EJunctionType.DIRECT) {
                return@modify currentJunction
            }

            // Each connecting road shall be represented by exactly one <connection> element. A connecting road may contain as many lanes as required.
            val connectingRoadIdsRepresentedByMultipleConnections = currentJunction.connection.map { it.connectingRoad }.flattenOption().groupingBy { it }.eachCount().filter { it.value > 1 }
            if (connectingRoadIdsRepresentedByMultipleConnections.isNotEmpty()) {
                messageList += DefaultMessage.of(
                    "MultipleConnectionsRepresentingSameConnectionRoad",
                    "Junctions contains multiple connections representing the same connecting road (affected connecting roads: ${connectingRoadIdsRepresentedByMultipleConnections.keys.joinToString()} )",
                    currentJunction.additionalId,
                    Severity.ERROR,
                    wasFixed = false
                )
            }

            // Junctions shall only be used when roads cannot be linked directly. They clarify ambiguities for the linking. Ambiguities are caused when a road has two or more possible predecessor or successor roads.
            // see: https://github.com/tum-gis/rtron/issues/24
            data class ConnectionRoadIds(val connectingRoadId: String, val predecessorRoadId: String, val successorRoadId: String)
            val junctionConnectionRoadIds = currentJunction.connection
                .map { it.connectingRoad }
                .flattenOption()
                .map { opendriveModel.getRoad(it) }
                .flattenOption()
                .map { ConnectionRoadIds(connectingRoadId = it.id, predecessorRoadId = it.link.flatMap { it.predecessor }.flatMap { it.getRoadPredecessorSuccessor() }.orNull()!!.first, successorRoadId = it.link.flatMap { it.successor }.flatMap { it.getRoadPredecessorSuccessor() }.orNull()!!.first) }
            val predecessorSuccessorRoadIds = junctionConnectionRoadIds.flatMap { listOf(it.predecessorRoadId, it.successorRoadId) }
            if (predecessorSuccessorRoadIds.distinct().size < predecessorSuccessorRoadIds.size) {
                messageList += DefaultMessage.of("InvalidJunctionUsage", "Junction shall not be used, since all roads can be directly linked without ambiguities. The connecting roads do not share a predecessor or successor road.", currentJunction.additionalId, Severity.ERROR, wasFixed = false)
            }

            currentJunction
        }

        return modifiedOpendriveModel
    }
}
