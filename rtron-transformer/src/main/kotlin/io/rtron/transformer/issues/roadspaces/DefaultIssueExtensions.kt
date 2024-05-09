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

package io.rtron.transformer.issues.roadspaces

import arrow.core.Option
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.Severity
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.toIdentifierText

fun DefaultIssue.Companion.of(
    type: String,
    info: String,
    location: AbstractRoadspacesIdentifier,
    incidentSeverity: Severity,
    wasFixed: Boolean,
): DefaultIssue {
    return DefaultIssue(type, info, location.toIdentifierText(), incidentSeverity, wasFixed)
}

fun DefaultIssue.Companion.of(
    type: String,
    info: String,
    location: Option<AbstractRoadspacesIdentifier>,
    incidentSeverity: Severity,
    wasFixed: Boolean,
): DefaultIssue {
    return DefaultIssue(type, info, location.toIdentifierText(), incidentSeverity, wasFixed)
}
