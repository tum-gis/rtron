/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.evaluator.opendrive.report

import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.containsFatalErrors
import io.rtron.io.messages.getTextSummary
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import kotlinx.serialization.Serializable

@Serializable
data class OpendriveEvaluationReport(
    val parameters: OpendriveEvaluatorParameters,

    var basicDataTypePlan: DefaultMessageList = DefaultMessageList(),
    var modelingRulesPlan: DefaultMessageList = DefaultMessageList(),
    var conversionRequirementsPlan: DefaultMessageList = DefaultMessageList()
) {

    /**
     * Returns a summary of the message numbers depending on the severity.
     */
    fun getTextSummary(): String =
        "Basic data type plan ${basicDataTypePlan.getTextSummary()}, " +
            "modeling rules plan: ${modelingRulesPlan.getTextSummary()}, " +
            "conversion requirements plan: ${conversionRequirementsPlan.getTextSummary()}"

    fun containsFatalErrors(): Boolean = basicDataTypePlan.containsFatalErrors() ||
        modelingRulesPlan.containsFatalErrors() || conversionRequirementsPlan.containsFatalErrors()
}
