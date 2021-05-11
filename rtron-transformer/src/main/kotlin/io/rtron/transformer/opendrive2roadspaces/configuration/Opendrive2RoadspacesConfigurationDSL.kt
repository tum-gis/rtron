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

class Opendrive2RoadspacesConfigurationBuilder(
    val projectId: String,
    val sourceFileIdentifier: FileIdentifier,
    val concurrentProcessing: Boolean
) {

    // Properties and Initializers

    /**
     * allowed tolerance when comparing double values
     */
    var tolerance: Double = 1E-7

    /**
     * prefix of attribute names
     */
    var attributesPrefix: String = "opendrive_"

    /**
     * [EPSG code](https://en.wikipedia.org/wiki/EPSG_Geodetic_Parameter_Dataset) of the coordinate reference system (obligatory for working with GIS applications)
     */
    var crsEpsg: Int = 0

    /**
     * offset by which the model is translated along the x axis
     */
    var offsetX: Double = 0.0

    /**
     * offset by which the model is translated along the y axis
     */
    var offsetY: Double = 0.0

    /**
     * offset by which the model is translated along the z axis
     */
    var offsetZ: Double = 0.0

    /**
     * linear extrapolation of lateral road shapes if they are not defined at the position (otherwise errors are thrown)
     */
    var extrapolateLateralRoadShapesProperty: Boolean = false

    // Methods

    fun build() = Opendrive2RoadspacesConfiguration(
        projectId,
        sourceFileIdentifier,
        concurrentProcessing,
        tolerance,
        attributesPrefix,
        crsEpsg,
        offsetX,
        offsetY,
        offsetZ,
        extrapolateLateralRoadShapesProperty
    )
}
