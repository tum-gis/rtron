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

package io.rtron.transformer.opendrive2roadspaces.header

import com.github.kittinunf.result.Result
import io.rtron.math.projection.CoordinateReferenceSystem
import io.rtron.model.roadspaces.Header
import io.rtron.std.handleSuccess
import io.rtron.transformer.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.model.opendrive.header.Header as OdrHeader

class HeaderBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Methods
    fun buildHeader(header: OdrHeader): Header {
        val crs = buildCoordinateSystem(header.geoReference)

        return Header(coordinateReferenceSystem = crs)
    }

    /**
     * Builds the [CoordinateReferenceSystem] for the [Header].
     */
    private fun buildCoordinateSystem(geoReference: String): Result<CoordinateReferenceSystem, Exception> {

        CoordinateReferenceSystem.of(configuration.crsEpsg).handleSuccess { return it }
        CoordinateReferenceSystem.of(geoReference).handleSuccess { return it }
        return Result.error(IllegalStateException("Unknown coordinate reference system."))
    }
}
