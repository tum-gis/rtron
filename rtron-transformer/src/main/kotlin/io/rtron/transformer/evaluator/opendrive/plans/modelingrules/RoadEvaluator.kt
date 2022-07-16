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
import io.rtron.io.messages.Message
import io.rtron.io.messages.MessageList
import io.rtron.math.std.fuzzyEquals
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.report.of

class RoadEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): MessageList {
        val messageList = MessageList()

        everyRoad.modify(opendriveModel) { currentRoad ->
            if (currentRoad.planView.geometry.any { it.s > currentRoad.length + configuration.numberTolerance })
                messageList += Message.of("Road contains geometry elements in the plan view, where s exceeds the total length of the road (${currentRoad.length}).", currentRoad.additionalId, isFatal = true, wasHealed = false)

            currentRoad
        }

        return messageList
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextMessageList<OpendriveModel> {
        val messageList = MessageList()
        var healedOpendriveModel = opendriveModel.copy()

        healedOpendriveModel = everyRoad.modify(healedOpendriveModel) { currentRoad ->

            if (currentRoad.planView.geometry.any { it.length <= configuration.numberTolerance }) {
                messageList += Message.of("Plan view contains geometry elements with a length of zero (below tolerance threshold), which are removed.", currentRoad.additionalId, isFatal = false, wasHealed = true)
                currentRoad.planView.geometry = currentRoad.planView.geometry.filter { it.length > configuration.numberTolerance }
            }

            /*val planViewGeometryLengthsSum = road.planView.geometry.sumOf { it.length }
            if (!fuzzyEquals(planViewGeometryLengthsSum, road.length, configuration.numberTolerance)) {
                healedViolations += OpendriveException.UnexpectedValue("length", road.length.toString(), ", as the sum of the individual plan view elements is different")
                road.length = planViewGeometryLengthsSum
            }*/

            currentRoad.planView.geometry.zipWithNext().forEach {
                val actualLength = it.second.s - it.first.s
                if (!fuzzyEquals(it.first.length, actualLength, configuration.numberTolerance)) {
                    messageList += Message.of("Length attribute (length=${it.first.length}) of the geometry element (s=${it.first.s}) does not match the start position (s=${it.second.s}) of the next geometry element.", currentRoad.additionalId, isFatal = false, wasHealed = true)
                    it.first.length = actualLength
                }
            }

            if (!fuzzyEquals(currentRoad.planView.geometry.last().s + currentRoad.planView.geometry.last().length, currentRoad.length, configuration.numberTolerance)) {
                messageList += Message.of("Length attribute (length=${currentRoad.planView.geometry.last().length}) of the last geometry element (s=${currentRoad.planView.geometry.last().s}) does not match the total road length (length=${currentRoad.length}).", currentRoad.additionalId, isFatal = false, wasHealed = true)
                currentRoad.planView.geometry.last().length = currentRoad.length - currentRoad.planView.geometry.last().s
            }

            currentRoad
        }

        return ContextMessageList(healedOpendriveModel, messageList)
    }
}
