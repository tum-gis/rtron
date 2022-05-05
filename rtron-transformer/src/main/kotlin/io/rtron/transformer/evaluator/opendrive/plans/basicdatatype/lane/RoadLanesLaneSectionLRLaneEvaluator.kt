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
import io.rtron.io.report.merge
import io.rtron.io.report.sequenceContextReport
import io.rtron.model.opendrive.additions.identifier.LaneIdentifier
import io.rtron.model.opendrive.additions.identifier.LaneSectionIdentifier
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLeftLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionRightLane
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.toReport

class RoadLanesLaneSectionLRLaneEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    @JvmName("evaluateFatalViolationsForLeftLane")
    fun evaluateFatalViolations(laneSectionIdentifier: LaneSectionIdentifier, leftLanes: List<RoadLanesLaneSectionLeftLane>): Report =
        leftLanes.map { evaluateFatalViolations(LaneIdentifier(it.id, laneSectionIdentifier), it) }.merge()

    @JvmName("evaluateNonFatalViolationsForRightLane")
    fun evaluateNonFatalViolations(laneSectionIdentifier: LaneSectionIdentifier, leftLanes: List<RoadLanesLaneSectionLeftLane>): ContextReport<List<RoadLanesLaneSectionLeftLane>> =
        leftLanes.map { evaluateNonFatalViolations(LaneIdentifier(it.id, laneSectionIdentifier), it) }.sequenceContextReport()

    private fun evaluateFatalViolations(laneIdentifier: LaneIdentifier, lane: RoadLanesLaneSectionLeftLane): Report {
        val report = Report()
        report += lane.getSevereViolations().toReport(laneIdentifier.toStringMap(), isFatal = true, wasHealed = false)
        return report
    }

    private fun evaluateNonFatalViolations(laneIdentifier: LaneIdentifier, lane: RoadLanesLaneSectionLeftLane): ContextReport<RoadLanesLaneSectionLeftLane> {
        val healedLane = lane.copy()
        val report = Report()

        report += healedLane.healMinorViolations().toReport(laneIdentifier.toStringMap(), isFatal = false, wasHealed = true)

        return ContextReport(healedLane, report)
    }

    @JvmName("evaluateFatalViolationsForRightLane")
    fun evaluateFatalViolations(laneSectionIdentifier: LaneSectionIdentifier, rightLanes: List<RoadLanesLaneSectionRightLane>): Report =
        rightLanes.map { evaluateFatalViolations(LaneIdentifier(it.id, laneSectionIdentifier), it) }.merge()

    @JvmName("evaluateNonFatalViolationsForLeftLane")
    fun evaluateNonFatalViolations(laneSectionIdentifier: LaneSectionIdentifier, rightLanes: List<RoadLanesLaneSectionRightLane>): ContextReport<List<RoadLanesLaneSectionRightLane>> =
        rightLanes.map { evaluateNonFatalViolations(LaneIdentifier(it.id, laneSectionIdentifier), it) }.sequenceContextReport()

    private fun evaluateFatalViolations(laneIdentifier: LaneIdentifier, lane: RoadLanesLaneSectionRightLane): Report {
        val report = Report()
        report += lane.getSevereViolations().toReport(laneIdentifier.toStringMap(), isFatal = true, wasHealed = false)
        return report
    }

    private fun evaluateNonFatalViolations(laneIdentifier: LaneIdentifier, lane: RoadLanesLaneSectionRightLane): ContextReport<RoadLanesLaneSectionRightLane> {
        val healedLane = lane.copy()
        val report = Report()

        report += healedLane.healMinorViolations().toReport(laneIdentifier.toStringMap(), isFatal = false, wasHealed = true)

        return ContextReport(healedLane, report)
    }
}
