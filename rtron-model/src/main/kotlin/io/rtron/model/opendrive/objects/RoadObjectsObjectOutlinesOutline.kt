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

package io.rtron.model.opendrive.objects

import arrow.core.None
import arrow.core.Option
import arrow.optics.optics
import io.rtron.model.opendrive.additions.identifier.AdditionalRoadObjectOutlineIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadObjectOutlineIdentifier
import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.model.opendrive.lane.ELaneType

@optics
data class RoadObjectsObjectOutlinesOutline(
    var cornerRoad: List<RoadObjectsObjectOutlinesOutlineCornerRoad> = emptyList(),
    var cornerLocal: List<RoadObjectsObjectOutlinesOutlineCornerLocal> = emptyList(),

    var closed: Option<Boolean> = None,
    var fillType: Option<EOutlineFillType> = None,
    var id: Option<Int> = None,
    var laneType: Option<ELaneType> = None,
    var outer: Option<Boolean> = None,

    override var additionalId: Option<RoadObjectOutlineIdentifier> = None
) : OpendriveElement(), AdditionalRoadObjectOutlineIdentifier {

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
        cornerRoad.isNotEmpty() && cornerRoad.any { it.hasPositiveHeight() }
    fun isPolyhedronDefinedByLocalCorners() =
        cornerLocal.isNotEmpty() && cornerLocal.any { it.hasPositiveHeight() }

    fun isLinearRingDefinedByRoadCorners() =
        cornerRoad.isNotEmpty() && cornerRoad.all { it.hasZeroHeight() }
    fun isLinearRingDefinedByLocalCorners() =
        cornerLocal.isNotEmpty() && cornerLocal.all { it.hasZeroHeight() }

    companion object
}
