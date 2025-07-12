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

package io.rtron.transformer.converter.opendrive2roadspaces.geometry

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.io.issues.mergeIssueLists
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.solid.Polyhedron3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.linear.dimensionOfSpan
import io.rtron.math.processing.isClockwiseOrdered
import io.rtron.math.processing.triangulation.Triangulator
import io.rtron.math.range.Tolerable
import io.rtron.model.opendrive.additions.identifier.RoadObjectOutlineIdentifier
import io.rtron.std.filterWindowedEnclosing
import io.rtron.std.filterWithNextEnclosing
import io.rtron.std.handleLeftAndFilter
import io.rtron.std.zipWithConsecutivesEnclosing
import io.rtron.std.zipWithNextEnclosing
import io.rtron.transformer.issues.opendrive.of
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
    fun buildFromVerticalOutlineElements(
        outlineId: RoadObjectOutlineIdentifier,
        outlineElements: NonEmptyList<VerticalOutlineElement>,
        tolerance: Double,
    ): Either<GeometryBuilderException, ContextIssueList<Polyhedron3D>> =
        either {
            val issueList = DefaultIssueList()

            // prepare vertical outline elements
            val preparedOutlineElements =
                prepareOutlineElements(outlineId, outlineElements, tolerance)
                    .bind()
                    .handleIssueList { issueList += it }

            // construct faces
            val baseFace =
                LinearRing3D(preparedOutlineElements.reversed().map { it.basePoint }.let { it.toNonEmptyListOrNull()!! }, tolerance)
            val topFace = LinearRing3D(preparedOutlineElements.flatMap { it.getHighestPointAdjacentToTheTop() }, tolerance)
            val sideFaces: List<LinearRing3D> =
                preparedOutlineElements
                    .zipWithNextEnclosing()
                    .flatMap { buildSideFace(it.first, it.second, tolerance).toList() }

            // triangulate faces
            val triangulatedFaces =
                (sideFaces + baseFace + topFace)
                    .map { currentFace -> Triangulator.triangulate(currentFace, tolerance) }
                    .handleLeftAndFilter { GeometryBuilderException.TriangulationException(it.value.message, outlineId).left().bind() }
                    .flatten()
                    .let { it.toNonEmptyListOrNull()!! }

            val polyhedron = Polyhedron3D(triangulatedFaces, tolerance)
            ContextIssueList(polyhedron, issueList)
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
        override val tolerance: Double,
    ) : Tolerable {
        // Properties and Initializers
        init {
            rightHeadPoint.onSome {
                require(leftHeadPoint.isSome()) { "Left head point must be present, if right head point is present." }
            }

            leftHeadPoint.onSome { currentLeftHeadPoint ->
                require(
                    basePoint.fuzzyUnequals(
                        currentLeftHeadPoint,
                        tolerance,
                    ),
                ) { "Left head point must be fuzzily unequal to base point." }

                rightHeadPoint.onSome { currentRightHeadPoint ->
                    require(
                        basePoint.fuzzyUnequals(
                            currentRightHeadPoint,
                            tolerance,
                        ),
                    ) { "Right head point must be fuzzily unequal to base point." }
                    require(
                        currentLeftHeadPoint.fuzzyUnequals(
                            currentRightHeadPoint,
                            tolerance,
                        ),
                    ) { "Left head point must be fuzzily unequal to the right point." }
                }
            }
        }

        private val leftLength: Double get() = leftHeadPoint.map { basePoint.distance(it) }.getOrElse { 0.0 }
        private val rightLength: Double get() = rightHeadPoint.map { basePoint.distance(it) }.getOrElse { 0.0 }

        // Methods
        fun containsHeadPoint() = leftHeadPoint.isSome() || rightHeadPoint.isSome()

        fun containsOneHeadPoint() = leftHeadPoint.isSome() && rightHeadPoint.isNone()

        fun getVerticesAsLeftBoundary(): List<Vector3D> {
            val midPoint =
                if (containsOneHeadPoint() || leftLength < rightLength) {
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

        fun getHeadPointAdjacentToTheRight() = if (rightHeadPoint.isSome()) rightHeadPoint else leftHeadPoint

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
                tolerance: Double,
            ): ContextIssueList<VerticalOutlineElement> {
                val issueList = DefaultIssueList()
                val headPoints = leftHeadPoint.toList() + rightHeadPoint.toList()

                // remove head points that are fuzzily equal to base point
                val prepHeadPoints = headPoints.filter { it.fuzzyUnequals(basePoint, tolerance) }
                if (prepHeadPoints.size < headPoints.size) {
                    issueList +=
                        DefaultIssue(
                            "",
                            "Height of outline element must be above tolerance.",
                            "",
                            Severity.WARNING,
                            true,
                        )
                }

                if (prepHeadPoints.size <= 1) {
                    return ContextIssueList(of(basePoint, prepHeadPoints, tolerance), issueList)
                }

                // if head points are fuzzily equal, take only one
                val verticalOutlineElement =
                    if (prepHeadPoints.first().fuzzyEquals(prepHeadPoints.last(), tolerance)) {
                        of(basePoint, prepHeadPoints.take(1), tolerance)
                    } else {
                        of(basePoint, prepHeadPoints, tolerance)
                    }
                return ContextIssueList(verticalOutlineElement, issueList)
            }

            /**
             * Returns a [VerticalOutlineElement] based on a provided list of [headPoints].
             *
             * @param basePoint base point of the outline element
             * @param headPoints a maximum number of two head points must be provided
             * @param tolerance allowed tolerance
             */
            fun of(
                basePoint: Vector3D,
                headPoints: List<Vector3D>,
                tolerance: Double,
            ): VerticalOutlineElement {
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
            fun of(
                elements: List<VerticalOutlineElement>,
                tolerance: Double,
            ): ContextIssueList<VerticalOutlineElement> {
                require(elements.isNotEmpty()) { "List of elements must not be empty." }
                require(
                    elements
                        .drop(1)
                        .all { it.basePoint == elements.first().basePoint },
                ) { "All elements must have the same base point." }
                val issueList = DefaultIssueList()

                if (elements.size == 1) {
                    return ContextIssueList(elements.first(), issueList)
                }

                if (elements.size > 2) {
                    issueList +=
                        DefaultIssue(
                            "OutlineContainsConsecutivelyFollowingElementDuplicates",
                            "Contains more than two consecutively following outline element duplicates.",
                            "",
                            Severity.WARNING,
                            wasFixed = false,
                        )
                }

                val basePoint = elements.first().basePoint
                val leftHeadPoint = elements.first().leftHeadPoint
                val rightHeadPoint = elements.last().getHeadPointAdjacentToTheRight()

                return of(basePoint, leftHeadPoint, rightHeadPoint, tolerance).appendReport(issueList)
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
        tolerance: Double,
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
    private fun prepareOutlineElements(
        outlineId: RoadObjectOutlineIdentifier,
        verticalOutlineElements: NonEmptyList<VerticalOutlineElement>,
        tolerance: Double,
    ): Either<GeometryBuilderException, ContextIssueList<NonEmptyList<VerticalOutlineElement>>> =
        either {
            val issueList = DefaultIssueList()

            // remove consecutively following line segment duplicates
            val elementsWithoutDuplicates =
                verticalOutlineElements.filterWithNextEnclosing {
                    a,
                    b,
                    ->
                    a.basePoint.fuzzyUnequals(b.basePoint, tolerance)
                }
            if (elementsWithoutDuplicates.size < verticalOutlineElements.size) {
                issueList +=
                    DefaultIssue.of(
                        "OutlineContainsConsecutivelyFollowingLineSegmentDuplicates",
                        "Ignoring at least one consecutively following line segment duplicate.",
                        outlineId,
                        Severity.WARNING,
                        wasFixed = true,
                    )
            }

            // if there are not enough points to construct a polyhedron
            if (elementsWithoutDuplicates.size < 3) {
                GeometryBuilderException
                    .NotEnoughValidOutlineElementsForPolyhedron(
                        outlineId,
                    ).left()
                    .bind<ContextIssueList<NonEmptyList<VerticalOutlineElement>>>()
            }

            // remove consecutively following side duplicates of the form (…, A, B, A, …)
            val cleanedElements =
                elementsWithoutDuplicates
                    .filterWindowedEnclosing(listOf(false, true, true)) { it[0].basePoint == it[2].basePoint }
            if (cleanedElements.size < elementsWithoutDuplicates.size) {
                issueList +=
                    DefaultIssue.of(
                        "OutlineContainsConsecutivelyFollowingSideDuplicates",
                        "Ignoring consecutively following side duplicates of the form (…, A, B, A, …).",
                        outlineId,
                        Severity.WARNING,
                        wasFixed = true,
                    )
            }

            // if the base points of the outline element are located on a line (or point)
            val innerBaseEdges =
                cleanedElements
                    .map { it.basePoint }
                    .filterIndexed {
                        index,
                        _,
                        ->
                        index != 0
                    }.map { it - cleanedElements.first().basePoint }
            val dimensionOfSpan = innerBaseEdges.map { it.toRealVector() }.dimensionOfSpan()
            if (dimensionOfSpan < 2) {
                GeometryBuilderException
                    .ColinearOutlineElementsForPolyhedron(
                        outlineId,
                    ).left()
                    .bind<ContextIssueList<NonEmptyList<VerticalOutlineElement>>>()
            }

            // if the outline elements are ordered clockwise yielding a wrong polygon orientation
            val projectedBasePoints = cleanedElements.map { it.basePoint.toVector2D(Vector3D.Z_AXIS) }
            val orderedElements =
                if (projectedBasePoints.distinct().size > 2 && projectedBasePoints.isClockwiseOrdered()) {
                    issueList +=
                        DefaultIssue.of(
                            "IncorrectOutlineOrientation",
                            "Outline elements are ordered clockwise but should be ordered counter-clockwise.",
                            outlineId,
                            Severity.ERROR,
                            wasFixed = true,
                        )
                    cleanedElements.reversed()
                } else {
                    cleanedElements
                }

            val elements: ContextIssueList<NonEmptyList<VerticalOutlineElement>> =
                orderedElements
                    .zipWithConsecutivesEnclosing { it.basePoint }
                    .map { VerticalOutlineElement.of(it, tolerance) }
                    .mergeIssueLists()
                    .map { it.toNonEmptyListOrNull()!! }

            elements
        }
}
