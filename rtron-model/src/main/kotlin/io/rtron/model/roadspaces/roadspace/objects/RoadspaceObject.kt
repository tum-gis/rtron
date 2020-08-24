/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList


/**
 * Represents an object within the road space.
 *
 * @param geometry geometry of the road space object
 * @param attributes attributes containing information about the road space object
 */
data class RoadspaceObject(
        val id: RoadspaceObjectIdentifier,
        val type: RoadObjectType = RoadObjectType.NONE,
        val geometry: List<AbstractGeometry3D> = listOf(),
        val attributes: AttributeList
) {

    // Properties and Initializers
    val name get() = id.roadspaceObjectName

    // Conversions
    override fun toString(): String {
        return "RoadObject(attributes=$attributes, geometry=$geometry)"
    }
}
