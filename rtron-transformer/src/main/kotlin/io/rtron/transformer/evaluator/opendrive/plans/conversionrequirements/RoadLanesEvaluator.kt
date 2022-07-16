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

import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.MessageList
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration

class RoadLanesEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): MessageList {
        val messageList = MessageList()

        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = MessageList()
        var healedOpendriveModel = opendriveModel

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
