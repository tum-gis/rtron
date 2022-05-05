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
import io.rtron.model.opendrive.additions.identifier.LaneSectionIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
import io.rtron.model.opendrive.lane.RoadLanesLaneSection
import io.rtron.std.present
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.toReport

class RoadLanesLaneSectionEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Properties and Initializers
    private val _roadLanesLaneSectionLRLaneEvaluator = RoadLanesLaneSectionLRLaneEvaluator(configuration)

    // Methods
    fun evaluateFatalViolations(roadIdentifier: RoadIdentifier, laneSections: List<RoadLanesLaneSection>): Report =
        laneSections.mapIndexed { currentIndex, currentLaneSection ->
            evaluateFatalViolations(LaneSectionIdentifier(currentIndex, roadIdentifier), currentLaneSection)
        }.merge()

    fun evaluateNonFatalViolations(roadIdentifier: RoadIdentifier, laneSections: List<RoadLanesLaneSection>): ContextReport<List<RoadLanesLaneSection>> =
        laneSections.mapIndexed { currentIndex, currentLaneSection ->
            evaluateNonFatalViolations(LaneSectionIdentifier(currentIndex, roadIdentifier), currentLaneSection)
        }.sequenceContextReport()

    private fun evaluateFatalViolations(laneSectionIdentifier: LaneSectionIdentifier, laneSection: RoadLanesLaneSection): Report {
        val report = Report()
        return report
    }

    private fun evaluateNonFatalViolations(laneSectionIdentifier: LaneSectionIdentifier, laneSection: RoadLanesLaneSection): ContextReport<RoadLanesLaneSection> {
        val healedLaneSection = laneSection.copy()
        val report = Report()

        report += healedLaneSection.healMinorViolations().toReport(laneSectionIdentifier.toStringMap(), isFatal = false, wasHealed = true)

        healedLaneSection.left.present { currentLaneSectionLeft ->
            _roadLanesLaneSectionLRLaneEvaluator.evaluateNonFatalViolations(laneSectionIdentifier, currentLaneSectionLeft.lane).let {
                report += it.report
                currentLaneSectionLeft.lane = it.value
            }
        }

        healedLaneSection.right.present { currentLaneSectionRight ->
            _roadLanesLaneSectionLRLaneEvaluator.evaluateNonFatalViolations(laneSectionIdentifier, currentLaneSectionRight.lane).let {
                report += it.report
                currentLaneSectionRight.lane = it.value
            }
        }

        return ContextReport(healedLaneSection, report)
    }
}
