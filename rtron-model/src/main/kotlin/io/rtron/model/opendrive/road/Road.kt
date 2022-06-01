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
import arrow.optics.optics
import io.rtron.model.opendrive.additions.identifier.AdditionalRoadIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
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

@optics
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
    var rule: Option<ETrafficRule> = None,

    override var additionalId: Option<RoadIdentifier> = None
) : OpendriveElement(), AdditionalRoadIdentifier {

    // Methods

    fun getJunctionOption(): Option<String> =
        if (junction.isNotEmpty() && junction != "-1") Some(junction) else None

    fun getElevationEntries(): Option<NonEmptyList<RoadElevationProfileElevation>> = elevationProfile.map { it.elevationValidated.toOption() }.flatten()
    fun getSuperelevationEntries(): Option<NonEmptyList<RoadLateralProfileSuperelevation>> = lateralProfile.flatMap { it.getSuperelevationEntries() }
    fun getShapeEntries(): Option<NonEmptyList<RoadLateralProfileShape>> = lateralProfile.flatMap { it.getShapeEntries() }

    companion object
}
