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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules

import arrow.core.Some
import arrow.core.flattenOption
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.model.opendrive.additions.optics.everyRoadObject
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.messages.opendrive.of

object RoadObjectsEvaluator {

    // Methods
    fun evaluate(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        modifiedOpendriveModel = everyRoad.modify(modifiedOpendriveModel) { currentRoad ->

            currentRoad.objects.tap { currentRoadObjects ->
                val roadObjectsFiltered = currentRoadObjects.roadObject.filter { it.s <= currentRoad.length + parameters.numberTolerance }
                if (currentRoadObjects.roadObject.size > roadObjectsFiltered.size) {
                    messageList += DefaultMessage.of("RoadObjectPositionNotInSValueRange", "Road object (number of objects affected: ${currentRoadObjects.roadObject.size - roadObjectsFiltered.size}) were removed since they were positioned outside the defined length of the road.", currentRoad.additionalId, Severity.ERROR, wasFixed = true)
                }
                currentRoadObjects.roadObject = roadObjectsFiltered
            }

            currentRoad
        }

        modifiedOpendriveModel = everyRoadObject.modify(modifiedOpendriveModel) { currentRoadObject ->

            // adding ids for outline elements
            currentRoadObject.outlines.tap { currentOutlinesElement ->
                val outlineElementsWithoutId = currentOutlinesElement.outline.filter { it.id.isEmpty() }

                if (outlineElementsWithoutId.isNotEmpty()) {
                    val startId: Int = currentOutlinesElement.outline.map { it.id }.flattenOption().maxOrNull() ?: 0
                    messageList += DefaultMessage.of("MissingValue", "Missing value for attribute 'id'.", currentRoadObject.additionalId, Severity.FATAL_ERROR, wasFixed = true)
                    outlineElementsWithoutId.forEachIndexed { index, outlineElement -> outlineElement.id = Some(startId + index) }
                }
            }

            currentRoadObject.outlines.tap { currentRoadObjectOutline ->
                if (currentRoadObjectOutline.outline.any { it.isPolyhedron() && !it.isPolyhedronUniquelyDefined() }) {
                    messageList += DefaultMessage.of("", "An <outline> element shall be followed by one or more <cornerRoad> elements or by one or more <cornerLocal> element. Since both are defined, the <cornerLocal> elements are removed.", currentRoadObject.additionalId, Severity.FATAL_ERROR, wasFixed = true)
                    currentRoadObjectOutline.outline.forEach { it.cornerLocal = emptyList() }
                }
            }

            if (currentRoadObject.height.isEmpty() && currentRoadObject.outlines.exists { it.containsPolyhedrons() }) {
                messageList += DefaultMessage.of("", "Road object contains a polyhedron with non-zero height, but the height of the road object element is ${currentRoadObject.height}.", currentRoadObject.additionalId, Severity.WARNING, wasFixed = false)
            }

            val repeatElementsFiltered = currentRoadObject.repeat.filter { it.length >= parameters.numberTolerance }
            if (repeatElementsFiltered.size < currentRoadObject.repeat.size) {
                // TODO: double check handling
                messageList += DefaultMessage.of("", "A repeat element should have a length higher than zero and threshold.", currentRoadObject.additionalId, Severity.FATAL_ERROR, wasFixed = true)
                currentRoadObject.repeat = repeatElementsFiltered
            }

            currentRoadObject
        }

        return modifiedOpendriveModel
    }
}
