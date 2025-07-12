/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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
import arrow.core.raise.either
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import io.rtron.math.processing.calculateBestFittingPlane
import io.rtron.math.transform.Affine3D
import io.rtron.math.transform.AffineSequence3D

/**
 * This algorithm wraps a [triangulationAlgorithm]. Before triangulating the vertices a best fitting plane is estimated
 * and the vertices are projected onto the plane in 2D. After performing the triangulation, the triangulation
 * order is used to recreate the triangles in 3D.
 *
 * @param triangulationAlgorithm actual triangulation algorithm applied after plane projection
 */
class ProjectedTriangulationAlgorithm(
    private val triangulationAlgorithm: TriangulationAlgorithm,
) : TriangulationAlgorithm() {
    override fun triangulate(
        vertices: NonEmptyList<Vector3D>,
        tolerance: Double,
    ): Either<TriangulatorException, List<Polygon3D>> =
        either {
            val projectedVertices = projectVertices(vertices, tolerance)
            val projectedPolygonsTriangulated =
                triangulationAlgorithm
                    .triangulate(projectedVertices, tolerance)
                    .bind()

            projectedPolygonsTriangulated.map { constructPolygon(it, projectedVertices, vertices, tolerance).bind() }
        }

    /**
     * Projects the [vertices] into a best fitting plane.
     */
    private fun projectVertices(
        vertices: NonEmptyList<Vector3D>,
        tolerance: Double,
    ): NonEmptyList<Vector3D> {
        val affine =
            run {
                val plane = vertices.calculateBestFittingPlane(tolerance)
                val affineTranslation = Affine3D.of(plane.point)
                val affineNewBasis = Affine3D.of(plane.vectorU, plane.vectorV, plane.normal)
                AffineSequence3D.of(affineTranslation, affineNewBasis).solve()
            }
        return affine.transform(vertices)
    }

    /**
     * Constructs the triangulated polygon with the original vertices in 3D.
     * The list of [allProjectedVertices] and [allOriginalVertices] are compared by their index for constructing
     * the triangulated polygon with the original vertices based on the information of the [projectedPolygon].
     *
     * @param projectedPolygon triangulated polygon in projected plane
     * @param allProjectedVertices list of all project vertices within the plane
     * @param allOriginalVertices list of all original vertices which must have the same order as [allProjectedVertices]
     * @return triangulated [Polygon3D] with the original vertices
     */
    private fun constructPolygon(
        projectedPolygon: Polygon3D,
        allProjectedVertices: List<Vector3D>,
        allOriginalVertices: List<Vector3D>,
        tolerance: Double,
    ): Either<TriangulatorException, Polygon3D> {
        if (!allProjectedVertices.containsAll(projectedPolygon.vertices)) {
            return TriangulatorException.DifferentVertices().left()
        }

        val constructedPolygon =
            projectedPolygon.vertices
                .map { allProjectedVertices.indexOf(it) }
                .map { allOriginalVertices[it] }
                .let { Polygon3D(it, tolerance) }
        return Either.Right(constructedPolygon)
    }
}
