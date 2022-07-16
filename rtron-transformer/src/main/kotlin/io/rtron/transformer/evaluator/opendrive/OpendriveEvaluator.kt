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

package io.rtron.transformer.evaluator.opendrive

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.plans.basicdatatype.BasicDataTypeEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.conversionrequirements.ConversionRequirementsEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.modelingrules.ModelingRulesEvaluator
import io.rtron.transformer.evaluator.opendrive.report.OpendriveEvaluationReport

class OpendriveEvaluator(
    val configuration: OpendriveEvaluatorConfiguration
) {
    // Properties and Initializers
    private val _basicDataTypeEvaluator = BasicDataTypeEvaluator(configuration)
    private val _modelingRulesEvaluator = ModelingRulesEvaluator(configuration)
    private val _conversionRequirementsEvaluator = ConversionRequirementsEvaluator(configuration)

    // Methods

    fun evaluate(opendriveModel: OpendriveModel): Pair<Option<OpendriveModel>, OpendriveEvaluationReport> {
        opendriveModel.updateAdditionalIdentifiers()
        var healedOpendriveModel = opendriveModel.copy()

        val report = OpendriveEvaluationReport()

        // basic data type evaluation
        report.basicDataTypeEvaluation = _basicDataTypeEvaluator.evaluateFatalViolations(healedOpendriveModel)
        if (report.basicDataTypeEvaluation.isNotEmpty())
            return None to report

        healedOpendriveModel = _basicDataTypeEvaluator.evaluateNonFatalViolations(healedOpendriveModel)
            .let {
                report.basicDataTypeEvaluation = it.messageList
                it.value
            }

        // modeling rules evaluation
        report.modelingRulesEvaluation = _modelingRulesEvaluator.evaluateFatalViolations(healedOpendriveModel)
        if (report.modelingRulesEvaluation.isNotEmpty())
            return None to report

        healedOpendriveModel = _modelingRulesEvaluator.evaluateNonFatalViolations(healedOpendriveModel)
            .let {
                report.modelingRulesEvaluation = it.messageList
                it.value
            }

        // conversion requirements evaluation
        report.conversionRequirementsEvaluation = _conversionRequirementsEvaluator.evaluateFatalViolations(healedOpendriveModel)
        if (report.conversionRequirementsEvaluation.isNotEmpty())
            return None to report
        healedOpendriveModel = _conversionRequirementsEvaluator.evaluateNonFatalViolations(healedOpendriveModel)
            .let {
                report.conversionRequirementsEvaluation = it.messageList
                it.value
            }

        return healedOpendriveModel.some() to report
    }
}
