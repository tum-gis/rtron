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

import io.rtron.model.opendrive.common.AdditionalData
import io.rtron.model.opendrive.common.EContactPoint
import io.rtron.std.Optional

data class RoadLinkPredecessorSuccessor(
    var additionalData: AdditionalData = AdditionalData(),
    var elementType: ERoadLinkElementType = ERoadLinkElementType.ROAD,
    var elementId: String = "",

    // variant 1
    var contactPoint: EContactPoint = EContactPoint.UNKNOWN,

    // variant 2
    var elementS: Double = Double.NaN
    // TODO: var elementDir:
) {

    // Methods

    fun getRoadPredecessorSuccessor(): Optional<String> =
        if (elementId.isNotEmpty() && elementType == ERoadLinkElementType.ROAD) Optional(elementId)
        else Optional.empty()

    fun getJunctionPredecessorSuccessor(): Optional<String> =
        if (elementId.isNotEmpty() && elementType == ERoadLinkElementType.JUNCTION) Optional(elementId)
        else Optional.empty()
}
