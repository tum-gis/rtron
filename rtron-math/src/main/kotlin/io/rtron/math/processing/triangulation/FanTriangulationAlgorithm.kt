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
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D

/**
 * Fan triangulation algorithm by simply selecting a base vertex and generating the triangles by iterating over
 * the remaining vertices. However, this approach is not suitable for concave polygons.
 * See the wikipedia article on [fan triangulation](https://en.wikipedia.org/wiki/Fan_triangulation).
 */
class FanTriangulationAlgorithm : TriangulationAlgorithm() {

    override fun triangulate(vertices: NonEmptyList<Vector3D>, tolerance: Double): Either<TriangulatorException, List<Polygon3D>> {
        if (vertices.tail.any { vertices.head.fuzzyEquals(it, tolerance) }) {
            return TriangulatorException.FirstVertexDuplicated().left()
        }

        val polygons = vertices.tail
            .zipWithNext()
            .map { Polygon3D(nonEmptyListOf(vertices.head, it.first, it.second), tolerance) }

        return polygons.right()
    }
}
