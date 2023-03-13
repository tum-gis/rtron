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

package io.rtron.math.processing.triangulation

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.continuations.either
import arrow.core.left
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D

/**
 * Abstract class for a triangulation algorithm in 3D.
 * See the wikipedia article of [polygon triangulation](https://en.wikipedia.org/wiki/Polygon_triangulation).
 */
abstract class TriangulationAlgorithm {

    /**
     * Performs the triangulation operation and checks whether all input vertices are still represented
     * after triangulation.
     *
     * @param vertices list of vertices representing the outline to be triangulated
     * @return list of triangulated [Polygon3D]
     */
    fun triangulateChecked(vertices: NonEmptyList<Vector3D>, tolerance: Double): Either<TriangulatorException, List<Polygon3D>> = either.eager {

        val triangles = triangulate(vertices, tolerance).bind()

        val newVertices = triangles.flatMap { it.vertices }
        if (newVertices.any { it !in vertices })
            TriangulatorException.DifferentVertices().left().bind<List<Polygon3D>>()

        triangles
    }

    /**
     * Triangulation algorithm implemented by concrete classes.
     *
     * @param vertices list of vertices representing the outline to be triangulated
     * @return list of triangulated [Polygon3D]
     */
    internal abstract fun triangulate(vertices: List<Vector3D>, tolerance: Double): Either<TriangulatorException, List<Polygon3D>>
}
