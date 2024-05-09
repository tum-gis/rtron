/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoadSignal
import io.rtron.model.opendrive.core.EUnit
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.modifiers.BasicDataTypeModifier
import io.rtron.transformer.issues.opendrive.of
import kotlin.math.max
import kotlin.math.min

object RoadSignalsEvaluator {
    // Methods
    fun evaluate(
        opendriveModel: OpendriveModel,
        parameters: OpendriveEvaluatorParameters,
        issueList: DefaultIssueList,
    ): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel =
            everyRoadSignal.modify(modifiedOpendriveModel) { currentRoadSignal ->

                currentRoadSignal.height =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRoadSignal.height, currentRoadSignal.additionalId, "height", issueList, parameters.numberTolerance,
                    )
                currentRoadSignal.hOffset =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        currentRoadSignal.hOffset, currentRoadSignal.additionalId, "hOffset", issueList,
                    )
                currentRoadSignal.pitch =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        currentRoadSignal.pitch, currentRoadSignal.additionalId, "pitch", issueList,
                    )
                currentRoadSignal.roll =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        currentRoadSignal.roll, currentRoadSignal.additionalId, "roll", issueList,
                    )
                currentRoadSignal.s =
                    BasicDataTypeModifier.modifyToFinitePositiveDouble(
                        currentRoadSignal.s, currentRoadSignal.additionalId, "s", issueList,
                    )
                currentRoadSignal.subtype =
                    BasicDataTypeModifier.modifyToNonBlankString(
                        currentRoadSignal.subtype, currentRoadSignal.additionalId, "subtype", issueList, fallbackValue = "-1",
                    )
                currentRoadSignal.t =
                    BasicDataTypeModifier.modifyToFiniteDouble(
                        currentRoadSignal.t, currentRoadSignal.additionalId, "t", issueList,
                    )
                currentRoadSignal.type =
                    BasicDataTypeModifier.modifyToNonBlankString(
                        currentRoadSignal.type, currentRoadSignal.additionalId, "type", issueList, fallbackValue = "-1",
                    )
                currentRoadSignal.value =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        currentRoadSignal.value, currentRoadSignal.additionalId, "value", issueList,
                    )

                if (currentRoadSignal.value.isSome() && currentRoadSignal.unit.isNone()) {
                    issueList +=
                        DefaultIssue.of(
                            "UnitAttributeMustBeDefinedWhenValueAttributeIsDefined",
                            "Attribute 'unit' shall be defined, when attribute 'value' is defined.",
                            currentRoadSignal.additionalId, Severity.WARNING, wasFixed = true,
                        )
                    currentRoadSignal.unit = EUnit.KILOMETER_PER_HOUR.some()
                }
                currentRoadSignal.width =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRoadSignal.width,
                        currentRoadSignal.additionalId, "width", issueList, parameters.numberTolerance,
                    )
                currentRoadSignal.zOffset =
                    BasicDataTypeModifier.modifyToFiniteDouble(
                        currentRoadSignal.zOffset,
                        currentRoadSignal.additionalId, "zOffset", issueList,
                    )

                currentRoadSignal.validity.filter { it.fromLane > it.toLane }.forEach { currentValidity ->
                    issueList +=
                        DefaultIssue.of(
                            "LaneValidityElementNotOrdered",
                            "The value of the @fromLane attribute shall be lower than or equal to the value of the @toLane attribute.",
                            currentRoadSignal.additionalId, Severity.ERROR, wasFixed = true,
                        )
                    currentValidity.fromLane = min(currentValidity.fromLane, currentValidity.toLane)
                    currentValidity.toLane = max(currentValidity.fromLane, currentValidity.toLane)
                }

                currentRoadSignal
            }

        return modifiedOpendriveModel
    }
}
