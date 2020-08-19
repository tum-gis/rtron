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

package io.rtron.model.roadspaces.roadspace

import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.road.Road


/**
 * A [Roadspace] is defined along a [referenceLine] and contains the [roadspaceObjects] belonging to the road space.
 */
data class Roadspace (
        val id: RoadspaceIdentifier,
        val length: Double = Double.NaN,
        val junction: String = "",

        val referenceLine: Curve3D,
        val road: Road,
        val roadspaceObjects: List<RoadspaceObject> = emptyList(),
        val attributes: AttributeList = AttributeList()
)
