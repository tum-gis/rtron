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

import org.citygml4j.core.model.core.AbstractOccupiedSpace

/**
 * Populates the LOD implicit geometry of an [AbstractOccupiedSpace] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 */
fun AbstractOccupiedSpace.populateLod1ImplicitGeometry(geometryTransformer: GeometryTransformer) {
    val implicitGeometryPropertyResult = geometryTransformer.getImplicitGeometry()
    require(implicitGeometryPropertyResult.isDefined()) { "Must contain implicit geometry." }

    lod1ImplicitRepresentation = implicitGeometryPropertyResult.orNull()!!
}
