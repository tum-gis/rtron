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

package io.rtron.transformer.evaluator.opendrive.plans.conversionrequirements

import io.rtron.io.messages.Message
import io.rtron.io.messages.MessageList
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyJunction
import io.rtron.model.opendrive.junction.EJunctionType
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.report.of

class JunctionEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): MessageList {
        val messageList = MessageList()

        everyJunction.modify(opendriveModel) { currentJunction ->

            if (currentJunction.typeValidated == EJunctionType.DEFAULT && currentJunction.connection.any { it.incomingRoad.isEmpty() || it.connectingRoad.isEmpty() })
                messageList += Message.of("Junction and junction type is not supported, since only junctions are supported that have connections with an incomingRoad and a connectionRoad.", currentJunction.additionalId, isFatal = true, wasHealed = false)

            currentJunction
        }

        return messageList
    }
}
