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

package io.rtron.model.opendrive.junction

import arrow.core.None
import arrow.core.Option
import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import io.rtron.io.report.MessageSeverity
import io.rtron.io.report.Report
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.core.OpendriveElement

data class JunctionConnection(
    var predecessor: Option<JunctionPredecessorSuccessor> = None,
    var successor: Option<JunctionPredecessorSuccessor> = None,
    var laneLink: List<JunctionConnectionLaneLink> = emptyList(),

    var connectingRoad: Option<String> = None,
    var contactPoint: Option<EContactPoint> = None,
    var id: String = "",
    var incomingRoad: Option<String> = None,
    var linkedRoad: Option<String> = None,
    var type: Option<EConnectionType> = None
) : OpendriveElement() {

    // Properties and Initializers
    val idValidated: Validated<OpendriveException.MissingValue, String>
        get() = if (id.isBlank()) OpendriveException.MissingValue("").invalid() else id.valid()

    // Methods
    fun getFatalViolations(): List<OpendriveException> =
        idValidated.fold({ listOf(it) }, { emptyList() })

    fun healMinorViolations(): Report {
        val report = Report()

        if (connectingRoad.exists { it.isBlank() }) {
            connectingRoad = None
            report.append("ConnectingRoad attribute is set, but only a blank string. Unsetting.", MessageSeverity.WARNING)
        }

        if (incomingRoad.exists { it.isBlank() }) {
            incomingRoad = None
            report.append("IncomingRoad attribute is set, but only a blank string. Unsetting.", MessageSeverity.WARNING)
        }

        if (linkedRoad.exists { it.isBlank() }) {
            linkedRoad = None
            report.append("LinkedRoad attribute is set, but only a blank string. Unsetting.", MessageSeverity.WARNING)
        }

        return report
    }
}
