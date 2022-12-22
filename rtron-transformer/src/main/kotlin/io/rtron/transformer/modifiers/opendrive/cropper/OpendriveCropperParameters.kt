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

package io.rtron.transformer.modifiers.opendrive.cropper

import arrow.core.Option
import arrow.core.toNonEmptyListOrNull
import arrow.core.toOption
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.geometry.euclidean.twod.surface.Polygon2D
import kotlinx.serialization.Serializable

@Serializable
data class OpendriveCropperParameters(
    /** allowed tolerance when comparing double values */
    val numberTolerance: Double,
    /** x values of cropping polygon */
    val cropPolygonX: List<Double>,
    /** y values of cropping polygon */
    val cropPolygonY: List<Double>,
) {

    // Properties and Initializers
    init {
        require(cropPolygonX.isEmpty() || cropPolygonX.size >= 3) { "cropPolygonX must be empty or have at least three values." }
        require(cropPolygonY.isEmpty() || cropPolygonX.size >= 3) { "cropPolygonY must be empty or have at least three values." }
        require(cropPolygonX.size == cropPolygonY.size) { "cropPolygonX must have the same number of values as cropPolygonY." }
    }

    // Methods
    fun getPolygon(): Option<Polygon2D> {
        val vertices = cropPolygonX.zip(cropPolygonY)
            .map { Vector2D(it.first, it.second) }
            .toNonEmptyListOrNull()
            .toOption()
        return vertices.map { Polygon2D(it, numberTolerance) }
    }

    companion object {
        val DEFAULT_CROP_POLYGON_X = emptyList<Double>()
        val DEFAULT_CROP_POLYGON_Y = emptyList<Double>()
        const val DEFAULT_NUMBER_TOLERANCE = 1E-7
    }
}
