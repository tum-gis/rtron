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
import io.rtron.math.geometry.euclidean.threed.Geometry3DVisitor
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.std.TWO_PI
import io.rtron.math.transform.AffineSequence3D
import kotlin.math.cos
import kotlin.math.sin


/**
 * Represents a circle with a certain [radius] in 3D.
 *
 * @param radius radius of the circle
 * @param numberSlices number of discretization steps for polygon construction
 */
data class Circle3D(
        val radius: Double,
        override val tolerance: Double,
        override val affineSequence: AffineSequence3D = AffineSequence3D.EMPTY,
        private val numberSlices: Int = DEFAULT_NUMBER_SLICES
) : AbstractSurface3D() {

    // Properties and Initializers
    init {
        require(radius.isFinite() && radius > 0.0) { "Radius must be finite and greater than zero." }
        require(numberSlices > 0.0) { "Number of slices must be greater than zero." }
    }

    // Methods
    override fun calculatePolygonsLocalCS(): Result<List<Polygon3D>, NoException> {
        val polygon = (0 until numberSlices)
                .map { TWO_PI * it / numberSlices }
                .map { calculatePoint(it) }
                .let { Polygon3D(it, tolerance) }

        return Result.success(listOf(polygon))
    }

    /** Calculates a point the circle based on the [angle] around the origin. */
    private fun calculatePoint(angle: Double = 0.0) =
            Vector3D(radius * cos(angle), radius * sin(angle), 0.0)

    override fun accept(visitor: Geometry3DVisitor) = visitor.visit(this)

    companion object {
        private const val DEFAULT_NUMBER_SLICES: Int = 16 // used for tesselation
    }
}
