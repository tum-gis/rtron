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

package io.rtron.readerwriter.citygml

import org.citygml4j.model.CityGMLVersion as GmlCitygmlVersion

enum class CitygmlVersion {
    V1_0,
    V2_0,
    V3_0
}

fun CitygmlVersion.toGmlCitygml(): GmlCitygmlVersion = when (this) {
    CitygmlVersion.V1_0 -> GmlCitygmlVersion.v1_0
    CitygmlVersion.V2_0 -> GmlCitygmlVersion.v2_0
    CitygmlVersion.V3_0 -> GmlCitygmlVersion.v3_0
}
