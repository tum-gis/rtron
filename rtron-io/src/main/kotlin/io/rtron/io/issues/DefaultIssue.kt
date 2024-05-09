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

import kotlinx.serialization.Serializable

/**
 * Single issue.
 */
@Serializable
data class DefaultIssue(
    val type: String,
    val info: String,
    val location: String,
    val incidentSeverity: Severity,
    val wasFixed: Boolean,
    val infoValues: Map<String, Double> = emptyMap(),
) {
    // Properties and Initializers
    val issueSeverity: Severity =
        when (Pair(incidentSeverity, wasFixed)) {
            Pair(Severity.FATAL_ERROR, true) -> Severity.ERROR
            Pair(Severity.ERROR, true) -> Severity.WARNING
            else -> incidentSeverity
        }

    companion object
}
