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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules

import arrow.core.None
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyJunction
import io.rtron.model.opendrive.junction.EJunctionType
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.report.of

class JunctionEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): Report {
        val report = Report()
        return report
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextReport<OpendriveModel> {
        val report = Report()
        var healedOpendriveModel = opendriveModel.copy()

        healedOpendriveModel = everyJunction.modify(healedOpendriveModel) { currentJunction ->

            // Junctions should not be used when only two roads meet.
            if (currentJunction.typeValidated == EJunctionType.DEFAULT && currentJunction.getNumberOfIncomingRoads() <= 2) {
                report += Message.of("Junctions of type default should only be used when at least three roads are coming in (currently incoming road ids: ${currentJunction.getIncomingRoadIds()})", currentJunction.additionalId, isFatal = false, wasHealed = false)
            }

            // The @mainRoad, @orientation, @sStart and @sEnd attributes shall only be specified for virtual junctions.
            if (currentJunction.typeValidated != EJunctionType.VIRTUAL) {
                currentJunction.mainRoad.tap {
                    report += Message.of("Attribute 'mainRoad' shall only be specified for virtual junctions", currentJunction.additionalId, isFatal = true, wasHealed = true)
                    currentJunction.mainRoad = None
                }

                currentJunction.orientation.tap {
                    report += Message.of("Attribute 'orientation' shall only be specified for virtual junctions", currentJunction.additionalId, isFatal = true, wasHealed = true)
                    currentJunction.orientation = None
                }

                currentJunction.sStart.tap {
                    report += Message.of("Attribute 'sStart' shall only be specified for virtual junctions", currentJunction.additionalId, isFatal = true, wasHealed = true)
                    currentJunction.sStart = None
                }

                currentJunction.sEnd.tap {
                    report += Message.of("Attribute 'sEnd' shall only be specified for virtual junctions", currentJunction.additionalId, isFatal = true, wasHealed = true)
                    currentJunction.sEnd = None
                }
            }

            currentJunction
        }

        return ContextReport(healedOpendriveModel, report)
    }
}
