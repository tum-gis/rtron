/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.converter.roadspaces2citygml.geometry

import io.rtron.math.geometry.GeometryException

sealed class GeometryTransformerException(val message: String) {
    data class NoSelectedPolygonsAvailable(val reason: String = "") : GeometryTransformerException("No MultiSurface geometry available. $reason")
    data class NoSuiteableSourceGeometry(val targetGeometry: String) : GeometryTransformerException("No suitable source geometry found for populating the $targetGeometry")
    data class GeometryGenerationException(val reason: String) : GeometryTransformerException("Error when generating the geometry. $reason")
}

fun GeometryException.toGeometryGenerationException() = GeometryTransformerException.GeometryGenerationException(message)
