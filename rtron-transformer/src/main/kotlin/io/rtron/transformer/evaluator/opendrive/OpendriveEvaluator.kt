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
import io.rtron.transformer.evaluator.opendrive.plans.basicdatatype.BasicDataTypeEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.conversionrequirements.ConversionRequirementsEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.modelingrules.ModelingRulesEvaluator
import io.rtron.transformer.evaluator.opendrive.report.OpendriveEvaluationReport

class OpendriveEvaluator(
    val parameters: OpendriveEvaluatorParameters
) {
    // Properties and Initializers
    private val _basicDataTypeEvaluator = BasicDataTypeEvaluator(parameters)
    private val _modelingRulesEvaluator = ModelingRulesEvaluator(parameters)
    private val _conversionRequirementsEvaluator = ConversionRequirementsEvaluator(parameters)

    // Methods

    fun evaluate(opendriveModel: OpendriveModel): Pair<Option<OpendriveModel>, OpendriveEvaluationReport> {
        opendriveModel.updateAdditionalIdentifiers()
        var healedOpendriveModel = opendriveModel.copy()

        val report = OpendriveEvaluationReport(parameters)

        // basic data type evaluation
        report.basicDataTypePlan = _basicDataTypeEvaluator.evaluateFatalViolations(healedOpendriveModel)
        if (report.basicDataTypePlan.isNotEmpty())
            return None to report

        healedOpendriveModel = _basicDataTypeEvaluator.evaluateNonFatalViolations(healedOpendriveModel)
            .let {
                report.basicDataTypePlan = it.messageList
                it.value
            }

        // modeling rules evaluation
        report.modelingRulesPlan = _modelingRulesEvaluator.evaluateFatalViolations(healedOpendriveModel)
        if (report.modelingRulesPlan.isNotEmpty())
            return None to report

        healedOpendriveModel = _modelingRulesEvaluator.evaluateNonFatalViolations(healedOpendriveModel)
            .let {
                report.modelingRulesPlan = it.messageList
                it.value
            }

        // conversion requirements evaluation
        report.conversionRequirementsPlan = _conversionRequirementsEvaluator.evaluateFatalViolations(healedOpendriveModel)
        if (report.conversionRequirementsPlan.isNotEmpty())
            return None to report
        healedOpendriveModel = _conversionRequirementsEvaluator.evaluateNonFatalViolations(healedOpendriveModel)
            .let {
                report.conversionRequirementsPlan = it.messageList
                it.value
            }

        return healedOpendriveModel.some() to report
    }
}
