/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.converter.opendrive2roadspaces.header

import arrow.core.None
import arrow.core.Option
import arrow.core.flatten
import arrow.core.some
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.math.projection.CoordinateReferenceSystem
import io.rtron.model.opendrive.core.HeaderGeoReference
import io.rtron.model.roadspaces.Header
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.model.opendrive.core.Header as OdrHeader

class HeaderBuilder(
    private val parameters: Opendrive2RoadspacesParameters,
) {
    // Methods
    fun buildHeader(header: OdrHeader): ContextIssueList<Header> {
        val issueList = DefaultIssueList()

        val crs = header.geoReference.map { buildCoordinateSystem(it).handleIssueList { issueList += it } }.flatten()
        val roadspacesHeader = Header(coordinateReferenceSystem = crs, name = header.name, date = header.date, vendor = header.vendor)

        return ContextIssueList(roadspacesHeader, issueList)
    }

    /**
     * Builds the [CoordinateReferenceSystem] for the [Header].
     */
    private fun buildCoordinateSystem(geoReference: HeaderGeoReference): ContextIssueList<Option<CoordinateReferenceSystem>> {
        val issueList = DefaultIssueList()

        CoordinateReferenceSystem.of(parameters.crsEpsg).onRight { return ContextIssueList(it.some(), issueList) }
        if (parameters.deriveCrsEpsgAutomatically) {
            CoordinateReferenceSystem.of(geoReference.content).onRight { return ContextIssueList(it.some(), issueList) }
            issueList +=
                DefaultIssue(
                    "AutomaticCrsEpsgCodeDerivationFailed",
                    "EPSG code of the coordinate reference system cannot be derived automatically from the OpenDRIVE header element; " +
                        "add the code explicitly as a command line argument if correct georeferencing is required.",
                    "Header element",
                    Severity.WARNING,
                    wasFixed = false,
                )
        }

        return ContextIssueList(None, issueList)
    }
}
