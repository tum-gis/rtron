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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules.objects

import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.io.report.merge
import io.rtron.io.report.sequenceContextReport
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadObjectIdentifier
import io.rtron.model.opendrive.objects.RoadObjectsObject
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.of

class RoadObjectsObjectEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(roadIdentifier: RoadIdentifier, roadObjectsObject: List<RoadObjectsObject>): Report =
        roadObjectsObject.map { evaluateFatalViolations(roadIdentifier, it) }.merge()

    fun evaluateNonFatalViolations(roadIdentifier: RoadIdentifier, roadObjectsObject: List<RoadObjectsObject>): ContextReport<List<RoadObjectsObject>> =
        roadObjectsObject.map { evaluateNonFatalViolations(roadIdentifier, it) }.sequenceContextReport()

    private fun evaluateFatalViolations(roadIdentifier: RoadIdentifier, roadObjectsObject: RoadObjectsObject): Report {
        val report = Report()

        return report
    }

    private fun evaluateNonFatalViolations(roadIdentifier: RoadIdentifier, roadObjectsObject: RoadObjectsObject): ContextReport<RoadObjectsObject> {
        val healedRoadObjectsObject = roadObjectsObject.copy()
        val report = Report()
        val roadObjectIdentifier = RoadObjectIdentifier(healedRoadObjectsObject.id, roadIdentifier)

        healedRoadObjectsObject.outlines.tap { currentRoadObjectOutline ->
            if (currentRoadObjectOutline.outline.any { it.isPolyhedron() && !it.isPolyhedronUniquelyDefined() }) {
                report += Message.of("An <outline> element shall be followed by one or more <cornerRoad> elements or by one or more <cornerLocal> element. Since both are defined, the <cornerLocal> elements are removed.", roadObjectIdentifier.toStringMap(), isFatal = true, wasHealed = true)
                currentRoadObjectOutline.outline.forEach { it.cornerLocal = emptyList() }
            }
        }

        if (healedRoadObjectsObject.height.isEmpty() && healedRoadObjectsObject.outlines.exists { it.containsPolyhedrons() }) {
            report += Message.of("Road object contains a polyhedron with non-zero height, but the height of the road object element is ${roadObjectsObject.height}.", roadObjectIdentifier.toStringMap(), isFatal = false, wasHealed = false)
        }

        return ContextReport(healedRoadObjectsObject, report)
    }
}
