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
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileShape
import io.rtron.std.filterToStrictSortingBy
import io.rtron.std.isSortedBy
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.manipulators.BasicDataTypeManipulator
import io.rtron.transformer.messages.opendrive.of

class RoadEvaluator(val parameters: OpendriveEvaluatorParameters) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): DefaultMessageList {
        val messageList = DefaultMessageList()

        everyRoad.modify(opendriveModel) { currentRoad ->

            if (currentRoad.planView.geometry.isEmpty())
                messageList += DefaultMessage.of("NoPlanViewGeometryElements", "Plan view of road does not contain any geometry elements.", currentRoad.additionalId, Severity.FATAL_ERROR, wasHealed = false)

            if (currentRoad.lanes.laneSection.isEmpty())
                messageList += DefaultMessage.of("NoLaneSections", "Road does not contain any lane sections.", currentRoad.additionalId, Severity.FATAL_ERROR, wasHealed = false)

            currentRoad
        }

        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = DefaultMessageList()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = everyRoad.modify(healedOpendriveModel) { currentRoad ->

            if (currentRoad.elevationProfile.exists { it.elevation.isEmpty() }) {
                messageList += DefaultMessage.of("NoElevationProfileElements", "Elevation profile contains no elements.", currentRoad.additionalId, Severity.WARNING, wasHealed = true)
                currentRoad.elevationProfile = None
            }

            currentRoad.elevationProfile.tap { elevationProfile ->

                elevationProfile.elevation = BasicDataTypeManipulator.filterToStrictlySorted(elevationProfile.elevation, { it.s }, currentRoad.additionalId, "elevation", messageList)

                /*val elevationEntriesFiltered = it.elevation.filterToStrictSortingBy { it.s }
                if (elevationEntriesFiltered.size < it.elevation.size) {
                    messageList += OpendriveException.NonStrictlySortedList("elevation", "Ignoring ${it.elevation.size - elevationEntriesFiltered.size} elevation entries which are not placed in strict order according to s.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                    it.elevation = elevationEntriesFiltered
                }*/
            }

            currentRoad.lateralProfile.tap {
                if (it.containsShapeProfile() && currentRoad.lanes.containsLaneOffset()) {
                    messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'lateralProfile.shape'", currentRoad.additionalId, Severity.WARNING, wasHealed = true)
                    it.shape = emptyList()
                }

                it.superelevation = BasicDataTypeManipulator.filterToStrictlySorted(it.superelevation, { it.s }, currentRoad.additionalId, "superelevation", messageList)
                /*val superelevationEntriesFiltered = it.superelevation.filterToStrictSortingBy { it.s }
                if (superelevationEntriesFiltered.size < it.superelevation.size) {
                    messageList += OpendriveException.NonStrictlySortedList("superelevation", "Ignoring ${it.superelevation.size - superelevationEntriesFiltered.size} superelevation entries which are not placed in strictly ascending order according to s.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                    it.superelevation = superelevationEntriesFiltered
                }*/

                it.shape = BasicDataTypeManipulator.filterToSorted(it.shape, { it.s }, currentRoad.additionalId, "shape", messageList)
                /*val shapeEntriesFilteredByS = it.shape.filterToSortingBy { it.s }
                if (shapeEntriesFilteredByS.size < it.shape.size) {
                    messageList += OpendriveException.NonSortedList("shape", "Ignoring ${it.shape.size - shapeEntriesFilteredByS.size} shape entries which are not placed in ascending order according to s.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                    it.shape = shapeEntriesFilteredByS
                }*/

                val shapeEntriesFilteredByT: List<RoadLateralProfileShape> = it.shape.groupBy { it.s }.flatMap { currentShapeSubEntries ->
                    currentShapeSubEntries.value.filterToStrictSortingBy { it.t }
                }
                if (shapeEntriesFilteredByT.size < it.shape.size) {
                    // OpendriveException.NonStrictlySortedList("shape", "Ignoring ${it.shape.size - shapeEntriesFilteredByT.size} shape entries which are not placed in ascending order according to t for each s group.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                    messageList += DefaultMessage.of("NonStrictlySortedList", "Ignoring ${it.shape.size - shapeEntriesFilteredByT.size} shape entries which are not placed in ascending order according to t for each s group.", currentRoad.additionalId, Severity.WARNING, wasHealed = true)
                    it.shape = shapeEntriesFilteredByT
                }
            }

            currentRoad.lanes.laneOffset = BasicDataTypeManipulator.filterToStrictlySorted(currentRoad.lanes.laneOffset, { it.s }, currentRoad.additionalId, "shape", messageList)

            /*val laneOffsetEntriesFiltered = currentRoad.lanes.laneOffset.filterToStrictSortingBy { it.s }
            if (laneOffsetEntriesFiltered.size < currentRoad.lanes.laneOffset.size) {
                messageList += OpendriveException.NonStrictlySortedList("shape", "Ignoring ${currentRoad.lanes.laneOffset.size - laneOffsetEntriesFiltered.size} lane offset entries which are not placed in strictly ascending order according to s.").toMessage(currentRoad.additionalId, isFatal = false, wasHealed = true)
                currentRoad.lanes.laneOffset = laneOffsetEntriesFiltered
            }*/

            // currentRoad.lanes.laneSection = BasicDataTypeManipulator.filterToSorted(currentRoad.lanes.laneSection, { it.s }, currentRoad.additionalId, "laneSection", messageList)

            if (!currentRoad.lanes.laneSection.isSortedBy { it.s }) {
                messageList += DefaultMessage.of("NonSortedList", "Sorting lane sections according to s.", currentRoad.additionalId, Severity.WARNING, wasHealed = true)
                currentRoad.lanes.laneSection = currentRoad.lanes.laneSection.sortedBy { it.s }
            }

            currentRoad
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
