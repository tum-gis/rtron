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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules.lane

import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.io.report.merge
import io.rtron.io.report.sequenceContextReport
import io.rtron.model.opendrive.additions.identifier.LaneSectionIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
import io.rtron.model.opendrive.lane.RoadLanesLaneSection
import io.rtron.std.isSortedDescending
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.of

class RoadLanesLaneSectionEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

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

        if (laneSection.center.getNumberOfLanes() != 1)
            report += Message.of("Lane section should contain exactly one center lane.", laneSectionIdentifier.toStringMap(), isFatal = true, wasHealed = false)

        if (laneSection.left.isEmpty() && laneSection.right.isEmpty())
            report += Message.of("Each road shall have a center lane and one lane with a width larger than 0.", laneSectionIdentifier.toStringMap(), isFatal = true, wasHealed = false)

        laneSection.left.tap { currentLaneSectionLeft ->
            val leftLaneIds = currentLaneSectionLeft.lane.map { it.id }
            val expectedIds = (currentLaneSectionLeft.getNumberOfLanes() downTo 1).toList()

            if (leftLaneIds.distinct().size < leftLaneIds.size)
                report += Message.of("Lane numbering shall be unique per lane section.", laneSectionIdentifier.toStringMap(), isFatal = true, wasHealed = false)
            if (!leftLaneIds.containsAll(expectedIds))
                report += Message.of("Lane numbering shall be consecutive without any gaps.", laneSectionIdentifier.toStringMap(), isFatal = true, wasHealed = false)
        }

        laneSection.right.tap { currentLaneSectionRight ->
            val rightLaneIds = currentLaneSectionRight.lane.map { it.id }
            val expectedIds = (-1 downTo -currentLaneSectionRight.getNumberOfLanes()).toList()

            if (rightLaneIds.distinct().size < rightLaneIds.size)
                report += Message.of("Lane numbering shall be unique per lane section.", laneSectionIdentifier.toStringMap(), isFatal = true, wasHealed = false)
            if (!rightLaneIds.containsAll(expectedIds))
                report += Message.of("Lane numbering shall be consecutive without any gaps.", laneSectionIdentifier.toStringMap(), isFatal = true, wasHealed = false)
        }

        return report
    }

    private fun evaluateNonFatalViolations(laneSectionIdentifier: LaneSectionIdentifier, laneSection: RoadLanesLaneSection): ContextReport<RoadLanesLaneSection> {
        val healedLaneSection = laneSection.copy()
        val report = Report()

        healedLaneSection.left.tap { currentLaneSectionLeft ->
            if (!currentLaneSectionLeft.lane.map { it.id }.isSortedDescending()) {
                currentLaneSectionLeft.lane = currentLaneSectionLeft.lane.sortedByDescending { it.id }
                report += Message.of("Lane numbering shall start with 1 next to the center lane, descend in negative t-direction and ascend in positive t-direction.", laneSectionIdentifier.toStringMap(), isFatal = true, wasHealed = true)
            }
        }

        healedLaneSection.right.tap { currentLaneSectionRight ->
            if (!currentLaneSectionRight.lane.map { it.id }.isSortedDescending()) {
                currentLaneSectionRight.lane = currentLaneSectionRight.lane.sortedByDescending { it.id }
                report += Message.of("Lane numbering shall start with 1 next to the center lane, descend in negative t-direction and ascend in positive t-direction.", laneSectionIdentifier.toStringMap(), isFatal = true, wasHealed = true)
            }
        }

        return ContextReport(healedLaneSection, report)
    }
}
