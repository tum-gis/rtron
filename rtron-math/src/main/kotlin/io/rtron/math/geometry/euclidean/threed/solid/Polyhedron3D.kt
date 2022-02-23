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

package io.rtron.math.geometry.euclidean.threed.solid

import arrow.core.Either
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import io.rtron.math.transform.AffineSequence3D

/**
 * Represents a polyhedron in 3D.
 *
 * @param polygons faces of the polyhedron geometry
 */
data class Polyhedron3D(
    val polygons: List<Polygon3D>,
    override val tolerance: Double,
    override val affineSequence: AffineSequence3D = AffineSequence3D.EMPTY
) : AbstractSolid3D() {

    // Properties and Initializers
    init {
        require(polygons.size >= 4) { "Polyhedron must have at least four polygons." }
    }

    // Methods
    override fun calculatePolygonsLocalCS(): Either<Exception, List<Polygon3D>> = Either.Right(polygons)
}
