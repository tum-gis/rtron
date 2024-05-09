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

package io.rtron.math.geometry.euclidean.threed.surface

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import io.rtron.math.geometry.GeometryException
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.math.geometry.toIllegalStateException
import io.rtron.math.range.DefinableDomain
import io.rtron.math.range.Range
import io.rtron.math.range.Tolerable

data class ParametricBoundedSurface3D(
    val leftBoundary: Curve3D,
    val rightBoundary: Curve3D,
    override val tolerance: Double,
    private val discretizationStepSize: Double,
) : AbstractSurface3D(), DefinableDomain<Double>, Tolerable {
    // Properties and Initializers
    init {
        require(leftBoundary.domain == rightBoundary.domain) { "Boundary curves must have the identical domain." }
        require(length > tolerance) { "Length must be greater than zero as well as the tolerance threshold." }
    }

    override val domain: Range<Double>
        get() = leftBoundary.domain

    val length: Double
        get() = leftBoundary.length

    private val leftVertices by lazy {
        val vertices =
            leftBoundary.calculatePointListGlobalCS(discretizationStepSize)
                .mapLeft { it.toIllegalStateException() }
                .getOrElse { throw it }
        vertices.toNonEmptyListOrNull()!!
    }

    private val rightVertices by lazy {
        val vertices =
            rightBoundary.calculatePointListGlobalCS(discretizationStepSize)
                .mapLeft { it.toIllegalStateException() }
                .getOrElse { throw it }
        vertices.toNonEmptyListOrNull()!!
    }

    // Methods

    override fun calculatePolygonsLocalCS(): Either<GeometryException.BoundaryRepresentationGenerationError, NonEmptyList<Polygon3D>> =
        either {
            LinearRing3D.ofWithDuplicatesRemoval(leftVertices, rightVertices, tolerance)
                .mapLeft { GeometryException.BoundaryRepresentationGenerationError(it.message) }
                .bind()
                .map { it.calculatePolygonsGlobalCS().bind() }
                .flatten()
                .let { it.toNonEmptyListOrNull()!! }
        }

    companion object {
        const val DEFAULT_STEP_SIZE: Double = 0.3 // used for tesselation
    }
}
