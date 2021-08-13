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

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.Geometry3DVisitor
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.TWO_PI
import io.rtron.math.transform.AffineSequence3D
import io.rtron.std.zipWithNextEnclosing
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents a cylinder in 3D which center is located at the local coordinate system's origin and raises in the
 * direction of the z axis.
 *
 * @param radius radius of the cylinder
 * @param height height of the cylinder
 */
data class Cylinder3D(
    val radius: Double,
    val height: Double,
    override val tolerance: Double,
    override val affineSequence: AffineSequence3D = AffineSequence3D.EMPTY,
    private val numberSlices: Int = DEFAULT_NUMBER_SLICES
) : AbstractSolid3D() {

    // Properties and Initializers
    init {
        require(radius.isFinite()) { "Radius value must be finite." }
        require(radius > tolerance) { "Radius value must be greater than zero and the tolerance threshold." }
        require(height.isFinite()) { "Height value must be finite." }
        require(height > tolerance) { "Height value must be greater than zero and the tolerance threshold." }
        require(numberSlices >= 3) { "Requiring at least three slices to construct a solid." }
    }

    val diameter = radius * 2.0

    // Methods
    override fun calculatePolygonsLocalCS(): Result<List<Polygon3D>, Exception> {

        val circleVertices = circleVertices()

        val basePolygon = Polygon3D(circleVertices.reversed().map { it.toVector3D(z = 0.0) }, tolerance)
        val topPolygon = Polygon3D(circleVertices.map { it.toVector3D(z = height) }, tolerance)

        val sidePolygons = circleVertices
            .zipWithNextEnclosing()
            .map {
                Polygon3D.of(
                    it.first.toVector3D(0.0),
                    it.second.toVector3D(0.0),
                    it.second.toVector3D(height),
                    it.first.toVector3D(height),
                    tolerance = tolerance
                )
            }

        return Result.success(sidePolygons + basePolygon + topPolygon)
    }

    /**
     * Calculates the points in 2D according to the [numberSlices].
     */
    private fun circleVertices(): List<Vector2D> =
        (0 until numberSlices)
            .map { it * TWO_PI / numberSlices }
            .map { angle -> Vector2D(radius * cos(angle), radius * sin(angle)) }

    override fun accept(visitor: Geometry3DVisitor) = visitor.visit(this)

    // Conversions
    override fun toString(): String {
        return "Cylinder(referencePose=$affineSequence, radius=$radius, height=$height)"
    }

    companion object {
        const val DEFAULT_NUMBER_SLICES: Int = 16 // used for tesselation
    }
}
