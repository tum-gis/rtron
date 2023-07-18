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

package io.rtron.model.roadspaces.roadspace.objects

import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.LateralLaneRangeIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceObjectIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList

/**
 * Represents an object within the road space.
 *
 * @param geometry geometry of the road space object
 * @param laneRelations object relations to road lanes
 * @param attributes attributes containing information about the road space object
 */
data class RoadspaceObject(
    val id: RoadspaceObjectIdentifier,
    val type: RoadObjectType = RoadObjectType.NONE,
    val geometry: AbstractGeometry3D,
    val laneRelations: List<LateralLaneRangeIdentifier>,
    val attributes: AttributeList
) {

    // Properties and Initializers
    val name get() = id.roadspaceObjectName

    // Methods

    /** Returns true, if the lane with [laneIdentifier] is related to this object. */
    fun isRelatedToLane(laneIdentifier: LaneIdentifier) = laneRelations.any { it.contains(laneIdentifier) }

    // Conversions
    override fun toString(): String {
        return "RoadObject(attributes=$attributes, geometry=$geometry)"
    }
}
