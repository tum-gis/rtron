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
import io.rtron.model.opendrive.additions.optics.everyRoadSignal
import io.rtron.model.opendrive.core.EUnit
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.messages.opendrive.of

class RoadSignalsEvaluator(val parameters: OpendriveEvaluatorParameters) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): DefaultMessageList {
        val messageList = DefaultMessageList()
        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = DefaultMessageList()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = everyRoadSignal.modify(healedOpendriveModel) { currentRoadSignal ->

            if (currentRoadSignal.height.exists { !it.isFinite() || it < parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'height'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadSignal.height = None
            }
            if (currentRoadSignal.hOffset.exists { !it.isFinite() }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'hOffset'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadSignal.hOffset = None
            }
            if (currentRoadSignal.pitch.exists { !it.isFinite() }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'hOffset'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadSignal.pitch = None
            }
            if (currentRoadSignal.roll.exists { !it.isFinite() }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'roll'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadSignal.roll = None
            }
            if (!currentRoadSignal.s.isFinite() || currentRoadSignal.s < 0.0) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 's'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadSignal.s = 0.0
            }
            if (currentRoadSignal.subtype.isBlank()) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'subtype'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                // messageList += OpendriveException.UnexpectedValue("subtype", currentRoadSignal.subtype, "Value shall not be empty, but -1.").toMessage(currentRoadSignal.additionalId, isFatal = true, wasHealed = true)
                currentRoadSignal.subtype = "-1"
            }
            if (!currentRoadSignal.t.isFinite() || currentRoadSignal.t < 0.0) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 't'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadSignal.t = 0.0
            }
            if (currentRoadSignal.type.isBlank()) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'type'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadSignal.type = "-1"
            }
            if (currentRoadSignal.value.exists { !it.isFinite() }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'value'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadSignal.value = None
            }
            if (currentRoadSignal.value.isDefined() && currentRoadSignal.unit.isEmpty()) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'unit'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                // messageList += OpendriveException.UnexpectedValue("unit", currentRoadSignal.value.toString(), "Attribute 'unit' shall be defined, when attribute 'value' is defined.").toMessage(currentRoadSignal.additionalId, isFatal = true, wasHealed = true)
                currentRoadSignal.unit = EUnit.KILOMETER_PER_HOUR.some()
            }
            if (currentRoadSignal.width.exists { !it.isFinite() || it < parameters.numberTolerance }) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'width'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadSignal.width = None
            }
            if (!currentRoadSignal.zOffset.isFinite()) {
                messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'zOffset'", currentRoadSignal.additionalId, Severity.WARNING, wasHealed = true)
                currentRoadSignal.zOffset = 0.0
            }

            currentRoadSignal
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
