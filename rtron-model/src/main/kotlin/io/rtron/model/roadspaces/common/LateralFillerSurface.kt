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

package io.rtron.model.roadspaces.common

import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.math.range.difference
import io.rtron.model.roadspaces.identifier.LateralLaneRangeIdentifier

data class LateralFillerSurface(val id: LateralLaneRangeIdentifier, val surface: AbstractSurface3D) {

    // Properties and Initializers
    init {
        require(id.laneIdRange.difference == 1) { "Lane identifiers must be laterally adjacent." }
    }
}
