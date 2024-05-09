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

package io.rtron.model.opendrive.road

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.model.opendrive.junction.EContactPoint
import io.rtron.model.opendrive.junction.EElementDir

data class RoadLinkPredecessorSuccessor(
    var contactPoint: Option<EContactPoint> = None,
    var elementDir: Option<EElementDir> = None,
    var elementId: String = "",
    var elementS: Option<Double> = None,
    var elementType: Option<ERoadLinkElementType> = None,
) : OpendriveElement() {
    // Methods

    fun getRoadPredecessorSuccessor(): Option<Pair<String, EContactPoint>> =
        if (elementId.isNotEmpty() && elementType.isSome { it == ERoadLinkElementType.ROAD }) {
            contactPoint.map { elementId to it }
        } else {
            None
        }

    fun getJunctionPredecessorSuccessor(): Option<String> =
        if (elementId.isNotEmpty() && elementType.isSome { it == ERoadLinkElementType.JUNCTION }) {
            Some(elementId)
        } else {
            None
        }
}
