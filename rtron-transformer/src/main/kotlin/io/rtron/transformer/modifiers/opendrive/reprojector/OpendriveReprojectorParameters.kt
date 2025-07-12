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

package io.rtron.transformer.modifiers.opendrive.reprojector

import arrow.core.None
import arrow.core.Some
import kotlinx.serialization.Serializable

@Serializable
data class OpendriveReprojectorParameters(
    /** perform the geospatial reprojection of the model geometries */
    val reprojectModel: Boolean,
    /** [EPSG code](https://en.wikipedia.org/wiki/EPSG_Geodetic_Parameter_Dataset) of the target coordinate reference system (obligatory for working with GIS applications) */
    val targetCrsEpsg: Int,
    /**  tolerance threshold when it t */
    val deviationWarningTolerance: Double,
) {
    fun getTargetCrsEpsg() = if (targetCrsEpsg == 0) None else Some(targetCrsEpsg)

    companion object {
        const val DEFAULT_REPROJECT_MODEL = false
        const val DEFAULT_TARGET_CRS_EPSG = 0
        const val DEFAULT_DEVIATION_WARNING_TOLERANCE = 0.03
    }
}
