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

package io.rtron.model.opendrive.additions.identifier

import arrow.core.Option

interface LaneIdentifierInterface {
    val laneId: Int
}

data class LaneIdentifier(override val laneId: Int, val laneSectionIdentifier: LaneSectionIdentifier) :
    AbstractOpendriveIdentifier(), LaneIdentifierInterface, RoadLaneSectionIdentifierInterface by laneSectionIdentifier {
    // Conversions
    override fun toIdentifierText() =
        "Lane: laneId=$laneId, " +
            "laneSectionIndex=${laneSectionIdentifier.laneSectionIndex}, " +
            "roadId=${laneSectionIdentifier.roadIdentifier.roadId}"
}

interface AdditionalLaneIdentifier {
    var additionalId: Option<LaneIdentifier>
}
