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
import io.rtron.model.opendrive.additions.optics.everyJunction
import io.rtron.model.opendrive.additions.optics.everyJunctionConnection
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.toMessage

class JunctionEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): MessageList {
        val messageList = MessageList()

        everyJunction.modify(opendriveModel) { currentJunction ->

            currentJunction.connectionValidated.tapInvalid {
                messageList += it.toMessage(currentJunction.additionalId, isFatal = true, wasHealed = false)
            }

            currentJunction.idValidated.tapInvalid {
                messageList += it.toMessage(currentJunction.additionalId, isFatal = true, wasHealed = false)
            }

            currentJunction
        }

        everyJunctionConnection.modify(opendriveModel) { currentJunctionConnection ->

            currentJunctionConnection.idValidated.tapInvalid {
                messageList += it.toMessage(currentJunctionConnection.additionalId, isFatal = true, wasHealed = false)
            }

            currentJunctionConnection
        }

        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = MessageList()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = everyJunction.modify(healedOpendriveModel) { currentJunction ->
            if (currentJunction.mainRoad.exists { it.isBlank() }) {
                messageList += OpendriveException.EmptyValueForOptionalAttribute("mainRoad").toMessage(currentJunction.additionalId, isFatal = false, wasHealed = true)
                currentJunction.mainRoad = None
            }

            if (currentJunction.name.exists { it.isBlank() }) {
                messageList += OpendriveException.EmptyValueForOptionalAttribute("name").toMessage(currentJunction.additionalId, isFatal = false, wasHealed = true)
                currentJunction.name = None
            }

            if (currentJunction.sEnd.exists { !it.isFinite() }) {
                messageList += OpendriveException.EmptyValueForOptionalAttribute("sEnd").toMessage(currentJunction.additionalId, isFatal = false, wasHealed = true)
                currentJunction.sEnd = None
            }

            if (currentJunction.sStart.exists { !it.isFinite() }) {
                messageList += OpendriveException.EmptyValueForOptionalAttribute("sStart").toMessage(currentJunction.additionalId, isFatal = false, wasHealed = true)
                currentJunction.sStart = None
            }

            currentJunction
        }

        healedOpendriveModel = everyJunctionConnection.modify(healedOpendriveModel) { currentJunctionConnection ->

            if (currentJunctionConnection.connectingRoad.exists { it.isBlank() }) {
                messageList += OpendriveException.EmptyValueForOptionalAttribute("connectingRoad").toMessage(currentJunctionConnection.additionalId, isFatal = false, wasHealed = true)
                currentJunctionConnection.connectingRoad = None
            }

            if (currentJunctionConnection.incomingRoad.exists { it.isBlank() }) {
                messageList += OpendriveException.EmptyValueForOptionalAttribute("incomingRoad").toMessage(currentJunctionConnection.additionalId, isFatal = false, wasHealed = true)
                currentJunctionConnection.incomingRoad = None
            }

            if (currentJunctionConnection.linkedRoad.exists { it.isBlank() }) {
                messageList += OpendriveException.EmptyValueForOptionalAttribute("linkedRoad").toMessage(currentJunctionConnection.additionalId, isFatal = false, wasHealed = true)
                currentJunctionConnection.linkedRoad = None
            }

            currentJunctionConnection
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
