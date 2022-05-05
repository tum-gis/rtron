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

package io.rtron.model.opendrive.road

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.flatten
import arrow.core.none
import io.rtron.math.std.fuzzyEquals
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.model.opendrive.lane.RoadLanes
import io.rtron.model.opendrive.objects.RoadObjects
import io.rtron.model.opendrive.railroad.RoadRailroad
import io.rtron.model.opendrive.road.elevation.RoadElevationProfile
import io.rtron.model.opendrive.road.elevation.RoadElevationProfileElevation
import io.rtron.model.opendrive.road.lateral.RoadLateralProfile
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileShape
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileSuperelevation
import io.rtron.model.opendrive.road.planview.RoadPlanView
import io.rtron.model.opendrive.signal.RoadSignals

data class Road(
    var link: Option<RoadLink> = None,
    var type: List<RoadType> = emptyList(),
    var planView: RoadPlanView = RoadPlanView(),
    var elevationProfile: Option<RoadElevationProfile> = None,
    var lateralProfile: Option<RoadLateralProfile> = None,
    var lanes: RoadLanes = RoadLanes(),
    var objects: Option<RoadObjects> = None,
    var signals: Option<RoadSignals> = None,
    var surface: Option<RoadSurface> = None,
    var railroad: Option<RoadRailroad> = None,

    var id: String = "",
    var junction: String = "",
    var length: Double = Double.NaN,
    var name: Option<String> = None,
    var rule: Option<ETrafficRule> = None
) : OpendriveElement() {

    // Methods

    fun getJunctionOption(): Option<String> =
        if (junction.isNotEmpty() && junction != "-1") Some(junction) else none()

    fun getElevationEntries(): Option<NonEmptyList<RoadElevationProfileElevation>> = elevationProfile.map { it.elevationValidated.toOption() }.flatten()
    fun getSuperelevationEntries(): Option<NonEmptyList<RoadLateralProfileSuperelevation>> = lateralProfile.map { it.getSuperelevationAsOptionNonEmptyList() }.flatten()
    fun getShapeEntries(): List<RoadLateralProfileShape> = lateralProfile.fold({ emptyList() }, { it.shape })

    fun getSevereViolations(): List<OpendriveException> = emptyList()

    fun healMinorViolations(tolerance: Double): List<OpendriveException> {
        val healedViolations = mutableListOf<OpendriveException>()

        val planViewGeometryLengthsSum = planView.geometry.sumOf { it.length }
        if (!fuzzyEquals(planViewGeometryLengthsSum, length, tolerance)) {
            healedViolations += OpendriveException.UnexpectedValue("length", length.toString(), ", as the sum of the individual plan view elements is different")
            length = planViewGeometryLengthsSum
        }

        lateralProfile.tap {
            if (it.containsShapeProfile() && lanes.containsLaneOffset()) {
                healedViolations += OpendriveException.UnexpectedValue(
                    "lateralProfile.shape", "",
                    "Lane offsets shall not be used together with road shapes. Removing the shape entries."
                )
                it.shape = emptyList()
            }
        }

        return healedViolations
    }
}
