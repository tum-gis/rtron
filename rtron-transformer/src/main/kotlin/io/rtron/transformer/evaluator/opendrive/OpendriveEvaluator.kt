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

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.plans.basicdatatype.BasicDataTypeEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.conversionrequirements.ConversionRequirementsEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.modelingrules.ModelingRulesEvaluator

class OpendriveEvaluator(
    val configuration: OpendriveEvaluatorConfiguration
) {
    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    private val _basicDataTypeEvaluator = BasicDataTypeEvaluator(configuration)
    private val _modelingRulesEvaluator = ModelingRulesEvaluator(configuration)
    private val _conversionRequirementsEvaluator = ConversionRequirementsEvaluator(configuration)

    // Methods

    fun evaluate(opendriveModel: OpendriveModel): Either<OpendriveEvaluatorException, OpendriveModel> = either.eager {
        opendriveModel.updateAdditionalIdentifiers()
        var healedOpendriveModel = opendriveModel.copy()

        val basicDataTypeReportFilePath =
            configuration.outputReportDirectoryPath.resolve(Path("reports/evaluator/opendrive/basicDataTypeEvaluationReport.json"))
        val basicDataTypeFatalViolationsReport = _basicDataTypeEvaluator.evaluateFatalViolations(healedOpendriveModel)
        if (basicDataTypeFatalViolationsReport.isNotEmpty()) {
            basicDataTypeFatalViolationsReport.write(basicDataTypeReportFilePath)
            _reportLogger.warn("Basic data types evaluator found ${basicDataTypeFatalViolationsReport.getTextSummary()}.")
            OpendriveEvaluatorException.FatalError("Basic data types evaluator detected fatal errors.").left()
                .bind<OpendriveEvaluatorException.FatalError>()
        }
        healedOpendriveModel = _basicDataTypeEvaluator
            .evaluateNonFatalViolations(healedOpendriveModel)
            .let {
                it.report.write(basicDataTypeReportFilePath)
                it.value
            }

        val modelingRulesReportFilePath =
            configuration.outputReportDirectoryPath.resolve(Path("reports/evaluator/opendrive/modelingRulesEvaluationReport.json"))
        val modelingRulesFatalViolationsReport =
            _modelingRulesEvaluator.evaluateFatalViolations(healedOpendriveModel)
        if (modelingRulesFatalViolationsReport.isNotEmpty()) {
            modelingRulesFatalViolationsReport.write(modelingRulesReportFilePath)
            _reportLogger.warn("Modeling rules evaluator found ${modelingRulesFatalViolationsReport.getTextSummary()}.")
            OpendriveEvaluatorException.FatalError("Modeling rules evaluator detected fatal errors.").left()
                .bind<OpendriveEvaluatorException.FatalError>()
        }
        healedOpendriveModel = _modelingRulesEvaluator
            .evaluateNonFatalViolations(healedOpendriveModel)
            .let {
                it.report.write(modelingRulesReportFilePath)
                it.value
            }

        val conversionRequirementsReportFilePath =
            configuration.outputReportDirectoryPath.resolve(Path("reports/evaluator/opendrive/conversionRequirementsEvaluationReport.json"))
        val conversionRequirementsFatalViolationsReport =
            _conversionRequirementsEvaluator.evaluateFatalViolations(healedOpendriveModel)
        if (conversionRequirementsFatalViolationsReport.isNotEmpty()) {
            conversionRequirementsFatalViolationsReport.write(modelingRulesReportFilePath)
            _reportLogger.warn("Conversion requirements evaluator found ${conversionRequirementsFatalViolationsReport.getTextSummary()}.")
            OpendriveEvaluatorException.FatalError("Conversion requirements evaluator detected fatal errors.").left()
                .bind<OpendriveEvaluatorException.FatalError>()
        }
        healedOpendriveModel = _conversionRequirementsEvaluator
            .evaluateNonFatalViolations(healedOpendriveModel)
            .let {
                it.report.write(conversionRequirementsReportFilePath)
                it.value
            }

        healedOpendriveModel
    }
}
