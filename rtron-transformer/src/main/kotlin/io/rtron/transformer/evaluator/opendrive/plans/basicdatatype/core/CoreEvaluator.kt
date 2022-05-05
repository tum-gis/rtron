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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype.core

import io.rtron.io.report.ContextReport
import io.rtron.io.report.Report
import io.rtron.model.opendrive.core.Header
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.toReport

class CoreEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(header: Header): Report {
        val report = Report()
        return report
    }

    fun evaluateNonFatalViolations(header: Header): ContextReport<Header> {
        val healedHeader = header.copy()
        val report = Report()

        report += healedHeader.healMinorViolations().toReport(header, isFatal = false, wasHealed = true)

        return ContextReport(header, report)
    }
}
