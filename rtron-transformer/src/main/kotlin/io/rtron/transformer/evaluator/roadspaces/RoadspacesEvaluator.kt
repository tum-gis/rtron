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

package io.rtron.transformer.evaluator.roadspaces

import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.transformer.evaluator.roadspaces.plans.modelingrules.ModelingRulesEvaluator
import io.rtron.transformer.evaluator.roadspaces.report.RoadspacesEvaluationReport

class RoadspacesEvaluator(
    val parameters: RoadspacesEvaluatorParameters
) {
    // Properties and Initializers
    private val _modelingRulesEvaluator = ModelingRulesEvaluator(parameters)

    // Methods

    fun evaluate(roadspacesModel: RoadspacesModel): Pair<RoadspacesModel, RoadspacesEvaluationReport> {

        val report = RoadspacesEvaluationReport(parameters)

        report.modelingRulesEvaluation = _modelingRulesEvaluator.evaluateNonFatalViolations(roadspacesModel)

        return roadspacesModel to report
    }
}
