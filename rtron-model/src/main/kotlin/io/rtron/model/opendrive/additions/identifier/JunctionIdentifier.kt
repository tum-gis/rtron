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

interface JunctionIdentifierInterface {
    val junctionId: String
}

data class JunctionIdentifier(override val junctionId: String) : AbstractOpendriveIdentifier(), JunctionIdentifierInterface {

    // Conversions
    override fun toIdentifierText() = "Junction: junctionId=$junctionId"
}

interface AdditionalJunctionIdentifier {
    var additionalId: Option<JunctionIdentifier>
}
