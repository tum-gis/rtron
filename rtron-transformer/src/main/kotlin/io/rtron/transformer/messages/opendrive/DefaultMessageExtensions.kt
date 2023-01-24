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

package io.rtron.transformer.messages.opendrive

import arrow.core.Option
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.additions.identifier.AbstractOpendriveIdentifier
import io.rtron.model.opendrive.additions.identifier.toIdentifierText

fun DefaultMessage.Companion.of(type: String, info: String, location: AbstractOpendriveIdentifier, incidentSeverity: Severity, wasFixed: Boolean): DefaultMessage {
    return DefaultMessage(type, info, location.toIdentifierText(), incidentSeverity, wasFixed)
}

fun DefaultMessage.Companion.of(type: String, info: String, location: Option<AbstractOpendriveIdentifier>, incidentSeverity: Severity, wasFixed: Boolean): DefaultMessage {
    return DefaultMessage(type, info, location.toIdentifierText(), incidentSeverity, wasFixed)
}
