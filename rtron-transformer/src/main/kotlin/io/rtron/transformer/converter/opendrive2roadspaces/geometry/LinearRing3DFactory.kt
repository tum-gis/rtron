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
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.toNonEmptyListOrNull
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.processing.isClockwiseOrdered
import io.rtron.math.processing.removeConsecutiveSideDuplicates
import io.rtron.math.processing.removeRedundantVerticesOnLineSegmentsEnclosing
import io.rtron.model.opendrive.additions.identifier.RoadObjectOutlineIdentifier
import io.rtron.std.filterWithNextEnclosing
import io.rtron.transformer.messages.opendrive.of

/**
 * Factory for building [LinearRing3D] for which multiple preparation steps are required to overcome
 * heterogeneous input.
 */
object LinearRing3DFactory {

    /**
     * Builds a [LinearRing3D] from a list of vertices by filtering and preparing the vertices.
     */
    fun buildFromVertices(outlineId: RoadObjectOutlineIdentifier, vertices: NonEmptyList<Vector3D>, tolerance: Double): Either<GeometryBuilderException, ContextMessageList<LinearRing3D>> = either.eager {
        val messageList = DefaultMessageList()

        // remove end element, if start and end element are equal
        val verticesWithoutClosing = if (vertices.first() == vertices.last()) {
            vertices.dropLast(1)
        } else {
            vertices
        }

        // remove consecutively following point duplicates
        val verticesWithoutPointDuplicates = verticesWithoutClosing.filterWithNextEnclosing { a, b -> a.fuzzyUnequals(b, tolerance) }
        if (verticesWithoutPointDuplicates.size < verticesWithoutClosing.size) {
            messageList += DefaultMessage.of("OutlineContainsConsecutivelyFollowingElementDuplicates", "Ignoring at least one consecutively following point duplicate.", outlineId, Severity.WARNING, wasFixed = true)
        }

        // remove consecutively following side duplicates
        val verticesWithoutSideDuplicates = verticesWithoutPointDuplicates.removeConsecutiveSideDuplicates()
        if (verticesWithoutSideDuplicates.size != verticesWithoutPointDuplicates.size) {
            messageList += DefaultMessage.of("OutlineContainsConsecutivelyFollowingSideDuplicates", "Ignoring at least one consecutively following side duplicate of the form (…, A, B, A,…).", outlineId, Severity.WARNING, wasFixed = true)
        }

        // remove vertices that are located on a line anyway
        val preparedVertices = verticesWithoutSideDuplicates
            .removeRedundantVerticesOnLineSegmentsEnclosing(tolerance)
        if (preparedVertices.size < verticesWithoutSideDuplicates.size) {
            messageList += DefaultMessage.of("OutlineContainsLinearlyRedundantVertices", "Ignoring at least one vertex due to linear redundancy.", outlineId, Severity.WARNING, wasFixed = true)
        }

        // if there are not enough points to construct a linear ring
        if (preparedVertices.size <= 2) {
            GeometryBuilderException.NotEnoughValidOutlineElementsForLinearRing(outlineId).left().bind<ContextMessageList<LinearRing3D>>()
        }

        // if the outline elements are ordered clockwise yielding a wrong polygon orientation
        val projectedVertices = preparedVertices.map { it.toVector2D(Vector3D.Z_AXIS) }
        val orderedVertices = if (projectedVertices.distinct().size > 2 && projectedVertices.isClockwiseOrdered()) {
            messageList += DefaultMessage.of("IncorrectOutlineOrientation", "Outline elements are ordered clockwise but should be ordered counter-clockwise.", outlineId, Severity.ERROR, wasFixed = true)
            preparedVertices.reversed()
        } else {
            preparedVertices
        }

        val linearRing = LinearRing3D(orderedVertices.toNonEmptyListOrNull()!!, tolerance)
        ContextMessageList(linearRing, messageList)
    }
}
