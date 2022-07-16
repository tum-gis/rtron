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

import io.rtron.math.geometry.euclidean.threed.solid.Cylinder3D
import io.rtron.math.geometry.euclidean.threed.solid.ParametricSweep3D
import java.util.regex.Pattern

data class Roadspaces2CitygmlConfiguration(
    val concurrentProcessing: Boolean,

    /** prefix for generated gml ids */
    val gmlIdPrefix: String = DEFAULT_GML_ID_PREFIX,
    /** prefix for identifier attribute names */
    val identifierAttributesPrefix: String = DEFAULT_IDENTIFIER_ATTRIBUTES_PREFIX,
    /** prefix for geometry attribute names */
    val geometryAttributesPrefix: String = DEFAULT_GEOMETRY_ATTRIBUTES_PREFIX,
    /** true, if nested attribute lists shall be flattened out */
    val flattenGenericAttributeSets: Boolean = DEFAULT_FLATTEN_GENERIC_ATTRIBUTE_SETS,
    /** distance between each discretization step for curves and surfaces */
    val discretizationStepSize: Double = DEFAULT_DISCRETIZATION_STEP_SIZE,
    /** distance between each discretization step for solid geometries of [ParametricSweep3D] */
    val sweepDiscretizationStepSize: Double = DEFAULT_SWEEP_DISCRETIZATION_STEP_SIZE,
    /** number of discretization points for a circle or cylinder */
    val circleSlices: Int = DEFAULT_CIRCLE_SLICES,
    /** true, if random ids shall be generated for the gml geometries */
    val generateRandomGeometryIds: Boolean = DEFAULT_GENERATE_RANDOM_GEOMETRY_IDS,
    /** if true, additional road lines, such as the reference line, lane boundaries, etc. are also transformed */
    val transformAdditionalRoadLines: Boolean = DEFAULT_TRANSFORM_ADDITIONAL_ROAD_LINES,
    /** if true, filler surfaces are generated to close gaps at lane transitions */
    val generateLongitudinalFillerSurfaces: Boolean = DEFAULT_GENERATE_LONGITUDINAL_FILLER_SURFACES,
    /** if true, only classes are populated that are also available in CityGML2 */
    val mappingBackwardsCompatibility: Boolean = DEFAULT_MAPPING_BACKWARDS_COMPATIBILITY
) {

    init {
        require(PATTERN_NCNAME.matcher(gmlIdPrefix).matches()) { "Provided gmlIdPrefix ($gmlIdPrefix) requires valid NCName pattern." }
    }

    companion object {
        val PATTERN_NCNAME: Pattern = Pattern.compile("[_\\p{L}][-_.\\p{L}0-9]*")!!

        const val DEFAULT_GML_ID_PREFIX = "UUID_"
        const val DEFAULT_IDENTIFIER_ATTRIBUTES_PREFIX = "identifier_"
        const val DEFAULT_GEOMETRY_ATTRIBUTES_PREFIX = "geometry_"
        const val DEFAULT_FLATTEN_GENERIC_ATTRIBUTE_SETS = true
        const val DEFAULT_DISCRETIZATION_STEP_SIZE = 0.7
        const val DEFAULT_SWEEP_DISCRETIZATION_STEP_SIZE = ParametricSweep3D.DEFAULT_STEP_SIZE
        const val DEFAULT_CIRCLE_SLICES = Cylinder3D.DEFAULT_NUMBER_SLICES
        const val DEFAULT_GENERATE_RANDOM_GEOMETRY_IDS = true
        const val DEFAULT_TRANSFORM_ADDITIONAL_ROAD_LINES = false
        const val DEFAULT_GENERATE_LONGITUDINAL_FILLER_SURFACES = true
        const val DEFAULT_MAPPING_BACKWARDS_COMPATIBILITY = true
    }
}
