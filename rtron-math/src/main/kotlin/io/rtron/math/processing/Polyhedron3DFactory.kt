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

import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.none
import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.solid.Polyhedron3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.linear.dimensionOfSpan
import io.rtron.math.processing.triangulation.ExperimentalTriangulator
import io.rtron.math.processing.triangulation.Triangulator
import io.rtron.math.range.Tolerable
import io.rtron.std.ContextMessage
import io.rtron.std.filterWindowedEnclosing
import io.rtron.std.filterWithNextEnclosing
import io.rtron.std.getResult
import io.rtron.std.handleFailure
import io.rtron.std.unwrapMessages
import io.rtron.std.zipWithConsecutivesEnclosing
import io.rtron.std.zipWithNextEnclosing

/**
 * Factory for building [Polyhedron3D] for which multiple preparation steps are required to overcome
 * heterogeneous input.
 */
object Polyhedron3DFactory {

    /**
     * Builds a [Polyhedron3D] from a list of [VerticalOutlineElement], which define the boundary of the [Polyhedron3D].
     *
     * @param outlineElements vertical line segments or points bounding the polyhedron
     */
    @OptIn(ExperimentalTriangulator::class)
    fun buildFromVerticalOutlineElements(outlineElements: List<VerticalOutlineElement>, tolerance: Double):
        Result<ContextMessage<Polyhedron3D>, Exception> {

            // prepare vertical outline elements
            val preparedOutlineElementsWithContext = prepareOutlineElements(outlineElements, tolerance)
                .handleFailure { return it }
            val messages = preparedOutlineElementsWithContext.messages
            val preparedOutlineElements = preparedOutlineElementsWithContext.value

            // construct faces
            val baseFace = LinearRing3D(preparedOutlineElements.reversed().map { it.basePoint }, tolerance)
            val topFace = LinearRing3D(preparedOutlineElements.flatMap { it.getHighestPointAdjacentToTheTop() }, tolerance)
            val sideFaces: List<LinearRing3D> = preparedOutlineElements
                .zipWithNextEnclosing()
                .flatMap { buildSideFace(it.first, it.second, tolerance).toList() }

            // triangulate faces
            val triangulatedFaces = (sideFaces + baseFace + topFace)
                .map { Triangulator.triangulate(it, tolerance) }
                .handleFailure { return it }
                .flatten()

            val polyhedron = ContextMessage(Polyhedron3D(triangulatedFaces, tolerance), messages)
            return Result.success(polyhedron)
        }

    /**
     * A vertical outline element is represented by a [basePoint] and an optional [leftHeadPoint] and [rightHeadPoint].
     * The [basePoint] defines the bound of the base surface
     *
     * @param basePoint base point of the outline element
     * @param leftHeadPoint left head point representing the bound point of the side surface to the left
     * @param rightHeadPoint right head point representing the bound point of the side surface to the right
     */
    data class VerticalOutlineElement(
        val basePoint: Vector3D,
        val leftHeadPoint: Option<Vector3D>,
        val rightHeadPoint: Option<Vector3D> = none(),
        override val tolerance: Double
    ) : Tolerable {

        // Properties and Initializers
        init {
            if (rightHeadPoint.isDefined())
                require(leftHeadPoint.isDefined()) { "Left head point must be present, if right head point is present." }

            if (leftHeadPoint.isDefined()) {
                val leftHeadPointValue = leftHeadPoint.getResult().handleFailure { throw it.error }
                require(basePoint.fuzzyUnequals(leftHeadPointValue, tolerance)) { "Left head point must be fuzzily unequal to base point." }

                if (rightHeadPoint.isDefined()) {
                    val rightHeadPointValue = rightHeadPoint.getResult().handleFailure { throw it.error }
                    require(basePoint.fuzzyUnequals(rightHeadPointValue, tolerance)) { "Right head point must be fuzzily unequal to base point." }

                    require(leftHeadPointValue.fuzzyUnequals(rightHeadPointValue, tolerance)) { "Left head point must be fuzzily unequal to the right point." }
                }
            }
        }

        private val leftLength: Double get() = leftHeadPoint.map { basePoint.distance(it) }.getOrElse { 0.0 }
        private val rightLength: Double get() = rightHeadPoint.map { basePoint.distance(it) }.getOrElse { 0.0 }

        // Methods
        fun containsHeadPoint() = leftHeadPoint.isDefined() || rightHeadPoint.isDefined()
        fun containsOneHeadPoint() = leftHeadPoint.isDefined() && rightHeadPoint.isEmpty()

        fun getVerticesAsLeftBoundary(): List<Vector3D> {
            val midPoint = if (containsOneHeadPoint() || leftLength < rightLength) leftHeadPoint.toList()
            else emptyList()
            return listOf(basePoint) + midPoint + rightHeadPoint.toList()
        }

        fun getVerticesAsRightBoundary(): List<Vector3D> {
            val midPoint = if (leftLength > rightLength) rightHeadPoint.toList() else emptyList()
            return listOf(basePoint) + midPoint + leftHeadPoint.toList()
        }

        fun getHeadPointAdjacentToTheRight() = if (rightHeadPoint.isDefined()) rightHeadPoint else leftHeadPoint

        fun getHighestPointAdjacentToTheTop(): List<Vector3D> =
            if (containsHeadPoint()) leftHeadPoint.toList() + rightHeadPoint.toList()
            else listOf(basePoint)

        companion object {

            fun of(
                basePoint: Vector3D,
                leftHeadPoint: Option<Vector3D>,
                rightHeadPoint: Option<Vector3D>,
                tolerance: Double
            ): ContextMessage<VerticalOutlineElement> {

                val infos = mutableListOf<String>()
                val headPoints = leftHeadPoint.toList() + rightHeadPoint.toList()

                // remove head points that are fuzzily equal to base point
                val prepHeadPoints = headPoints.filter { it.fuzzyUnequals(basePoint, tolerance) }
                if (prepHeadPoints.size < headPoints.size)
                    infos += "Height of outline element must be above tolerance."

                if (prepHeadPoints.size <= 1)
                    return ContextMessage(of(basePoint, prepHeadPoints, tolerance), infos)

                // if head points are fuzzily equal, take only one
                return if (prepHeadPoints.first().fuzzyEquals(prepHeadPoints.last(), tolerance))
                    ContextMessage(of(basePoint, prepHeadPoints.take(1), tolerance), infos)
                else ContextMessage(of(basePoint, prepHeadPoints, tolerance), infos)
            }

            /**
             * Returns a [VerticalOutlineElement] based on a provided list of [headPoints].
             *
             * @param basePoint base point of the outline element
             * @param headPoints a maximum number of two head points must be provided
             * @param tolerance allowed tolerance
             */
            fun of(basePoint: Vector3D, headPoints: List<Vector3D>, tolerance: Double): VerticalOutlineElement {
                require(headPoints.size <= 2) { "Must contain not more than two head points." }

                val leftHeadPoint = if (headPoints.isNotEmpty()) Some(headPoints.first()) else none()
                val rightHeadPont = if (headPoints.size == 2) Some(headPoints.last()) else none()

                return VerticalOutlineElement(basePoint, leftHeadPoint, rightHeadPont, tolerance)
            }

            /**
             * Returns a [VerticalOutlineElement] by merging a list of [elements].
             *
             * @param elements list of [VerticalOutlineElement] which must all contain the same base point
             * @param tolerance allowed tolerance
             */
            fun of(elements: List<VerticalOutlineElement>, tolerance: Double): ContextMessage<VerticalOutlineElement> {
                require(elements.isNotEmpty()) { "List of elements must not be empty." }
                require(elements.drop(1).all { it.basePoint == elements.first().basePoint }) { "All elements must have the same base point." }

                if (elements.size == 1)
                    return ContextMessage(elements.first())

                val message = if (elements.size > 2) "Contains more than two consecutively following outline " +
                    "element duplicates." else ""

                val basePoint = elements.first().basePoint
                val leftHeadPoint = elements.first().leftHeadPoint
                val rightHeadPoint = elements.last().getHeadPointAdjacentToTheRight()

                return of(basePoint, leftHeadPoint, rightHeadPoint, tolerance).appendMessages(message)
            }
        }
    }

    /**
     * Builds a side face, whereas the [leftElement] and [rightElement] represent the boundaries.
     * Furthermore, the height of the previous or succeeding side face is taken into account, since it has an effect
     * on the vertices of the side face in focus.
     *
     * @param leftElement left boundary of side surface to be constructed
     * @param rightElement right boundary of side surface to be constructed
     * @param tolerance allowed tolerance
     */
    private fun buildSideFace(
        leftElement: VerticalOutlineElement,
        rightElement: VerticalOutlineElement,
        tolerance: Double
    ): Option<LinearRing3D> {

        if (!leftElement.containsHeadPoint() && !rightElement.containsHeadPoint())
            return none()

        val vertices = rightElement.getVerticesAsRightBoundary() + leftElement.getVerticesAsLeftBoundary().reversed()
        val linearRing = LinearRing3D(vertices, tolerance)
        return Some(linearRing)
    }

    /**
     * Preparation and cleanup of [verticalOutlineElements] including the removal of duplicates and error messaging.
     */
    private fun prepareOutlineElements(verticalOutlineElements: List<VerticalOutlineElement>, tolerance: Double):
        Result<ContextMessage<List<VerticalOutlineElement>>, Exception> {

            val infos = mutableListOf<String>()

            // remove consecutively following line segment duplicates
            val elementsWithoutDuplicates = verticalOutlineElements.filterWithNextEnclosing { a, b -> a.basePoint.fuzzyUnequals(b.basePoint, tolerance) }
            if (elementsWithoutDuplicates.size < verticalOutlineElements.size)
                infos += "Removing at least one consecutively following line segment duplicate."

            // if there are not enough points to construct a polyhedron
            if (elementsWithoutDuplicates.size < 3)
                return Result.error(IllegalStateException("A polyhedron requires at least three valid outline elements."))

            // remove consecutively following side duplicates of the form (…, A, B, A, …)
            val cleanedElements = elementsWithoutDuplicates
                .filterWindowedEnclosing(listOf(false, true, true)) { it[0].basePoint == it[2].basePoint }
            if (cleanedElements.size < elementsWithoutDuplicates.size)
                infos += "Removing consecutively following side duplicates of the form (…, A, B, A, …)."

            // if the base points of the outline element are located on a line (or point)
            val innerBaseEdges = cleanedElements.map { it.basePoint }.filterIndexed { index, _ -> index != 0 }.map { it - cleanedElements.first().basePoint }
            val dimensionOfSpan = innerBaseEdges.map { it.toRealVector() }.dimensionOfSpan()
            if (dimensionOfSpan < 2)
                return Result.error(IllegalStateException("A polyhedron requires at least three valid outline elements, which are not colinear (located on a line)."))

            val elements: ContextMessage<List<VerticalOutlineElement>> = cleanedElements
                .zipWithConsecutivesEnclosing { it.basePoint }
                .map { VerticalOutlineElement.of(it, tolerance) }
                .unwrapMessages()

            return Result.success(elements)
        }
}
