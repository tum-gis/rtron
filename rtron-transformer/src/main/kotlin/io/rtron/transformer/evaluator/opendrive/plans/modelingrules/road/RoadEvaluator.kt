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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules.road

import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.io.report.merge
import io.rtron.io.report.sequenceContextReport
import io.rtron.math.std.fuzzyEquals
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
import io.rtron.model.opendrive.road.Road
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.of

class RoadEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(roads: List<Road>): Report =
        roads.map { evaluateFatalViolations(it) }.merge()

    fun evaluateNonFatalViolations(roads: List<Road>): ContextReport<List<Road>> =
        roads.map { evaluateNonFatalViolations(it) }.sequenceContextReport()

    private fun evaluateFatalViolations(road: Road): Report {
        val report = Report()
        val roadIdentifier = RoadIdentifier(road.id)

        if (road.planView.geometry.any { it.s > road.length + configuration.numberTolerance })
            report += Message.of("Road contains geometry elements in the plan view, where s exceeds the total length of the road (${road.length}).", roadIdentifier.toStringMap(), isFatal = true, wasHealed = false)

        return report
    }

    private fun evaluateNonFatalViolations(road: Road): ContextReport<Road> {
        val healedRoad = road.copy()
        val report = Report()
        val roadIdentifier = RoadIdentifier(road.id)

        healedRoad.planView.geometry.zipWithNext().forEach {
            val actualLength = it.second.s - it.first.s
            if (!fuzzyEquals(it.first.length, actualLength, configuration.numberTolerance)) {
                report += Message.of("Length attribute (length=${it.first.length}) of the geometry element (s=${it.first.s}) does not match the start position (s=${it.second.s}) of the next geometry element.", roadIdentifier.toStringMap(), isFatal = false, wasHealed = true)
                it.first.length = actualLength
            }
        }

        if (!fuzzyEquals(healedRoad.planView.geometry.last().s + healedRoad.planView.geometry.last().length, road.length, configuration.numberTolerance)) {
            report += Message.of("Length attribute (length=${healedRoad.planView.geometry.last().length}) of the last geometry element (s=${healedRoad.planView.geometry.last().s}) does not match the total road length (length=${road.length}).", roadIdentifier.toStringMap(), isFatal = false, wasHealed = true)
            healedRoad.planView.geometry.last().length = road.length - healedRoad.planView.geometry.last().s
        }

        return ContextReport(healedRoad, report)
    }
}
