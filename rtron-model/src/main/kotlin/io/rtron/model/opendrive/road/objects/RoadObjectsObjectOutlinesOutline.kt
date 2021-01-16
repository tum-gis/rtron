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

package io.rtron.model.opendrive.road.objects

import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.ELaneType
import io.rtron.model.opendrive.common.EOutlineFillType
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData

class RoadObjectsObjectOutlinesOutline(
    var cornerRoad: List<RoadObjectsObjectOutlinesOutlineCornerRoad> = listOf(),
    var cornerLocal: List<RoadObjectsObjectOutlinesOutlineCornerLocal> = listOf(),

    var userData: List<UserData> = listOf(),
    var include: List<Include> = listOf(),
    var dataQuality: DataQuality = DataQuality(),

    var id: String = "",
    var fillType: EOutlineFillType = EOutlineFillType.UNKNOWN,
    var outer: Boolean = true,
    var closed: Boolean = true,
    var laneType: ELaneType = ELaneType.NONE
) {

    // Methods
    fun isPolyhedronUniquelyDefined() = (isPolyhedronDefinedByRoadCorners() && !isPolyhedronDefinedByLocalCorners()) ||
        (!isPolyhedronDefinedByRoadCorners() && isPolyhedronDefinedByLocalCorners())

    fun isLinearRingUniquelyDefined() = (isLinearRingDefinedByRoadCorners() && !isLinearRingDefinedByLocalCorners()) ||
        (!isLinearRingDefinedByRoadCorners() && isLinearRingDefinedByLocalCorners())

    /** Returns true, if the provided geometry information correspond to a polyhedron. */
    fun isPolyhedron() = isPolyhedronDefinedByRoadCorners() || isPolyhedronDefinedByLocalCorners()

    /** Returns true, if the provided geometry information correspond to a linear ring. */
    fun isLinearRing() = isLinearRingDefinedByRoadCorners() || isLinearRingDefinedByLocalCorners()

    fun isPolyhedronDefinedByRoadCorners() =
        cornerRoad.isNotEmpty() && cornerRoad.any { it.isSetBasePoint() && it.hasPositiveHeight() }
    fun isPolyhedronDefinedByLocalCorners() =
        cornerLocal.isNotEmpty() && cornerLocal.any { it.isSetBasePoint() && it.hasPositiveHeight() }

    fun isLinearRingDefinedByRoadCorners() =
        cornerRoad.isNotEmpty() && cornerRoad.all { it.isSetBasePoint() && it.hasZeroHeight() }
    fun isLinearRingDefinedByLocalCorners() =
        cornerLocal.isNotEmpty() && cornerLocal.all { it.isSetBasePoint() && it.hasZeroHeight() }
}
