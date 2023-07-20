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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype

import arrow.core.None
import arrow.core.some
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoadObject
import io.rtron.model.opendrive.additions.optics.everyRoadObjectOutlineElement
import io.rtron.model.opendrive.additions.optics.everyRoadObjectRepeatElement
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.modifiers.BasicDataTypeModifier
import io.rtron.transformer.messages.opendrive.of
import kotlin.math.max
import kotlin.math.min

object RoadObjectsEvaluator {

    // Methods
    fun evaluate(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel = everyRoadObject.modify(modifiedOpendriveModel) { currentRoadObject ->

            currentRoadObject.s = BasicDataTypeModifier.modifyToFinitePositiveDouble(currentRoadObject.s, currentRoadObject.additionalId, "s", messageList)
            currentRoadObject.t = BasicDataTypeModifier.modifyToFiniteDouble(currentRoadObject.t, currentRoadObject.additionalId, "t", messageList)
            currentRoadObject.zOffset = BasicDataTypeModifier.modifyToFiniteDouble(currentRoadObject.zOffset, currentRoadObject.additionalId, "zOffset", messageList)
            currentRoadObject.hdg = BasicDataTypeModifier.modifyToOptionalFiniteDouble(currentRoadObject.hdg, currentRoadObject.additionalId, "hdg", messageList)
            currentRoadObject.roll = BasicDataTypeModifier.modifyToOptionalFiniteDouble(currentRoadObject.roll, currentRoadObject.additionalId, "roll", messageList)
            currentRoadObject.pitch = BasicDataTypeModifier.modifyToOptionalFiniteDouble(currentRoadObject.pitch, currentRoadObject.additionalId, "pitch", messageList)
            currentRoadObject.height = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRoadObject.height, currentRoadObject.additionalId, "height", messageList)

            if (currentRoadObject.height.exists { 0.0 < it && it < parameters.numberTolerance }) {
                currentRoadObject.height = parameters.numberTolerance.some()
            }

            currentRoadObject.radius = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRoadObject.radius, currentRoadObject.additionalId, "radius", messageList, parameters.numberTolerance)
            currentRoadObject.length = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRoadObject.length, currentRoadObject.additionalId, "length", messageList, parameters.numberTolerance)
            currentRoadObject.width = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRoadObject.width, currentRoadObject.additionalId, "width", messageList, parameters.numberTolerance)

            currentRoadObject.validity.filter { it.fromLane > it.toLane }.forEach { currentValidity ->
                messageList += DefaultMessage.of("LaneValidityElementNotOrdered", "The value of the @fromLane attribute shall be lower than or equal to the value of the @toLane attribute.", currentRoadObject.additionalId, Severity.ERROR, wasFixed = true)
                currentValidity.fromLane = min(currentValidity.fromLane, currentValidity.toLane)
                currentValidity.toLane = max(currentValidity.fromLane, currentValidity.toLane)
            }

            if (currentRoadObject.outlines.exists { it.outline.isEmpty() }) {
                messageList += DefaultMessage("EmptyValueForOptionalAttribute", "Attribute 'outlines' is set with an empty value even though the attribute itself is optional.", "Header element", Severity.WARNING, wasFixed = true)
                currentRoadObject.outlines = None
            }

            val repeatElementsFiltered = currentRoadObject.repeat.filter { it.s.isFinite() && it.tStart.isFinite() && it.zOffsetStart.isFinite() }
            if (repeatElementsFiltered.size < currentRoadObject.repeat.size) {
                messageList += DefaultMessage.of("UnexpectedValues", "Ignoring ${currentRoadObject.repeat.size - repeatElementsFiltered.size} repeat entries which do not have a finite s, tStart, zOffsetStart value.", currentRoadObject.additionalId, Severity.FATAL_ERROR, wasFixed = true)
                // messageList += OpendriveException.UnexpectedValues("s, tStart, zOffsetStart", "Ignoring ${currentRoadObject.repeat.size - repeatElementsFiltered.size} repeat entries which do not have a finite s, tStart and zOffsetStart value.").toMessage(currentRoadObject.additionalId, isFatal = false, wasFixed: Boolean)
                currentRoadObject.repeat = repeatElementsFiltered
            }

            currentRoadObject
        }

        modifiedOpendriveModel = everyRoadObjectOutlineElement.modify(modifiedOpendriveModel) { currentOutlineElement ->

            val cornerRoadElementsFiltered = currentOutlineElement.cornerRoad.filter { it.s.isFinite() && it.t.isFinite() && it.dz.isFinite() }
            if (cornerRoadElementsFiltered.size < currentOutlineElement.cornerRoad.size) {
                // messageList += OpendriveException.UnexpectedValues("s, t, dz", "Ignoring ${currentOutlineElement.cornerRoad.size - cornerRoadElementsFiltered.size} cornerRoad entries which do not have a finite s, t and dz value.").toMessage(currentOutlineElement.additionalId, isFatal = false, wasFixed: Boolean)
                messageList += DefaultMessage.of("UnexpectedValues", "Ignoring ${currentOutlineElement.cornerRoad.size - cornerRoadElementsFiltered.size} cornerRoad entries which do not have a finite s, t and dz value.", currentOutlineElement.additionalId, Severity.FATAL_ERROR, wasFixed = true)

                currentOutlineElement.cornerRoad = cornerRoadElementsFiltered
            }

            currentOutlineElement.cornerRoad.forEach { currentCornerRoad ->
                if (!currentCornerRoad.height.isFinite() || currentCornerRoad.height < 0.0) {
                    messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'height'", currentOutlineElement.additionalId, Severity.WARNING, wasFixed = true)
                    currentCornerRoad.height = 0.0
                }

                if (0.0 < currentCornerRoad.height && currentCornerRoad.height <= parameters.numberTolerance) {
                    currentCornerRoad.height = 0.0
                }
            }

            val cornerLocalElementsFiltered = currentOutlineElement.cornerLocal.filter { it.u.isFinite() && it.v.isFinite() && it.z.isFinite() }
            if (cornerLocalElementsFiltered.size < currentOutlineElement.cornerLocal.size) {
                // messageList += OpendriveException.UnexpectedValues("s, t, dz", "Ignoring ${currentOutlineElement.cornerLocal.size - cornerLocalElementsFiltered.size} cornerLocal entries which do not have a finite u, v and z value.").toMessage(currentOutlineElement.additionalId, isFatal = false, wasFixed = true)
                messageList += DefaultMessage.of("UnexpectedValues", "Ignoring ${currentOutlineElement.cornerRoad.size - cornerRoadElementsFiltered.size} cornerRoad entries which do not have a finite s, t and dz value.", currentOutlineElement.additionalId, Severity.FATAL_ERROR, wasFixed = true)

                currentOutlineElement.cornerLocal = cornerLocalElementsFiltered
            }

            currentOutlineElement.cornerLocal.forEach { currentCornerLocal ->
                if (!currentCornerLocal.height.isFinite() || currentCornerLocal.height < 0.0) {
                    messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'height'", currentOutlineElement.additionalId, Severity.WARNING, wasFixed = true)
                    currentCornerLocal.height = 0.0
                }

                if (0.0 < currentCornerLocal.height && currentCornerLocal.height <= parameters.numberTolerance) {
                    currentCornerLocal.height = 0.0
                }
            }

            currentOutlineElement
        }

        modifiedOpendriveModel = everyRoadObjectRepeatElement.modify(modifiedOpendriveModel) { currentRepeatElement ->
            require(currentRepeatElement.s.isFinite()) { "Must already be filtered." }
            require(currentRepeatElement.tStart.isFinite()) { "Must already be filtered." }
            require(currentRepeatElement.zOffsetStart.isFinite()) { "Must already be filtered." }

            currentRepeatElement.distance = BasicDataTypeModifier.modifyToFinitePositiveDouble(currentRepeatElement.distance, currentRepeatElement.additionalId, "distance", messageList)
            currentRepeatElement.heightEnd = BasicDataTypeModifier.modifyToFinitePositiveDouble(currentRepeatElement.heightEnd, currentRepeatElement.additionalId, "heightEnd", messageList)
            currentRepeatElement.heightStart = BasicDataTypeModifier.modifyToFinitePositiveDouble(currentRepeatElement.heightStart, currentRepeatElement.additionalId, "heightStart", messageList)
            currentRepeatElement.length = BasicDataTypeModifier.modifyToFinitePositiveDouble(currentRepeatElement.length, currentRepeatElement.additionalId, "length", messageList)
            currentRepeatElement.lengthEnd = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRepeatElement.lengthEnd, currentRepeatElement.additionalId, "lengthEnd", messageList, parameters.numberTolerance)
            currentRepeatElement.lengthStart = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRepeatElement.lengthStart, currentRepeatElement.additionalId, "lengthStart", messageList, parameters.numberTolerance)
            currentRepeatElement.radiusEnd = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRepeatElement.radiusEnd, currentRepeatElement.additionalId, "radiusEnd", messageList, parameters.numberTolerance)
            currentRepeatElement.radiusStart = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRepeatElement.radiusStart, currentRepeatElement.additionalId, "radiusStart", messageList, parameters.numberTolerance)

            if (!currentRepeatElement.tEnd.isFinite()) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'tEnd'", currentRepeatElement.additionalId, Severity.WARNING, wasFixed = true)
                currentRepeatElement.tEnd = currentRepeatElement.tStart
            }
            currentRepeatElement.widthEnd = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRepeatElement.widthEnd, currentRepeatElement.additionalId, "widthEnd", messageList, parameters.numberTolerance)
            currentRepeatElement.widthStart = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRepeatElement.widthStart, currentRepeatElement.additionalId, "widthStart", messageList, parameters.numberTolerance)
            currentRepeatElement.zOffsetEnd = BasicDataTypeModifier.modifyToFiniteDouble(currentRepeatElement.zOffsetEnd, currentRepeatElement.additionalId, "zOffsetEnd", messageList)

            currentRepeatElement
        }

        return modifiedOpendriveModel
    }
}
