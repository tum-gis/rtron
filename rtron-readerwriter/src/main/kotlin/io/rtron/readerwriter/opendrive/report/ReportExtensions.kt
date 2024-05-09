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

package io.rtron.readerwriter.opendrive.report

import io.rtron.io.issues.Severity
import jakarta.xml.bind.ValidationEvent

fun ValidationEvent.toIssue(): SchemaValidationIssue {
    val text = this.message ?: ""
    val severity =
        when (this.severity) {
            ValidationEvent.WARNING -> Severity.WARNING
            ValidationEvent.ERROR -> Severity.ERROR
            ValidationEvent.FATAL_ERROR -> Severity.FATAL_ERROR
            else -> Severity.WARNING
        }

    val lineNumber = this.locator.lineNumber
    val columnNumber = this.locator.columnNumber

    return SchemaValidationIssue(text, severity, lineNumber, columnNumber)
}
