/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

interface RoadObjectRepeatInterface {
    val repeatIndex: Int
}

data class RoadObjectRepeatIdentifier(override val repeatIndex: Int, val roadObjectIdentifier: RoadObjectIdentifier) :
    AbstractOpendriveIdentifier(), RoadObjectRepeatInterface, RoadIdentifierInterface by roadObjectIdentifier {

    // Conversions
    override fun toIdentifierText() = "Road object repeat element: repeatIndex=$repeatIndex, " +
        "roadObjectId=${roadObjectIdentifier.roadObjectId}, " +
        "roadId=${roadObjectIdentifier.roadId}"
}

interface AdditionalRoadObjectRepeatIdentifier {
    var additionalId: Option<RoadObjectRepeatIdentifier>
}
