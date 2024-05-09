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

import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.issues.opendrive.of

object RoadSignalsEvaluator {
    // Methods
    fun evaluate(
        opendriveModel: OpendriveModel,
        parameters: OpendriveEvaluatorParameters,
        issueList: DefaultIssueList,
    ): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel =
            everyRoad.modify(modifiedOpendriveModel) { currentRoad ->

                currentRoad.signals.onSome { currentRoadSignals ->
                    val signalsFiltered =
                        currentRoadSignals.signal.filter { it.s <= currentRoad.length + parameters.numberTolerance }
                    if (currentRoadSignals.signal.size > signalsFiltered.size) {
                        issueList +=
                            DefaultIssue.of(
                                "RoadSignalPositionNotInSValueRange",
                                "Road signals (number of objects affected: ${currentRoadSignals.signal.size -
                                    signalsFiltered.size}) were removed since they were positioned outside the defined " +
                                    "length of the road.",
                                currentRoad.additionalId, Severity.ERROR, wasFixed = true,
                            )
                    }
                    currentRoadSignals.signal = signalsFiltered
                }

                currentRoad
            }
        return modifiedOpendriveModel
    }
}
