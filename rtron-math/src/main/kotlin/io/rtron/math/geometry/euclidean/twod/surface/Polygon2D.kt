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

package io.rtron.math.geometry.euclidean.twod.surface

import arrow.core.NonEmptyList
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.std.noneWithNextEnclosing
import java.awt.geom.Path2D

/**
 * Planar polygon consisting of a list of [vertices].
 *
 * @param vertices vertices of the polygon must be located in a plane
 */
data class Polygon2D(
    val vertices: NonEmptyList<Vector2D>,
    override val tolerance: Double,
) : AbstractSurface2D() {

    // Properties and Initializers
    private val numberOfVertices = vertices.size
    init {
        require(numberOfVertices >= 3) { "Not enough vertices provided for constructing a polygon." }
        require(vertices.noneWithNextEnclosing { a, b -> a.fuzzyEquals(b, tolerance) }) { "Consecutively following point duplicates found." }
    }

    private val awtPath by lazy {
        val path: Path2D = Path2D.Double()
        path.moveTo(vertices.first().x, vertices.first().y)
        vertices.tail.forEach {
            path.lineTo(it.x, it.y)
        }
        path.closePath()
        path
    }

    // Methods
    override fun contains(point: Vector2D): Boolean {
        return awtPath.contains(point.x, point.y)
    }
}
