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

package io.rtron.transformer.evaluator.opendrive.plans.conversionrequirements

import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyJunction
import io.rtron.model.opendrive.junction.EJunctionType
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.messages.opendrive.of

object JunctionEvaluator {

    // Methods
    fun evaluate(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        everyJunction.modify(modifiedOpendriveModel) { currentJunction ->

            if (currentJunction.typeValidated == EJunctionType.DEFAULT && currentJunction.connection.any { it.incomingRoad.isEmpty() || it.connectingRoad.isEmpty() }) {
                messageList += DefaultMessage.of("", "Junction and junction type is not supported, since only junctions are supported that have connections with an incomingRoad and a connectionRoad.", currentJunction.additionalId, Severity.FATAL_ERROR, wasFixed = false)
            }

            currentJunction
        }

        return modifiedOpendriveModel
    }
}
