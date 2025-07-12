/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.modifiers.opendrive.remover

import arrow.core.flattenOption
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.model.opendrive.additions.optics.everyRoadObjectContainer
import io.rtron.model.opendrive.objects.EObjectType

class OpendriveObjectRemover(
    val parameters: OpendriveObjectRemoverParameters,
) {
    fun modify(opendriveModel: OpendriveModel): Pair<OpendriveModel, OpendriveObjectRemoverReport> {
        val report = OpendriveObjectRemoverReport(parameters)
        var modifiedOpendriveModel = opendriveModel.copy()
        modifiedOpendriveModel.updateAdditionalIdentifiers()

        if (parameters.removeRoadObjectsWithoutType) {
            modifiedOpendriveModel =
                everyRoadObjectContainer.modify(modifiedOpendriveModel) { currentRoadObjectContainer ->
                    val numberOfRoadObjectsBefore = currentRoadObjectContainer.roadObject.count()

                    currentRoadObjectContainer.roadObject = currentRoadObjectContainer.roadObject.filter { it.type.isSome() }

                    report.numberOfRemovedRoadObjectsWithoutType +=
                        currentRoadObjectContainer.roadObject.count() - numberOfRoadObjectsBefore

                    currentRoadObjectContainer
                }
        }

        if (parameters.removeRoadObjectsOfTypes.isNotEmpty()) {
            modifiedOpendriveModel =
                everyRoadObjectContainer.modify(modifiedOpendriveModel) { currentRoadObjectContainer ->

                    val numberOfRoadObjectsBefore: Map<EObjectType, Int> =
                        currentRoadObjectContainer.roadObject
                            .map { it.type }
                            .flattenOption()
                            .groupingBy { it }
                            .eachCount()

                    currentRoadObjectContainer.roadObject =
                        currentRoadObjectContainer.roadObject
                            .filter { currentRoadObject ->
                                currentRoadObject.type.fold({ true }, { !parameters.removeRoadObjectsOfTypes.contains(it) })
                            }

                    val roadObjectTypeCountAfter: Map<EObjectType, Int> =
                        currentRoadObjectContainer.roadObject
                            .map { it.type }
                            .flattenOption()
                            .groupingBy { it }
                            .eachCount()

                    numberOfRoadObjectsBefore
                        .map { it.key to it.value - roadObjectTypeCountAfter.getOrDefault(it.key, 0) }
                        .filter { it.second != 0 }
                        .forEach {
                            report.numberOfRemovedRoadObjectsWithType[it.first] =
                                report.numberOfRemovedRoadObjectsWithType.getOrDefault(it.first, 0) + it.second
                        }

                    currentRoadObjectContainer
                }
        }

        return modifiedOpendriveModel to report
    }
}
