/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssueList
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.plans.AbstractOpendriveEvaluator

class ModelingRulesEvaluator(val parameters: OpendriveEvaluatorParameters) : AbstractOpendriveEvaluator() {

    // Methods
    override fun evaluate(opendriveModel: OpendriveModel): ContextIssueList<OpendriveModel> {
        val issueList = DefaultIssueList()
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel = RoadEvaluator.evaluate(modifiedOpendriveModel, parameters, issueList)
        modifiedOpendriveModel = RoadLanesEvaluator.evaluate(modifiedOpendriveModel, parameters, issueList)
        modifiedOpendriveModel = RoadObjectsEvaluator.evaluate(modifiedOpendriveModel, parameters, issueList)
        modifiedOpendriveModel = RoadSignalsEvaluator.evaluate(modifiedOpendriveModel, parameters, issueList)
        modifiedOpendriveModel = JunctionEvaluator.evaluate(modifiedOpendriveModel, parameters, issueList)

        return ContextIssueList(modifiedOpendriveModel, issueList)
    }
}
