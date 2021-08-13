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

package io.rtron.math.processing.triangulation

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import io.rtron.math.processing.isPlanar
import io.rtron.std.handleSuccess

@RequiresOptIn(message = "The triangulation functionality is experimental.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalTriangulator

/**
 * Performs triangulation operations on polygons in 3D.
 * See the wikipedia article of [polygon triangulation](https://en.wikipedia.org/wiki/Polygon_triangulation).
 */
@ExperimentalTriangulator
object Triangulator {

    private val standardTriangulationAlgorithm = Poly2TriTriangulationAlgorithm()
    private val fallbackTriangulationAlgorithm = ProjectedTriangulationAlgorithm(standardTriangulationAlgorithm)
    private val fanTriangulationAlgorithm = FanTriangulationAlgorithm()

    /**
     * Returns a list of triangles from a [linearRing].
     *
     * @param linearRing linear ring to be triangulated
     */
    fun triangulate(linearRing: LinearRing3D, tolerance: Double): Result<List<Polygon3D>, Exception> {

        if (linearRing.vertices.isPlanar(tolerance))
            return Result.success(listOf(Polygon3D(linearRing.vertices, tolerance)))

        // run triangulation algorithms until one succeeds
        val errorStandard = standardTriangulationAlgorithm.triangulateChecked(linearRing.vertices, tolerance)
            .handleSuccess { return it }
        fallbackTriangulationAlgorithm.triangulateChecked(linearRing.vertices, tolerance)
            .handleSuccess { return it }
        fanTriangulationAlgorithm.triangulateChecked(linearRing.vertices, tolerance)
            .handleSuccess { return it }

        return Result.error(errorStandard)
    }
}
