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

package io.rtron.model.roadspaces.common

import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier

/**
 * Represents a filler surface with it's [surface] geometry and information from which [fromLaneId] to which
 * [toLaneId] it fills.
 */
sealed class FillerSurface(
    val fromLaneId: LaneIdentifier,
    val toLaneId: LaneIdentifier,
    val surface: AbstractSurface3D
) {
    // Properties and Initializers

    init {
        require(fromLaneId != toLaneId) { "Lane identifier and lane identifier of successor must not be the same." }
    }
}

class LateralFillerSurface(fromLaneId: LaneIdentifier, toLaneId: LaneIdentifier, surface: AbstractSurface3D) : FillerSurface(fromLaneId, toLaneId, surface) {

    // Properties and Initializers
    init {
        require(fromLaneId.isLaterallyAdjacent(toLaneId)) { "Lane identifiers must be laterally adjacent." }
    }
}

class LongitudinalFillerSurfaceWithinRoad(fromLaneId: LaneIdentifier, toLaneId: LaneIdentifier, surface: AbstractSurface3D) : FillerSurface(fromLaneId, toLaneId, surface) {

    // Properties and Initializers
    init {
        require(fromLaneId.isWithinSameRoad(toLaneId)) { "Lane identifiers must be located within the same road." }
    }
}

class LongitudinalFillerSurfaceBetweenRoads(fromLaneId: LaneIdentifier, toLaneId: LaneIdentifier, surface: AbstractSurface3D) : FillerSurface(fromLaneId, toLaneId, surface) {

    // Properties and Initializers
    init {
        require(!fromLaneId.isWithinSameRoad(toLaneId)) { "Lane identifiers must not be located within the same road." }
    }
}
