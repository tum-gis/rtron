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
import io.rtron.io.messages.MessageList
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.additions.optics.everyLaneSection
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionLeftLane
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionRightLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLane
import io.rtron.std.filterToStrictSortingBy
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.toMessage
import io.rtron.transformer.evaluator.opendrive.report.toReport

class RoadLanesEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): MessageList {
        val messageList = MessageList()

        everyRoadLanesLaneSectionLeftLane.modify(opendriveModel) { currentLeftLane ->
            messageList += getSevereViolations(currentLeftLane).toReport(currentLeftLane.additionalId, isFatal = true, wasHealed = false)
            currentLeftLane
        }

        everyRoadLanesLaneSectionRightLane.modify(opendriveModel) { currentRightLane ->
            messageList += getSevereViolations(currentRightLane).toReport(currentRightLane.additionalId, isFatal = true, wasHealed = false)
            currentRightLane
        }

        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = MessageList()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = everyLaneSection.modify(healedOpendriveModel) { currentLaneSection ->

            currentLaneSection.left.tap {
                if (it.lane.isEmpty()) {
                    messageList += OpendriveException.EmptyValueForOptionalAttribute("left").toMessage(currentLaneSection.additionalId, isFatal = false, wasHealed = true)
                    currentLaneSection.left = None
                }
            }

            currentLaneSection.right.tap {
                if (it.lane.isEmpty()) {
                    messageList += OpendriveException.EmptyValueForOptionalAttribute("left").toMessage(currentLaneSection.additionalId, isFatal = false, wasHealed = true)
                    currentLaneSection.right = None
                }
            }

            if (currentLaneSection.center.lane.isEmpty()) {
                messageList += OpendriveException.EmptyList("lane").toMessage(currentLaneSection.additionalId, isFatal = false, wasHealed = true)
                currentLaneSection.center.lane += RoadLanesLaneSectionCenterLane()
            }

            currentLaneSection
        }

        healedOpendriveModel = everyRoadLanesLaneSectionLeftLane.modify(healedOpendriveModel) { currentLeftLane ->
            messageList += healMinorViolations(currentLeftLane).toReport(currentLeftLane.additionalId, isFatal = true, wasHealed = false)
            currentLeftLane
        }

        healedOpendriveModel = everyRoadLanesLaneSectionRightLane.modify(healedOpendriveModel) { currentRightLane ->
            messageList += healMinorViolations(currentRightLane).toReport(currentRightLane.additionalId, isFatal = true, wasHealed = false)
            currentRightLane
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }

    private fun getSevereViolations(lane: RoadLanesLaneSectionLRLane): List<OpendriveException> {
        val severeViolations = mutableListOf<OpendriveException>()

        lane.getLaneWidthEntries().tap {
            if (it.head.sOffset > configuration.numberTolerance)
                severeViolations += OpendriveException.NonStrictlySortedList("width", "The width of the lane shall be defined for the full length of the lane section. This means that there must be a <width> element for s=0.")
        }

        return severeViolations
    }

    private fun healMinorViolations(lane: RoadLanesLaneSectionLRLane): List<OpendriveException> {
        val healedViolations = mutableListOf<OpendriveException>()

        val widthEntriesFiltered = lane.width.filterToStrictSortingBy { it.sOffset }
        if (widthEntriesFiltered.size < lane.width.size) {
            healedViolations += OpendriveException.NonStrictlySortedList("width", "Ignoring ${lane.width.size - widthEntriesFiltered.size} width entries which are not placed in strict order according to sOffset.")
            lane.width = widthEntriesFiltered
        }

        val widthEntriesFilteredBySOffsetFinite = lane.width.filter { currentWidth -> currentWidth.sOffset.isFinite() && currentWidth.sOffset >= 0.0 }
        if (widthEntriesFilteredBySOffsetFinite.size < lane.width.size) {
            healedViolations += OpendriveException.UnexpectedValues("sOffset", "Ignoring ${lane.width.size - widthEntriesFilteredBySOffsetFinite.size} width entries where sOffset is not-finite and positive.")
            lane.width = widthEntriesFilteredBySOffsetFinite
        }

        val widthEntriesFilteredByCoefficientsFinite = lane.width.filter { currentWidth -> currentWidth.coefficients.all { it.isFinite() } }
        if (widthEntriesFilteredByCoefficientsFinite.size < lane.width.size) {
            healedViolations += OpendriveException.UnexpectedValues("a, b, c, d", "Ignoring ${lane.width.size - widthEntriesFilteredByCoefficientsFinite.size} width entries where coefficients are not finite.")
            lane.width = widthEntriesFilteredByCoefficientsFinite
        }

        val heightEntriesFiltered = lane.height.filterToStrictSortingBy { it.sOffset }
        if (heightEntriesFiltered.size < lane.height.size) {
            healedViolations += OpendriveException.NonStrictlySortedList("height", "Ignoring ${lane.height.size - heightEntriesFiltered.size} height entries which are not placed in strict order according to sOffset.")
            lane.height = heightEntriesFiltered
        }

        val heightEntriesFilteredByCoefficientsFinite = lane.height.filter { it.inner.isFinite() && it.outer.isFinite() }
        if (heightEntriesFilteredByCoefficientsFinite.size < lane.height.size) {
            healedViolations += OpendriveException.UnexpectedValues("inner, outer", "Ignoring ${lane.height.size - heightEntriesFilteredByCoefficientsFinite.size} height entries where inner or outer is not finite.")
            lane.height = heightEntriesFilteredByCoefficientsFinite
        }

        return healedViolations
    }
}
