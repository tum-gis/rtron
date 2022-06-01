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

package io.rtron.transformer.converter.opendrive2roadspaces.geometry

import io.rtron.model.opendrive.additions.identifier.AbstractOpendriveIdentifier

sealed class GeometryBuilderException(val message: String, open val location: AbstractOpendriveIdentifier) {

    data class NotEnoughValidOutlineElementsForPolyhedron(override val location: AbstractOpendriveIdentifier) :
        GeometryBuilderException("A polyhedron requires at least three valid outline elements.", location)

    data class ColinearOutlineElementsForPolyhedron(override val location: AbstractOpendriveIdentifier) :
        GeometryBuilderException("A polyhedron requires at least three valid outline elements, which are not colinear (located on a line).", location)

    data class TriangulationException(val reason: String, override val location: AbstractOpendriveIdentifier) :
        GeometryBuilderException("Triangulation algorithm failed: $reason", location)

    data class NotEnoughValidOutlineElementsForLinearRing(override val location: AbstractOpendriveIdentifier) :
        GeometryBuilderException("A linear ring requires at least three valid vertices.", location)
}

fun GeometryBuilderException.toIllegalStateException() = IllegalStateException(this.message)
