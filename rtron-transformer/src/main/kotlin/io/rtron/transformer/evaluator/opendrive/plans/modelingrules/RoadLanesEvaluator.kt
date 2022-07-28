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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules

import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyLaneSection
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.std.isSortedDescending
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.messages.opendrive.of

class RoadLanesEvaluator(val parameters: OpendriveEvaluatorParameters) {

    // Methods

    fun evaluateFatalViolations(opendriveModel: OpendriveModel): DefaultMessageList {
        val messageList = DefaultMessageList()

        everyRoad.modify(opendriveModel) { currentRoad ->

            if (currentRoad.lanes.getLaneSectionLengths(currentRoad.length).any { it <= parameters.numberTolerance })
                messageList += DefaultMessage.of("", "The length of lane sections shall be greater than zero.", currentRoad.additionalId, Severity.FATAL_ERROR, wasHealed = false)

            currentRoad
        }

        everyLaneSection.modify(opendriveModel) { currentLaneSection ->
            if (currentLaneSection.center.getNumberOfLanes() != 1)
                messageList += DefaultMessage.of("", "Lane section should contain exactly one center lane.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasHealed = false)

            if (currentLaneSection.getNumberOfLeftRightLanes() == 0)
                messageList += DefaultMessage.of("", "Each lane section shall contain at least one <right> or <left> element.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasHealed = false)

            currentLaneSection.left.tap { currentLaneSectionLeft ->
                val leftLaneIds = currentLaneSectionLeft.lane.map { it.id }
                val expectedIds = (currentLaneSectionLeft.getNumberOfLanes() downTo 1).toList()

                if (leftLaneIds.distinct().size < leftLaneIds.size)
                    messageList += DefaultMessage.of("", "Lane numbering shall be unique per lane section.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasHealed = false)
                if (!leftLaneIds.containsAll(expectedIds))
                    messageList += DefaultMessage.of("", "Lane numbering shall be consecutive without any gaps.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasHealed = false)
            }

            currentLaneSection.right.tap { currentLaneSectionRight ->
                val rightLaneIds = currentLaneSectionRight.lane.map { it.id }
                val expectedIds = (-1 downTo -currentLaneSectionRight.getNumberOfLanes()).toList()

                if (rightLaneIds.distinct().size < rightLaneIds.size)
                    messageList += DefaultMessage.of("", "Lane numbering shall be unique per lane section.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasHealed = false)
                if (!rightLaneIds.containsAll(expectedIds))
                    messageList += DefaultMessage.of("", "Lane numbering shall be consecutive without any gaps.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasHealed = false)
            }

            currentLaneSection
        }

        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = DefaultMessageList()
        var healedOpendriveModel = opendriveModel.copy()

        healedOpendriveModel = everyLaneSection.modify(healedOpendriveModel) { currentLaneSection ->

            currentLaneSection.left.tap { currentLaneSectionLeft ->
                if (!currentLaneSectionLeft.lane.map { it.id }.isSortedDescending()) {
                    currentLaneSectionLeft.lane = currentLaneSectionLeft.lane.sortedByDescending { it.id }
                    messageList += DefaultMessage.of("", "Lane numbering shall start with 1 next to the center lane, descend in negative t-direction and ascend in positive t-direction.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasHealed = true)
                }
            }

            currentLaneSection.right.tap { currentLaneSectionRight ->
                if (!currentLaneSectionRight.lane.map { it.id }.isSortedDescending()) {
                    currentLaneSectionRight.lane = currentLaneSectionRight.lane.sortedByDescending { it.id }
                    messageList += DefaultMessage.of("", "Lane numbering shall start with 1 next to the center lane, descend in negative t-direction and ascend in positive t-direction.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasHealed = true)
                }
            }

            currentLaneSection
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
