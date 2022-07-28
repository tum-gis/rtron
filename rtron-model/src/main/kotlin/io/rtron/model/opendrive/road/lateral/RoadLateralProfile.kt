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

package io.rtron.model.opendrive.road.lateral

import arrow.core.NonEmptyList
import arrow.core.Option
import io.rtron.model.opendrive.core.OpendriveElement

data class RoadLateralProfile(
    var superelevation: List<RoadLateralProfileSuperelevation> = emptyList(),
    var shape: List<RoadLateralProfileShape> = emptyList(),
) : OpendriveElement() {

    // Methods
    fun getSuperelevationEntries(): Option<NonEmptyList<RoadLateralProfileSuperelevation>> = NonEmptyList.fromList(superelevation)
    fun getShapeEntries(): Option<NonEmptyList<RoadLateralProfileShape>> = NonEmptyList.fromList(shape)

    fun containsSuperelevationProfile() = superelevation.isNotEmpty()
    fun containsShapeProfile() = shape.isNotEmpty()
}
