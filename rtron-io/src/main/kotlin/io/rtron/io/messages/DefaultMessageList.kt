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

package io.rtron.io.messages

typealias DefaultMessageList = MessageList<DefaultMessage>

/**
 * Returns the number of entries with a certain [severity].
 */
fun DefaultMessageList.getNumberOfMessages(severity: Severity) = getMessages().filter { it.messageSeverity == severity }.size

/**
 * Returns true, if list contains messages with fatal error severity.
 */
fun DefaultMessageList.containsFatalErrors() = getMessages().any { it.messageSeverity == Severity.FATAL_ERROR }

/**
 * Returns a summary of the message numbers depending on the severity.
 */
fun DefaultMessageList.getTextSummary(): String {
    val numberOfWarnings = getNumberOfMessages(severity = Severity.WARNING)
    val numberOfErrors = getNumberOfMessages(severity = Severity.ERROR)
    val numberOfFatalErrors = getNumberOfMessages(severity = Severity.FATAL_ERROR)

    return "$numberOfWarnings warnings, $numberOfErrors errors, $numberOfFatalErrors fatal errors"
}
