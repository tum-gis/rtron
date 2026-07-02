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

package io.rtron.model.citygml.code

import org.xmlobjects.gml.model.basictypes.Code

enum class SolitaryVegetationObjectClassCode(
    val code: Code,
) {
    SHRUB(Code("1000")),
    LOW_PLANTS(Code("1010")),
    MEDIUM_HIGH_PLANTS(Code("1020")),
    HIGH_PLANTS(Code("1030")),
    GRASSES(Code("1040")),
    FERNS(Code("1050")),
    CONIFEROUS_TREE(Code("1060")),
    DECIDUOUS_TREE(Code("1070")),
    BUSHES(Code("1080")),
    AQUATIC_PLANTS(Code("1090")),
    CLIMBER(Code("1100")),
    UNKNOWN(Code("9999")),
}

enum class SolitaryVegetationObjectFunctionCode(
    val code: Code,
) {
    SHRUB(Code("1000")),
    LOW_PLANTS(Code("1010")),
    MEDIUM_HIGH_PLANTS(Code("1020")),
    HIGH_PLANTS(Code("1030")),
    GRASSES(Code("1040")),
    FERNS(Code("1050")),
    CONIFEROUS_TREE(Code("1060")),
    DECIDUOUS_TREE(Code("1070")),
    BUSHES(Code("1080")),
    AQUATIC_PLANTS(Code("1090")),
    CLIMBER(Code("1100")),
    UNKNOWN(Code("9999")),
}

enum class SolitaryVegetationObjectUsageCode(
    val code: Code,
) {
    SHRUB(Code("1000")),
    LOW_PLANTS(Code("1010")),
    MEDIUM_HIGH_PLANTS(Code("1020")),
    HIGH_PLANTS(Code("1030")),
    GRASSES(Code("1040")),
    FERNS(Code("1050")),
    CONIFEROUS_TREE(Code("1060")),
    DECIDUOUS_TREE(Code("1070")),
    BUSHES(Code("1080")),
    AQUATIC_PLANTS(Code("1090")),
    CLIMBER(Code("1100")),
    UNKNOWN(Code("9999")),
}
