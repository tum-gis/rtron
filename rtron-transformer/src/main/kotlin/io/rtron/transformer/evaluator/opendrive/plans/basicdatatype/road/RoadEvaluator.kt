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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype.road

import io.rtron.io.report.ContextReport
import io.rtron.io.report.Report
import io.rtron.io.report.merge
import io.rtron.io.report.sequenceContextReport
import io.rtron.model.opendrive.road.Road
import io.rtron.std.present
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.toReport

class RoadEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(roads: List<Road>): Report {
        val report = Report()

        report += roads.map { evaluateFatalViolations(it) }.merge()

        return report
    }

    fun evaluateNonFatalViolations(roads: List<Road>): ContextReport<List<Road>> =
        roads.map { evaluateNonFatalViolations(it) }.sequenceContextReport()

    private fun evaluateFatalViolations(road: Road): Report {
        val report = Report()
        report += road.getSevereViolations().toReport(road, isFatal = true, wasHealed = false)
        report += road.planView.getSevereViolations().toReport(road, isFatal = true, wasHealed = false)

        return report
    }

    private fun evaluateNonFatalViolations(road: Road): ContextReport<Road> {
        val healedRoad = road.copy()
        val report = Report()

        report += healedRoad.healMinorViolations(configuration.numberTolerance).toReport(road, isFatal = false, wasHealed = true)

        healedRoad.elevationProfile.present {
            report += it.healMinorViolations().toReport(road, isFatal = false, wasHealed = true)
        }

        healedRoad.lateralProfile.present {
            report += it.healMinorViolations().toReport(road, isFatal = false, wasHealed = true)
        }

        return ContextReport(healedRoad, report)
    }
}
