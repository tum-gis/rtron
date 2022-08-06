/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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
import arrow.core.continuations.either
import arrow.core.getOrHandle
import arrow.core.separateEither
import arrow.core.some
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.io.messages.handleMessageList
import io.rtron.io.messages.mergeToReport
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.math.geometry.euclidean.threed.solid.Cuboid3D
import io.rtron.math.geometry.euclidean.threed.solid.Cylinder3D
import io.rtron.math.geometry.euclidean.threed.solid.ParametricSweep3D
import io.rtron.math.geometry.euclidean.threed.solid.Polyhedron3D
import io.rtron.math.transform.Affine3D
import io.rtron.math.transform.AffineSequence3D
import io.rtron.model.opendrive.objects.RoadObjectsObject
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlinesOutline
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlinesOutlineCornerRoad
import io.rtron.model.opendrive.objects.RoadObjectsObjectRepeat
import io.rtron.transformer.converter.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.messages.opendrive.of

/**
 * Builder for solid geometries in 3D from the OpenDRIVE data model.
 */
object Solid3DBuilder {

    // Methods

    /**
     * Builds a list of cuboids from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildCuboids(roadObject: RoadObjectsObject, curveAffine: Affine3D, numberTolerance: Double): ContextMessageList<List<Cuboid3D>> {
        val cuboidList = mutableListOf<Cuboid3D>()
        val messageList = DefaultMessageList()

        if (roadObject.isCuboid()) {
            val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
            val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
            cuboidList += Cuboid3D.of(roadObject.length, roadObject.width, roadObject.heightValidated, numberTolerance, affineSequence)
        }

        if (roadObject.repeat.any { it.isRepeatedCuboid() })
            messageList += DefaultMessage.of("", "Cuboid geometries in the repeat elements are currently not supported.", roadObject.additionalId, Severity.WARNING, wasFixed = false)

        return ContextMessageList(cuboidList, messageList)
    }

    /**
     * Builds a list of cylinders from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildCylinders(roadObject: RoadObjectsObject, curveAffine: Affine3D, numberTolerance: Double): ContextMessageList<List<Cylinder3D>> {
        val cylinderList = mutableListOf<Cylinder3D>()
        val messageList = DefaultMessageList()

        if (roadObject.isCylinder()) {
            val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
            val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
            cylinderList += Cylinder3D.of(roadObject.radius, roadObject.height, numberTolerance, affineSequence)
        }

        if (roadObject.repeat.any { it.isRepeatCylinder() })
            messageList += DefaultMessage.of("", "Cylinder geometries in the repeat elements are currently not supported.", roadObject.additionalId, Severity.WARNING, wasFixed = false)

        return ContextMessageList(cylinderList, messageList)
    }

    /**
     * Builds a list of polyhedrons from OpenDRIVE road objects defined by road corner outlines.
     *
     * @param id identifier of the road space object for error logging
     * @param roadObject road object of OpenDRIVE
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     * @return list of polyhedrons
     */
    fun buildPolyhedronsByRoadCorners(roadObject: RoadObjectsObject, roadReferenceLine: Curve3D, numberTolerance: Double): ContextMessageList<List<Polyhedron3D>> {
        val messageList = DefaultMessageList()

        val (builderExceptions, polyhedronsWithContext) = roadObject
            .getPolyhedronsDefinedByRoadCorners()
            .map { buildPolyhedronByRoadCorners(it, roadReferenceLine, numberTolerance) }
            .separateEither()

        messageList += builderExceptions.map { DefaultMessage.of("", it.message, it.location, Severity.WARNING, wasFixed = true) }.mergeToReport()
        val polyhedrons = polyhedronsWithContext.handleMessageList { messageList += it.messageList }

        return ContextMessageList(polyhedrons, messageList)
    }

    /**
     * Builds a single polyhedron from an OpenDRIVE road object defined by road corner outlines.
     */
    private fun buildPolyhedronByRoadCorners(outline: RoadObjectsObjectOutlinesOutline, referenceLine: Curve3D, numberTolerance: Double):
        Either<GeometryBuilderException, ContextMessageList<Polyhedron3D>> {
        require(outline.isPolyhedronDefinedByRoadCorners()) { "Outline does not contain a polyhedron represented by road corners." }
        require(outline.cornerLocal.all { it.height == 0.0 || numberTolerance <= it.height }) { "All cornerRoad elements must have a height of either zero or above the tolerance threshold." }
        val outlineId = outline.additionalId.toEither { IllegalStateException("Additional outline ID must be available.") }.getOrHandle { throw it }

        val verticalOutlineElements = outline.cornerRoad
            .map { buildVerticalOutlineElement(it, referenceLine, numberTolerance) }
            .let { NonEmptyList.fromListUnsafe(it) }

        return Polyhedron3DFactory.buildFromVerticalOutlineElements(outlineId, verticalOutlineElements, numberTolerance)
    }

    /**
     * Builds a vertical outline element from OpenDRIVE's road corner element and it's height.
     *
     * @param cornerRoad road corner element of OpenDRIVE which defines one corner of a road object
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     */
    private fun buildVerticalOutlineElement(cornerRoad: RoadObjectsObjectOutlinesOutlineCornerRoad, roadReferenceLine: Curve3D, numberTolerance: Double):
        Polyhedron3DFactory.VerticalOutlineElement {

        val curveRelativeOutlineElementGeometry = cornerRoad.getPoints()

        val basePoint = roadReferenceLine.transform(curveRelativeOutlineElementGeometry.first)
        val headPoint = curveRelativeOutlineElementGeometry.second
            .map { point -> roadReferenceLine.transform(point) }

        return Polyhedron3DFactory.VerticalOutlineElement(basePoint, headPoint, tolerance = numberTolerance)
    }

    /**
     * Builds a list of polyhedrons from OpenDRIVE road objects defined by local corner outlines.
     *
     * @param id identifier of the road space object for error logging
     * @param roadObject road object of OpenDRIVE
     * @param curveAffine affine transformation matrix from the curve
     * @return list of polyhedrons
     */
    fun buildPolyhedronsByLocalCorners(roadObject: RoadObjectsObject, curveAffine: Affine3D, numberTolerance: Double):
        ContextMessageList<List<Polyhedron3D>> {
        val messageList = DefaultMessageList()
        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)

        val (builderExceptions, polyhedronsWithContext) = roadObject
            .getPolyhedronsDefinedByLocalCorners()
            .map { buildPolyhedronByLocalCorners(it, numberTolerance) }
            .separateEither()

        messageList += builderExceptions
            .map { DefaultMessage.of("", it.message, it.location, Severity.WARNING, wasFixed = true) }
            .mergeToReport()
        val polyhedrons = polyhedronsWithContext
            .handleMessageList { messageList += it.messageList }
            .map { it.copy(affineSequence = affineSequence) }

        return ContextMessageList(polyhedrons, messageList)
    }

    /**
     * Builds a single polyhedron from an OpenDRIVE road object defined by local corner outlines.
     */
    private fun buildPolyhedronByLocalCorners(outline: RoadObjectsObjectOutlinesOutline, numberTolerance: Double):
        Either<GeometryBuilderException, ContextMessageList<Polyhedron3D>> = either.eager {
        require(outline.isPolyhedronDefinedByLocalCorners()) { "Outline does not contain a polyhedron represented by local corners." }
        require(outline.cornerLocal.all { it.height == 0.0 || numberTolerance <= it.height }) { "All cornerLocal elements must have a height of either zero or above the tolerance threshold." }
        val outlineId = outline.additionalId.toEither { IllegalStateException("Additional outline ID must be available.") }.getOrHandle { throw it }

        val messageList = DefaultMessageList()

        val verticalOutlineElements = outline.cornerLocal
            .map { Polyhedron3DFactory.VerticalOutlineElement.of(it.getBasePoint(), it.getHeadPoint(), None, numberTolerance) }
            .handleMessageList { messageList += it.messageList }
            .let { NonEmptyList.fromListUnsafe(it) }

        val polyhedronWithContextMessageList = Polyhedron3DFactory.buildFromVerticalOutlineElements(outlineId, verticalOutlineElements, numberTolerance).bind()
        polyhedronWithContextMessageList
    }

    /**
     * Builds a parametric sweep from OpenDRIVE road objects defined by the repeat entries.
     *
     * @param roadObjectRepeat repeated road object of OpenDRIVE
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     */
    fun buildParametricSweeps(roadObjectRepeat: RoadObjectsObjectRepeat, roadReferenceLine: Curve3D, numberTolerance: Double): Option<ParametricSweep3D> {
        if (!roadObjectRepeat.isParametricSweep()) return None

        // curve over which the object is moved
        val objectReferenceCurve2D = Curve2DBuilder.buildLateralTranslatedCurve(roadObjectRepeat, roadReferenceLine, numberTolerance)
        val objectReferenceHeight = FunctionBuilder.buildStackedHeightFunctionFromRepeat(roadObjectRepeat, roadReferenceLine)

        // dimensions of the sweep
        val heightFunction = roadObjectRepeat.getObjectHeightFunction()
        val widthFunction = roadObjectRepeat.getObjectWidthFunction()

        val parametricSweep3D = ParametricSweep3D(
            objectReferenceCurve2D,
            objectReferenceHeight,
            heightFunction,
            widthFunction,
            numberTolerance,
            ParametricSweep3D.DEFAULT_STEP_SIZE
        )
        return parametricSweep3D.some()
    }
}
