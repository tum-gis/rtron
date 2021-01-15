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

package io.rtron.math.processing

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.std.ContextMessage
import io.rtron.std.distinctConsecutiveEnclosing

/**
 * Factory for building [LinearRing3D] for which multiple preparation steps are required to overcome
 * heterogeneous input.
 */
object LinearRing3DFactory {

    /**
     * Builds a [LinearRing3D] from a list of vertices by filtering and preparing the vertices.
     */
    fun buildFromVertices(vertices: List<Vector3D>, tolerance: Double):
            Result<ContextMessage<LinearRing3D>, IllegalArgumentException> {
        require(vertices.isNotEmpty()) { "List of vertices must not be empty." }

        val infos = mutableListOf<String>()

        // remove end element, if start and end element are equal
        val verticesWithoutClosing = if (vertices.first() == vertices.last())
            vertices.dropLast(1) else vertices

        // remove consecutively following point duplicates
        val verticesWithoutPointDuplicates = verticesWithoutClosing.distinctConsecutiveEnclosing { it }
        if (verticesWithoutPointDuplicates.size < verticesWithoutClosing.size)
            infos += "Removing at least one consecutively following point duplicate."

        // remove consecutively following side duplicates
        val verticesWithoutSideDuplicates = verticesWithoutPointDuplicates.removeConsecutiveSideDuplicates()
        if (verticesWithoutSideDuplicates.size != verticesWithoutPointDuplicates.size)
            infos += "Removing at least one consecutively following side duplicate of the form (…, A, B, A,…)."

        // remove vertices that are located on a line anyway
        val preparedVertices = verticesWithoutSideDuplicates
                .removeRedundantVerticesOnLineSegmentsEnclosing(tolerance)
        if (preparedVertices.size < verticesWithoutSideDuplicates.size)
            infos += "Removing at least one vertex due to linear redundancy."

        // if there are not enough points to construct a linear ring
        if (preparedVertices.size <= 2)
            return Result.error(IllegalArgumentException("A linear ring requires at least three valid vertices."))

        val linearRing = LinearRing3D(preparedVertices, tolerance)
        return Result.success(ContextMessage(linearRing, infos))
    }
}
