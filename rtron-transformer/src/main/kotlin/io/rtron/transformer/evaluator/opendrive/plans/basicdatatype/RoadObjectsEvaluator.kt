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
import arrow.core.some
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoadObject
import io.rtron.model.opendrive.additions.optics.everyRoadObjectOutlineElement
import io.rtron.model.opendrive.additions.optics.everyRoadObjectRepeatElement
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.messages.opendrive.of

class RoadObjectsEvaluator(val parameters: OpendriveEvaluatorParameters) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): DefaultMessageList {
        val messageList = DefaultMessageList()
        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = DefaultMessageList()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = everyRoadObject.modify(healedOpendriveModel) { currentRoadObject ->

            if (!currentRoadObject.s.isFinite() || currentRoadObject.s < 0.0) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 's'", currentRoadObject.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadObject.s = 0.0
            }
            if (!currentRoadObject.t.isFinite()) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 't'", currentRoadObject.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadObject.t = 0.0
            }
            if (!currentRoadObject.zOffset.isFinite()) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'zOffset'", currentRoadObject.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadObject.zOffset = 0.0
            }

            if (currentRoadObject.hdg.exists { !it.isFinite() }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'hdg'", currentRoadObject.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadObject.hdg = None
            }

            if (currentRoadObject.roll.exists { !it.isFinite() }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'roll'", currentRoadObject.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadObject.roll = None
            }

            if (currentRoadObject.pitch.exists { !it.isFinite() }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'pitch'", currentRoadObject.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadObject.roll = None
            }

            if (currentRoadObject.height.exists { !it.isFinite() || it < 0.0 }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'height'", currentRoadObject.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadObject.height = None
            }

            if (currentRoadObject.height.exists { 0.0 < it && it < parameters.numberTolerance }) {
                currentRoadObject.height = parameters.numberTolerance.some()
            }

            if (currentRoadObject.radius.exists { !it.isFinite() || it <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'radius'", currentRoadObject.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadObject.radius = None
            }

            if (currentRoadObject.length.exists { !it.isFinite() || it <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'length'", currentRoadObject.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadObject.length = None
            }

            if (currentRoadObject.width.exists { !it.isFinite() || it <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'width'", currentRoadObject.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadObject.width = None
            }

            if (currentRoadObject.outlines.exists { it.outline.isEmpty() }) {
                messageList += DefaultMessage("EmptyValueForOptionalAttribute", "Attribute 'outlines' is set with an empty value even though the attribute itself is optional.", "Header element", Severity.WARNING, wasHealed = true)
                currentRoadObject.outlines = None
            }

            val repeatElementsFiltered = currentRoadObject.repeat.filter { it.s.isFinite() && it.tStart.isFinite() && it.zOffsetStart.isFinite() }
            if (repeatElementsFiltered.size < currentRoadObject.repeat.size) {
                messageList += DefaultMessage.of("UnexpectedValues", "Ignoring ${currentRoadObject.repeat.size - repeatElementsFiltered.size} repeat entries which do not have a finite s, tStart, zOffsetStart value.", currentRoadObject.additionalId, Severity.FATAL_ERROR, wasHealed = true)
                // messageList += OpendriveException.UnexpectedValues("s, tStart, zOffsetStart", "Ignoring ${currentRoadObject.repeat.size - repeatElementsFiltered.size} repeat entries which do not have a finite s, tStart and zOffsetStart value.").toMessage(currentRoadObject.additionalId, isFatal = false, wasHealed = true)
                currentRoadObject.repeat = repeatElementsFiltered
            }

            currentRoadObject
        }

        healedOpendriveModel = everyRoadObjectOutlineElement.modify(healedOpendriveModel) { currentOutlineElement ->

            val cornerRoadElementsFiltered = currentOutlineElement.cornerRoad.filter { it.s.isFinite() && it.t.isFinite() && it.dz.isFinite() }
            if (cornerRoadElementsFiltered.size < currentOutlineElement.cornerRoad.size) {
                // messageList += OpendriveException.UnexpectedValues("s, t, dz", "Ignoring ${currentOutlineElement.cornerRoad.size - cornerRoadElementsFiltered.size} cornerRoad entries which do not have a finite s, t and dz value.").toMessage(currentOutlineElement.additionalId, isFatal = false, wasHealed = true)
                messageList += DefaultMessage.of("UnexpectedValues", "Ignoring ${currentOutlineElement.cornerRoad.size - cornerRoadElementsFiltered.size} cornerRoad entries which do not have a finite s, t and dz value.", currentOutlineElement.additionalId, Severity.FATAL_ERROR, wasHealed = true)

                currentOutlineElement.cornerRoad = cornerRoadElementsFiltered
            }

            currentOutlineElement.cornerRoad.forEach { currentCornerRoad ->
                if (!currentCornerRoad.height.isFinite() || currentCornerRoad.height < 0.0) {
                    messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'height'", currentOutlineElement.additionalId, Severity.WARNING, wasHealed = true)
                    currentCornerRoad.height = 0.0
                }

                if (0.0 < currentCornerRoad.height && currentCornerRoad.height <= parameters.numberTolerance) {
                    currentCornerRoad.height = 0.0
                }
            }

            val cornerLocalElementsFiltered = currentOutlineElement.cornerLocal.filter { it.u.isFinite() && it.v.isFinite() && it.z.isFinite() }
            if (cornerLocalElementsFiltered.size < currentOutlineElement.cornerLocal.size) {
                // messageList += OpendriveException.UnexpectedValues("s, t, dz", "Ignoring ${currentOutlineElement.cornerLocal.size - cornerLocalElementsFiltered.size} cornerLocal entries which do not have a finite u, v and z value.").toMessage(currentOutlineElement.additionalId, isFatal = false, wasHealed = true)
                messageList += DefaultMessage.of("UnexpectedValues", "Ignoring ${currentOutlineElement.cornerRoad.size - cornerRoadElementsFiltered.size} cornerRoad entries which do not have a finite s, t and dz value.", currentOutlineElement.additionalId, Severity.FATAL_ERROR, wasHealed = true)

                currentOutlineElement.cornerLocal = cornerLocalElementsFiltered
            }

            currentOutlineElement.cornerLocal.forEach { currentCornerLocal ->
                if (!currentCornerLocal.height.isFinite() || currentCornerLocal.height < 0.0) {
                    messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'height'", currentOutlineElement.additionalId, Severity.WARNING, wasHealed = true)
                    currentCornerLocal.height = 0.0
                }

                if (0.0 < currentCornerLocal.height && currentCornerLocal.height <= parameters.numberTolerance) {
                    currentCornerLocal.height = 0.0
                }
            }

            currentOutlineElement
        }

        healedOpendriveModel = everyRoadObjectRepeatElement.modify(healedOpendriveModel) { currentRepeatElement ->
            require(currentRepeatElement.s.isFinite()) { "Must already be filtered." }
            require(currentRepeatElement.tStart.isFinite()) { "Must already be filtered." }
            require(currentRepeatElement.zOffsetStart.isFinite()) { "Must already be filtered." }

            if (!currentRepeatElement.distance.isFinite() || currentRepeatElement.distance < 0.0) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'distance'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.distance = 0.0
            }
            if (!currentRepeatElement.heightEnd.isFinite() || currentRepeatElement.heightEnd < 0.0) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'heightEnd'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.heightEnd = 0.0
            }
            if (!currentRepeatElement.heightStart.isFinite() || currentRepeatElement.heightStart < 0.0) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'heightStart'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.heightStart = 0.0
            }
            if (!currentRepeatElement.length.isFinite() || currentRepeatElement.length < 0.0) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'length'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.length = 0.0
            }
            if (currentRepeatElement.lengthEnd.exists { !it.isFinite() || it <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'lengthEnd'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.lengthEnd = None
            }
            if (currentRepeatElement.lengthStart.exists { !it.isFinite() || it <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'lengthStart'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.lengthStart = None
            }
            if (currentRepeatElement.radiusEnd.exists { !it.isFinite() || it <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'radiusEnd'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.radiusEnd = None
            }
            if (currentRepeatElement.radiusStart.exists { !it.isFinite() || it <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'radiusStart'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.radiusStart = None
            }
            if (!currentRepeatElement.tEnd.isFinite()) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'tEnd'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.tEnd = currentRepeatElement.tStart
            }
            if (currentRepeatElement.widthEnd.exists { !it.isFinite() || it <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'widthEnd'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.widthEnd = None
            }
            if (currentRepeatElement.widthStart.exists { !it.isFinite() || it <= parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'widthStart'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.widthStart = None
            }
            if (!currentRepeatElement.zOffsetEnd.isFinite()) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'zOffsetEnd'", currentRepeatElement.additionalId, Severity.WARNING, wasHealed = true)
                currentRepeatElement.zOffsetEnd = currentRepeatElement.zOffsetStart
            }

            currentRepeatElement
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
