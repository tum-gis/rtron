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

package io.rtron.math.geometry.euclidean.threed.curve

import com.github.kittinunf.result.Result
import io.rtron.math.container.ConcatenationContainer
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.Range
import io.rtron.std.cumulativeSum
import io.rtron.std.filterWithNext
import io.rtron.std.handleFailure
import io.rtron.std.noneWithNext

/**
 * Curve specified by a sequence of [vertices].
 *
 * @param vertices linearly connected vertices
 * @param tolerance allowed tolerance
 */
class LineString3D(
    val vertices: List<Vector3D>,
    override val tolerance: Double
) : AbstractCurve3D() {

    // Properties and Initializers
    init {
        require(vertices.size >= 2) { "Must at least contain two vertices." }
        require(vertices.noneWithNext { a, b -> a.fuzzyEquals(b, tolerance) }) { "Must not contain consecutively following point duplicates." }
    }

    private val segments = vertices.zipWithNext().map { LineSegment3D(it.first, it.second, tolerance) }
    private val lengths = segments.map { it.length }
    private val absoluteStarts = lengths.cumulativeSum().dropLast(1)
    private val absoluteDomains = absoluteStarts.zipWithNext().map { Range.closedOpen(it.first, it.second) } +
        Range.closed(absoluteStarts.last(), absoluteStarts.last() + lengths.last())

    private val container = ConcatenationContainer(segments, absoluteDomains, absoluteStarts, tolerance)
    override val domain get() = container.domain

    // Methods

    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Result<Vector3D, Exception> {
        val localMember = container
            .fuzzySelectMember(curveRelativePoint.curvePosition, tolerance)
            .handleFailure { return it }
        val localPoint = CurveRelativeVector1D(localMember.localParameter)

        return localMember.member.calculatePointGlobalCS(localPoint)
    }

    companion object {

        fun of(vertices: List<Vector3D>, tolerance: Double): Result<LineString3D, IllegalArgumentException> {
            val adjustedVertices = vertices.filterWithNext { a, b -> a.fuzzyUnequals(b, tolerance) }
            return if (adjustedVertices.size > 1) Result.success(LineString3D(adjustedVertices, tolerance))
            else Result.error(IllegalArgumentException("Not enough vertices for constructing a line segment."))
        }
    }
}
