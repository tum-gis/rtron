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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype

import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyJunction
import io.rtron.model.opendrive.additions.optics.everyJunctionConnection
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.modifiers.BasicDataTypeModifier
import io.rtron.transformer.issues.opendrive.of

object JunctionEvaluator {

    // Methods
    fun evaluate(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, issueList: DefaultIssueList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        everyJunction.modify(modifiedOpendriveModel) { currentJunction ->

            if (currentJunction.connection.isEmpty()) {
                issueList += DefaultIssue.of("EmptyList", "List for attribute 'connection' is empty, but it has to contain at least one element.", currentJunction.additionalId, Severity.FATAL_ERROR, wasFixed = false)
            }

            if (currentJunction.id.isBlank()) {
                issueList += DefaultIssue.of("MissingValue", "Missing value for attribute 'ID'.", currentJunction.additionalId, Severity.FATAL_ERROR, wasFixed = false)
            }

            currentJunction
        }

        everyJunctionConnection.modify(modifiedOpendriveModel) { currentJunctionConnection ->

            if (currentJunctionConnection.id.isBlank()) {
                issueList += DefaultIssue.of("MissingValue", "Missing value for attribute 'ID'.", currentJunctionConnection.additionalId, Severity.FATAL_ERROR, wasFixed = false)
            }

            currentJunctionConnection
        }

        modifiedOpendriveModel = everyJunction.modify(modifiedOpendriveModel) { currentJunction ->
            currentJunction.mainRoad = BasicDataTypeModifier.modifyToOptionalString(currentJunction.mainRoad, currentJunction.additionalId, "mainRoad", issueList)
            currentJunction.name = BasicDataTypeModifier.modifyToOptionalString(currentJunction.name, currentJunction.additionalId, "name", issueList)

            currentJunction.sEnd = BasicDataTypeModifier.modifyToOptionalFiniteDouble(currentJunction.sEnd, currentJunction.additionalId, "sEnd", issueList)
            currentJunction.sStart = BasicDataTypeModifier.modifyToOptionalFiniteDouble(currentJunction.sStart, currentJunction.additionalId, "sStart", issueList)

            currentJunction
        }

        modifiedOpendriveModel = everyJunctionConnection.modify(modifiedOpendriveModel) { currentJunctionConnection ->

            currentJunctionConnection.connectingRoad = BasicDataTypeModifier.modifyToOptionalString(currentJunctionConnection.connectingRoad, currentJunctionConnection.additionalId, "connectingRoad", issueList)
            currentJunctionConnection.incomingRoad = BasicDataTypeModifier.modifyToOptionalString(currentJunctionConnection.incomingRoad, currentJunctionConnection.additionalId, "incomingRoad", issueList)
            currentJunctionConnection.linkedRoad = BasicDataTypeModifier.modifyToOptionalString(currentJunctionConnection.linkedRoad, currentJunctionConnection.additionalId, "linkedRoad", issueList)

            currentJunctionConnection
        }

        return modifiedOpendriveModel
    }
}
