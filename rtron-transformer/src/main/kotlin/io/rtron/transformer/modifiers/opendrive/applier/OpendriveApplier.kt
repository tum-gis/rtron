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

package io.rtron.transformer.modifiers.opendrive.applier

import arrow.core.getOrNone
import arrow.core.some
import arrow.core.toOption
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoad

class OpendriveApplier(
    val parameters: OpendriveApplierParameters,
) {
    // Properties and Initializers

    // Methods
    fun modify(
        opendriveModel: OpendriveModel,
        rules: OpendriveApplierRules,
    ): Pair<OpendriveModel, OpendriveApplierReport> {
        val report = OpendriveApplierReport(parameters)

        everyRoad.modify(opendriveModel) { currentRoad ->

            rules.roads.getOrNone(currentRoad.id).onSome { currentRoadRule ->
                currentRoadRule.name.toOption().onSome { currentRoad.name = it.some() }
            }

            currentRoad
        }

        return opendriveModel to report
    }
}
