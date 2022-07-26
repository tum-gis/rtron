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
import io.rtron.model.opendrive.additions.optics.everyJunction
import io.rtron.model.opendrive.additions.optics.everyJunctionConnection
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.messages.opendrive.of

class JunctionEvaluator(val parameters: OpendriveEvaluatorParameters) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): DefaultMessageList {
        val messageList = DefaultMessageList()

        everyJunction.modify(opendriveModel) { currentJunction ->

            if (currentJunction.connection.isEmpty())
                messageList += DefaultMessage.of("EmptyList", "List for attribute 'connection' is empty, but it has to contain at least one element", currentJunction.additionalId, Severity.FATAL_ERROR, wasHealed = false)

            if (currentJunction.id.isBlank())
                messageList += DefaultMessage.of("MissingValue", "Missing value for attribute 'ID'.", currentJunction.additionalId, Severity.FATAL_ERROR, wasHealed = false)

            currentJunction
        }

        everyJunctionConnection.modify(opendriveModel) { currentJunctionConnection ->

            if (currentJunctionConnection.id.isBlank())
                messageList += DefaultMessage.of("MissingValue", "Missing value for attribute 'ID'.", currentJunctionConnection.additionalId, Severity.FATAL_ERROR, wasHealed = false)

            currentJunctionConnection
        }

        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = DefaultMessageList()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = everyJunction.modify(healedOpendriveModel) { currentJunction ->
            if (currentJunction.mainRoad.exists { it.isBlank() }) {
                messageList += DefaultMessage.of("EmptyValueForOptionalAttribute", "Attribute 'mainRoad' is set with an empty value even though the attribute itself is optional.", currentJunction.additionalId, Severity.WARNING, wasHealed = true)
                currentJunction.mainRoad = None
            }

            if (currentJunction.name.exists { it.isBlank() }) {
                messageList += DefaultMessage.of("EmptyValueForOptionalAttribute", "Attribute 'name' is set with an empty value even though the attribute itself is optional.", currentJunction.additionalId, Severity.WARNING, wasHealed = true)
                currentJunction.name = None
            }

            if (currentJunction.sEnd.exists { !it.isFinite() }) {
                messageList += DefaultMessage.of("EmptyValueForOptionalAttribute", "Attribute 'sEnd' is set with an empty value even though the attribute itself is optional.", currentJunction.additionalId, Severity.WARNING, wasHealed = true)
                currentJunction.sEnd = None
            }

            if (currentJunction.sStart.exists { !it.isFinite() }) {
                messageList += DefaultMessage.of("EmptyValueForOptionalAttribute", "Attribute 'sStart' is set with an empty value even though the attribute itself is optional.", currentJunction.additionalId, Severity.WARNING, wasHealed = true)
                currentJunction.sStart = None
            }

            currentJunction
        }

        healedOpendriveModel = everyJunctionConnection.modify(healedOpendriveModel) { currentJunctionConnection ->

            if (currentJunctionConnection.connectingRoad.exists { it.isBlank() }) {
                messageList += DefaultMessage.of("EmptyValueForOptionalAttribute", "Attribute 'connectingRoad' is set with an empty value even though the attribute itself is optional.", currentJunctionConnection.additionalId, Severity.WARNING, wasHealed = true)
                currentJunctionConnection.connectingRoad = None
            }

            if (currentJunctionConnection.incomingRoad.exists { it.isBlank() }) {
                messageList += DefaultMessage.of("EmptyValueForOptionalAttribute", "Attribute 'incomingRoad' is set with an empty value even though the attribute itself is optional.", currentJunctionConnection.additionalId, Severity.WARNING, wasHealed = true)
                currentJunctionConnection.incomingRoad = None
            }

            if (currentJunctionConnection.linkedRoad.exists { it.isBlank() }) {
                messageList += DefaultMessage.of("EmptyValueForOptionalAttribute", "Attribute 'linkedRoad' is set with an empty value even though the attribute itself is optional.", currentJunctionConnection.additionalId, Severity.WARNING, wasHealed = true)
                currentJunctionConnection.linkedRoad = None
            }

            currentJunctionConnection
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
