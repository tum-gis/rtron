/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.projection

import com.github.kittinunf.result.Result
import io.rtron.std.handleFailure
import org.locationtech.proj4j.CRSFactory as ProjCRSFactory
import org.locationtech.proj4j.CoordinateReferenceSystem as ProjCoordinateReferenceSystem

/**
 * Representation of a coordinate reference system.
 * See the wikipedia article of [Spatial Reference System](https://en.wikipedia.org/wiki/Spatial_reference_system).
 *
 * @param crs adapted CRS class from PROJ
 */
class CoordinateReferenceSystem(
    private val crs: ProjCoordinateReferenceSystem
) {

    // Properties and Initializers

    /** Name of CRS */
    val name: String get() = crs.name

    /**
     * EPSG code of the CRS.
     * See the wikipedia article of [EPSG](https://en.wikipedia.org/wiki/EPSG_Geodetic_Parameter_Dataset).
     */
    val epsgCode: Int get() = name.split(":").last().toInt()

    /** GML compliant srsName of the CRS */
    val srsName: String get() = "urn:ogc:def:crs:EPSG::$epsgCode"

    // Methods

    companion object {

        private val projCRSFactory = ProjCRSFactory()

        /**
         * Creates a [CoordinateReferenceSystem] based on the provided [epsgCode].
         */
        fun of(epsgCode: Int): Result<CoordinateReferenceSystem, Exception> = of("EPSG:$epsgCode")

        /**
         * Creates a [CoordinateReferenceSystem] based on the provided [crsName].
         */
        fun of(crsName: String): Result<CoordinateReferenceSystem, Exception> {
            val crs = try {
                projCRSFactory.createFromName(crsName)
            } catch (e: Exception) {
                return Result.error(e)
            }
            return when (crs) {
                null -> Result.error(IllegalArgumentException("Unknown EPSG code."))
                else -> Result.success(CoordinateReferenceSystem(crs))
            }
        }

        /**
         * Creates a [CoordinateReferenceSystem] based on the provided PROJ.4 string.
         *
         * @param parameters PROJ.4 projection parameter string
         */
        fun ofParameters(parameters: String): Result<CoordinateReferenceSystem, Exception> {
            val epsgCode = parametersToEpsgCode(parameters).handleFailure { return it }
            return of(epsgCode)
        }

        private fun parametersToEpsgCode(parameters: String): Result<Int, IllegalArgumentException> {
            val result = projCRSFactory.readEpsgFromParameters(parameters)
                ?: return Result.error(IllegalArgumentException("Cannot derive EPSG code from parameters."))

            return Result.success(result.toInt())
        }
    }
}
