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

import arrow.core.Option
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.identifier.LaneIdentifier
import io.rtron.model.opendrive.additions.optics.everyLaneSection
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionLeftLane
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionRightLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMark
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.messages.opendrive.of

object RoadLanesEvaluator {

    // Methods
    fun evaluate(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        everyRoad.modify(modifiedOpendriveModel) { currentRoad ->

            if (currentRoad.lanes.getLaneSectionLengths(currentRoad.length).any { it <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of("LaneSectionLengthBelowTolerance", "Lane sections has a length of zero or below the tolerance.", currentRoad.additionalId, Severity.FATAL_ERROR, wasFixed = false)
            }

            currentRoad
        }

        everyLaneSection.modify(modifiedOpendriveModel) { currentLaneSection ->
            if (currentLaneSection.center.getNumberOfLanes() != 1) {
                messageList += DefaultMessage.of("LaneSectionContainsNoCenterLane", "Lane section contains no center lane.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasFixed = false)
            }

            if (currentLaneSection.getNumberOfLeftRightLanes() == 0) {
                messageList += DefaultMessage.of("LaneSectionContainsNoLeftOrRightLane", "Lane section contains neither a left nor a right lane.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasFixed = false)
            }

            currentLaneSection.left.tap { currentLaneSectionLeft ->
                val leftLaneIds = currentLaneSectionLeft.lane.map { it.id }
                val expectedIds = (currentLaneSectionLeft.getNumberOfLanes() downTo 1).toList()

                if (leftLaneIds.distinct().size < leftLaneIds.size) {
                    messageList += DefaultMessage.of("LaneIdDuplicatesWithinLeftLaneSection", "Lane ids are not unique within the left lane section.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasFixed = false)
                }
                if (!leftLaneIds.containsAll(expectedIds)) {
                    messageList += DefaultMessage.of("NonConsecutiveLaneIdsWithinLeftLaneSection", "Lane numbering shall be consecutive without any gaps.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasFixed = false)
                }
            }

            currentLaneSection.right.tap { currentLaneSectionRight ->
                val rightLaneIds = currentLaneSectionRight.lane.map { it.id }
                val expectedIds = (-1 downTo -currentLaneSectionRight.getNumberOfLanes()).toList()

                if (rightLaneIds.distinct().size < rightLaneIds.size) {
                    messageList += DefaultMessage.of("LaneIdDuplicatesWithinRightLaneSection", "Lane ids are not unique within the right lane section.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasFixed = false)
                }
                if (!rightLaneIds.containsAll(expectedIds)) {
                    messageList += DefaultMessage.of("NonConsecutiveLaneIdsWithinRightLaneSection", "Lane numbering shall be consecutive without any gaps.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasFixed = false)
                }
            }

            currentLaneSection
        }

        modifiedOpendriveModel = everyRoadLanesLaneSectionCenterLane.modify(modifiedOpendriveModel) { currentCenterLane ->
            currentCenterLane.roadMark = filterToNonZeroLengthRoadMarks(currentCenterLane.roadMark, currentCenterLane.additionalId, parameters, messageList)
            currentCenterLane
        }
        modifiedOpendriveModel = everyRoadLanesLaneSectionLeftLane.modify(modifiedOpendriveModel) { currentLeftLane ->
            currentLeftLane.roadMark = filterToNonZeroLengthRoadMarks(currentLeftLane.roadMark, currentLeftLane.additionalId, parameters, messageList)
            currentLeftLane
        }
        modifiedOpendriveModel = everyRoadLanesLaneSectionRightLane.modify(modifiedOpendriveModel) { currentRightLane ->
            currentRightLane.roadMark = filterToNonZeroLengthRoadMarks(currentRightLane.roadMark, currentRightLane.additionalId, parameters, messageList)
            currentRightLane
        }

        return modifiedOpendriveModel
    }

    private fun filterToNonZeroLengthRoadMarks(roadMarks: List<RoadLanesLaneSectionLCRLaneRoadMark>, location: Option<LaneIdentifier>, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): List<RoadLanesLaneSectionLCRLaneRoadMark> {
        if (roadMarks.isEmpty()) return emptyList()

        val roadMarksFiltered: List<RoadLanesLaneSectionLCRLaneRoadMark> = roadMarks.zipWithNext().map { it.first to it.second.sOffset - it.first.sOffset }.filter { it.second >= parameters.numberTolerance }.map { it.first } + roadMarks.last()
        if (roadMarksFiltered.size < roadMarks.size) {
            messageList += DefaultMessage.of("RoadMarkWithLengthBelowThreshold", "Center lane contains roadMarks with length zero (or below threshold).", location, Severity.ERROR, wasFixed = true)
        }

        return roadMarksFiltered
    }
}
