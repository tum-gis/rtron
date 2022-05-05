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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype.lane

import io.rtron.io.report.ContextReport
import io.rtron.io.report.Report
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
import io.rtron.model.opendrive.lane.RoadLanes
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.toReport

class RoadLanesEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Properties and Initializers
    private val _roadLanesLaneSectionEvaluator = RoadLanesLaneSectionEvaluator(configuration)

    // Methods
    fun evaluateFatalViolations(roadIdentifier: RoadIdentifier, roadLanes: RoadLanes): Report {
        val report = Report()

        report += roadLanes.getSevereViolations().toReport(roadIdentifier.toStringMap(), isFatal = true, wasHealed = false)

        report += _roadLanesLaneSectionEvaluator.evaluateFatalViolations(roadIdentifier, roadLanes.laneSection)

        return report
    }

    fun evaluateNonFatalViolations(roadIdentifier: RoadIdentifier, roadLanes: RoadLanes): ContextReport<RoadLanes> {
        val healedRoadLanes = roadLanes.copy()
        val report = Report()

        report += healedRoadLanes.healMinorViolations().toReport(roadIdentifier.toStringMap(), isFatal = false, wasHealed = true)

        _roadLanesLaneSectionEvaluator.evaluateNonFatalViolations(roadIdentifier, healedRoadLanes.laneSection).let {
            report += it.report
            healedRoadLanes.laneSection = it.value
        }

        return ContextReport(healedRoadLanes, report)
    }
}
