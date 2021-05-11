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

package io.rtron.transformer.opendrive2roadspaces.configuration

import io.rtron.io.files.FileIdentifier
import io.rtron.math.geometry.euclidean.twod.point.Vector2D

/**
 * Transformation configuration for the OpenDRIVE to RoadSpace transformer.
 */
data class Opendrive2RoadspacesConfiguration(
    val projectId: String,
    val sourceFileIdentifier: FileIdentifier,
    val concurrentProcessing: Boolean,

    val tolerance: Double,
    val attributesPrefix: String,
    val crsEpsg: Int,
    val offsetX: Double,
    val offsetY: Double,
    val offsetZ: Double,
    val extrapolateLateralRoadShapes: Boolean
) {

    /**
     * offset in the xy plane as vector
     */
    val offsetXY: Vector2D = Vector2D(offsetX, offsetY)

    override fun toString() =
        "Opendrive2RoadspacesConfiguration(tolerance=$tolerance, attributesPrefix=$attributesPrefix," +
            " crsEpsg=$crsEpsg, offsetX=$offsetX, offsetY=$offsetY, offsetZ=$offsetZ, " +
            "extrapolateLateralRoadShapes=$extrapolateLateralRoadShapes)"
}
