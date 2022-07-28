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

package io.rtron.transformer.modifiers.opendrive.shifter

import arrow.core.some
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.model.opendrive.additions.optics.everyRoadElevationProfileElement
import io.rtron.model.opendrive.additions.optics.everyRoadPlanViewGeometry
import io.rtron.model.opendrive.road.elevation.RoadElevationProfile
import io.rtron.model.opendrive.road.elevation.RoadElevationProfileElevation

class OpendriveShifter(
    val parameters: OpendriveShifterParameters
) {

    fun modify(opendriveModel: OpendriveModel): Pair<OpendriveModel, OpendriveShifterReport> {
        val report = OpendriveShifterReport(parameters)

        opendriveModel.updateAdditionalIdentifiers()

        var modifiedOpendriveModel = opendriveModel.copy()

        // XY axes
        modifiedOpendriveModel = everyRoadPlanViewGeometry.modify(modifiedOpendriveModel) { currentPlanViewGeometry ->
            currentPlanViewGeometry.x = currentPlanViewGeometry.x + parameters.offsetX
            currentPlanViewGeometry.y = currentPlanViewGeometry.y + parameters.offsetY

            currentPlanViewGeometry
        }

        // Z axis
        modifiedOpendriveModel = everyRoadElevationProfileElement.modify(modifiedOpendriveModel) { currentElevationProfileElement ->
            currentElevationProfileElement.a = parameters.offsetZ
            currentElevationProfileElement
        }

        modifiedOpendriveModel = everyRoad.modify(modifiedOpendriveModel) { currentRoad ->
            if (currentRoad.elevationProfile.isEmpty())
                currentRoad.elevationProfile = RoadElevationProfile(emptyList()).some()

            currentRoad.elevationProfile.tap {
                if (it.elevation.isEmpty())
                    it.elevation += RoadElevationProfileElevation(parameters.offsetZ, 0.0, 0.0, 0.0, 0.0)
            }

            currentRoad
        }

        return modifiedOpendriveModel to report
    }
}
