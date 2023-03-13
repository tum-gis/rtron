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
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.std.fuzzyEquals
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Curve2DBuilder
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.messages.opendrive.of

object RoadEvaluator {

    // Methods
    fun evaluate(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        everyRoad.modify(modifiedOpendriveModel) { currentRoad ->
            if (currentRoad.planView.geometry.any { it.s > currentRoad.length + parameters.numberTolerance }) {
                messageList += DefaultMessage.of("", "Road contains geometry elements in the plan view, where s exceeds the total length of the road (${currentRoad.length}).", currentRoad.additionalId, Severity.WARNING, wasFixed = false)
            }

            currentRoad
        }

        modifiedOpendriveModel = everyRoad.modify(modifiedOpendriveModel) { currentRoad ->
            // TODO: consolidate location handling

            if (currentRoad.planView.geometry.any { it.length <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of(
                    "PlanViewGeometryElementZeroLength",
                    "Plan view contains geometry elements with a length of zero (below tolerance threshold), which are removed.",
                    currentRoad.additionalId,
                    Severity.WARNING,
                    wasFixed = true
                )
                currentRoad.planView.geometry =
                    currentRoad.planView.geometry.filter { it.length > parameters.numberTolerance }
            }

            currentRoad
        }

        modifiedOpendriveModel.road = modifiedOpendriveModel.road.filter { currentRoad ->
            if (currentRoad.planView.geometry.isEmpty()) {
                messageList += DefaultMessage.of(
                    "RoadWithoutValidPlanViewGeometryElement",
                    "Road does not contain any valid geometry element in the planView.",
                    currentRoad.additionalId,
                    Severity.FATAL_ERROR,
                    wasFixed = false
                )
            }
            currentRoad.planView.geometry.isNotEmpty()
        }

        modifiedOpendriveModel = everyRoad.modify(modifiedOpendriveModel) { currentRoad ->
            val location = currentRoad.additionalId.fold({ "" }, { it.toIdentifierText() })

            /*val planViewGeometryLengthsSum = road.planView.geometry.sumOf { it.length }
            if (!fuzzyEquals(planViewGeometryLengthsSum, road.length, parameters.numberTolerance)) {
                healedViolations += OpendriveException.UnexpectedValue("length", road.length.toString(), ", as the sum of the individual plan view elements is different")
                road.length = planViewGeometryLengthsSum
            }*/

            currentRoad.planView.geometry.zipWithNext().forEach {
                val actualLength = it.second.s - it.first.s
                if (!fuzzyEquals(it.first.length, actualLength, parameters.numberTolerance)) {
                    messageList += DefaultMessage.of("", "Length attribute (length=${it.first.length}) of the geometry element (s=${it.first.s}) does not match the start position (s=${it.second.s}) of the next geometry element.", currentRoad.additionalId, Severity.WARNING, wasFixed = true)
                    it.first.length = actualLength
                }
            }

            if (!fuzzyEquals(currentRoad.planView.geometry.last().s + currentRoad.planView.geometry.last().length, currentRoad.length, parameters.numberTolerance)) {
                messageList += DefaultMessage.of("", "Length attribute (length=${currentRoad.planView.geometry.last().length}) of the last geometry element (s=${currentRoad.planView.geometry.last().s}) does not match the total road length (length=${currentRoad.length}).", currentRoad.additionalId, Severity.WARNING, wasFixed = true)
                currentRoad.planView.geometry.last().length = currentRoad.length - currentRoad.planView.geometry.last().s
            }

            // check gaps and kinks of reference line curve

            val (curveMembers, _, _) = Curve2DBuilder.prepareCurveMembers(
                currentRoad.planView.geometryAsNonEmptyList,
                parameters.numberTolerance
            )
            curveMembers.zipWithNext().forEach {
                val frontCurveMemberEndPose = it.first.calculatePoseGlobalCSUnbounded(CurveRelativeVector1D(it.first.length))
                val backCurveMemberStartPose = it.second.calculatePoseGlobalCSUnbounded(CurveRelativeVector1D.ZERO)

                val distance = frontCurveMemberEndPose.point.distance(backCurveMemberStartPose.point)
                if (distance > parameters.planViewGeometryDistanceTolerance) {
                    messageList += DefaultMessage(
                        "GapBetweenPlanViewGeometryElements",
                        "Geometry elements contain a gap " +
                            "from ${frontCurveMemberEndPose.point} to ${backCurveMemberStartPose.point} with an euclidean distance " +
                            "of $distance above the tolerance of ${parameters.planViewGeometryDistanceTolerance}.",
                        location,
                        Severity.FATAL_ERROR,
                        wasFixed = false
                    )
                } else if (distance > parameters.planViewGeometryDistanceWarningTolerance) {
                    messageList += DefaultMessage(
                        "GapBetweenPlanViewGeometryElements",
                        "Geometry elements contain a gap " +
                            "from ${frontCurveMemberEndPose.point} to ${backCurveMemberStartPose.point} with an euclidean distance " +
                            "of $distance above the warning tolerance of ${parameters.planViewGeometryDistanceWarningTolerance}.",
                        location,
                        Severity.WARNING,
                        wasFixed = false
                    )
                }

                val angleDifference = frontCurveMemberEndPose.rotation.difference(backCurveMemberStartPose.rotation)
                if (angleDifference > parameters.planViewGeometryAngleTolerance) {
                    messageList += DefaultMessage(
                        "KinkBetweenPlanViewGeometryElements",
                        "Geometry elements contain a kink " +
                            "from ${frontCurveMemberEndPose.point} to ${backCurveMemberStartPose.point} with an angle difference " +
                            "of $angleDifference above the tolerance of ${parameters.planViewGeometryAngleTolerance}.",
                        location,
                        Severity.FATAL_ERROR,
                        wasFixed = false
                    )
                } else if (angleDifference > parameters.planViewGeometryAngleWarningTolerance) {
                    messageList += DefaultMessage(
                        "KinkBetweenPlanViewGeometryElements",
                        "Geometry elements contain a gap " +
                            "from ${frontCurveMemberEndPose.point} to ${backCurveMemberStartPose.point} with an angle difference " +
                            "of $angleDifference above the warning tolerance of ${parameters.planViewGeometryAngleWarningTolerance}.",
                        location,
                        Severity.WARNING,
                        wasFixed = false
                    )
                }
            }

            currentRoad
        }

        val junctionIdentifiers = modifiedOpendriveModel.junction.map { it.id }
        modifiedOpendriveModel = everyRoad.modify(modifiedOpendriveModel) { currentRoad ->
            currentRoad.getJunctionOption().tap { currentJunctionId ->
                if (currentJunctionId !in junctionIdentifiers) {
                    messageList += DefaultMessage(
                        "RoadBelongsToNonExistingJunction",
                        "Road belongs to a junction (id=${currentRoad.junction}) that does not exist.",
                        currentRoad.id,
                        Severity.ERROR,
                        wasFixed = true
                    )
                    currentRoad.junction = ""
                }
            }

            currentRoad.link.tap { currentLink ->
                if (currentLink.predecessor.exists { currentPredecessor -> currentPredecessor.getJunctionPredecessorSuccessor().exists { !junctionIdentifiers.contains(it) } }) {
                    messageList += DefaultMessage(
                        "RoadLinkPredecessorRefersToNonExistingJunction",
                        "Road link predecessor references a junction (id=${currentLink.predecessor.fold({ "" }, { it.elementId })}) that does not exist.",
                        currentRoad.id,
                        Severity.ERROR,
                        wasFixed = true
                    )
                    currentLink.predecessor = None
                }
                if (currentLink.successor.exists { currentSuccessor -> currentSuccessor.getJunctionPredecessorSuccessor().exists { !junctionIdentifiers.contains(it) } }) {
                    messageList += DefaultMessage(
                        "RoadLinkSuccessorRefersToNonExistingJunction",
                        "Road link successor references a junction (id=${currentLink.successor.fold({ "" }, { it.elementId })}) that does not exist.",
                        currentRoad.id,
                        Severity.ERROR,
                        wasFixed = true
                    )
                    currentLink.successor = None
                }
            }

            currentRoad
        }

        return modifiedOpendriveModel
    }
}
