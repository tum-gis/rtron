/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyLaneSection
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionCenterLaneRoadMark
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionLeftLane
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionLeftLaneRoadMark
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionRightLane
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionRightLaneRoadMark
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMark
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLRLane
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.modifiers.BasicDataTypeModifier
import io.rtron.transformer.issues.opendrive.of

object RoadLanesEvaluator {
    // Methods
    fun evaluate(
        opendriveModel: OpendriveModel,
        parameters: OpendriveEvaluatorParameters,
        issueList: DefaultIssueList,
    ): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        everyRoadLanesLaneSectionLeftLane.modify(modifiedOpendriveModel) { currentLeftLane ->
            issueList += evaluateFatalViolations(currentLeftLane, parameters)
            currentLeftLane
        }

        everyRoadLanesLaneSectionRightLane.modify(modifiedOpendriveModel) { currentRightLane ->
            issueList += evaluateFatalViolations(currentRightLane, parameters)
            currentRightLane
        }

        modifiedOpendriveModel =
            everyLaneSection.modify(modifiedOpendriveModel) { currentLaneSection ->

                currentLaneSection.left.onSome {
                    if (it.lane.isEmpty()) {
                        issueList +=
                            DefaultIssue.of(
                                "EmptyValueForOptionalAttribute",
                                "Attribute 'left' is set with an empty value even though the attribute itself is optional.",
                                currentLaneSection.additionalId,
                                Severity.WARNING,
                                wasFixed = true,
                            )
                        currentLaneSection.left = None
                    }
                }

                currentLaneSection.right.onSome {
                    if (it.lane.isEmpty()) {
                        issueList +=
                            DefaultIssue.of(
                                "EmptyValueForOptionalAttribute",
                                "Attribute 'right' is set with an empty value even though the attribute itself is optional.",
                                currentLaneSection.additionalId,
                                Severity.WARNING,
                                wasFixed = true,
                            )
                        currentLaneSection.right = None
                    }
                }

                currentLaneSection
            }

        // lanes
        modifiedOpendriveModel =
            everyRoadLanesLaneSectionCenterLane.modify(modifiedOpendriveModel) { currentCenterLane ->
                issueList += evaluateNonFatalViolations(currentCenterLane, parameters)
                currentCenterLane
            }

        modifiedOpendriveModel =
            everyRoadLanesLaneSectionLeftLane.modify(modifiedOpendriveModel) { currentLeftLane ->
                issueList += evaluateNonFatalViolations(currentLeftLane, parameters)
                currentLeftLane
            }

        modifiedOpendriveModel =
            everyRoadLanesLaneSectionRightLane.modify(modifiedOpendriveModel) { currentRightLane ->
                issueList += evaluateNonFatalViolations(currentRightLane, parameters)
                currentRightLane
            }

        // road marks
        modifiedOpendriveModel =
            everyRoadLanesLaneSectionCenterLaneRoadMark.modify(modifiedOpendriveModel) { currentRoadMark ->
                issueList += evaluateNonFatalViolations(currentRoadMark, parameters)
                currentRoadMark
            }

        modifiedOpendriveModel =
            everyRoadLanesLaneSectionLeftLaneRoadMark.modify(modifiedOpendriveModel) { currentRoadMark ->
                issueList += evaluateNonFatalViolations(currentRoadMark, parameters)
                currentRoadMark
            }

        modifiedOpendriveModel =
            everyRoadLanesLaneSectionRightLaneRoadMark.modify(modifiedOpendriveModel) { currentRoadMark ->
                issueList += evaluateNonFatalViolations(currentRoadMark, parameters)
                currentRoadMark
            }

        return modifiedOpendriveModel
    }

    private fun evaluateFatalViolations(
        lane: RoadLanesLaneSectionLRLane,
        parameters: OpendriveEvaluatorParameters,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()
        return issueList
    }

    private fun evaluateNonFatalViolations(
        lane: RoadLanesLaneSectionLRLane,
        parameters: OpendriveEvaluatorParameters,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        lane.getLaneWidthEntries().onSome {
            if (it.head.sOffset > parameters.numberTolerance) {
                issueList +=
                    DefaultIssue.of(
                        "LaneWidthEntriesNotDefinedFromStart",
                        "The width of the lane shall be defined for the full length of the lane section starting with a " +
                            "<width> element for s=0. The first available element is copied and positioned at s=0.",
                        lane.additionalId,
                        Severity.FATAL_ERROR,
                        wasFixed = true,
                    )
                val firstEntry = it.head.copy().apply { sOffset = 0.0 }
                lane.width = listOf(firstEntry) + lane.width
            }
        }

        lane.width =
            BasicDataTypeModifier.filterToStrictlySorted(
                lane.width,
                { it.sOffset },
                lane.additionalId,
                "width",
                issueList,
            )

        val widthEntriesFilteredBySOffsetFinite =
            lane.width.filter { currentWidth -> currentWidth.sOffset.isFinite() && currentWidth.sOffset >= 0.0 }
        if (widthEntriesFilteredBySOffsetFinite.size < lane.width.size) {
            issueList +=
                DefaultIssue.of(
                    "UnexpectedValues",
                    "Ignoring ${lane.width.size - widthEntriesFilteredBySOffsetFinite.size} width entries where sOffset is " +
                        "not-finite and positive.",
                    lane.additionalId,
                    Severity.FATAL_ERROR,
                    wasFixed = true,
                )
            lane.width = widthEntriesFilteredBySOffsetFinite
        }

        val widthEntriesFilteredByCoefficientsFinite =
            lane.width.filter { currentWidth -> currentWidth.coefficients.all { it.isFinite() } }
        if (widthEntriesFilteredByCoefficientsFinite.size < lane.width.size) {
            issueList +=
                DefaultIssue.of(
                    "UnexpectedValues",
                    "Ignoring ${lane.width.size - widthEntriesFilteredByCoefficientsFinite.size} width entries where " +
                        "coefficients \"a, b, c, d\", are not finite.",
                    lane.additionalId,
                    Severity.FATAL_ERROR,
                    wasFixed = true,
                )
            lane.width = widthEntriesFilteredByCoefficientsFinite
        }

        lane.height =
            BasicDataTypeModifier.filterToStrictlySorted(
                lane.height,
                { it.sOffset },
                lane.additionalId,
                "height",
                issueList,
            )

        val heightEntriesFilteredByCoefficientsFinite =
            lane.height.filter { it.inner.isFinite() && it.outer.isFinite() }
        if (heightEntriesFilteredByCoefficientsFinite.size < lane.height.size) {
            issueList +=
                DefaultIssue.of(
                    "UnexpectedValues",
                    "Ignoring ${lane.height.size - heightEntriesFilteredByCoefficientsFinite.size} height entries where inner or " +
                        "outer is not finite.",
                    lane.additionalId,
                    Severity.FATAL_ERROR,
                    wasFixed = true,
                )
            lane.height = heightEntriesFilteredByCoefficientsFinite
        }

        lane.roadMark =
            BasicDataTypeModifier.filterToStrictlySorted(
                lane.roadMark,
                { it.sOffset },
                lane.additionalId,
                "roadMark",
                issueList,
            )

        return issueList
    }

    private fun evaluateNonFatalViolations(
        lane: RoadLanesLaneSectionCenterLane,
        parameters: OpendriveEvaluatorParameters,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        lane.roadMark =
            BasicDataTypeModifier.filterToStrictlySorted(
                lane.roadMark,
                { it.sOffset },
                lane.additionalId,
                "roadMark",
                issueList,
            )

        return issueList
    }

    private fun evaluateNonFatalViolations(
        roadMark: RoadLanesLaneSectionLCRLaneRoadMark,
        parameters: OpendriveEvaluatorParameters,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        roadMark.width =
            BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                roadMark.width,
                roadMark.additionalId,
                "width",
                issueList,
                parameters.numberTolerance,
            )

        roadMark.height =
            BasicDataTypeModifier.modifyToOptionalFinitePositiveDouble(
                roadMark.height,
                roadMark.additionalId,
                "height",
                issueList,
                parameters.numberTolerance,
            )

        roadMark.type.onSome {
            it.width =
                BasicDataTypeModifier.modifyToFinitePositiveDouble(
                    it.width,
                    roadMark.additionalId,
                    "width",
                    issueList,
                )
        }

        return issueList
    }
}
