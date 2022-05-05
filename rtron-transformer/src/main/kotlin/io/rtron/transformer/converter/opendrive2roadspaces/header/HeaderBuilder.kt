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

package io.rtron.transformer.converter.opendrive2roadspaces.header

import arrow.core.None
import arrow.core.Option
import arrow.core.flatten
import arrow.core.toOption
import io.rtron.io.logging.LogManager
import io.rtron.math.projection.CoordinateReferenceSystem
import io.rtron.model.opendrive.core.HeaderGeoReference
import io.rtron.model.roadspaces.Header
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.model.opendrive.core.Header as OdrHeader

class HeaderBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {
    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    // Methods
    fun buildHeader(header: OdrHeader): Header {
        val crs = header.geoReference.map { buildCoordinateSystem(it) }.flatten()
        return Header(coordinateReferenceSystem = crs)
    }

    /**
     * Builds the [CoordinateReferenceSystem] for the [Header].
     */
    private fun buildCoordinateSystem(geoReference: HeaderGeoReference): Option<CoordinateReferenceSystem> {

        CoordinateReferenceSystem.of(configuration.crsEpsg).tap { return it.toOption() }
        CoordinateReferenceSystem.of(geoReference.content).tap { return it.toOption() }

        _reportLogger.warn("Unknown coordinate reference system.")
        return None
    }
}
