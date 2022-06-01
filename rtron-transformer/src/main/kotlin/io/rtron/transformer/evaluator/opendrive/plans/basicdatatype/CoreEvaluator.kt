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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype

import arrow.core.None
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Report
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.additions.optics.everyHeaderOffset
import io.rtron.model.opendrive.header
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.toMessage

class CoreEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): Report {
        val report = Report()

        opendriveModel.roadValidated.tapInvalid {
            report += it.toMessage(emptyMap(), isFatal = true, wasHealed = false)
        }

        OpendriveModel.header.get(opendriveModel).also { header ->
            header.revMajorValidated.tapInvalid {
                report += it.toMessage(emptyMap(), isFatal = true, wasHealed = false)
            }

            header.revMinorValidated.tapInvalid {
                report += it.toMessage(emptyMap(), isFatal = true, wasHealed = false)
            }
        }

        return report
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextReport<OpendriveModel> {
        val report = Report()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = OpendriveModel.header.modify(healedOpendriveModel) { header ->
            if (header.name.exists { it.isEmpty() }) {
                report += OpendriveException.EmptyValueForOptionalAttribute("name").toMessage(emptyMap(), isFatal = false, wasHealed = true)
                header.name = None
            }

            if (header.date.exists { it.isEmpty() }) {
                report += OpendriveException.EmptyValueForOptionalAttribute("date").toMessage(emptyMap(), isFatal = false, wasHealed = true)
                header.date = None
            }

            if (header.vendor.exists { it.isEmpty() }) {
                report += OpendriveException.EmptyValueForOptionalAttribute("vendor").toMessage(emptyMap(), isFatal = false, wasHealed = true)
                header.vendor = None
            }

            if (header.north.exists { !it.isFinite() }) {
                report += OpendriveException.EmptyValueForOptionalAttribute("north").toMessage(emptyMap(), isFatal = false, wasHealed = true)
                header.north = None
            }

            header
        }

        healedOpendriveModel = everyHeaderOffset.modify(healedOpendriveModel) { currentHeaderOffset ->

            if (!currentHeaderOffset.x.isFinite()) {
                report += OpendriveException.UnexpectedValue("x", currentHeaderOffset.x.toString(), "Value should be finite.").toMessage(emptyMap(), isFatal = false, wasHealed = true)
                currentHeaderOffset.x = 0.0
            }

            if (!currentHeaderOffset.y.isFinite()) {
                report += OpendriveException.UnexpectedValue("y", currentHeaderOffset.y.toString(), "Value should be finite.").toMessage(emptyMap(), isFatal = false, wasHealed = true)
                currentHeaderOffset.y = 0.0
            }

            if (!currentHeaderOffset.z.isFinite()) {
                report += OpendriveException.UnexpectedValue("z", currentHeaderOffset.z.toString(), "Value should be finite.").toMessage(emptyMap(), isFatal = false, wasHealed = true)
                currentHeaderOffset.z = 0.0
            }

            if (!currentHeaderOffset.hdg.isFinite()) {
                report += OpendriveException.UnexpectedValue("hdg", currentHeaderOffset.hdg.toString(), "Value should be finite.").toMessage(emptyMap(), isFatal = false, wasHealed = true)
                currentHeaderOffset.hdg = 0.0
            }

            currentHeaderOffset
        }

        return ContextReport(healedOpendriveModel, report)
    }
}
