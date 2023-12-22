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

package io.rtron.model.opendrive.lane

import arrow.core.None
import arrow.core.Option
import arrow.optics.optics
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.model.opendrive.additions.identifier.AdditionalLaneSectionIdentifier
import io.rtron.model.opendrive.additions.identifier.LaneSectionIdentifier
import io.rtron.model.opendrive.core.OpendriveElement

@optics
data class RoadLanesLaneSection(
    var left: Option<RoadLanesLaneSectionLeft> = None,
    var center: RoadLanesLaneSectionCenter = RoadLanesLaneSectionCenter(),
    var right: Option<RoadLanesLaneSectionRight> = None,

    var s: Double = Double.NaN,
    var singleSide: Option<Boolean> = None,

    override var additionalId: Option<LaneSectionIdentifier> = None
) : OpendriveElement(), AdditionalLaneSectionIdentifier {

    // Properties and Initializers
    val laneSectionStart get() = CurveRelativeVector1D(s)

    // Methods
    fun getNumberOfLeftLanes() = left.fold({ 0 }, { it.getNumberOfLanes() })
    fun getNumberOfRightLanes() = right.fold({ 0 }, { it.getNumberOfLanes() })

    fun getNumberOfLeftRightLanes() = getNumberOfLeftLanes() + getNumberOfRightLanes()
    fun getNumberOfLanes() = center.getNumberOfLanes() + getNumberOfLeftRightLanes()

    fun getCenterLane() = center.lane.first()
    fun getLeftLanes(): Map<Int, RoadLanesLaneSectionLRLane> = left.fold({ emptyMap() }, { it.getLanes() })
    fun getRightLanes(): Map<Int, RoadLanesLaneSectionLRLane> = right.fold({ emptyMap() }, { it.getLanes() })
    fun getLeftRightLanes(): Map<Int, RoadLanesLaneSectionLRLane> = getLeftLanes() + getRightLanes()

    companion object
}
