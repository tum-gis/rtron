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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules

import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.MessageList
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.plans.AbstractOpendriveEvaluator

class ModelingRulesEvaluator(val configuration: OpendriveEvaluatorConfiguration) :
    AbstractOpendriveEvaluator() {

    // Properties amd Initializers
    private val _roadEvaluator = RoadEvaluator(configuration)
    private val _roadLanesEvaluator = RoadLanesEvaluator(configuration)
    private val _roadObjectsEvaluator = RoadObjectsEvaluator(configuration)
    private val _junctionEvaluator = JunctionEvaluator(configuration)

    // Methods
    override fun evaluateFatalViolations(opendriveModel: OpendriveModel): MessageList {
        val messageList = MessageList()

        messageList += _roadEvaluator.evaluateFatalViolations(opendriveModel)
        messageList += _roadLanesEvaluator.evaluateFatalViolations(opendriveModel)
        messageList += _roadLanesEvaluator.evaluateFatalViolations(opendriveModel)
        messageList += _roadObjectsEvaluator.evaluateFatalViolations(opendriveModel)
        messageList += _junctionEvaluator.evaluateFatalViolations(opendriveModel)

        return messageList
    }

    override fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        var healedOpendriveModel = opendriveModel.copy()
        val messageList = MessageList()

        _roadEvaluator.evaluateNonFatalViolations(healedOpendriveModel).let {
            healedOpendriveModel = it.value
            messageList += it.messageList
        }

        _roadLanesEvaluator.evaluateNonFatalViolations(healedOpendriveModel).let {
            healedOpendriveModel = it.value
            messageList += it.messageList
        }

        _roadObjectsEvaluator.evaluateNonFatalViolations(healedOpendriveModel).let {
            healedOpendriveModel = it.value
            messageList += it.messageList
        }

        _junctionEvaluator.evaluateNonFatalViolations(healedOpendriveModel).let {
            healedOpendriveModel = it.value
            messageList += it.messageList
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
