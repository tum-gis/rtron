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

package io.rtron.math.geometry.euclidean.threed.curve

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNone
import io.rtron.math.container.ConcatenationContainer
import io.rtron.math.geometry.GeometryException
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.Range
import io.rtron.std.cumulativeSum
import io.rtron.std.filterWithNext
import io.rtron.std.noneWithNext

/**
 * Curve specified by a sequence of [vertices].
 *
 * @param vertices linearly connected vertices
 * @param tolerance allowed tolerance
 */
class LineString3D(
    val vertices: NonEmptyList<Vector3D>,
    override val tolerance: Double,
) : AbstractCurve3D() {
    // Properties and Initializers
    init {
        require(vertices.size >= 2) { "Must at least contain two vertices." }
        require(
            vertices.noneWithNext {
                a,
                b,
                ->
                a.fuzzyEquals(b, tolerance)
            },
        ) { "Must not contain consecutively following point duplicates." }
    }

    private val segments = vertices.zipWithNext().map { LineSegment3D(it.first, it.second, tolerance) }
    private val lengths = segments.map { it.length }
    private val absoluteStarts = lengths.cumulativeSum().dropLast(1)
    private val absoluteDomains =
        absoluteStarts.zipWithNext().map { Range.closedOpen(it.first, it.second) } +
            Range.closed(absoluteStarts.last(), absoluteStarts.last() + lengths.last())

    private val container = ConcatenationContainer(segments, absoluteDomains, absoluteStarts, tolerance)
    override val domain get() = container.domain

    // Methods

    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Vector3D {
        val localMember =
            container
                .fuzzySelectMember(curveRelativePoint.curvePosition, tolerance)
                .getOrElse { throw it }
        val localPoint = CurveRelativeVector1D(localMember.localParameter)

        return localMember.member.calculatePointGlobalCSUnbounded(localPoint)
    }

    companion object {
        fun of(
            vertices: NonEmptyList<Vector3D>,
            tolerance: Double,
        ): Either<GeometryException, LineString3D> =
            either {
                val adjustedVertices =
                    vertices
                        .filterWithNext { a, b -> a.fuzzyUnequals(b, tolerance) }
                        .toNonEmptyListOrNone()
                        .toEither { GeometryException.NotEnoughVertices("No vertex for constructing a line segment") }
                        .bind()

                if (adjustedVertices.size < 2) {
                    GeometryException.NotEnoughVertices("Not enough vertices for constructing a line segment").left().bind<LineString3D>()
                }

                LineString3D(adjustedVertices, tolerance)
            }
    }
}
