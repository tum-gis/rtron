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

package io.rtron.transformer.converter.roadspaces2citygml.configuration

import io.rtron.io.files.FileIdentifier
import io.rtron.io.files.Path
import io.rtron.math.geometry.euclidean.threed.solid.Cylinder3D
import io.rtron.math.geometry.euclidean.threed.solid.ParametricSweep3D

class Roadspaces2CitygmlConfigurationBuilder(
    val projectId: String,
    val sourceFileIdentifier: FileIdentifier,
    val concurrentProcessing: Boolean
) {

    // Properties and Initializers

    /**
     * path for report
     */
    var outputReportDirectoryPath: Path = Path("./")

    /**
     * prefix for generated gml ids
     */
    var gmlIdPrefix: String = "UUID_"

    /**
     * prefix for identifier attribute names
     */
    var identifierAttributesPrefix: String = "identifier_"

    /**
     * prefix for geometry attribute names
     */
    var geometryAttributesPrefix: String = "geometry_"

    /**
     * true, if nested attribute lists shall be flattened out
     */
    var flattenGenericAttributeSets: Boolean = true

    /**
     * distance between each discretization step for curves and surfaces
     */
    var discretizationStepSize: Double = 0.7

    /**
     * distance between each discretization step for solid geometries of [ParametricSweep3D]
     */
    var sweepDiscretizationStepSize: Double = ParametricSweep3D.DEFAULT_STEP_SIZE

    /**
     * number of discretization points for a circle or cylinder
     */
    var circleSlices: Int = Cylinder3D.DEFAULT_NUMBER_SLICES

    /**
     * true, if random ids shall be generated for the gml geometries
     */
    var generateRandomGeometryIds: Boolean = true

    /**
     * if true, additional road lines, such as the reference line, lane boundaries, etc. are also transformed
     */
    var transformAdditionalRoadLines: Boolean = false

    /**
     * if true, filler surfaces are generated to close gaps at lane transitions
     */
    var generateLongitudinalFillerSurfaces: Boolean = true

    /**
     * if true, only classes are populated that are also available in CityGML2
     */
    var mappingBackwardsCompatibility: Boolean = true

    fun build() = Roadspaces2CitygmlConfiguration(
        projectId,
        sourceFileIdentifier,
        concurrentProcessing,
        outputReportDirectoryPath,
        gmlIdPrefix,
        identifierAttributesPrefix,
        geometryAttributesPrefix,
        flattenGenericAttributeSets,
        discretizationStepSize,
        sweepDiscretizationStepSize,
        circleSlices,
        generateRandomGeometryIds,
        transformAdditionalRoadLines,
        generateLongitudinalFillerSurfaces,
        mappingBackwardsCompatibility
    )
}
