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

package io.rtron.math.geometry.euclidean.threed.solid

import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.nonEmptyListOf
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import io.rtron.math.std.DEFAULT_TOLERANCE
import io.rtron.math.transform.AffineSequence3D

/**
 * Represents a cuboid in 3D with the dimension ([length], [width], [height]). The origin of the local coordinate system
 * is located at the center of the ground face.
 *
 * @param length length of cuboid in the direction of the x axis
 * @param width width of cuboid in the direction of the y axis
 * @param height height of the cuboid in the direction of the z axis
 */
data class Cuboid3D(
    val length: Double,
    val width: Double,
    val height: Double,
    override val tolerance: Double,
    override val affineSequence: AffineSequence3D = AffineSequence3D.EMPTY
) : AbstractSolid3D() {

    // Properties and Initializers
    init {
        require(length.isFinite()) { "Length value must be finite." }
        require(length > tolerance) { "Length value must be greater than zero and the tolerance threshold." }
        require(width.isFinite()) { "Width value must be finite." }
        require(width > tolerance) { "Width value must be greater than zero and the tolerance threshold." }
        require(height.isFinite()) { "Height value must be finite." }
        require(height > tolerance) { "Height value must be greater than zero and the tolerance threshold." }
    }

    private val halfLength = length / 2.0
    private val halfWidth = width / 2.0

    // see the wikipedia article on [quadrants](https://en.wikipedia.org/wiki/Quadrant_(plane_geometry))
    private val vertexBaseQuadrantI = Vector3D(halfLength, halfWidth, 0.0)
    private val vertexBaseQuadrantII = Vector3D(-halfLength, halfWidth, 0.0)
    private val vertexBaseQuadrantIII = Vector3D(-halfLength, -halfWidth, 0.0)
    private val vertexBaseQuadrantIV = Vector3D(halfLength, -halfWidth, 0.0)

    private val vertexElevatedQuadrantI = Vector3D(halfLength, halfWidth, height)
    private val vertexElevatedQuadrantII = Vector3D(-halfLength, halfWidth, height)
    private val vertexElevatedQuadrantIII = Vector3D(-halfLength, -halfWidth, height)
    private val vertexElevatedQuadrantIV = Vector3D(halfLength, -halfWidth, height)

    private val basePolygon = Polygon3D.of(
        vertexBaseQuadrantI,
        vertexBaseQuadrantIV,
        vertexBaseQuadrantIII,
        vertexBaseQuadrantII,
        tolerance = tolerance
    )

    private val elevatedPolygon = Polygon3D.of(
        vertexElevatedQuadrantI,
        vertexElevatedQuadrantII,
        vertexElevatedQuadrantIII,
        vertexElevatedQuadrantIV,
        tolerance = tolerance
    )

    private val frontPolygon = Polygon3D.of(
        vertexBaseQuadrantI,
        vertexElevatedQuadrantI,
        vertexElevatedQuadrantIV,
        vertexBaseQuadrantIV,
        tolerance = tolerance
    )

    private val leftPolygon = Polygon3D.of(
        vertexBaseQuadrantI,
        vertexBaseQuadrantII,
        vertexElevatedQuadrantII,
        vertexElevatedQuadrantI,
        tolerance = tolerance
    )

    private val backPolygon = Polygon3D.of(
        vertexBaseQuadrantII,
        vertexBaseQuadrantIII,
        vertexElevatedQuadrantIII,
        vertexElevatedQuadrantII,
        tolerance = tolerance
    )

    private val rightPolygon = Polygon3D.of(
        vertexElevatedQuadrantIV,
        vertexElevatedQuadrantIII,
        vertexBaseQuadrantIII,
        vertexBaseQuadrantIV,
        tolerance = tolerance
    )

    // Methods
    override fun calculatePolygonsLocalCS(): NonEmptyList<Polygon3D> {
        return nonEmptyListOf(basePolygon, elevatedPolygon, frontPolygon, leftPolygon, backPolygon, rightPolygon)
    }

    // Conversions
    override fun toString(): String {
        return "Cuboid3D(referencePose=$affineSequence, width=$length, height=$width, depth=$height)"
    }

    companion object {
        val UNIT = Cuboid3D(1.0, 1.0, 1.0, DEFAULT_TOLERANCE)

        fun of(length: Option<Double>, width: Option<Double>, height: Option<Double>, tolerance: Double, affineSequence: AffineSequence3D = AffineSequence3D.EMPTY): Cuboid3D {
            require(length.isSome()) { "Length must be defined." }
            require(width.isSome()) { "Width must be defined." }
            require(height.isSome()) { "Height must be defined." }

            return Cuboid3D(length.getOrNull()!!, width.getOrNull()!!, height.getOrNull()!!, tolerance, affineSequence)
        }
    }
}
