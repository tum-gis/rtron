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
import arrow.core.getOrHandle
import arrow.core.separateEither
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.io.report.handleReport
import io.rtron.io.report.mergeToReport
import io.rtron.math.analysis.function.univariate.combination.StackedFunction
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Circle3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.geometry.euclidean.threed.surface.ParametricBoundedSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.Rectangle3D
import io.rtron.math.transform.Affine3D
import io.rtron.math.transform.AffineSequence3D
import io.rtron.model.opendrive.objects.RoadObjectsObject
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlinesOutline
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlinesOutlineCornerRoad
import io.rtron.model.opendrive.objects.RoadObjectsObjectRepeat
import io.rtron.transformer.converter.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.report.of

/**
 * Builder for surface geometries in 3D from the OpenDRIVE data model.
 */
class Surface3DBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _functionBuilder = FunctionBuilder(configuration)
    private val _curve2DBuilder = Curve2DBuilder(configuration)

    // Methods

    /**
     * Builds a list of rectangles from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildRectangles(roadObject: RoadObjectsObject, curveAffine: Affine3D): ContextReport<List<Rectangle3D>> {
        val rectangleList = mutableListOf<Rectangle3D>()
        val report = Report()

        if (roadObject.isRectangle()) {
            val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
            val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
            rectangleList += Rectangle3D.of(roadObject.length, roadObject.width, configuration.numberTolerance, affineSequence)
        }

        if (roadObject.repeat.any { it.isRepeatedCuboid() })
            report += Message.of("Cuboid geometries in the repeat elements are currently not supported.", roadObject.additionalId, isFatal = false, wasHealed = false)

        return ContextReport(rectangleList, report)
    }

    /**
     * Builds a list of circles from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildCircles(roadObject: RoadObjectsObject, curveAffine: Affine3D): ContextReport<List<Circle3D>> {
        val circleList = mutableListOf<Circle3D>()
        val report = Report()

        if (roadObject.isCircle()) {
            val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
            val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
            circleList += Circle3D.of(roadObject.radius, configuration.numberTolerance, affineSequence)
        }

        if (roadObject.repeat.any { it.isRepeatCylinder() })
            report += Message.of("Cuboid geometries in the repeat elements are currently not supported.", roadObject.additionalId, isFatal = false, wasHealed = false)

        return ContextReport(circleList, report)
    }

    /**
     * Builds a list of linear rings from an OpenDRIVE road object defined by road corner outlines.
     */
    fun buildLinearRingsByRoadCorners(roadObject: RoadObjectsObject, referenceLine: Curve3D): ContextReport<List<LinearRing3D>> {
        val report = Report()

        val (builderExceptions, linearRingsWithContext) = roadObject.getLinearRingsDefinedByRoadCorners()
            .map { buildLinearRingByRoadCorners(it, referenceLine) }
            .separateEither()

        report += builderExceptions.map { Message.of(it.message, it.location, isFatal = false, wasHealed = true) }.mergeToReport()
        val linearRings = linearRingsWithContext.handleReport { report += it.report }

        return ContextReport(linearRings, report)
    }

    /**
     * Builds a single linear ring from an OpenDRIVE road object defined by road corner outlines.
     */
    private fun buildLinearRingByRoadCorners(outline: RoadObjectsObjectOutlinesOutline, referenceLine: Curve3D):
        Either<GeometryBuilderException, ContextReport<LinearRing3D>> {
        require(outline.isLinearRingDefinedByRoadCorners()) { "Outline does not contain a linear ring represented by road corners." }
        require(outline.cornerRoad.all { it.height == 0.0 }) { "All cornerRoad elements must have a zero height." }
        val outlineId = outline.additionalId.toEither { IllegalStateException("Additional outline ID must be available.") }.getOrHandle { throw it }

        val vertices = outline.cornerRoad
            .map { buildVertices(it, referenceLine) }
            .let { NonEmptyList.fromListUnsafe(it) }

        return LinearRing3DFactory.buildFromVertices(outlineId, vertices, configuration.numberTolerance)
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
    fun buildLinearRingsByLocalCorners(roadObject: RoadObjectsObject, curveAffine: Affine3D): ContextReport<List<LinearRing3D>> {
        val report = Report()
        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)

        val (builderExceptions, linearRingsWithContext) = roadObject
            .getLinearRingsDefinedByLocalCorners()
            .map { buildLinearRingByLocalCorners(it) }
            .separateEither()

        report += builderExceptions
            .map { Message.of(it.message, it.location, isFatal = false, wasHealed = true) }
            .mergeToReport()
        val linearRings = linearRingsWithContext
            .handleReport { report += it.report }
            .map { it.copy(affineSequence = affineSequence) }

        return ContextReport(linearRings, report)
    }

    /**
     * Builds a single linear ring from an OpenDRIVE road object defined by local corner outlines.
     */
    private fun buildLinearRingByLocalCorners(outline: RoadObjectsObjectOutlinesOutline): Either<GeometryBuilderException, ContextReport<LinearRing3D>> {
        val outlineId = outline.additionalId.toEither { IllegalStateException("Additional outline ID must be available.") }.getOrHandle { throw it }

        val vertices = outline.cornerLocal
            .map { it.getBasePoint() }
            .let { NonEmptyList.fromListUnsafe(it) }

        return LinearRing3DFactory.buildFromVertices(outlineId, vertices, configuration.numberTolerance)
    }

    /**
     * Builds a parametric bounded surface from OpenDRIVE road objects defined by repeat entries representing a horizontal surface.
     */
    fun buildParametricBoundedSurfacesByHorizontalRepeat(
        roadObjectRepeat: RoadObjectsObjectRepeat,
        roadReferenceLine: Curve3D
    ): List<ParametricBoundedSurface3D> {
        if (!roadObjectRepeat.isHorizontalParametricBoundedSurface()) return emptyList()

        // curve over which the object is moved
        val objectReferenceCurve2D = _curve2DBuilder.buildLateralTranslatedCurve(roadObjectRepeat, roadReferenceLine)
        val objectReferenceHeight = _functionBuilder.buildStackedHeightFunctionFromRepeat(roadObjectRepeat, roadReferenceLine)

        // dimension of the object
        val widthFunction = roadObjectRepeat.getObjectWidthFunction()

        // absolute boundary curves
        val leftBoundaryCurve2D = objectReferenceCurve2D.addLateralTranslation(widthFunction, -0.5)
        val leftBoundary = Curve3D(leftBoundaryCurve2D, objectReferenceHeight)
        val rightBoundaryCurve2D = objectReferenceCurve2D.addLateralTranslation(widthFunction, +0.5)
        val rightBoundary = Curve3D(rightBoundaryCurve2D, objectReferenceHeight)

        val parametricBoundedSurface = ParametricBoundedSurface3D(leftBoundary, rightBoundary, configuration.numberTolerance, ParametricBoundedSurface3D.DEFAULT_STEP_SIZE)
        return listOf(parametricBoundedSurface)
    }

    /**
     * Builds a parametric bounded surface from OpenDRIVE road objects defined by repeat entries representing a vertical surface.
     */
    fun buildParametricBoundedSurfacesByVerticalRepeat(
        roadObjectRepeat: RoadObjectsObjectRepeat,
        roadReferenceLine: Curve3D
    ): List<ParametricBoundedSurface3D> {
        if (!roadObjectRepeat.isVerticalParametricBoundedSurface()) return emptyList()

        // curve over which the object is moved
        val objectReferenceCurve2D = _curve2DBuilder.buildLateralTranslatedCurve(roadObjectRepeat, roadReferenceLine)
        val objectReferenceHeight = _functionBuilder.buildStackedHeightFunctionFromRepeat(roadObjectRepeat, roadReferenceLine)

        // dimension of the object
        val heightFunction = roadObjectRepeat.getObjectHeightFunction()

        // absolute boundary curves
        val lowerBoundary = Curve3D(objectReferenceCurve2D, objectReferenceHeight)
        val upperBoundaryHeight = StackedFunction.ofSum(objectReferenceHeight, heightFunction, defaultValue = 0.0)
        val upperBoundary = Curve3D(objectReferenceCurve2D, upperBoundaryHeight)

        val parametricBoundedSurface = ParametricBoundedSurface3D(lowerBoundary, upperBoundary, configuration.numberTolerance, ParametricBoundedSurface3D.DEFAULT_STEP_SIZE)
        return listOf(parametricBoundedSurface)
    }
}
