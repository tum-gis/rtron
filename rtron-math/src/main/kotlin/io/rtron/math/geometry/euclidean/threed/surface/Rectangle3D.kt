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
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.transform.AffineSequence3D


/**
 * Rectangle with a certain [length] and [width] whereby the origin is located at the rectangle's center at z=0.
 *
 * @param length length of rectangle in the direction of the x axis
 * @param width width of rectangle in the direction of the y axis
 */
data class Rectangle3D (
        val length: Double,
        val width: Double,
        override val affineSequence: AffineSequence3D = AffineSequence3D.EMPTY
) : AbstractSurface3D() {

    // Properties and Initializers
    private val halfLength = length / 2.0
    private val halfWidth = width / 2.0

    private val upperLeftPoint = Vector3D(-halfLength, halfWidth, 0.0)
    private val upperRightPoint = Vector3D(halfLength, halfWidth, 0.0)
    private val lowerLeftPoint = Vector3D(-halfLength, -halfWidth, 0.0)
    private val lowerRightPoint = Vector3D(halfLength, -halfWidth, 0.0)

    // Methods
    override fun calculatePolygonsLocalCS(): Result<List<Polygon3D>, Exception> {
        val vertices = listOf(upperRightPoint, upperLeftPoint, lowerLeftPoint, lowerRightPoint)
        return Result.success(listOf(Polygon3D(vertices)))
    }

}
