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

package io.rtron.transformer.evaluator.roadspaces.plans.modelingrules

import arrow.core.getOrElse
import arrow.core.handleError
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.io.messages.merge
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.toIllegalStateException
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.ContactPoint
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluatorParameters
import io.rtron.transformer.evaluator.roadspaces.plans.AbstractRoadspacesEvaluator

class ModelingRulesEvaluator(val parameters: RoadspacesEvaluatorParameters) : AbstractRoadspacesEvaluator() {

    // Methods
    override fun evaluate(roadspacesModel: RoadspacesModel): DefaultMessageList {
        val messageList = DefaultMessageList()

        messageList += roadspacesModel.getAllRoadspaces().map { evaluateRoadLinkages(it, roadspacesModel) }.merge()

        return messageList
    }

    private fun evaluateRoadLinkages(roadspace: Roadspace, roadspacesModel: RoadspacesModel): DefaultMessageList {
        val messageList = DefaultMessageList()

        roadspace.road.getAllLeftRightLaneIdentifiers()
            .filter { roadspace.road.isInLastLaneSection(it) }
            .forEach { laneId ->
                val successorLaneIds = roadspacesModel.getSuccessorLaneIdentifiers(laneId).getOrElse { throw it }

                messageList += successorLaneIds.map { successorLaneId ->
                    evaluateLaneTransition(laneId, successorLaneId, roadspace.road, roadspacesModel.getRoadspace(successorLaneId.toRoadspaceIdentifier()).getOrElse { throw it }.road, roadspacesModel)
                }.merge()
            }

        return messageList
    }

    private fun evaluateLaneTransition(laneId: LaneIdentifier, successorLaneId: LaneIdentifier, road: Road, successorRoad: Road, roadspacesModel: RoadspacesModel): DefaultMessageList {
        require(laneId !== successorLaneId) { "Lane identifiers of current and of successor lane must be different." }
        require(laneId.roadspaceId !== successorLaneId.roadspaceId) { "Lane identifiers of current and of successor lane must be different regarding their roadspaceId." }
        require(road.id !== successorRoad.id) { "Road and successor road must be different." }

        val messageList = DefaultMessageList()

        val laneLeftLaneBoundaryPoint: Vector3D = road.getLeftLaneBoundary(laneId).getOrElse { throw it }
            .calculateEndPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrElse { throw it }
        val laneRightLaneBoundaryPoint: Vector3D = road.getRightLaneBoundary(laneId).getOrElse { throw it }
            .calculateEndPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrElse { throw it }

        // false, if the successor lane is connected by its end (leads to swapping of the vertices)
        val successorContactStart = !road.linkage.successorRoadspaceContactPointId
            .map { it.roadspaceContactPoint }
            .exists { it == ContactPoint.END }

        val successorLeftLaneBoundary = successorRoad.getLeftLaneBoundary(successorLaneId)
            .handleError { return messageList }.getOrElse { throw it } // TODO: identify inconsistencies in the topology of the model
        val successorRightLaneBoundary = successorRoad.getRightLaneBoundary(successorLaneId)
            .handleError { return messageList }.getOrElse { throw it } // TODO: identify inconsistencies in the topology of the model

        // if contact of successor at the start, normal connecting
        // if contact of the successor at the end, the end positions have to be calculated and left and right boundary have to be swapped
        val laneLeftLaneBoundarySuccessorPoint =
            if (successorContactStart) successorLeftLaneBoundary.calculateStartPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrElse { throw it }
            else successorRightLaneBoundary.calculateEndPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrElse { throw it }
        val laneRightLaneBoundarySuccessorPoint =
            if (successorContactStart) successorRightLaneBoundary.calculateStartPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrElse { throw it }
            else successorLeftLaneBoundary.calculateEndPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrElse { throw it }

        val location = "from ${laneId.toIdentifierText()} to ${successorLaneId.toIdentifierText()}"

        val leftLaneBoundaryTransitionDistance = laneLeftLaneBoundaryPoint.distance(laneLeftLaneBoundarySuccessorPoint)
        if (leftLaneBoundaryTransitionDistance >= parameters.laneTransitionDistanceTolerance) {
            val infoValues = mapOf("euclidean_distance" to leftLaneBoundaryTransitionDistance)
            messageList += DefaultMessage("LeftLaneBoundaryTransitionGap", "Left boundary of lane should be connected to its successive lane (euclidean distance: $leftLaneBoundaryTransitionDistance, successor: $successorContactStart).", location, Severity.WARNING, wasFixed = false, infoValues)
        }

        val rightLaneBoundaryTransitionDistance = laneRightLaneBoundaryPoint.distance(laneRightLaneBoundarySuccessorPoint)
        if (rightLaneBoundaryTransitionDistance >= parameters.laneTransitionDistanceTolerance) {
            val infoValues = mapOf("euclidean_distance" to rightLaneBoundaryTransitionDistance)
            messageList += DefaultMessage("RightLaneBoundaryTransitionGap", "Right boundary of lane should be connected to its successive lane (euclidean distance: $rightLaneBoundaryTransitionDistance successor: $successorContactStart).", location, Severity.WARNING, wasFixed = false, infoValues)
        }

        return messageList
    }
}
