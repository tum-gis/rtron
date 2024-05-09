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

package io.rtron.transformer.evaluator.opendrive.plans.conversionrequirements

import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyJunction
import io.rtron.model.opendrive.junction.EJunctionType
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.issues.opendrive.of

object JunctionEvaluator {
    // Methods
    fun evaluate(
        opendriveModel: OpendriveModel,
        parameters: OpendriveEvaluatorParameters,
        issueList: DefaultIssueList,
    ): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel =
            everyJunction.modify(modifiedOpendriveModel) { currentJunction ->

                if (currentJunction.typeValidated == EJunctionType.DEFAULT &&
                    currentJunction.connection.any { it.incomingRoad.isNone() }
                ) {
                    issueList +=
                        DefaultIssue.of(
                            "DefaultJunctionWithoutIncomingRoad",
                            "Junction of type default has no connection with an incoming road.",
                            currentJunction.additionalId, Severity.FATAL_ERROR, wasFixed = false,
                        )
                }

                if (currentJunction.typeValidated == EJunctionType.DEFAULT &&
                    currentJunction.connection.any { it.connectingRoad.isNone() }
                ) {
                    issueList +=
                        DefaultIssue.of(
                            "DefaultJunctionWithoutConnectingRoad",
                            "Junction of type default has no connection with a connecting road.",
                            currentJunction.additionalId, Severity.FATAL_ERROR, wasFixed = false,
                        )
                }

                currentJunction
            }

        return modifiedOpendriveModel
    }
}
