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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype

import arrow.core.some
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoadSignal
import io.rtron.model.opendrive.core.EUnit
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.modifiers.BasicDataTypeModifier
import io.rtron.transformer.messages.opendrive.of

object RoadSignalsEvaluator {

    // Methods
    fun evaluate(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel = everyRoadSignal.modify(modifiedOpendriveModel) { currentRoadSignal ->

            currentRoadSignal.height = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRoadSignal.height, currentRoadSignal.additionalId, "height", messageList, parameters.numberTolerance)
            currentRoadSignal.hOffset = BasicDataTypeModifier.modifyToOptionalFiniteDouble(currentRoadSignal.hOffset, currentRoadSignal.additionalId, "hOffset", messageList)
            currentRoadSignal.pitch = BasicDataTypeModifier.modifyToOptionalFiniteDouble(currentRoadSignal.pitch, currentRoadSignal.additionalId, "pitch", messageList)
            currentRoadSignal.roll = BasicDataTypeModifier.modifyToOptionalFiniteDouble(currentRoadSignal.roll, currentRoadSignal.additionalId, "roll", messageList)
            currentRoadSignal.s = BasicDataTypeModifier.modifyToFinitePositiveDouble(currentRoadSignal.s, currentRoadSignal.additionalId, "s", messageList)
            currentRoadSignal.subtype = BasicDataTypeModifier.modifyToNonBlankString(currentRoadSignal.subtype, currentRoadSignal.additionalId, "subtype", messageList, fallbackValue = "-1")
            currentRoadSignal.t = BasicDataTypeModifier.modifyToFinitePositiveDouble(currentRoadSignal.t, currentRoadSignal.additionalId, "t", messageList)
            currentRoadSignal.type = BasicDataTypeModifier.modifyToNonBlankString(currentRoadSignal.type, currentRoadSignal.additionalId, "type", messageList, fallbackValue = "-1")
            currentRoadSignal.value = BasicDataTypeModifier.modifyToOptionalFiniteDouble(currentRoadSignal.value, currentRoadSignal.additionalId, "value", messageList)

            if (currentRoadSignal.value.isDefined() && currentRoadSignal.unit.isEmpty()) {
                messageList += DefaultMessage.of("UnitAttributeMustBeDefinedWhenValueAttributeIsDefined", "Attribute 'unit' shall be defined, when attribute 'value' is defined.", currentRoadSignal.additionalId, Severity.WARNING, wasFixed = true)
                currentRoadSignal.unit = EUnit.KILOMETER_PER_HOUR.some()
            }
            currentRoadSignal.width = BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(currentRoadSignal.width, currentRoadSignal.additionalId, "width", messageList, parameters.numberTolerance)
            currentRoadSignal.zOffset = BasicDataTypeModifier.modifyToFiniteDouble(currentRoadSignal.zOffset, currentRoadSignal.additionalId, "zOffset", messageList)

            currentRoadSignal
        }

        return modifiedOpendriveModel
    }
}
