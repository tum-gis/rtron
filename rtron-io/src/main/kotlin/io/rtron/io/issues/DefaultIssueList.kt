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

package io.rtron.io.issues

typealias DefaultIssueList = IssueList<DefaultIssue>

/**
 * Returns the number of entries with a certain [severity].
 */
fun DefaultIssueList.getNumberOfIssues(severity: Severity) = getIssues().count { it.issueSeverity == severity }

/**
 * Returns true, if list contains issues with fatal error severity.
 */
fun DefaultIssueList.containsFatalErrors() = getIssues().any { it.issueSeverity == Severity.FATAL_ERROR }

/**
 * Returns a summary of the issue numbers depending on the severity.
 */
fun DefaultIssueList.getTextSummary(): String {
    val numberOfWarnings = getNumberOfIssues(severity = Severity.WARNING)
    val numberOfErrors = getNumberOfIssues(severity = Severity.ERROR)
    val numberOfFatalErrors = getNumberOfIssues(severity = Severity.FATAL_ERROR)

    return "$numberOfWarnings warnings, $numberOfErrors errors, $numberOfFatalErrors fatal errors"
}
