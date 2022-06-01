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
import java.util.regex.Pattern

data class Roadspaces2CitygmlConfiguration(
    val projectId: String,
    val sourceFileIdentifier: FileIdentifier,
    val concurrentProcessing: Boolean,

    val outputReportDirectoryPath: Path,

    val gmlIdPrefix: String,
    val identifierAttributesPrefix: String,
    val geometryAttributesPrefix: String,
    val flattenGenericAttributeSets: Boolean,
    val discretizationStepSize: Double,
    val sweepDiscretizationStepSize: Double,
    val circleSlices: Int,
    val generateRandomGeometryIds: Boolean,
    val transformAdditionalRoadLines: Boolean,
    val generateLongitudinalFillerSurfaces: Boolean,
    val mappingBackwardsCompatibility: Boolean
) {

    init {
        require(PATTERN_NCNAME.matcher(gmlIdPrefix).matches()) { "Provided gmlIdPrefix ($gmlIdPrefix) requires valid NCName pattern." }
    }

    val outputReportFilePath: Path = outputReportDirectoryPath.resolve(Path("reports/converter/roadspaces2citygml/conversion.json"))

    // Conversions
    override fun toString(): String =
        "Roadspaces2CitygmlConfiguration(gmlIdPrefix=$gmlIdPrefix, identifierAttributesPrefix=$identifierAttributesPrefix, " +
            "flattenGenericAttributeSets=$flattenGenericAttributeSets, discretizationStepSize=$discretizationStepSize, " +
            "sweepDiscretizationStepSize=$sweepDiscretizationStepSize, circleSlices=$circleSlices, " +
            "generateRandomGeometryIds=$generateRandomGeometryIds, transformAdditionalRoadLines=$transformAdditionalRoadLines, " +
            "mappingBackwardsCompatibility=$mappingBackwardsCompatibility)"

    companion object {
        val PATTERN_NCNAME: Pattern = Pattern.compile("[_\\p{L}][-_.\\p{L}0-9]*")!!
    }
}
