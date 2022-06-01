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

import arrow.core.getOrHandle
import arrow.core.handleError
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.io.report.merge
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.toIllegalStateException
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.ContactPoint
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.transformer.evaluator.roadspaces.configuration.RoadspacesEvaluatorConfiguration
import io.rtron.transformer.evaluator.roadspaces.plans.AbstractRoadspacesEvaluator
import io.rtron.transformer.report.of

class ModelingRulesEvaluator(val configuration: RoadspacesEvaluatorConfiguration) :
    AbstractRoadspacesEvaluator() {

    // Properties amd Initializers

    // Methods
    override fun evaluateNonFatalViolations(roadspacesModel: RoadspacesModel): Report {
        val report = Report()

        report += roadspacesModel.getAllRoadspaces().map { evaluateRoadLinkages(it, roadspacesModel) }.merge()

        return report
    }

    private fun evaluateRoadLinkages(roadspace: Roadspace, roadspacesModel: RoadspacesModel): Report {
        val report = Report()

        roadspace.road.getAllLeftRightLaneIdentifiers()
            .filter { roadspace.road.isInLastLaneSection(it) }
            .forEach { laneId ->
                val successorLaneIds = roadspacesModel.getSuccessorLaneIdentifiers(laneId).getOrHandle { throw it }

                report += successorLaneIds.map { successorLaneId ->
                    evaluateLaneTransition(laneId, successorLaneId, roadspace.road, roadspacesModel.getRoadspace(successorLaneId.toRoadspaceIdentifier()).getOrHandle { throw it }.road, roadspacesModel)
                }.merge()
            }

        return report
    }

    private fun evaluateLaneTransition(laneId: LaneIdentifier, successorLaneId: LaneIdentifier, road: Road, successorRoad: Road, roadspacesModel: RoadspacesModel): Report {
        require(laneId !== successorLaneId) { "Lane identifiers of current and of successor lane must be different." }
        require(laneId.roadspaceId !== successorLaneId.roadspaceId) { "Lane identifiers of current and of successor lane must be different regarding their roadspaceId." }
        require(road.id !== successorRoad.id) { "Road and successor road must be different." }

        val report = Report()

        val laneLeftLaneBoundaryPoint: Vector3D = road.getLeftLaneBoundary(laneId).getOrHandle { throw it }
            .calculateEndPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrHandle { throw it }
        val laneRightLaneBoundaryPoint: Vector3D = road.getRightLaneBoundary(laneId).getOrHandle { throw it }
            .calculateEndPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrHandle { throw it }

        // false, if the successor lane is connected by its end (leads to swapping of the vertices)
        val successorContactStart = !road.linkage.successorRoadspaceContactPointId
            .map { it.roadspaceContactPoint }
            .exists { it == ContactPoint.END }

        val successorLeftLaneBoundary = successorRoad.getLeftLaneBoundary(successorLaneId)
            .handleError { return report }.getOrHandle { throw it } // TODO: identify inconsistencies in the topology of the model
        val successorRightLaneBoundary = successorRoad.getRightLaneBoundary(successorLaneId)
            .handleError { return report }.getOrHandle { throw it } // TODO: identify inconsistencies in the topology of the model

        // if contact of successor at the start, normal connecting
        // if contact of the successor at the end, the end positions have to be calculated and left and right boundary have to be swapped
        val laneLeftLaneBoundarySuccessorPoint =
            if (successorContactStart) successorLeftLaneBoundary.calculateStartPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrHandle { throw it }
            else successorRightLaneBoundary.calculateEndPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrHandle { throw it }
        val laneRightLaneBoundarySuccessorPoint =
            if (successorContactStart) successorRightLaneBoundary.calculateStartPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrHandle { throw it }
            else successorLeftLaneBoundary.calculateEndPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrHandle { throw it }

        // reporting
        val location: Map<String, String> = laneId.toStringMap().map { "from_${it.key}" to it.value }.toMap() +
            successorLaneId.toStringMap().map { "to_${it.key}" to it.value }.toMap()

        if (laneLeftLaneBoundaryPoint.fuzzyUnequals(laneLeftLaneBoundarySuccessorPoint, configuration.distanceTolerance))
            report += Message.of("Left boundary of lane should be connected to its successive lane (euclidean distance: ${laneLeftLaneBoundaryPoint.distance(laneLeftLaneBoundarySuccessorPoint)}, successor: $successorContactStart).", location, isFatal = false, wasHealed = false)

        if (laneRightLaneBoundaryPoint.fuzzyUnequals(laneRightLaneBoundarySuccessorPoint, configuration.distanceTolerance))
            report += Message.of("Right boundary of lane should be connected to its successive lane (euclidean distance: ${laneRightLaneBoundaryPoint.distance(laneRightLaneBoundarySuccessorPoint)} successor: $successorContactStart).", location, isFatal = false, wasHealed = false)

        return report
    }
}
