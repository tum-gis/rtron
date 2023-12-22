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
import arrow.core.getOrElse
import arrow.core.separateEither
import arrow.core.toNonEmptyListOrNull
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.io.issues.handleIssueList
import io.rtron.io.issues.mergeToReport
import io.rtron.math.analysis.function.univariate.combination.StackedFunction
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Circle3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.geometry.euclidean.threed.surface.ParametricBoundedSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.Rectangle3D
import io.rtron.math.std.PI
import io.rtron.math.transform.Affine3D
import io.rtron.math.transform.AffineSequence3D
import io.rtron.model.opendrive.objects.RoadObjectsObject
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlinesOutline
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlinesOutlineCornerRoad
import io.rtron.model.opendrive.objects.RoadObjectsObjectRepeat
import io.rtron.model.opendrive.signal.RoadSignalsSignal
import io.rtron.transformer.converter.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.issues.opendrive.of

/**
 * Builder for surface geometries in 3D from the OpenDRIVE data model.
 */
object Surface3DBuilder {

    // Methods

    /** Builds a rectangle from an [RoadObjectsObject] with [curveAffine] being the affine transformation at respective curve. */
    fun buildRectangle(roadObject: RoadObjectsObject, curveAffine: Affine3D, numberTolerance: Double): Rectangle3D {
        require(roadObject.containsRectangle()) { "Road object must contain rectangle." }

        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
        return Rectangle3D.of(roadObject.length, roadObject.width, numberTolerance, affineSequence)
    }

    /** Builds a rectangle from an [RoadSignalsSignal] with [curveAffine] being the affine transformation at respective curve. */
    fun buildRectangle(roadObject: RoadSignalsSignal, curveAffine: Affine3D, numberTolerance: Double): Rectangle3D {
        require(roadObject.containsRectangle()) { "Road signal must contain rectangle." }

        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        // needs to be rotated since rectangle is defined with length in x-axis and width in y-axis
        val objectRotation = Affine3D.of(Rotation3D.of(0.0, -PI / 2.0, 0.0))
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine, objectRotation)
        return Rectangle3D.of(roadObject.height, roadObject.width, numberTolerance, affineSequence)
    }

    /** Builds a circle from an [RoadObjectsObject] with [curveAffine] being the affine transformation at respective curve. */

    fun buildCircle(roadObject: RoadObjectsObject, curveAffine: Affine3D, numberTolerance: Double): Circle3D {
        require(roadObject.containsCircle()) { "Road object must contain circle." }

        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
        return Circle3D.of(roadObject.radius, numberTolerance, affineSequence)
    }

    /**
     * Builds a list of linear rings from an OpenDRIVE road object defined by road corner outlines.
     */
    fun buildLinearRingsByRoadCorners(roadObject: RoadObjectsObject, referenceLine: Curve3D, numberTolerance: Double): ContextIssueList<List<LinearRing3D>> {
        val issueList = DefaultIssueList()

        val (builderExceptions, linearRingsWithContext) = roadObject.getLinearRingsDefinedByRoadCorners()
            .map { buildLinearRingByRoadCorners(it, referenceLine, numberTolerance) }
            .separateEither()

        issueList += builderExceptions.map { DefaultIssue.of("LinearRingNotConstructableFromRoadCornerOutlines", it.message, it.location, Severity.WARNING, wasFixed = true) }.mergeToReport()
        val linearRings = linearRingsWithContext.handleIssueList { issueList += it.issueList }

        return ContextIssueList(linearRings, issueList)
    }

    /**
     * Builds a single linear ring from an OpenDRIVE road object defined by road corner outlines.
     */
    private fun buildLinearRingByRoadCorners(outline: RoadObjectsObjectOutlinesOutline, referenceLine: Curve3D, numberTolerance: Double):
        Either<GeometryBuilderException, ContextIssueList<LinearRing3D>> {
        require(outline.isLinearRingDefinedByRoadCorners()) { "Outline does not contain a linear ring represented by road corners." }
        require(outline.cornerRoad.all { it.height == 0.0 }) { "All cornerRoad elements must have a zero height." }
        val outlineId = outline.additionalId.toEither { IllegalStateException("Additional outline ID must be available.") }.getOrElse { throw it }

        val vertices = outline.cornerRoad
            .map { buildVertices(it, referenceLine) }
            .let { it.toNonEmptyListOrNull()!! }

        return LinearRing3DFactory.buildFromVertices(outlineId, vertices, numberTolerance)
    }

    /**
     * Builds a vertex from the OpenDRIVE road corner element.
     */
    private fun buildVertices(cornerRoad: RoadObjectsObjectOutlinesOutlineCornerRoad, referenceLine: Curve3D): Vector3D {
        val affine = referenceLine.calculateAffine(cornerRoad.curveRelativePosition)

        val basePoint = cornerRoad.getBasePoint()
        return affine.transform(basePoint.getCartesianCurveOffset())
    }

    /**
     * Builds a list of linear rings from an OpenDRIVE road object defined by local corner outlines.
     */
    fun buildLinearRingsByLocalCorners(roadObject: RoadObjectsObject, curveAffine: Affine3D, numberTolerance: Double): ContextIssueList<List<LinearRing3D>> {
        val issueList = DefaultIssueList()
        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)

        val (builderExceptions, linearRingsWithContext) = roadObject
            .getLinearRingsDefinedByLocalCorners()
            .map { buildLinearRingByLocalCorners(it, numberTolerance) }
            .separateEither()

        issueList += builderExceptions
            .map { DefaultIssue.of("LinearRingNotConstructableFromLocalCornerOutlines", it.message, it.location, Severity.WARNING, wasFixed = true) }
            .mergeToReport()
        val linearRings = linearRingsWithContext
            .handleIssueList { issueList += it.issueList }
            .map { it.copy(affineSequence = affineSequence) }

        return ContextIssueList(linearRings, issueList)
    }

    /**
     * Builds a single linear ring from an OpenDRIVE road object defined by local corner outlines.
     */
    private fun buildLinearRingByLocalCorners(outline: RoadObjectsObjectOutlinesOutline, numberTolerance: Double): Either<GeometryBuilderException, ContextIssueList<LinearRing3D>> {
        val outlineId = outline.additionalId.toEither { IllegalStateException("Additional outline ID must be available.") }.getOrElse { throw it }

        val vertices = outline.cornerLocal
            .map { it.getBasePoint() }
            .let { it.toNonEmptyListOrNull()!! }

        return LinearRing3DFactory.buildFromVertices(outlineId, vertices, numberTolerance)
    }

    /**
     * Builds a parametric bounded surface from OpenDRIVE road objects defined by repeat entries representing a horizontal surface.
     */
    fun buildParametricBoundedSurfacesByHorizontalRepeat(
        roadObjectRepeat: RoadObjectsObjectRepeat,
        roadReferenceLine: Curve3D,
        numberTolerance: Double
    ): List<ParametricBoundedSurface3D> {
        if (!roadObjectRepeat.containsHorizontalParametricBoundedSurface()) return emptyList()

        // curve over which the object is moved
        val objectReferenceCurve2D = Curve2DBuilder.buildLateralTranslatedCurve(roadObjectRepeat, roadReferenceLine, numberTolerance)
        val objectReferenceHeight = FunctionBuilder.buildStackedHeightFunctionFromRepeat(roadObjectRepeat, roadReferenceLine)

        // dimension of the object
        val widthFunction = roadObjectRepeat.getObjectWidthFunction()

        // absolute boundary curves
        val leftBoundaryCurve2D = objectReferenceCurve2D.addLateralTranslation(widthFunction, -0.5)
        val leftBoundary = Curve3D(leftBoundaryCurve2D, objectReferenceHeight)
        val rightBoundaryCurve2D = objectReferenceCurve2D.addLateralTranslation(widthFunction, +0.5)
        val rightBoundary = Curve3D(rightBoundaryCurve2D, objectReferenceHeight)

        val parametricBoundedSurface = ParametricBoundedSurface3D(leftBoundary, rightBoundary, numberTolerance, ParametricBoundedSurface3D.DEFAULT_STEP_SIZE)
        return listOf(parametricBoundedSurface)
    }

    /**
     * Builds a parametric bounded surface from OpenDRIVE road objects defined by repeat entries representing a vertical surface.
     */
    fun buildParametricBoundedSurfacesByVerticalRepeat(
        roadObjectRepeat: RoadObjectsObjectRepeat,
        roadReferenceLine: Curve3D,
        numberTolerance: Double
    ): List<ParametricBoundedSurface3D> {
        if (!roadObjectRepeat.containsVerticalParametricBoundedSurface()) return emptyList()

        // curve over which the object is moved
        val objectReferenceCurve2D = Curve2DBuilder.buildLateralTranslatedCurve(roadObjectRepeat, roadReferenceLine, numberTolerance)
        val objectReferenceHeight = FunctionBuilder.buildStackedHeightFunctionFromRepeat(roadObjectRepeat, roadReferenceLine)

        // dimension of the object
        val heightFunction = roadObjectRepeat.getObjectHeightFunction()

        // absolute boundary curves
        val lowerBoundary = Curve3D(objectReferenceCurve2D, objectReferenceHeight)
        val upperBoundaryHeight = StackedFunction.ofSum(objectReferenceHeight, heightFunction, defaultValue = 0.0)
        val upperBoundary = Curve3D(objectReferenceCurve2D, upperBoundaryHeight)

        val parametricBoundedSurface = ParametricBoundedSurface3D(lowerBoundary, upperBoundary, numberTolerance, ParametricBoundedSurface3D.DEFAULT_STEP_SIZE)
        return listOf(parametricBoundedSurface)
    }
}
