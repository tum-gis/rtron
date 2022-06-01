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
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.io.report.handleReport
import io.rtron.io.report.mergeToReport
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
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.report.of

/**
 * Builder for solid geometries in 3D from the OpenDRIVE data model.
 */
class Solid3DBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _functionBuilder = FunctionBuilder(configuration)
    private val _curve2DBuilder = Curve2DBuilder(configuration)

    // Methods

    /**
     * Builds a list of cuboids from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildCuboids(roadObject: RoadObjectsObject, curveAffine: Affine3D): ContextReport<List<Cuboid3D>> {
        val cuboidList = mutableListOf<Cuboid3D>()
        val report = Report()

        if (roadObject.isCuboid()) {
            val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
            val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
            cuboidList += Cuboid3D.of(roadObject.length, roadObject.width, roadObject.heightValidated, configuration.numberTolerance, affineSequence)
        }

        if (roadObject.repeat.any { it.isRepeatedCuboid() })
            report += Message.of("Cuboid geometries in the repeat elements are currently not supported.", roadObject.additionalId, isFatal = false, wasHealed = false)

        return ContextReport(cuboidList, report)
    }

    /**
     * Builds a list of cylinders from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildCylinders(roadObject: RoadObjectsObject, curveAffine: Affine3D): ContextReport<List<Cylinder3D>> {
        val cylinderList = mutableListOf<Cylinder3D>()
        val report = Report()

        if (roadObject.isCylinder()) {
            val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
            val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
            cylinderList += Cylinder3D.of(roadObject.radius, roadObject.height, configuration.numberTolerance, affineSequence)
        }

        if (roadObject.repeat.any { it.isRepeatCylinder() })
            report += Message.of("Cylinder geometries in the repeat elements are currently not supported.", roadObject.additionalId, isFatal = false, wasHealed = false)

        return ContextReport(cylinderList, report)
    }

    /**
     * Builds a list of polyhedrons from OpenDRIVE road objects defined by road corner outlines.
     *
     * @param id identifier of the road space object for error logging
     * @param roadObject road object of OpenDRIVE
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     * @return list of polyhedrons
     */
    fun buildPolyhedronsByRoadCorners(roadObject: RoadObjectsObject, roadReferenceLine: Curve3D): ContextReport<List<Polyhedron3D>> {
        val report = Report()

        val (builderExceptions, polyhedronsWithContext) = roadObject
            .getPolyhedronsDefinedByRoadCorners()
            .map { buildPolyhedronByRoadCorners(it, roadReferenceLine) }
            .separateEither()

        report += builderExceptions.map { Message.of(it.message, it.location, isFatal = false, wasHealed = true) }.mergeToReport()
        val polyhedrons = polyhedronsWithContext.handleReport { report += it.report }

        return ContextReport(polyhedrons, report)
    }

    /**
     * Builds a single polyhedron from an OpenDRIVE road object defined by road corner outlines.
     */
    private fun buildPolyhedronByRoadCorners(outline: RoadObjectsObjectOutlinesOutline, referenceLine: Curve3D):
        Either<GeometryBuilderException, ContextReport<Polyhedron3D>> {
        require(outline.isPolyhedronDefinedByRoadCorners()) { "Outline does not contain a polyhedron represented by road corners." }
        require(outline.cornerRoad.all { configuration.numberTolerance <= it.height }) { "All cornerRoad elements must have a height above the tolerance threshold." }
        val outlineId = outline.additionalId.toEither { IllegalStateException("Additional outline ID must be available.") }.getOrHandle { throw it }

        val verticalOutlineElements = outline.cornerRoad
            .map { buildVerticalOutlineElement(it, referenceLine) }
            .let { NonEmptyList.fromListUnsafe(it) }

        return Polyhedron3DFactory.buildFromVerticalOutlineElements(outlineId, verticalOutlineElements, configuration.numberTolerance)
    }

    /**
     * Builds a vertical outline element from OpenDRIVE's road corner element and it's height.
     *
     * @param cornerRoad road corner element of OpenDRIVE which defines one corner of a road object
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     */
    private fun buildVerticalOutlineElement(cornerRoad: RoadObjectsObjectOutlinesOutlineCornerRoad, roadReferenceLine: Curve3D):
        Polyhedron3DFactory.VerticalOutlineElement {

        val curveRelativeOutlineElementGeometry = cornerRoad.getPoints()

        val basePoint = roadReferenceLine.transform(curveRelativeOutlineElementGeometry.first)
        val headPoint = curveRelativeOutlineElementGeometry.second
            .map { point -> roadReferenceLine.transform(point) }

        return Polyhedron3DFactory.VerticalOutlineElement(basePoint, headPoint, tolerance = configuration.numberTolerance)
    }

    /**
     * Builds a list of polyhedrons from OpenDRIVE road objects defined by local corner outlines.
     *
     * @param id identifier of the road space object for error logging
     * @param roadObject road object of OpenDRIVE
     * @param curveAffine affine transformation matrix from the curve
     * @return list of polyhedrons
     */
    fun buildPolyhedronsByLocalCorners(roadObject: RoadObjectsObject, curveAffine: Affine3D):
        ContextReport<List<Polyhedron3D>> {
        val report = Report()
        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)

        val (builderExceptions, polyhedronsWithContext) = roadObject
            .getPolyhedronsDefinedByLocalCorners()
            .map { buildPolyhedronByLocalCorners(it) }
            .separateEither()

        report += builderExceptions
            .map { Message.of(it.message, it.location, isFatal = false, wasHealed = true) }
            .mergeToReport()
        val polyhedrons = polyhedronsWithContext
            .handleReport { report += it.report }
            .map { it.copy(affineSequence = affineSequence) }

        return ContextReport(polyhedrons, report)

        /*return roadObject
            .getPolyhedronsDefinedByLocalCorners()
            .map { buildPolyhedronByLocalCorners(it) }
            .handleLeftAndFilter { reportLogger.log(it, id.toString()) }
            .handleReport { reportLogger.log(it, id.toString()) }
            .map { it.copy(affineSequence = affineSequence) }*/
    }

    /**
     * Builds a single polyhedron from an OpenDRIVE road object defined by local corner outlines.
     */
    private fun buildPolyhedronByLocalCorners(outline: RoadObjectsObjectOutlinesOutline):
        Either<GeometryBuilderException, ContextReport<Polyhedron3D>> = either.eager {
        require(outline.isPolyhedronDefinedByLocalCorners()) { "Outline does not contain a polyhedron represented by local corners." }
        require(outline.cornerLocal.all { configuration.numberTolerance <= it.height }) { "All cornerLocal elements must have a height above the tolerance threshold." }
        val outlineId = outline.additionalId.toEither { IllegalStateException("Additional outline ID must be available.") }.getOrHandle { throw it }

        val report = Report()

        val verticalOutlineElements = outline.cornerLocal
            .map { it.getPoints() }
            .map { Polyhedron3DFactory.VerticalOutlineElement.of(it.first, it.second, None, configuration.numberTolerance) }
            .handleReport { report += it.report }
            .let { NonEmptyList.fromListUnsafe(it) }

        val polyhedronWithContextReport = Polyhedron3DFactory.buildFromVerticalOutlineElements(outlineId, verticalOutlineElements, configuration.numberTolerance).bind()
        polyhedronWithContextReport
    }

    /**
     * Builds a parametric sweep from OpenDRIVE road objects defined by the repeat entries.
     *
     * @param roadObject road object of OpenDRIVE
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     */
    fun buildParametricSweeps(roadObjectRepeat: RoadObjectsObjectRepeat, roadReferenceLine: Curve3D): Option<ParametricSweep3D> {
        if (!roadObjectRepeat.isParametricSweep()) return None

        // curve over which the object is moved
        val objectReferenceCurve2D = _curve2DBuilder.buildLateralTranslatedCurve(roadObjectRepeat, roadReferenceLine)
        val objectReferenceHeight = _functionBuilder.buildStackedHeightFunctionFromRepeat(roadObjectRepeat, roadReferenceLine)

        // dimensions of the sweep
        val heightFunction = roadObjectRepeat.getObjectHeightFunction()
        val widthFunction = roadObjectRepeat.getObjectWidthFunction()

        val parametricSweep3D = ParametricSweep3D(
            objectReferenceCurve2D,
            objectReferenceHeight,
            heightFunction,
            widthFunction,
            configuration.numberTolerance,
            ParametricSweep3D.DEFAULT_STEP_SIZE
        )
        return parametricSweep3D.some()
    }
}
