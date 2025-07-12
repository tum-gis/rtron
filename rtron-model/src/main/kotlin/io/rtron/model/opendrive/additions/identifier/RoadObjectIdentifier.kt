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

package io.rtron.model.opendrive.additions.identifier

import arrow.core.Option

interface RoadObjectIdentifierInterface {
    val roadObjectId: String
}

data class RoadObjectIdentifier(
    override val roadObjectId: String,
    val roadIdentifier: RoadIdentifier,
) : AbstractOpendriveIdentifier(),
    RoadObjectIdentifierInterface,
    RoadIdentifierInterface by roadIdentifier {
    // Conversions
    override fun toIdentifierText() = "Road object: roadObjectId=$roadObjectId, roadId=$roadId"
}

interface AdditionalRoadObjectIdentifier {
    var additionalId: Option<RoadObjectIdentifier>
}
