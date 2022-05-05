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

package io.rtron.readerwriter.opendrive.reader.validation

import io.rtron.io.report.Message
import io.rtron.io.report.MessageSeverity
import io.rtron.readerwriter.opendrive.OpendriveVersion
import jakarta.xml.bind.ValidationEvent

fun ValidationEvent.toMessage(opendriveSchemaVersion: OpendriveVersion): Message {
    val text = this.message ?: ""
    val severity = when (this.severity) {
        ValidationEvent.WARNING -> MessageSeverity.WARNING
        ValidationEvent.ERROR -> MessageSeverity.ERROR
        ValidationEvent.FATAL_ERROR -> MessageSeverity.FATAL_ERROR
        else -> MessageSeverity.WARNING
    }

    val identifier = mapOf("opendriveSchemaVersion" to opendriveSchemaVersion.toString())

    val lineNumber = this.locator.lineNumber
    val columnNumber = this.locator.columnNumber
    val location = mapOf(
        "lineNumber" to lineNumber.toString(),
        "columnNumber" to columnNumber.toString()
    )

    return Message(text, severity, identifier, location)
}
