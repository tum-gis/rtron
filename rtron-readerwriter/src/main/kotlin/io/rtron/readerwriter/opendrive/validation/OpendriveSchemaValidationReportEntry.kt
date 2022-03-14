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

package io.rtron.readerwriter.opendrive.validation

import jakarta.xml.bind.ValidationEvent
import kotlinx.serialization.Serializable

/**
 * Single entry for the schema validation report.
 */
@Serializable
data class OpendriveSchemaValidationReportEntry(val severity: Severity, val message: String, val lineNumber: Int, val columnNumber: Int) {

    enum class Severity {
        WARNING,
        ERROR,
        FATAL_ERROR,
        UNKNOWN
    }

    companion object {
        fun of(validationEvent: ValidationEvent): OpendriveSchemaValidationReportEntry {
            val severity = when (validationEvent.severity) {
                ValidationEvent.WARNING -> Severity.WARNING
                ValidationEvent.ERROR -> Severity.ERROR
                ValidationEvent.FATAL_ERROR -> Severity.FATAL_ERROR
                else -> Severity.UNKNOWN
            }
            val message = validationEvent.message ?: ""

            val lineNumber: Int = validationEvent.locator.lineNumber
            val columnNumber: Int = validationEvent.locator.columnNumber

            return OpendriveSchemaValidationReportEntry(severity, message, lineNumber, columnNumber)
        }
    }
}
