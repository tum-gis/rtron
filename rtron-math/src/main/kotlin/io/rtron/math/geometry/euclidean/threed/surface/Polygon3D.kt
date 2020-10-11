/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.geometry.euclidean.threed.surface

import com.github.kittinunf.result.NoException
import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.linear.dimensionOfSpan
import io.rtron.math.processing.calculateNormal
import io.rtron.math.processing.isPlanar
import io.rtron.math.std.DEFAULT_TOLERANCE
import io.rtron.math.transform.AffineSequence3D
import io.rtron.std.distinctConsecutiveEnclosing


/**
 * Planar polygon consisting of a list of [vertices].
 *
 * @param vertices vertices of the polygon must be located in a plane
 */
data class Polygon3D(
        val vertices: List<Vector3D> = listOf(),
        override val tolerance: Double,
        override val affineSequence: AffineSequence3D = AffineSequence3D.EMPTY
) : AbstractSurface3D() {

    // Properties and Initializers
    private val numberOfVertices = vertices.size

    /** list of edges between the first vertex and all other vertices */
    private val innerEdges = vertices.filterIndexed { index, _ -> index != 0 }.map { it - vertices.first() }

    /** dimension of the polygon's span used to check whether the polygon is planar */
    private val dimensionSpan = innerEdges.map { it.toRealVector() }.dimensionOfSpan()

    init {
        require(numberOfVertices >= 3)
        { "Not enough vertices provided for constructing a polygon." }
        require(vertices.distinctConsecutiveEnclosing { it }.size == vertices.size)
        { "Consecutively following point duplicates found." }
        require(dimensionSpan >= 2)
        { "The dimension of the span is too low ($dimensionSpan) which might be caused by colinear vertices." }
        require(vertices.isPlanar(tolerance))
        { "The vertices of a polygon must be located in a plane." }
    }


    // Methods

    /** Returns the normal of the polygon. */
    fun getNormal(): Result<Vector3D, IllegalStateException> =
            this.vertices.calculateNormal().normalized().let { Result.success(it) }

    /** Returns a new polygon with an opposite facing by reversing the vertices order */
    fun reversed() = Polygon3D(vertices.reversed(), tolerance, affineSequence)

    override fun calculatePolygonsLocalCS(): Result<List<Polygon3D>, NoException> = Result.success(listOf(this))

    // Conversions
    /** Returns the coordinates of all vertices as a flattened list */
    fun toVertexPositionElementList() = this.vertices.flatMap { it.toDoubleList() }

    companion object {
        val TETRAGON = of(
                Vector3D(-1.0, -1.0, 0.0),
                Vector3D(-1.0, 1.0, 0.0),
                Vector3D(1.0, 1.0, 0.0),
                Vector3D(1.0, -1.0, 0.0), tolerance = DEFAULT_TOLERANCE)

        /**
         * Constructs a polygon based on the [vectors].
         */
        fun of(vararg vectors: Vector3D, tolerance: Double) = Polygon3D(vectors.toList(), tolerance)

        /**
         * Constructs a polygon based on a [Triple] of [vectors].
         */
        fun of(vectors: Triple<Vector3D, Vector3D, Vector3D>, tolerance: Double) =
                Polygon3D(vectors.toList(), tolerance)
    }
}
