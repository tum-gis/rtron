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

package io.rtron.io.report

import io.rtron.io.files.File
import io.rtron.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Report() {

    // Properties and Initializers
    private val messages: MutableList<Message> = mutableListOf()

    // val incidents: MutableList<Incident> = mutableListOf()

    constructor(messages: List<Message>) : this() {
        append(messages)
    }

    // Operators
    operator fun plusAssign(other: Report) {
        append(other.messages)
    }

    operator fun plusAssign(other: Message) {
        append(other)
    }

    // Methods
    fun getMessages(): List<Message> = messages

    fun isEmpty(): Boolean = messages.isEmpty()
    fun isNotEmpty(): Boolean = messages.isNotEmpty()

    fun append(message: String, severity: MessageSeverity, identifier: Map<String, String> = emptyMap(), location: Map<String, String> = emptyMap()) {
        messages += Message(message, severity, identifier, location)
    }

    fun append(message: Message) { this.messages += message }
    fun append(messages: List<Message>) { this.messages += messages }

    fun write(path: Path) {
        // if (messages.isEmpty()) return
        if (!path.parent.exists())
            path.parent.createDirectory()

        val jsonFormatter = Json {
            prettyPrint = true
            encodeDefaults = true
        }

        val jsonFileContent = jsonFormatter.encodeToString(this)
        File(path).writeText(jsonFileContent)
    }

    /**
     * Returns the number of entries with a certain [severity].
     */
    fun getNumberOfMessages(severity: MessageSeverity) = messages.filter { it.severity == severity }.size

    /**
     * Returns a summary of the message numbers depending on the severity.
     */
    fun getTextSummary(): String {
        val numberOfWarnings = getNumberOfMessages(severity = MessageSeverity.WARNING)
        val numberOfErrors = getNumberOfMessages(severity = MessageSeverity.ERROR)
        val numberOfFatalErrors = getNumberOfMessages(severity = MessageSeverity.FATAL_ERROR)

        return "$numberOfWarnings warnings, $numberOfErrors errors, $numberOfFatalErrors fatal errors"
    }
}

fun List<Report>.merge(): Report = Report(flatMap { it.getMessages() })
