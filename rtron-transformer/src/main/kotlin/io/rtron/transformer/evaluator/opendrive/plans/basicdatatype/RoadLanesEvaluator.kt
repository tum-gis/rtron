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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype

import arrow.core.None
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyLaneSection
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionLeftLane
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionRightLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLane
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.manipulators.BasicDataTypeManipulator
import io.rtron.transformer.messages.opendrive.of

class RoadLanesEvaluator(val parameters: OpendriveEvaluatorParameters) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): DefaultMessageList {
        val messageList = DefaultMessageList()

        everyRoadLanesLaneSectionLeftLane.modify(opendriveModel) { currentLeftLane ->
            messageList += getSevereViolations(currentLeftLane)
            currentLeftLane
        }

        everyRoadLanesLaneSectionRightLane.modify(opendriveModel) { currentRightLane ->
            messageList += getSevereViolations(currentRightLane)
            currentRightLane
        }

        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = DefaultMessageList()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = everyLaneSection.modify(healedOpendriveModel) { currentLaneSection ->

            currentLaneSection.left.tap {
                if (it.lane.isEmpty()) {
                    messageList += DefaultMessage.of("EmptyValueForOptionalAttribute", "Attribute 'left' is set with an empty value even though the attribute itself is optional.", currentLaneSection.additionalId, Severity.WARNING, wasHealed = true)
                    currentLaneSection.left = None
                }
            }

            currentLaneSection.right.tap {
                if (it.lane.isEmpty()) {
                    messageList += DefaultMessage.of("EmptyValueForOptionalAttribute", "Attribute 'right' is set with an empty value even though the attribute itself is optional.", currentLaneSection.additionalId, Severity.WARNING, wasHealed = true)
                    currentLaneSection.right = None
                }
            }

            if (currentLaneSection.center.lane.isEmpty()) {
                messageList += DefaultMessage.of("NoLanesInLaneSection", "Lane section does not contain lanes.", currentLaneSection.additionalId, Severity.FATAL_ERROR, wasHealed = false)
                currentLaneSection.center.lane += RoadLanesLaneSectionCenterLane()
            }

            currentLaneSection
        }

        healedOpendriveModel = everyRoadLanesLaneSectionLeftLane.modify(healedOpendriveModel) { currentLeftLane ->
            messageList += healMinorViolations(currentLeftLane)
            currentLeftLane
        }

        healedOpendriveModel = everyRoadLanesLaneSectionRightLane.modify(healedOpendriveModel) { currentRightLane ->
            messageList += healMinorViolations(currentRightLane)
            currentRightLane
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }

    private fun getSevereViolations(lane: RoadLanesLaneSectionLRLane): DefaultMessageList {
        val messageList = DefaultMessageList()

        lane.getLaneWidthEntries().tap {
            if (it.head.sOffset > parameters.numberTolerance)
                messageList += DefaultMessage.of("NonStrictlySortedList", "The width of the lane shall be defined for the full length of the lane section. This means that there must be a <width> element for s=0.", lane.additionalId, Severity.FATAL_ERROR, wasHealed = false)
        }

        return messageList
    }

    private fun healMinorViolations(lane: RoadLanesLaneSectionLRLane): DefaultMessageList {
        val messageList = DefaultMessageList()

        lane.width = BasicDataTypeManipulator.filterToStrictlySorted(lane.width, { it.sOffset }, lane.additionalId, "width", messageList)

        /*val widthEntriesFiltered = lane.width.filterToStrictSortingBy { it.sOffset }
        if (widthEntriesFiltered.size < lane.width.size) {
            healedViolations += OpendriveException.NonStrictlySortedList("width", "Ignoring ${lane.width.size - widthEntriesFiltered.size} width entries which are not placed in strict order according to sOffset.")
            lane.width = widthEntriesFiltered
        }*/

        val widthEntriesFilteredBySOffsetFinite = lane.width.filter { currentWidth -> currentWidth.sOffset.isFinite() && currentWidth.sOffset >= 0.0 }
        if (widthEntriesFilteredBySOffsetFinite.size < lane.width.size) {
            // messageList += OpendriveException.UnexpectedValues("sOffset", "Ignoring ${lane.width.size - widthEntriesFilteredBySOffsetFinite.size} width entries where sOffset is not-finite and positive.").toMessage(lane.additionalId, isFatal = true, wasHealed = true)
            messageList += DefaultMessage.of("UnexpectedValues", "Ignoring ${lane.width.size - widthEntriesFilteredBySOffsetFinite.size} width entries where sOffset is not-finite and positive.", lane.additionalId, Severity.FATAL_ERROR, wasHealed = true)
            lane.width = widthEntriesFilteredBySOffsetFinite
        }

        val widthEntriesFilteredByCoefficientsFinite = lane.width.filter { currentWidth -> currentWidth.coefficients.all { it.isFinite() } }
        if (widthEntriesFilteredByCoefficientsFinite.size < lane.width.size) {
            messageList += DefaultMessage.of("UnexpectedValues", "Ignoring ${lane.width.size - widthEntriesFilteredByCoefficientsFinite.size} width entries where coefficients \"a, b, c, d\", are not finite.", lane.additionalId, Severity.FATAL_ERROR, wasHealed = true)
            // messageList += OpendriveException.UnexpectedValues("a, b, c, d", "Ignoring ${lane.width.size - widthEntriesFilteredByCoefficientsFinite.size} width entries where coefficients are not finite.").toMessage(lane.additionalId, isFatal = true, wasHealed = true)
            lane.width = widthEntriesFilteredByCoefficientsFinite
        }

        lane.height = BasicDataTypeManipulator.filterToStrictlySorted(lane.height, { it.sOffset }, lane.additionalId, "height", messageList)
        /*val heightEntriesFiltered = lane.height.filterToStrictSortingBy { it.sOffset }
        if (heightEntriesFiltered.size < lane.height.size) {
            messageList += OpendriveException.NonStrictlySortedList("height", "Ignoring ${lane.height.size - heightEntriesFiltered.size} height entries which are not placed in strict order according to sOffset.").toMessage(lane.additionalId, isFatal = true, wasHealed = true)
            lane.height = heightEntriesFiltered
        }*/

        val heightEntriesFilteredByCoefficientsFinite = lane.height.filter { it.inner.isFinite() && it.outer.isFinite() }
        if (heightEntriesFilteredByCoefficientsFinite.size < lane.height.size) {
            messageList += DefaultMessage.of("UnexpectedValues", "Ignoring ${lane.height.size - heightEntriesFilteredByCoefficientsFinite.size} height entries where inner or outer is not finite.", lane.additionalId, Severity.FATAL_ERROR, wasHealed = true)
            // messageList += OpendriveException.UnexpectedValues("inner, outer", "Ignoring ${lane.height.size - heightEntriesFilteredByCoefficientsFinite.size} height entries where inner or outer is not finite.").toMessage(lane.additionalId, isFatal = true, wasHealed = true)
            lane.height = heightEntriesFilteredByCoefficientsFinite
        }

        return messageList
    }
}
