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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype.objects

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Report
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
import io.rtron.model.opendrive.objects.RoadObjects
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration

class RoadObjectsEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Properties and Initializers
    private val _roadObjectsObjectEvaluator = RoadObjectsObjectEvaluator(configuration)

    // Methods
    fun evaluateFatalViolations(roadIdentifier: RoadIdentifier, roadObjects: Option<RoadObjects>): Report =
        roadObjects.fold({ Report() }, { evaluateFatalViolations(roadIdentifier, it) })

    fun evaluateNonFatalViolations(roadIdentifier: RoadIdentifier, roadObjects: Option<RoadObjects>): ContextReport<Option<RoadObjects>> =
        roadObjects.fold({ ContextReport(None, Report()) }, { currentRoadObjects ->
            evaluateNonFatalViolations(roadIdentifier, currentRoadObjects).map { Some(it) }
        })

    private fun evaluateFatalViolations(roadIdentifier: RoadIdentifier, roadObjects: RoadObjects): Report {
        val report = Report()

        report += _roadObjectsObjectEvaluator.evaluateFatalViolations(roadIdentifier, roadObjects.roadObject)

        return report
    }

    private fun evaluateNonFatalViolations(roadIdentifier: RoadIdentifier, roadObjects: RoadObjects): ContextReport<RoadObjects> {
        val healedRoadObjects = roadObjects.copy()
        val report = Report()

        _roadObjectsObjectEvaluator.evaluateNonFatalViolations(roadIdentifier, healedRoadObjects.roadObject).let {
            report += it.report
            healedRoadObjects.roadObject = it.value
        }

        return ContextReport(healedRoadObjects, report)
    }
}
