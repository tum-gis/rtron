/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.model.opendrive.road.objects

import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData

class RoadObjectsObjectOutlines(
    var outline: List<RoadObjectsObjectOutlinesOutline> = listOf(),

    var userData: List<UserData> = listOf(),
    var include: List<Include> = listOf(),
    var dataQuality: DataQuality = DataQuality()
) {

    // Methods
    fun getPolyhedronsDefinedByRoadCorners() = outline.filter { it.isPolyhedronDefinedByRoadCorners() }
    fun getPolyhedronsDefinedByLocalCorners() = outline.filter { it.isPolyhedronDefinedByLocalCorners() }

    fun getLinearRingsDefinedByRoadCorners() = outline.filter { it.isLinearRingDefinedByRoadCorners() }
    fun getLinearRingsDefinedByLocalCorners() = outline.filter { it.isLinearRingDefinedByLocalCorners() }

    fun numberOfPolyhedrons() = getPolyhedronsDefinedByRoadCorners().size + getPolyhedronsDefinedByLocalCorners().size
    fun numberOfLinearRings() = getLinearRingsDefinedByRoadCorners().size + getLinearRingsDefinedByLocalCorners().size

    fun containsPolyhedrons() = numberOfPolyhedrons() > 0
    fun containsLinearRings() = numberOfLinearRings() > 0

    fun containsGeometries() = containsPolyhedrons() || containsLinearRings()
}
