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
import io.rtron.math.geometry.euclidean.threed.curve.LineSegment3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.solid.Polyhedron3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.processing.triangulation.ExperimentalTriangulator
import io.rtron.math.processing.triangulation.Triangulator
import io.rtron.std.*


/**
 * Factory for building [Polyhedron3D] for which multiple preparation steps are required to overcome
 * heterogeneous input.
 */
object Polyhedron3DFactory {

    /**
     * Builds a [Polyhedron3D] based on a list of vertical bars. Each vertical bar is represented as [LineSegment3D]
     * and various preparation steps are conducted to return a valid polyhedron.
     *
     * @param verticalBars list of vertical bars whereby the start points define the polyhedron's base polygon and the
     * end points define the polyhedron's top polygon
     * @return resulting [Polyhedron3D] wrapped in a [ContextMessage] in case of processing log messages
     */
    @OptIn(ExperimentalTriangulator::class)
    fun buildFromVerticalBars(verticalBars: List<LineSegment3D>): Result<ContextMessage<Polyhedron3D>, Exception> {

        val preparedBars = prepareBars(verticalBars).handleFailure { return it }

        // zip with consecutively following elements
        val barZips = preparedBars.value.zipWithConsecutivesEnclosing { it.start }
        require(barZips.all { it.size == 1 || it.size == 2 })
        { "Prepared vertical bar zips must have either one or two elements." }

        // construct faces
        val baseFace = LinearRing3D(barZips.reversed().map { it.first().start })
        val topFace = LinearRing3D(barZips.map { it.first().end })
        val sideFaces = barZips
                .zipWithNextEnclosing()
                .map { SideFaceBoundaries(it.first, it.second) }
                .map { it.constructSideFace() }

        val triangulatedFaces = (sideFaces + baseFace + topFace)
                .map { Triangulator.triangulate(it) }
                .handleFailure { return it }
                .flatten()

        val polyhedron = Polyhedron3D(triangulatedFaces)
        return Result.success(ContextMessage(polyhedron, preparedBars.messages))
    }

    /**
     * Preparation and cleanup of [verticalBars] including the removal of duplicates and error messaging.
     */
    private fun prepareBars(verticalBars: List<LineSegment3D>): Result<ContextMessage<List<LineSegment3D>>, Exception> {
        val infos = mutableListOf<String>()

        // remove end element, if start and end element are equal
        val barsWithoutClosing = if (verticalBars.first() == verticalBars.last())
            verticalBars.dropLast(1) else verticalBars

        // remove consecutively following line segment duplicates
        val barsWithoutDuplicates = barsWithoutClosing.distinctConsecutiveEnclosing { it }
        if (barsWithoutDuplicates.size < barsWithoutClosing.size)
            infos += "Removing at least one consecutively following line segment duplicate."

        // if there are not enough points to construct a polyhedron
        if (barsWithoutDuplicates.size < 3)
            return Result.error(IllegalStateException("A polyhedron requires at least three valid outline elements."))

        // remove consecutively following side duplicates of the form (…, A, B, A, …)
        val cleanedBars =
                barsWithoutDuplicates.filterWindowedEnclosing(listOf(false, true, true)) { it[0] == it[2] }
        if (cleanedBars.size < barsWithoutDuplicates.size)
            infos += "Removing consecutively following side duplicates of the form (…, A, B, A, …)."

        // remove all elements that are surrounded by two outline elements (all having the same starting point)
        val preparedBars = cleanedBars.filterWindowedEnclosing(listOf(false, true, false))
            { it[0].start == it[1].start && it[1].start == it[2].start }
        if (preparedBars.size < cleanedBars.size)
            infos += "Removing elements that are surrounded by outline elements having the same starting point."

        return Result.success(ContextMessage(preparedBars, infos))
    }

    /**
     * Helper class for constructing the linear rings based on a list vertical bars.
     * The left and right boundary of the face to be constructed might be represented by multiple vertical bars.
     *
     * @param leftVerticalBars list of vertical bars defining the left side of the linear ring
     * @param rightVerticalBars list of vertical bars defining the right side of the linear ring
     */
    private data class SideFaceBoundaries(
            private val leftVerticalBars: List<LineSegment3D>,
            private val rightVerticalBars: List<LineSegment3D>
    ) {

        init {
            require(leftVerticalBars.size == 1 || leftVerticalBars.size == 2)
            { "Requiring one or two left vertical bars to construct a side face." }
            require(rightVerticalBars.size == 1 || rightVerticalBars.size == 2)
            { "Requiring one or two right vertical bars to construct a side face." }
            require(leftVerticalBars.all { leftVerticalBars.first().start == it.start })
            { "All left vertical bars must have the same starting point." }
            require(rightVerticalBars.all { rightVerticalBars.first().start == it.start })
            { "All right vertical bars must have the same starting point." }
        }

        /**
         * Constructs the side face represented as [LinearRing3D].
         */
        fun constructSideFace(): LinearRing3D {
            val vertices = getVerticesOfRightLeg() + getVerticesOfLeftLegReversed()
            return LinearRing3D(vertices)
        }

        private fun isCurrentFaceHigherThanPrevious() =
                leftVerticalBars.first().length < leftVerticalBars.last().length

        private fun isCurrentFaceHigherThanNext() =
                rightVerticalBars.first().length > rightVerticalBars.last().length

        /**
         * Returns the list of vertices of the left leg in a reversed order for easier side face construction.
         * The inclusion of vertices depends on whether the current face is higher than the previous.
         */
        private fun getVerticesOfLeftLegReversed(): List<Vector3D> =
                if (isCurrentFaceHigherThanPrevious())
                    listOf(leftVerticalBars.last().end, leftVerticalBars.first().end, leftVerticalBars.last().start)
                else listOf(leftVerticalBars.last().end, leftVerticalBars.last().start)

        /**
         * Returns the list of vertices of the right leg.
         * The inclusion of vertices depends on whether the current face is higher than the next.
         */
        private fun getVerticesOfRightLeg(): List<Vector3D> =
                if (isCurrentFaceHigherThanNext())
                    listOf(rightVerticalBars.first().start, rightVerticalBars.last().end, rightVerticalBars.first().end)
                else listOf(rightVerticalBars.first().start, rightVerticalBars.first().end)
    }

}
