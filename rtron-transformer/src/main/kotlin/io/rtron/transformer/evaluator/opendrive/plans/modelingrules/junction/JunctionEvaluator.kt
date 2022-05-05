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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules.junction

import arrow.core.None
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.io.report.sequenceContextReport
import io.rtron.model.opendrive.additions.identifier.JunctionIdentifier
import io.rtron.model.opendrive.junction.EJunctionType
import io.rtron.model.opendrive.junction.Junction
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.of

class JunctionEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(junctions: List<Junction>): Report {
        val report = Report()
        return report
    }

    fun evaluateNonFatalViolations(junctions: List<Junction>): ContextReport<List<Junction>> =
        junctions.map { evaluateNonFatalViolations(it) }.sequenceContextReport()

    private fun evaluateNonFatalViolations(junction: Junction): ContextReport<Junction> {
        val healedJunction = junction.copy()
        val report = Report()
        val junctionIdentifier = JunctionIdentifier(junction.id).toStringMap()

        // Junctions should not be used when only two roads meet.
        if (junction.typeValidated == EJunctionType.DEFAULT && junction.getNumberOfIncomingRoads() <= 2) {
            report += Message.of("Junctions of type default should only be used when at least three roads are coming in (currently incoming road ids: ${junction.getIncomingRoadIds()})", junctionIdentifier, isFatal = false, wasHealed = false)
        }

        // The @mainRoad, @orientation, @sStart and @sEnd attributes shall only be specified for virtual junctions.
        if (junction.typeValidated != EJunctionType.VIRTUAL) {
            junction.mainRoad.tap {
                junction.mainRoad = None
                report += Message.of("Attribute 'mainRoad' shall only be specified for virtual junctions", junctionIdentifier, isFatal = true, wasHealed = true)
            }

            junction.orientation.tap {
                junction.orientation = None
                report += Message.of("Attribute 'orientation' shall only be specified for virtual junctions", junctionIdentifier, isFatal = true, wasHealed = true)
            }

            junction.sStart.tap {
                junction.sStart = None
                report += Message.of("Attribute 'sStart' shall only be specified for virtual junctions", junctionIdentifier, isFatal = true, wasHealed = true)
            }

            junction.sEnd.tap {
                junction.sEnd = None
                report += Message.of("Attribute 'sEnd' shall only be specified for virtual junctions", junctionIdentifier, isFatal = true, wasHealed = true)
            }
        }

        return ContextReport(healedJunction, report)
    }
}
