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

package io.rtron.transformer.converter.opendrive2roadspaces.geometry

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.continuations.either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.io.messages.mergeMessageLists
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.solid.Polyhedron3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.linear.dimensionOfSpan
import io.rtron.math.processing.triangulation.Triangulator
import io.rtron.math.range.Tolerable
import io.rtron.model.opendrive.additions.identifier.RoadObjectOutlineIdentifier
import io.rtron.std.filterWindowedEnclosing
import io.rtron.std.filterWithNextEnclosing
import io.rtron.std.handleLeftAndFilter
import io.rtron.std.zipWithConsecutivesEnclosing
import io.rtron.std.zipWithNextEnclosing
import io.rtron.transformer.messages.opendrive.of
import kotlin.collections.flatten

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
    fun buildFromVerticalOutlineElements(outlineId: RoadObjectOutlineIdentifier, outlineElements: NonEmptyList<VerticalOutlineElement>, tolerance: Double):
        Either<GeometryBuilderException, ContextMessageList<Polyhedron3D>> = either.eager {
        val messageList = DefaultMessageList()

        // prepare vertical outline elements
        val preparedOutlineElements = prepareOutlineElements(outlineId, outlineElements, tolerance)
            .bind()
            .handleMessageList { messageList += it }

        // construct faces
        val baseFace = LinearRing3D(preparedOutlineElements.reversed().map { it.basePoint }.let { it.toNonEmptyListOrNull()!! }, tolerance)
        val topFace = LinearRing3D(preparedOutlineElements.flatMap { it.getHighestPointAdjacentToTheTop() }, tolerance)
        val sideFaces: List<LinearRing3D> = preparedOutlineElements
            .zipWithNextEnclosing()
            .flatMap { buildSideFace(it.first, it.second, tolerance).toList() }

        // triangulate faces
        val triangulatedFaces = (sideFaces + baseFace + topFace)
            .map { currentFace -> Triangulator.triangulate(currentFace, tolerance) }
            .handleLeftAndFilter { GeometryBuilderException.TriangulationException(it.value.message, outlineId).left().bind() }
            .flatten()
            .let { it.toNonEmptyListOrNull()!! }

        val polyhedron = Polyhedron3D(triangulatedFaces, tolerance)
        ContextMessageList(polyhedron, messageList)
    }

    /**
     * A vertical outline element is represented by a [basePoint] and an optional [leftHeadPoint] and [rightHeadPoint].
     * The [basePoint] defines the bound of the base surface.
     *
     * @param basePoint base point of the outline element
     * @param leftHeadPoint left head point representing the bound point of the side surface to the left
     * @param rightHeadPoint right head point representing the bound point of the side surface to the right
     */
    data class VerticalOutlineElement(
        val basePoint: Vector3D,
        val leftHeadPoint: Option<Vector3D>,
        val rightHeadPoint: Option<Vector3D> = None,
        override val tolerance: Double
    ) : Tolerable {

        // Properties and Initializers
        init {
            rightHeadPoint.tap {
                require(leftHeadPoint.isDefined()) { "Left head point must be present, if right head point is present." }
            }

            leftHeadPoint.tap { currentLeftHeadPoint ->
                require(basePoint.fuzzyUnequals(currentLeftHeadPoint, tolerance)) { "Left head point must be fuzzily unequal to base point." }

                rightHeadPoint.tap { currentRightHeadPoint ->
                    require(basePoint.fuzzyUnequals(currentRightHeadPoint, tolerance)) { "Right head point must be fuzzily unequal to base point." }
                    require(currentLeftHeadPoint.fuzzyUnequals(currentRightHeadPoint, tolerance)) { "Left head point must be fuzzily unequal to the right point." }
                }
            }
        }

        private val leftLength: Double get() = leftHeadPoint.map { basePoint.distance(it) }.getOrElse { 0.0 }
        private val rightLength: Double get() = rightHeadPoint.map { basePoint.distance(it) }.getOrElse { 0.0 }

        // Methods
        fun containsHeadPoint() = leftHeadPoint.isDefined() || rightHeadPoint.isDefined()
        fun containsOneHeadPoint() = leftHeadPoint.isDefined() && rightHeadPoint.isEmpty()

        fun getVerticesAsLeftBoundary(): List<Vector3D> {
            val midPoint = if (containsOneHeadPoint() || leftLength < rightLength) {
                leftHeadPoint.toList()
            } else {
                emptyList()
            }
            return listOf(basePoint) + midPoint + rightHeadPoint.toList()
        }

        fun getVerticesAsRightBoundary(): List<Vector3D> {
            val midPoint = if (leftLength > rightLength) rightHeadPoint.toList() else emptyList()
            return listOf(basePoint) + midPoint + leftHeadPoint.toList()
        }

        fun getHeadPointAdjacentToTheRight() = if (rightHeadPoint.isDefined()) rightHeadPoint else leftHeadPoint

        fun getHighestPointAdjacentToTheTop(): NonEmptyList<Vector3D> =
            if (containsHeadPoint()) {
                (leftHeadPoint.toList() + rightHeadPoint.toList()).toNonEmptyListOrNull()!!
            } else {
                nonEmptyListOf(basePoint)
            }

        companion object {

            fun of(
                basePoint: Vector3D,
                leftHeadPoint: Option<Vector3D>,
                rightHeadPoint: Option<Vector3D>,
                tolerance: Double
            ): ContextMessageList<VerticalOutlineElement> {
                val messageList = DefaultMessageList()
                val headPoints = leftHeadPoint.toList() + rightHeadPoint.toList()

                // remove head points that are fuzzily equal to base point
                val prepHeadPoints = headPoints.filter { it.fuzzyUnequals(basePoint, tolerance) }
                if (prepHeadPoints.size < headPoints.size) {
                    messageList += DefaultMessage("", "Height of outline element must be above tolerance.", "", Severity.WARNING, true)
                }

                if (prepHeadPoints.size <= 1) {
                    return ContextMessageList(of(basePoint, prepHeadPoints, tolerance), messageList)
                }

                // if head points are fuzzily equal, take only one
                val verticalOutlineElement =
                    if (prepHeadPoints.first().fuzzyEquals(prepHeadPoints.last(), tolerance)) {
                        of(basePoint, prepHeadPoints.take(1), tolerance)
                    } else {
                        of(basePoint, prepHeadPoints, tolerance)
                    }
                return ContextMessageList(verticalOutlineElement, messageList)
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

                val leftHeadPoint = if (headPoints.isNotEmpty()) Some(headPoints.first()) else None
                val rightHeadPont = if (headPoints.size == 2) Some(headPoints.last()) else None

                return VerticalOutlineElement(basePoint, leftHeadPoint, rightHeadPont, tolerance)
            }

            /**
             * Returns a [VerticalOutlineElement] by merging a list of [elements].
             *
             * @param elements list of [VerticalOutlineElement] which must all contain the same base point
             * @param tolerance allowed tolerance
             */
            fun of(elements: List<VerticalOutlineElement>, tolerance: Double): ContextMessageList<VerticalOutlineElement> {
                require(elements.isNotEmpty()) { "List of elements must not be empty." }
                require(elements.drop(1).all { it.basePoint == elements.first().basePoint }) { "All elements must have the same base point." }
                val messageList = DefaultMessageList()

                if (elements.size == 1) {
                    return ContextMessageList(elements.first(), messageList)
                }

                if (elements.size > 2) {
                    messageList += DefaultMessage("OutlineContainsConsecutivelyFollowingElementDuplicates", "Contains more than two consecutively following outline element duplicates.", "", Severity.WARNING, wasFixed = false)
                }

                val basePoint = elements.first().basePoint
                val leftHeadPoint = elements.first().leftHeadPoint
                val rightHeadPoint = elements.last().getHeadPointAdjacentToTheRight()

                return of(basePoint, leftHeadPoint, rightHeadPoint, tolerance).appendReport(messageList)
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
        if (!leftElement.containsHeadPoint() && !rightElement.containsHeadPoint()) {
            return None
        }

        val vertices = rightElement.getVerticesAsRightBoundary() + leftElement.getVerticesAsLeftBoundary().reversed()
        val linearRing = LinearRing3D(vertices.toNonEmptyListOrNull()!!, tolerance)
        return Some(linearRing)
    }

    /**
     * Preparation and cleanup of [verticalOutlineElements] including the removal of duplicates and error messaging.
     */
    private fun prepareOutlineElements(outlineId: RoadObjectOutlineIdentifier, verticalOutlineElements: NonEmptyList<VerticalOutlineElement>, tolerance: Double):
        Either<GeometryBuilderException, ContextMessageList<NonEmptyList<VerticalOutlineElement>>> = either.eager {
        val messageList = DefaultMessageList()

        // remove consecutively following line segment duplicates
        val elementsWithoutDuplicates = verticalOutlineElements.filterWithNextEnclosing { a, b -> a.basePoint.fuzzyUnequals(b.basePoint, tolerance) }
        if (elementsWithoutDuplicates.size < verticalOutlineElements.size) {
            messageList += DefaultMessage.of("", "Ignoring at least one consecutively following line segment duplicate.", outlineId, Severity.WARNING, wasFixed = true)
        }

        // if there are not enough points to construct a polyhedron
        if (elementsWithoutDuplicates.size < 3) {
            GeometryBuilderException.NotEnoughValidOutlineElementsForPolyhedron(outlineId).left().bind<ContextMessageList<NonEmptyList<VerticalOutlineElement>>>()
        }

        // remove consecutively following side duplicates of the form (…, A, B, A, …)
        val cleanedElements = elementsWithoutDuplicates
            .filterWindowedEnclosing(listOf(false, true, true)) { it[0].basePoint == it[2].basePoint }
        if (cleanedElements.size < elementsWithoutDuplicates.size) {
            messageList += DefaultMessage.of("", "Ignoring consecutively following side duplicates of the form (…, A, B, A, …).", outlineId, Severity.WARNING, wasFixed = true)
        }

        // if the base points of the outline element are located on a line (or point)
        val innerBaseEdges = cleanedElements.map { it.basePoint }.filterIndexed { index, _ -> index != 0 }.map { it - cleanedElements.first().basePoint }
        val dimensionOfSpan = innerBaseEdges.map { it.toRealVector() }.dimensionOfSpan()
        if (dimensionOfSpan < 2) {
            GeometryBuilderException.ColinearOutlineElementsForPolyhedron(outlineId).left().bind<ContextMessageList<NonEmptyList<VerticalOutlineElement>>>()
        }

        val elements: ContextMessageList<NonEmptyList<VerticalOutlineElement>> = cleanedElements
            .zipWithConsecutivesEnclosing { it.basePoint }
            .map { VerticalOutlineElement.of(it, tolerance) }
            .mergeMessageLists()
            .map { it.toNonEmptyListOrNull()!! }

        elements
    }
}
