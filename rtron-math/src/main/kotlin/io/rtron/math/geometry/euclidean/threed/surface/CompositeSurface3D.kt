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
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import io.rtron.math.geometry.GeometryException

/**
 * Represents a composition of multiple surface members.
 *
 * @param surfaceMembers surface members to be composited
 */
class CompositeSurface3D(
    private val surfaceMembers: NonEmptyList<AbstractSurface3D>,
) : AbstractSurface3D() {
    // Properties and Initializers
    init {
        require(surfaceMembers.isNotEmpty()) { "Composite surface must contain members." }
        require(
            surfaceMembers.all { surfaceMembers.first().tolerance == it.tolerance },
        ) { "All surface members must have the same tolerance." }
    }

    override val tolerance: Double get() = surfaceMembers.first().tolerance

    // Secondary Constructors
    constructor(surfaceMember: AbstractSurface3D) : this(nonEmptyListOf(surfaceMember))

    // Methods
    override fun calculatePolygonsLocalCS(): Either<GeometryException.BoundaryRepresentationGenerationError, NonEmptyList<Polygon3D>> =
        either {
            val polygons: List<Polygon3D> = surfaceMembers.map { it.calculatePolygonsGlobalCS().bind() }.flatten()
            polygons.let { it.toNonEmptyListOrNull()!! }
        }
}
