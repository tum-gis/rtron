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

package io.rtron.transformer.evaluator.opendrive.plans.conversionrequirements.road

import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.io.report.sequenceContextReport
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
import io.rtron.model.opendrive.road.Road
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.of

class RoadEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(roads: List<Road>): Report {
        val report = Report()
        return report
    }

    fun evaluateNonFatalViolations(roads: List<Road>): ContextReport<List<Road>> =
        roads.map { evaluateNonFatalViolations(it) }.sequenceContextReport()

    private fun evaluateNonFatalViolations(road: Road): ContextReport<Road> {
        val healedRoad = road.copy()
        val report = Report()
        val roadIdentifier = RoadIdentifier(road.id)

        if (healedRoad.planView.geometry.any { it.length <= configuration.numberTolerance }) {
            report += Message.of("Plan view contains geometry elements with a length of zero (below tolerance threshold), which are removed.", roadIdentifier.toStringMap(), isFatal = false, wasHealed = true)
            healedRoad.planView.geometry = healedRoad.planView.geometry.filter { it.length > configuration.numberTolerance }
        }

        return ContextReport(healedRoad, report)
    }
}
