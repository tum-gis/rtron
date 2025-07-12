/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.modifiers.opendrive.offset.resolver

import arrow.core.None
import arrow.core.some
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.model.opendrive.additions.optics.everyRoadElevationProfileElement
import io.rtron.model.opendrive.additions.optics.everyRoadPlanViewGeometry
import io.rtron.model.opendrive.core.HeaderOffset
import io.rtron.model.opendrive.road.elevation.RoadElevationProfile
import io.rtron.model.opendrive.road.elevation.RoadElevationProfileElevation

/**
 * Resolves the offset of the OpenDRIVE header by applying it to the plan view geometries and elevation.
 * This resolution is implemented as a transformer, since most software tools are not supporting this feature yet.
 */
class OpendriveOffsetResolver {
    fun modify(opendriveModel: OpendriveModel): Pair<OpendriveModel, OpendriveOffsetResolverReport> {
        val report = OpendriveOffsetResolverReport(emptyList())

        var modifiedOpendriveModel = opendriveModel.copy()
        modifiedOpendriveModel.updateAdditionalIdentifiers()
        if (modifiedOpendriveModel.header.offset.isNone()) {
            report.messages += "No offset values in header available."
            return modifiedOpendriveModel to report
        }

        val headerOffset: HeaderOffset = modifiedOpendriveModel.header.offset.getOrNull()!!
        modifiedOpendriveModel.header.offset = None

        // XY axes
        modifiedOpendriveModel =
            everyRoadPlanViewGeometry.modify(modifiedOpendriveModel) { currentPlanViewGeometry ->
                val modifiedPlanViewGeometry = currentPlanViewGeometry.copy()
                modifiedPlanViewGeometry.x = modifiedPlanViewGeometry.x + headerOffset.x
                modifiedPlanViewGeometry.y = modifiedPlanViewGeometry.y + headerOffset.y

                modifiedPlanViewGeometry
            }

        // Z axis
        modifiedOpendriveModel =
            everyRoadElevationProfileElement.modify(modifiedOpendriveModel) { currentElevationProfileElement ->
                val modifiedElevationProfileElement = currentElevationProfileElement.copy()
                modifiedElevationProfileElement.a = modifiedElevationProfileElement.a + headerOffset.z
                modifiedElevationProfileElement
            }

        modifiedOpendriveModel =
            everyRoad.modify(modifiedOpendriveModel) { currentRoad ->
                val modifiedRoad = currentRoad.copy()

                if (modifiedRoad.elevationProfile.isNone()) {
                    modifiedRoad.elevationProfile = RoadElevationProfile(emptyList()).some()
                }

                modifiedRoad.elevationProfile.onSome {
                    if (it.elevation.isEmpty()) {
                        it.elevation += RoadElevationProfileElevation(headerOffset.z, 0.0, 0.0, 0.0, 0.0)
                    }
                }

                modifiedRoad
            }

        report.messages += "Offset of x=${headerOffset.x}, y=${headerOffset.y}, z=${headerOffset.z} resolved."

        return modifiedOpendriveModel to report
    }
}
