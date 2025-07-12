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

package io.rtron.transformer.modifiers.opendrive.cropper

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.some
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.geometry.euclidean.twod.surface.Polygon2D
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.model.opendrive.additions.optics.everyJunction
import io.rtron.model.opendrive.additions.optics.everyRoad

class OpendriveCropper(
    val parameters: OpendriveCropperParameters,
) {
    fun modify(opendriveModel: OpendriveModel): Pair<Option<OpendriveModel>, OpendriveCropperReport> {
        val report = OpendriveCropperReport(parameters)
        val modifiedOpendriveModel = opendriveModel.copy()
        modifiedOpendriveModel.updateAdditionalIdentifiers()

        val cropPolygon: Polygon2D =
            parameters.getPolygon().getOrElse {
                report.message = "No cropping polygon available."
                return modifiedOpendriveModel.some() to report
            }

        // remove all roads for which the reference line lies in the crop polygon
        val roadsFiltered =
            modifiedOpendriveModel.road.filter { currentRoad ->
                currentRoad.planView.geometry.any { cropPolygon.contains(Vector2D(it.x, it.y)) }
            }
        report.numberOfRoadsOriginally = modifiedOpendriveModel.road.size
        report.numberOfRoadsRemaining = roadsFiltered.size
        if (roadsFiltered.isEmpty()) {
            report.message = "No roads remaining in model after cropping."
            return None to report
        }
        modifiedOpendriveModel.road = roadsFiltered
        val remainingRoadIds: Set<String> = roadsFiltered.map { it.id }.toSet()

        // remove all the connections in the junctions, which have links to removed roads
        everyJunction.modify(modifiedOpendriveModel) { currentJunction ->
            val connectionsFiltered =
                currentJunction.connection.filter { currentConnection ->
                    currentConnection.incomingRoad.fold({ true }, { it in remainingRoadIds }) &&
                        currentConnection.connectingRoad.fold({ true }, { it in remainingRoadIds }) &&
                        currentConnection.linkedRoad.fold({ true }, { it in remainingRoadIds })
                }
            currentJunction.connection = connectionsFiltered
            currentJunction
        }
        // remove all the junctions with no connections left
        val junctionsFiltered = modifiedOpendriveModel.junction.filter { it.connection.isNotEmpty() }
        report.numberOfJunctionsOriginally = modifiedOpendriveModel.junction.size
        report.numberOfJunctionsRemaining = junctionsFiltered.size
        modifiedOpendriveModel.junction = junctionsFiltered
        val remainingJunctionsIds = modifiedOpendriveModel.junction.map { it.id }

        // remove roads belonging to a junction, which was removed
        val remainingJunctionIds = junctionsFiltered.map { it.id }.toSet()
        val roadsFilteredFiltered =
            roadsFiltered.filter { currentRoad ->
                currentRoad.getJunctionOption().fold({ true }, { remainingJunctionIds.contains(it) })
            }
        modifiedOpendriveModel.road = roadsFilteredFiltered

        // adjust the links of each road
        everyRoad.modify(modifiedOpendriveModel) { currentRoad ->

            currentRoad.link.onSome { currentLink ->
                // remove the predecessor link, if it is a junction, which is not contained in the remaining junctions
                if (currentLink.predecessor.isSome { currentPredecessor ->
                        currentPredecessor.getJunctionPredecessorSuccessor().isSome { it !in remainingJunctionsIds }
                    }
                ) {
                    currentLink.predecessor = None
                }

                // remove the predecessor link, if it is a road, which is not contained in the remaining roads
                if (currentLink.predecessor.isSome { currentPredecessor ->
                        currentPredecessor.getRoadPredecessorSuccessor().isSome { it.first !in remainingRoadIds }
                    }
                ) {
                    currentLink.predecessor = None
                }

                // remove the successor link, if it is a junction, which is not contained in the remaining junctions
                if (currentLink.successor.isSome { currentPredecessor ->
                        currentPredecessor.getJunctionPredecessorSuccessor().isSome { it !in remainingJunctionsIds }
                    }
                ) {
                    currentLink.successor = None
                }

                // remove the successor link, if it is a junction, which is not contained in the remaining junctions
                if (currentLink.successor.isSome { currentPredecessor ->
                        currentPredecessor.getRoadPredecessorSuccessor().isSome { it.first !in remainingRoadIds }
                    }
                ) {
                    currentLink.successor = None
                }
            }

            currentRoad
        }

        return modifiedOpendriveModel.some() to report
    }
}
