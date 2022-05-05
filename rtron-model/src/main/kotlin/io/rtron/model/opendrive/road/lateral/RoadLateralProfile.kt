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
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.std.filterToStrictSortingBy

data class RoadLateralProfile(
    var superelevation: List<RoadLateralProfileSuperelevation> = emptyList(),
    var shape: List<RoadLateralProfileShape> = emptyList(),
) : OpendriveElement() {

    // Validation Methods
    fun healMinorViolations(): List<OpendriveException> {
        val healedViolations = mutableListOf<OpendriveException>()

        val superelevationEntriesFiltered = superelevation.filterToStrictSortingBy { it.s }
        if (superelevationEntriesFiltered.size < superelevation.size) {
            healedViolations += OpendriveException.NonStrictlySortedList("superelevation", "Ignoring ${superelevation.size - superelevationEntriesFiltered.size} superelevation entries which are not placed in strict order according to s.")
            superelevation = superelevationEntriesFiltered
        }

        return healedViolations
    }

    // Methods
    fun getSuperelevationAsOptionNonEmptyList(): Option<NonEmptyList<RoadLateralProfileSuperelevation>> = NonEmptyList.fromList(superelevation)

    fun containsSuperelevationProfile() = superelevation.isNotEmpty()
    fun containsShapeProfile() = shape.isNotEmpty()
}
