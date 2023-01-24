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
import io.rtron.io.messages.containsFatalErrors
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.transformer.evaluator.opendrive.plans.basicdatatype.BasicDataTypeEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.conversionrequirements.ConversionRequirementsEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.modelingrules.ModelingRulesEvaluator
import io.rtron.transformer.evaluator.opendrive.report.OpendriveEvaluationReport
import mu.KotlinLogging

class OpendriveEvaluator(
    val parameters: OpendriveEvaluatorParameters
) {
    // Properties and Initializers
    private val logger = KotlinLogging.logger {}

    private val basicDataTypeEvaluator = BasicDataTypeEvaluator(parameters)
    private val modelingRulesEvaluator = ModelingRulesEvaluator(parameters)
    private val conversionRequirementsEvaluator = ConversionRequirementsEvaluator(parameters)

    // Methods

    fun evaluate(opendriveModel: OpendriveModel): Pair<Option<OpendriveModel>, OpendriveEvaluationReport> {
        logger.info("Parameters: $parameters.")

        opendriveModel.updateAdditionalIdentifiers()
        var modifiedOpendriveModel = opendriveModel.copy()

        val report = OpendriveEvaluationReport(parameters)

        // basic data type evaluation
        basicDataTypeEvaluator.evaluate(modifiedOpendriveModel).let {
            report.basicDataTypePlan = it.messageList
            modifiedOpendriveModel = it.value
        }
        if (report.basicDataTypePlan.containsFatalErrors())
            return None to report

        // modeling rules evaluation
        modelingRulesEvaluator.evaluate(modifiedOpendriveModel).let {
            report.modelingRulesPlan = it.messageList
            modifiedOpendriveModel = it.value
        }
        if (report.modelingRulesPlan.containsFatalErrors())
            return None to report

        // conversion requirements evaluation
        conversionRequirementsEvaluator.evaluate(modifiedOpendriveModel).let {
            report.conversionRequirementsPlan = it.messageList
            modifiedOpendriveModel = it.value
        }
        if (report.conversionRequirementsPlan.containsFatalErrors())
            return None to report

        return modifiedOpendriveModel.some() to report
    }
}
