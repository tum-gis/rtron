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

package io.rtron.transformer.evaluator.opendrive.modifiers

import arrow.core.None
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.identifier.JunctionIdentifier
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.model.opendrive.additions.optics.everyRoadLink

object JunctionModifier {
    fun removeJunction(
        opendriveModel: OpendriveModel,
        id: JunctionIdentifier,
    ): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        // remove the junction itself
        modifiedOpendriveModel.junction = modifiedOpendriveModel.junction.filter { it.id != id.junctionId }

        // remove junction references in each road
        everyRoad.modify(modifiedOpendriveModel) { currentRoad ->
            if (currentRoad.junction == id.junctionId) {
                currentRoad.junction = ""
            }

            currentRoad
        }

        // remove links to junction to be deleted
        everyRoadLink.modify(modifiedOpendriveModel) { currentLink ->

            // remove the predecessor link, if it is the junction to be deleted
            if (currentLink.predecessor.isSome { currentPredecessor ->
                    currentPredecessor.getJunctionPredecessorSuccessor().isSome { it == id.junctionId }
                }
            ) {
                currentLink.predecessor = None
            }

            // remove the successor link, if it is the junction to be deleted
            if (currentLink.successor.isSome { currentSuccessor ->
                    currentSuccessor.getJunctionPredecessorSuccessor().isSome { it == id.junctionId }
                }
            ) {
                currentLink.successor = None
            }

            currentLink
        }

        return modifiedOpendriveModel
    }
}
