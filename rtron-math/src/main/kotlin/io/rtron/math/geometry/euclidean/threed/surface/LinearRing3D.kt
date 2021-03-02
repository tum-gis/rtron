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

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.Geometry3DVisitor
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.linear.dimensionOfSpan
import io.rtron.math.processing.isPlanar
import io.rtron.math.processing.triangulation.ExperimentalTriangulator
import io.rtron.math.processing.triangulation.Triangulator
import io.rtron.math.range.Tolerable
import io.rtron.math.transform.AffineSequence3D
import io.rtron.std.filterWithNextEnclosing
import io.rtron.std.noneWithNextEnclosing

/**
 * Linear ring of a list of [vertices]. The linear ring is not required to be planar.
 *
 * @param vertices vertices for constructing the linear ring
 */
data class LinearRing3D(
    val vertices: List<Vector3D>,
    override val tolerance: Double,
    override val affineSequence: AffineSequence3D = AffineSequence3D.EMPTY
) : AbstractSurface3D(), Tolerable {

    // Properties and Initializers
    private val numberOfVertices = vertices.size

    /** list of edges between the first vertex and all other vertices */
    private val innerEdges = vertices.filterIndexed { index, _ -> index != 0 }.map { it - vertices.first() }

    /** dimension of the polygon's span used to check whether the polygon is planar */
    private val dimensionSpan = innerEdges.map { it.toRealVector() }.dimensionOfSpan()

    init {
        require(numberOfVertices >= 3) { "Not enough vertices provided for constructing a linear ring." }
        require(vertices.noneWithNextEnclosing { a, b -> a.fuzzyEquals(b, tolerance) }) { "Consecutively following point duplicates found." }
        require(dimensionSpan >= 2) { "The dimension of the span is too low ($dimensionSpan), which might be caused by colinear vertices (all vertices located on a line)." }
    }

    // Methods

    /** Returns true if the vertices are placed in a plane */
    fun isPlanar() = vertices.isPlanar(tolerance)

    @OptIn(ExperimentalTriangulator::class)
    override fun calculatePolygonsLocalCS(): Result<List<Polygon3D>, Exception> =
        Triangulator.triangulate(this, tolerance)

    override fun accept(visitor: Geometry3DVisitor) = visitor.visit(this)

    companion object {
        val UNIT = LinearRing3D(listOf(-Vector3D.X_AXIS, Vector3D.X_AXIS, Vector3D.Y_AXIS), 0.0)

        /**
         * Creates a linear ring based on the provided [vertices].
         */
        fun of(vararg vertices: Vector3D, tolerance: Double) =
            LinearRing3D(vertices.toList(), tolerance)

        /**
         * Creates multiple linear rings from two lists of vertices [leftVertices] and [rightVertices].
         * A list of linear rings are created by iterating over both lists jointly.
         *
         * @param leftVertices left vertices for the linear rings construction
         * @param rightVertices right vertices for the linear rings construction
         */
        fun of(leftVertices: List<Vector3D>, rightVertices: List<Vector3D>, tolerance: Double): List<LinearRing3D> {
            require(leftVertices.size >= 2) { "At least two left vertices required." }
            require(rightVertices.size >= 2) { "At least two right vertices required." }

            data class VertexPair(val left: Vector3D, val right: Vector3D)
            val vertexPairs = leftVertices.zip(rightVertices).map { VertexPair(it.first, it.second) }

            val linearRingVertices = vertexPairs.zipWithNext()
                .map { listOf(it.first.right, it.second.right, it.second.left, it.first.left) }

            return linearRingVertices.map { LinearRing3D(it, tolerance) }
        }

        /**
         * Creates multiple linear rings from two lists of vertices [leftVertices] and [rightVertices].
         * A list of linear rings are created by iterating over both lists jointly.
         * Possible consecutively following point duplicates are removed before construction.
         *
         * @param leftVertices left vertices for the linear rings construction
         * @param rightVertices right vertices for the linear rings construction
         */
        fun ofWithDuplicatesRemoval(leftVertices: List<Vector3D>, rightVertices: List<Vector3D>, tolerance: Double):
            Result<List<LinearRing3D>, Exception> {
                require(leftVertices.size >= 2) { "At least two left vertices required." }
                require(rightVertices.size >= 2) { "At least two right vertices required." }

                data class VertexPair(val left: Vector3D, val right: Vector3D)
                val vertexPairs = leftVertices.zip(rightVertices).map { VertexPair(it.first, it.second) }

                val linearRings: List<LinearRing3D> = vertexPairs
                    .zipWithNext()
                    .map { listOf(it.first.right, it.second.right, it.second.left, it.first.left) }
                    .map { currentVertices -> currentVertices.filterWithNextEnclosing { a, b -> a.fuzzyUnequals(b, tolerance) } }
                    .filter { it.distinct().count() >= 3 }
                    .map { LinearRing3D(it, tolerance) }

                return if (linearRings.isEmpty()) Result.error(IllegalArgumentException("Not enough valid linear rings could be constructed."))
                else Result.success(linearRings)
            }
    }
}
