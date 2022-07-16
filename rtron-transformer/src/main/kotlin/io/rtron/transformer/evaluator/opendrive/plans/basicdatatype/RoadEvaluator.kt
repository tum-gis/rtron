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

import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.MessageList
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileShape
import io.rtron.std.filterToSortingBy
import io.rtron.std.filterToStrictSortingBy
import io.rtron.std.isSortedBy
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.toMessage

class RoadEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): MessageList {
        val messageList = MessageList()

        everyRoad.modify(opendriveModel) { currentRoad ->

            currentRoad.planView.geometryValidated.tapInvalid {
                messageList += it.toMessage(currentRoad.additionalId, isFatal = true, wasHealed = false)
            }

            currentRoad.lanes.laneSectionValidated.tapInvalid {
                messageList += it.toMessage(currentRoad.additionalId, isFatal = true, wasHealed = false)
            }

            currentRoad
        }

        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = MessageList()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = everyRoad.modify(healedOpendriveModel) { currentRoad ->

            currentRoad.elevationProfile.tap {
                val elevationEntriesFiltered = it.elevation.filterToStrictSortingBy { it.s }
                if (elevationEntriesFiltered.size < it.elevation.size) {
                    messageList += OpendriveException.NonStrictlySortedList("elevation", "Ignoring ${it.elevation.size - elevationEntriesFiltered.size} elevation entries which are not placed in strict order according to s.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                    it.elevation = elevationEntriesFiltered
                }
            }

            currentRoad.lateralProfile.tap {
                if (it.containsShapeProfile() && currentRoad.lanes.containsLaneOffset()) {
                    messageList += OpendriveException.UnexpectedValue("lateralProfile.shape", "", "Lane offsets shall not be used together with road shapes. Removing the shape entries.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                    it.shape = emptyList()
                }

                val superelevationEntriesFiltered = it.superelevation.filterToStrictSortingBy { it.s }
                if (superelevationEntriesFiltered.size < it.superelevation.size) {
                    messageList += OpendriveException.NonStrictlySortedList("superelevation", "Ignoring ${it.superelevation.size - superelevationEntriesFiltered.size} superelevation entries which are not placed in strictly ascending order according to s.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                    it.superelevation = superelevationEntriesFiltered
                }

                val shapeEntriesFilteredByS = it.shape.filterToSortingBy { it.s }
                if (shapeEntriesFilteredByS.size < it.shape.size) {
                    messageList += OpendriveException.NonSortedList("shape", "Ignoring ${it.shape.size - shapeEntriesFilteredByS.size} shape entries which are not placed in ascending order according to s.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                    it.shape = shapeEntriesFilteredByS
                }

                val shapeEntriesFilteredByT: List<RoadLateralProfileShape> = it.shape.groupBy { it.s }.flatMap { currentShapeSubEntries ->
                    currentShapeSubEntries.value.filterToStrictSortingBy { it.t }
                }
                if (shapeEntriesFilteredByT.size < it.shape.size) {
                    messageList += OpendriveException.NonStrictlySortedList("shape", "Ignoring ${it.shape.size - shapeEntriesFilteredByT.size} shape entries which are not placed in ascending order according to t for each s group.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                    it.shape = shapeEntriesFilteredByT
                }
            }

            val laneOffsetEntriesFiltered = currentRoad.lanes.laneOffset.filterToStrictSortingBy { it.s }
            if (laneOffsetEntriesFiltered.size < currentRoad.lanes.laneOffset.size) {
                messageList += OpendriveException.NonStrictlySortedList("shape", "Ignoring ${currentRoad.lanes.laneOffset.size - laneOffsetEntriesFiltered.size} lane offset entries which are not placed in strictly ascending order according to s.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                currentRoad.lanes.laneOffset = laneOffsetEntriesFiltered
            }

            if (!currentRoad.lanes.laneSection.isSortedBy { it.s }) {
                messageList += OpendriveException.NonSortedList("laneSection").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                currentRoad.lanes.laneSection = currentRoad.lanes.laneSection.sortedBy { it.s }
            }

            currentRoad
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
