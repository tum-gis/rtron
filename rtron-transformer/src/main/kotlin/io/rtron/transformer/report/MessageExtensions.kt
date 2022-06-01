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

package io.rtron.transformer.report

import arrow.core.Option
import io.rtron.io.report.Message
import io.rtron.io.report.MessageSeverity
import io.rtron.model.opendrive.additions.identifier.AbstractOpendriveIdentifier
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier

fun Message.Companion.of(text: String, location: Map<String, String>, isFatal: Boolean, wasHealed: Boolean): Message {
    val textPrefix: String = when (Pair(isFatal, wasHealed)) {
        Pair(true, true) -> "Fatal violation of the following rule was identified and healed"
        Pair(true, false) -> "Fatal violation of the following rule was identified and could not be healed"
        Pair(false, true) -> "Deviation from the following rule was identified and healed"
        else -> "Deviation from the following rule was identified and could not be healed"
    }
    val messageText = "$textPrefix: $text"

    val severity: MessageSeverity = when (Pair(isFatal, wasHealed)) {
        Pair(true, true) -> MessageSeverity.ERROR
        Pair(true, false) -> MessageSeverity.FATAL_ERROR
        Pair(false, true) -> MessageSeverity.WARNING
        else -> MessageSeverity.WARNING
    }

    val identifier = emptyMap<String, String>()

    return Message(messageText, severity, identifier, location)
}

@JvmName("MessageOfAbstractOpendriveIdentifier")
fun Message.Companion.of(text: String, identifier: Option<AbstractOpendriveIdentifier>, isFatal: Boolean, wasHealed: Boolean): Message {
    return of(text, identifier.fold({ emptyMap() }, { it.toStringMap() }), isFatal, wasHealed)
}

fun Message.Companion.of(text: String, identifier: AbstractOpendriveIdentifier, isFatal: Boolean, wasHealed: Boolean): Message {
    return of(text, identifier.toStringMap(), isFatal, wasHealed)
}

@JvmName("MessageOfAbstractRoadspacesIdentifier")
fun Message.Companion.of(text: String, identifier: Option<AbstractRoadspacesIdentifier>, isFatal: Boolean, wasHealed: Boolean): Message {
    return of(text, identifier.fold({ emptyMap() }, { it.toStringMap() }), isFatal, wasHealed)
}

fun Message.Companion.of(text: String, identifier: AbstractRoadspacesIdentifier, isFatal: Boolean, wasHealed: Boolean): Message {
    return of(text, identifier.toStringMap(), isFatal, wasHealed)
}
