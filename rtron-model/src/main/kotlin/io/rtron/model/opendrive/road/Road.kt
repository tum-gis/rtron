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

package io.rtron.model.opendrive.road

import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import com.github.kittinunf.result.Result
import io.rtron.math.std.fuzzyEquals
import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.ETrafficRule
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData
import io.rtron.model.opendrive.road.lanes.RoadLanes
import io.rtron.model.opendrive.road.lateralprofile.RoadLateralProfile
import io.rtron.model.opendrive.road.objects.RoadObjects
import io.rtron.model.opendrive.road.planview.RoadPlanView
import io.rtron.model.opendrive.road.signals.RoadSignals
import io.rtron.std.ContextMessage

data class Road(
    var link: RoadLink = RoadLink(),
    var type: List<RoadType> = listOf(),
    var planView: RoadPlanView = RoadPlanView(),
    var elevationProfile: RoadElevationProfile = RoadElevationProfile(),
    var lateralProfile: RoadLateralProfile = RoadLateralProfile(),
    var lanes: RoadLanes = RoadLanes(),
    var objects: RoadObjects = RoadObjects(),
    var signals: RoadSignals = RoadSignals(),
    var surface: RoadSurface = RoadSurface(),
    var railroad: RoadRailroad = RoadRailroad(),

    var userData: List<UserData> = listOf(),
    var include: List<Include> = listOf(),
    var dataQuality: DataQuality = DataQuality(),

    var name: String = "",
    var length: Double = Double.NaN,
    var id: String = "",
    var junction: String = "",
    var rule: ETrafficRule = ETrafficRule.RIGHTHANDTRAFFIC
) {

    // Methods

    fun getJunction(): Option<String> =
        if (junction.isNotEmpty() && junction != "-1") Some(junction) else none()

    fun isProcessable(tolerance: Double): Result<ContextMessage<Unit>, IllegalStateException> {
        val infos = mutableListOf<String>()

        val planViewGeometryLengthsSum = planView.geometry.sumOf { it.length }
        if (!fuzzyEquals(planViewGeometryLengthsSum, length, tolerance))
            return Result.error(
                IllegalStateException(
                    "Given length of road (${this.length}) is different than " +
                        "the sum of the individual plan view elements ($planViewGeometryLengthsSum)."
                )
            )

        if (lateralProfile.containsShapeProfile() && lanes.containsLaneOffset())
            infos += "Road contains both a lateral road shape and a lane offset, whereby the combination of shapes " +
                "and non-linear offsets should be avoided according to the standard."

        return Result.success(ContextMessage(Unit, infos))
    }
}
