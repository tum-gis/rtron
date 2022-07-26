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
import io.rtron.model.opendrive.additions.optics.everyHeaderOffset
import io.rtron.model.opendrive.header
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters

class CoreEvaluator(val parameters: OpendriveEvaluatorParameters) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): DefaultMessageList {
        val messageList = DefaultMessageList()

        if (opendriveModel.road.isEmpty())
            messageList += DefaultMessage("NoRoadsContained", "Document does not contain any roads.", "", Severity.FATAL_ERROR, wasHealed = false)

        OpendriveModel.header.get(opendriveModel).also { header ->
            if (header.revMajor < 0)
                messageList += DefaultMessage("UnkownOpendriveMajorVersionNumber", "", "Header element", Severity.FATAL_ERROR, wasHealed = false)

            if (header.revMinor < 0)
                messageList += DefaultMessage("UnkownOpendriveMinorVersionNumber", "", "Header element", Severity.FATAL_ERROR, wasHealed = false)
        }

        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = DefaultMessageList()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = OpendriveModel.header.modify(healedOpendriveModel) { header ->
            if (header.name.exists { it.isEmpty() }) {
                messageList += DefaultMessage("EmptyValueForOptionalAttribute", "Attribute 'name' is set with an empty value even though the attribute itself is optional.", "Header element", Severity.WARNING, wasHealed = true)
                header.name = None
            }

            if (header.date.exists { it.isEmpty() }) {
                messageList += DefaultMessage("EmptyValueForOptionalAttribute", "Attribute 'date' is set with an empty value even though the attribute itself is optional.", "Header element", Severity.WARNING, wasHealed = true)
                header.date = None
            }

            if (header.vendor.exists { it.isEmpty() }) {
                messageList += DefaultMessage("EmptyValueForOptionalAttribute", "Attribute 'vendor' is set with an empty value even though the attribute itself is optional.", "Header element", Severity.WARNING, wasHealed = true)
                header.vendor = None
            }

            if (header.north.exists { !it.isFinite() }) {
                messageList += DefaultMessage("EmptyValueForOptionalAttribute", "Attribute 'north' is set with an empty value even though the attribute itself is optional.", "Header element", Severity.WARNING, wasHealed = true)
                header.north = None
            }

            header
        }

        healedOpendriveModel = everyHeaderOffset.modify(healedOpendriveModel) { currentHeaderOffset ->

            if (!currentHeaderOffset.x.isFinite()) {
                messageList += DefaultMessage("UnexpectedValue", "Unexpected value for attribute 'x'", "Header element", Severity.WARNING, wasHealed = true)
                currentHeaderOffset.x = 0.0
            }

            if (!currentHeaderOffset.y.isFinite()) {
                messageList += DefaultMessage("UnexpectedValue", "Unexpected value for attribute 'y'", "Header element", Severity.WARNING, wasHealed = true)
                currentHeaderOffset.y = 0.0
            }

            if (!currentHeaderOffset.z.isFinite()) {
                messageList += DefaultMessage("UnexpectedValue", "Unexpected value for attribute 'z'", "Header element", Severity.WARNING, wasHealed = true)
                currentHeaderOffset.z = 0.0
            }

            if (!currentHeaderOffset.hdg.isFinite()) {
                messageList += DefaultMessage("UnexpectedValue", "Unexpected value for attribute 'hdg'", "Header element", Severity.WARNING, wasHealed = true)
                currentHeaderOffset.hdg = 0.0
            }

            currentHeaderOffset
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
