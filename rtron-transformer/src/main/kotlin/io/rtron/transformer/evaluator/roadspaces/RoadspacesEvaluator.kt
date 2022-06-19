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

import arrow.core.Either
import arrow.core.continuations.either
import io.rtron.io.logging.LogManager
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.transformer.evaluator.roadspaces.configuration.RoadspacesEvaluatorConfiguration
import io.rtron.transformer.evaluator.roadspaces.plans.modelingrules.ModelingRulesEvaluator
import kotlin.io.path.Path

class RoadspacesEvaluator(
    val configuration: RoadspacesEvaluatorConfiguration
) {
    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    // private val _basicDataTypeEvaluator = BasicDataTypeEvaluator(configuration)
    private val _modelingRulesEvaluator = ModelingRulesEvaluator(configuration)
    // private val _conversionRequirementsEvaluator = ConversionRequirementsEvaluator(configuration)

    // Methods

    fun evaluate(roadspacesModel: RoadspacesModel): Either<RoadspacesEvaluatorException, RoadspacesModel> = either.eager {

        val modelingRulesReportFilePath = configuration.outputReportDirectoryPath.resolve(Path("reports/evaluator/roadspaces/modelingRulesEvaluationReport.json"))
        _modelingRulesEvaluator.evaluateNonFatalViolations(roadspacesModel).write(modelingRulesReportFilePath)

        roadspacesModel
    }
}
