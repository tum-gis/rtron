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

package io.rtron.readerwriter.opendrive.reader.validation

import io.rtron.io.issues.IssueList
import io.rtron.readerwriter.opendrive.report.SchemaValidationIssue
import io.rtron.readerwriter.opendrive.report.toIssue
import jakarta.xml.bind.ValidationEvent
import jakarta.xml.bind.ValidationEventHandler

class OpendriveValidationEventHandler : ValidationEventHandler {

    // Properties and Initializers
    val validationEvents: MutableList<ValidationEvent> = mutableListOf()

    // Methods
    override fun handleEvent(event: ValidationEvent): Boolean {
        validationEvents.add(event)
        return true
    }

    fun clear() {
        validationEvents.clear()
    }

    // Conversions
    fun toIssueList(): IssueList<SchemaValidationIssue> {
        val issues = this.validationEvents.map { it.toIssue() }
        return IssueList.of(issues)
    }
}
