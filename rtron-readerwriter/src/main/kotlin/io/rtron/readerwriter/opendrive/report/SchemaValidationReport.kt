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

package io.rtron.readerwriter.opendrive.report

import io.rtron.io.issues.IssueList
import io.rtron.io.issues.Severity
import io.rtron.readerwriter.opendrive.version.OpendriveVersion
import kotlinx.serialization.Serializable

@Serializable
class SchemaValidationReport(
    val opendriveVersion: OpendriveVersion,

    val validationIssues: IssueList<SchemaValidationIssue> = IssueList(),

    val completedSuccessfully: Boolean = true,
    val validationAbortIssue: String = ""
) {

    fun validationProcessAborted() = !completedSuccessfully

    fun containsFatalErrorIssues(): Boolean =
        validationIssues.getIssues().any { it.severity == Severity.FATAL_ERROR }
}
