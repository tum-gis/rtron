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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules

import arrow.core.Some
import arrow.core.flattenOption
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.model.opendrive.additions.optics.everyRoadObject
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.issues.opendrive.of

object RoadObjectsEvaluator {
    // Methods
    fun evaluate(
        opendriveModel: OpendriveModel,
        parameters: OpendriveEvaluatorParameters,
        issueList: DefaultIssueList,
    ): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel =
            everyRoad.modify(modifiedOpendriveModel) { currentRoad ->

                currentRoad.objects.onSome { currentRoadObjects ->
                    val roadObjectsFiltered =
                        currentRoadObjects.roadObject.filter { it.s <= currentRoad.length + parameters.numberTolerance }
                    if (currentRoadObjects.roadObject.size > roadObjectsFiltered.size) {
                        issueList +=
                            DefaultIssue.of(
                                "RoadObjectPositionNotInSValueRange",
                                "Road object (number of objects affected: ${currentRoadObjects.roadObject.size -
                                    roadObjectsFiltered.size}) were removed since they were positioned outside the " +
                                    "defined length of the road.",
                                currentRoad.additionalId, Severity.ERROR, wasFixed = true,
                            )
                    }
                    currentRoadObjects.roadObject = roadObjectsFiltered

                    val roadObjectsFilteredRepeat =
                        currentRoadObjects.roadObject.filter { currentRoadObject ->
                            currentRoadObject.repeat.isEmpty() ||
                                currentRoadObject.repeat.all { it.s + it.length <= currentRoad.length + parameters.numberTolerance }
                        }
                    if (currentRoadObjects.roadObject.size > roadObjectsFilteredRepeat.size) {
                        issueList +=
                            DefaultIssue.of(
                                "RoadObjectRepeatPositionNotInSValueRange",
                                "Road object repeats (number of objects affected: ${currentRoadObjects.roadObject.size -
                                    roadObjectsFiltered.size}) were removed since they were positioned outside the " +
                                    "defined length of the road.",
                                currentRoad.additionalId, Severity.ERROR, wasFixed = true,
                            )
                    }
                    currentRoadObjects.roadObject = roadObjectsFilteredRepeat
                }

                currentRoad
            }

        modifiedOpendriveModel =
            everyRoadObject.modify(modifiedOpendriveModel) { currentRoadObject ->

                // adding ids for outline elements
                currentRoadObject.outlines.onSome { currentOutlinesElement ->
                    val outlineElementsWithoutId = currentOutlinesElement.outline.filter { it.id.isNone() }

                    if (outlineElementsWithoutId.isNotEmpty()) {
                        val startId: Int = currentOutlinesElement.outline.map { it.id }.flattenOption().maxOrNull() ?: 0
                        issueList +=
                            DefaultIssue.of(
                                "MissingValue",
                                "Missing value for attribute 'id'.",
                                currentRoadObject.additionalId, Severity.FATAL_ERROR, wasFixed = true,
                            )
                        outlineElementsWithoutId.forEachIndexed { index, outlineElement ->
                            outlineElement.id = Some(startId + index)
                        }
                    }
                }

                currentRoadObject.outlines.onSome { currentRoadObjectOutline ->
                    if (currentRoadObjectOutline.outline.any { it.isPolyhedron() && !it.isPolyhedronUniquelyDefined() }) {
                        issueList +=
                            DefaultIssue.of(
                                "SimultaneousDefinitionCornerRoadCornerLocal",
                                "An <outline> element shall be followed by one or more <cornerRoad> elements or by one or more " +
                                    "<cornerLocal> element. Since both are defined, the <cornerLocal> elements are removed.",
                                currentRoadObject.additionalId, Severity.FATAL_ERROR, wasFixed = true,
                            )
                        currentRoadObjectOutline.outline.forEach { it.cornerLocal = emptyList() }
                    }
                }

                val repeatElementsFiltered = currentRoadObject.repeat.filter { it.length >= parameters.numberTolerance }
                if (repeatElementsFiltered.size < currentRoadObject.repeat.size) {
                    // TODO: double check handling
                    issueList +=
                        DefaultIssue.of(
                            "RepeatElementHasZeroLength",
                            "A repeat element should have a length higher than zero and tolerance.",
                            currentRoadObject.additionalId, Severity.FATAL_ERROR, wasFixed = true,
                        )
                    currentRoadObject.repeat = repeatElementsFiltered
                }

                currentRoadObject
            }

        return modifiedOpendriveModel
    }
}
