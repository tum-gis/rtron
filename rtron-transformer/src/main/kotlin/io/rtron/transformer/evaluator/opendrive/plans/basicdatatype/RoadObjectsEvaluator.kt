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

import arrow.core.None
import arrow.core.some
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoadObject
import io.rtron.model.opendrive.additions.optics.everyRoadObjectOutlineElement
import io.rtron.model.opendrive.additions.optics.everyRoadObjectRepeatElement
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.modifiers.BasicDataTypeModifier
import io.rtron.transformer.issues.opendrive.of
import kotlin.math.max
import kotlin.math.min

object RoadObjectsEvaluator {
    // Methods
    fun evaluate(
        opendriveModel: OpendriveModel,
        parameters: OpendriveEvaluatorParameters,
        issueList: DefaultIssueList,
    ): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel =
            everyRoadObject.modify(modifiedOpendriveModel) { currentRoadObject ->

                currentRoadObject.s =
                    BasicDataTypeModifier.modifyToFinitePositiveDouble(
                        currentRoadObject.s,
                        currentRoadObject.additionalId,
                        "s",
                        issueList,
                    )
                currentRoadObject.t =
                    BasicDataTypeModifier.modifyToFiniteDouble(
                        currentRoadObject.t,
                        currentRoadObject.additionalId,
                        "t",
                        issueList,
                    )
                currentRoadObject.zOffset =
                    BasicDataTypeModifier.modifyToFiniteDouble(
                        currentRoadObject.zOffset,
                        currentRoadObject.additionalId,
                        "zOffset",
                        issueList,
                    )
                currentRoadObject.hdg =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        currentRoadObject.hdg,
                        currentRoadObject.additionalId,
                        "hdg",
                        issueList,
                    )
                currentRoadObject.roll =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        currentRoadObject.roll,
                        currentRoadObject.additionalId,
                        "roll",
                        issueList,
                    )
                currentRoadObject.pitch =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        currentRoadObject.pitch,
                        currentRoadObject.additionalId,
                        "pitch",
                        issueList,
                    )
                currentRoadObject.height =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRoadObject.height,
                        currentRoadObject.additionalId,
                        "height",
                        issueList,
                    )

                if (currentRoadObject.height.isSome { 0.0 < it && it < parameters.numberTolerance }) {
                    currentRoadObject.height = parameters.numberTolerance.some()
                }

                currentRoadObject.radius =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRoadObject.radius,
                        currentRoadObject.additionalId,
                        "radius",
                        issueList,
                        parameters.numberTolerance,
                    )
                currentRoadObject.length =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRoadObject.length,
                        currentRoadObject.additionalId,
                        "length",
                        issueList,
                        parameters.numberTolerance,
                    )
                currentRoadObject.width =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRoadObject.width,
                        currentRoadObject.additionalId,
                        "width",
                        issueList,
                        parameters.numberTolerance,
                    )

                currentRoadObject.validity.filter { it.fromLane > it.toLane }.forEach { currentValidity ->
                    issueList +=
                        DefaultIssue.of(
                            "LaneValidityElementNotOrdered",
                            "The value of the @fromLane attribute shall be lower than or equal to the value of the @toLane attribute.",
                            currentRoadObject.additionalId, Severity.ERROR, wasFixed = true,
                        )
                    currentValidity.fromLane = min(currentValidity.fromLane, currentValidity.toLane)
                    currentValidity.toLane = max(currentValidity.fromLane, currentValidity.toLane)
                }

                if (currentRoadObject.outlines.isSome { it.outline.isEmpty() }) {
                    issueList +=
                        DefaultIssue(
                            "EmptyValueForOptionalAttribute",
                            "Attribute 'outlines' is set with an empty value even though the attribute itself is optional.",
                            "Header element", Severity.WARNING, wasFixed = true,
                        )
                    currentRoadObject.outlines = None
                }

                val repeatElementsFiltered =
                    currentRoadObject.repeat.filter { it.s.isFinite() && it.tStart.isFinite() && it.zOffsetStart.isFinite() }
                if (repeatElementsFiltered.size < currentRoadObject.repeat.size) {
                    issueList +=
                        DefaultIssue.of(
                            "UnexpectedValues",
                            "Ignoring ${currentRoadObject.repeat.size - repeatElementsFiltered.size} repeat entries which do not " +
                                "have a finite s, tStart, zOffsetStart value.",
                            currentRoadObject.additionalId, Severity.FATAL_ERROR, wasFixed = true,
                        )
                    // issueList += OpendriveException.UnexpectedValues("s, tStart, zOffsetStart",
                    // "Ignoring ${currentRoadObject.repeat.size - repeatElementsFiltered.size} repeat entries which do not have a
                    // finite s, tStart and zOffsetStart value.").toIssue(currentRoadObject.additionalId,
                    // isFatal = false, wasFixed: Boolean)
                    currentRoadObject.repeat = repeatElementsFiltered
                }

                currentRoadObject
            }

        modifiedOpendriveModel =
            everyRoadObjectOutlineElement.modify(modifiedOpendriveModel) { currentOutlineElement ->

                val cornerRoadElementsFiltered =
                    currentOutlineElement.cornerRoad.filter { it.s.isFinite() && it.t.isFinite() && it.dz.isFinite() }
                if (cornerRoadElementsFiltered.size < currentOutlineElement.cornerRoad.size) {
                    // issueList += OpendriveException.UnexpectedValues("s, t, dz",
                    // "Ignoring ${currentOutlineElement.cornerRoad.size - cornerRoadElementsFiltered.size} cornerRoad entries which
                    // do not have a finite s, t and dz value.").toIssue(currentOutlineElement.additionalId,
                    // isFatal = false, wasFixed: Boolean)
                    issueList +=
                        DefaultIssue.of(
                            "UnexpectedValues",
                            "Ignoring ${currentOutlineElement.cornerRoad.size - cornerRoadElementsFiltered.size} cornerRoad entries " +
                                "which do not have a finite s, t and dz value.",
                            currentOutlineElement.additionalId, Severity.FATAL_ERROR, wasFixed = true,
                        )

                    currentOutlineElement.cornerRoad = cornerRoadElementsFiltered
                }

                currentOutlineElement.cornerRoad.forEach { currentCornerRoad ->
                    if (!currentCornerRoad.height.isFinite() || currentCornerRoad.height < 0.0) {
                        issueList +=
                            DefaultIssue.of(
                                "UnexpectedValue", "Unexpected value for attribute 'height'",
                                currentOutlineElement.additionalId, Severity.WARNING, wasFixed = true,
                            )
                        currentCornerRoad.height = 0.0
                    }

                    if (0.0 < currentCornerRoad.height && currentCornerRoad.height <= parameters.numberTolerance) {
                        currentCornerRoad.height = 0.0
                    }
                }

                val cornerLocalElementsFiltered =
                    currentOutlineElement.cornerLocal.filter { it.u.isFinite() && it.v.isFinite() && it.z.isFinite() }
                if (cornerLocalElementsFiltered.size < currentOutlineElement.cornerLocal.size) {
                    // issueList += OpendriveException.UnexpectedValues("s, t, dz",
                    // "Ignoring ${currentOutlineElement.cornerLocal.size - cornerLocalElementsFiltered.size} cornerLocal entries which
                    // do not have a finite u, v and z value.").toIssue(currentOutlineElement.additionalId,
                    // isFatal = false, wasFixed = true)
                    issueList +=
                        DefaultIssue.of(
                            "UnexpectedValues",
                            "Ignoring ${currentOutlineElement.cornerRoad.size - cornerRoadElementsFiltered.size} cornerRoad entries " +
                                "which do not have a finite s, t and dz value.",
                            currentOutlineElement.additionalId, Severity.FATAL_ERROR, wasFixed = true,
                        )

                    currentOutlineElement.cornerLocal = cornerLocalElementsFiltered
                }

                currentOutlineElement.cornerLocal.forEach { currentCornerLocal ->
                    if (!currentCornerLocal.height.isFinite() || currentCornerLocal.height < 0.0) {
                        issueList +=
                            DefaultIssue.of(
                                "UnexpectedValue", "Unexpected value for attribute 'height'",
                                currentOutlineElement.additionalId, Severity.WARNING, wasFixed = true,
                            )
                        currentCornerLocal.height = 0.0
                    }

                    if (0.0 < currentCornerLocal.height && currentCornerLocal.height <= parameters.numberTolerance) {
                        currentCornerLocal.height = 0.0
                    }
                }

                currentOutlineElement
            }

        modifiedOpendriveModel =
            everyRoadObjectRepeatElement.modify(modifiedOpendriveModel) { currentRepeatElement ->
                require(currentRepeatElement.s.isFinite()) { "Must already be filtered." }
                require(currentRepeatElement.tStart.isFinite()) { "Must already be filtered." }
                require(currentRepeatElement.zOffsetStart.isFinite()) { "Must already be filtered." }

                currentRepeatElement.distance =
                    BasicDataTypeModifier.modifyToFinitePositiveDouble(
                        currentRepeatElement.distance, currentRepeatElement.additionalId, "distance", issueList,
                    )
                currentRepeatElement.heightEnd =
                    BasicDataTypeModifier.modifyToFinitePositiveDouble(
                        currentRepeatElement.heightEnd, currentRepeatElement.additionalId, "heightEnd", issueList,
                    )
                currentRepeatElement.heightStart =
                    BasicDataTypeModifier.modifyToFinitePositiveDouble(
                        currentRepeatElement.heightStart, currentRepeatElement.additionalId, "heightStart", issueList,
                    )
                currentRepeatElement.length =
                    BasicDataTypeModifier.modifyToFinitePositiveDouble(
                        currentRepeatElement.length, currentRepeatElement.additionalId, "length", issueList,
                    )
                currentRepeatElement.lengthEnd =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRepeatElement.lengthEnd, currentRepeatElement.additionalId, "lengthEnd", issueList,
                        parameters.numberTolerance,
                    )
                currentRepeatElement.lengthStart =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRepeatElement.lengthStart, currentRepeatElement.additionalId, "lengthStart", issueList,
                        parameters.numberTolerance,
                    )
                currentRepeatElement.radiusEnd =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRepeatElement.radiusEnd, currentRepeatElement.additionalId, "radiusEnd", issueList,
                        parameters.numberTolerance,
                    )
                currentRepeatElement.radiusStart =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRepeatElement.radiusStart, currentRepeatElement.additionalId, "radiusStart", issueList,
                        parameters.numberTolerance,
                    )

                if (!currentRepeatElement.tEnd.isFinite()) {
                    issueList +=
                        DefaultIssue.of(
                            "UnexpectedValue", "Unexpected value for attribute 'tEnd'",
                            currentRepeatElement.additionalId, Severity.WARNING, wasFixed = true,
                        )
                    currentRepeatElement.tEnd = currentRepeatElement.tStart
                }
                currentRepeatElement.widthEnd =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRepeatElement.widthEnd, currentRepeatElement.additionalId, "widthEnd", issueList,
                        parameters.numberTolerance,
                    )
                currentRepeatElement.widthStart =
                    BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                        currentRepeatElement.widthStart, currentRepeatElement.additionalId, "widthStart", issueList,
                        parameters.numberTolerance,
                    )
                currentRepeatElement.zOffsetEnd =
                    BasicDataTypeModifier.modifyToFiniteDouble(
                        currentRepeatElement.zOffsetEnd, currentRepeatElement.additionalId, "zOffsetEnd", issueList,
                    )

                currentRepeatElement
            }

        return modifiedOpendriveModel
    }
}
