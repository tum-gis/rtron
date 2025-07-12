/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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
import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.separateEither
import arrow.core.some
import arrow.core.toNonEmptyListOrNull
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.io.issues.handleIssueList
import io.rtron.io.issues.mergeToReport
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
import io.rtron.transformer.issues.opendrive.of

/**
 * Builder for solid geometries in 3D from the OpenDRIVE data model.
 */
object Solid3DBuilder {
    // Methods

    /**
     * Builds a list of cuboids from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildCuboids(
        roadObject: RoadObjectsObject,
        curveAffine: Affine3D,
        numberTolerance: Double,
    ): Cuboid3D {
        require(roadObject.containsCuboid()) { "Road object must contain cuboid." }

        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)

        return Cuboid3D.of(
            roadObject.length,
            roadObject.width,
            roadObject.heightValidated,
            numberTolerance,
            affineSequence,
        )
    }

    /**
     * Builds a list of cylinders from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildCylinders(
        roadObject: RoadObjectsObject,
        curveAffine: Affine3D,
        numberTolerance: Double,
    ): Cylinder3D {
        require(roadObject.containsCylinder()) { "Road object must contain cylinder." }

        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
        return Cylinder3D.of(roadObject.radius, roadObject.height, numberTolerance, affineSequence)
    }

    /**
     * Builds a list of polyhedrons from OpenDRIVE road objects defined by road corner outlines.
     *
     * @param roadObject road object of OpenDRIVE
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     * @return list of polyhedrons
     */
    fun buildPolyhedronsByRoadCorners(
        roadObject: RoadObjectsObject,
        roadReferenceLine: Curve3D,
        numberTolerance: Double,
    ): ContextIssueList<List<Polyhedron3D>> {
        val issueList = DefaultIssueList()

        val (builderExceptions, polyhedronsWithContext) =
            roadObject
                .getPolyhedronsDefinedByRoadCorners()
                .map { buildPolyhedronByRoadCorners(it, roadReferenceLine, numberTolerance) }
                .separateEither()

        issueList +=
            builderExceptions
                .map {
                    DefaultIssue.of(
                        "PolyhedronNotConstructableFromRoadCornerOutlines",
                        it.message,
                        it.location,
                        Severity.WARNING,
                        wasFixed = true,
                    )
                }.mergeToReport()
        val polyhedrons = polyhedronsWithContext.handleIssueList { issueList += it.issueList }

        return ContextIssueList(polyhedrons, issueList)
    }

    /**
     * Builds a single polyhedron from an OpenDRIVE road object defined by road corner outlines.
     */
    private fun buildPolyhedronByRoadCorners(
        outline: RoadObjectsObjectOutlinesOutline,
        referenceLine: Curve3D,
        numberTolerance: Double,
    ): Either<GeometryBuilderException, ContextIssueList<Polyhedron3D>> {
        require(outline.isPolyhedronDefinedByRoadCorners()) { "Outline does not contain a polyhedron represented by road corners." }
        require(
            outline.cornerRoad.all {
                it.height == 0.0 || numberTolerance <= it.height
            },
        ) { "All cornerRoad elements must have a height of either zero or above the tolerance threshold." }
        val outlineId =
            outline.additionalId
                .toEither {
                    IllegalStateException(
                        "Additional outline ID must be available.",
                    )
                }.getOrElse { throw it }

        val verticalOutlineElements =
            outline.cornerRoad
                .map { buildVerticalOutlineElement(it, referenceLine, numberTolerance) }
                .let { it.toNonEmptyListOrNull()!! }

        return Polyhedron3DFactory.buildFromVerticalOutlineElements(outlineId, verticalOutlineElements, numberTolerance)
    }

    /**
     * Builds a list of polyhedrons from OpenDRIVE road objects defined by extruded top road corner outlines.
     *
     * @param roadObject road object of OpenDRIVE
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     * @param extrusionHeight height by which the top road corners are to be extruded
     * @return list of polyhedrons
     */
    fun buildPolyhedronsByExtrudedTopRoadCorners(
        roadObject: RoadObjectsObject,
        roadReferenceLine: Curve3D,
        extrusionHeight: Double,
        numberTolerance: Double,
    ): ContextIssueList<List<Polyhedron3D>> {
        val issueList = DefaultIssueList()

        val (builderExceptions, polyhedronsWithContext) =
            (roadObject.getLinearRingsDefinedByRoadCorners() + roadObject.getPolyhedronsDefinedByRoadCorners())
                .map { buildPolyhedronByExtrudedTopRoadCorners(it, roadReferenceLine, extrusionHeight, numberTolerance) }
                .separateEither()

        issueList +=
            builderExceptions
                .map {
                    DefaultIssue.of(
                        "PolyhedronNotConstructableFromRoadCornerOutlines",
                        it.message,
                        it.location,
                        Severity.WARNING,
                        wasFixed = true,
                    )
                }.mergeToReport()
        val polyhedrons = polyhedronsWithContext.handleIssueList { issueList += it.issueList }

        return ContextIssueList(polyhedrons, issueList)
    }

    /**
     * Builds a polyhedron from an OpenDRIVE road object defined by extruded top road corner outlines.
     */
    private fun buildPolyhedronByExtrudedTopRoadCorners(
        outline: RoadObjectsObjectOutlinesOutline,
        referenceLine: Curve3D,
        extrusionHeight: Double,
        numberTolerance: Double,
    ): Either<GeometryBuilderException, ContextIssueList<Polyhedron3D>> {
        require(
            outline.cornerRoad.all {
                it.height == 0.0 || numberTolerance <= it.height
            },
        ) { "All cornerRoad elements must have a height of either zero or above the tolerance threshold." }
        val outlineId =
            outline.additionalId
                .toEither {
                    IllegalStateException(
                        "Additional outline ID must be available.",
                    )
                }.getOrElse { throw it }

        val verticalOutlineElements =
            outline.cornerRoad
                .map { it.copy(dz = it.dz + it.height, height = it.dz + it.height + extrusionHeight) }
                .map { buildVerticalOutlineElement(it, referenceLine, numberTolerance) }
                .let { it.toNonEmptyListOrNull()!! }

        return Polyhedron3DFactory.buildFromVerticalOutlineElements(outlineId, verticalOutlineElements, numberTolerance)
    }

    /**
     * Builds a vertical outline element from OpenDRIVE's road corner element, and it's height.
     *
     * @param cornerRoad road corner element of OpenDRIVE which defines one corner of a road object
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     */
    private fun buildVerticalOutlineElement(
        cornerRoad: RoadObjectsObjectOutlinesOutlineCornerRoad,
        roadReferenceLine: Curve3D,
        numberTolerance: Double,
    ): Polyhedron3DFactory.VerticalOutlineElement {
        val curveRelativeOutlineElementGeometry = cornerRoad.getPoints()

        val basePoint = roadReferenceLine.transform(curveRelativeOutlineElementGeometry.first)
        val headPoint =
            curveRelativeOutlineElementGeometry.second
                .map { point -> roadReferenceLine.transform(point) }

        return Polyhedron3DFactory.VerticalOutlineElement(basePoint, headPoint, tolerance = numberTolerance)
    }

    /**
     * Builds a list of polyhedrons from OpenDRIVE road objects defined by local corner outlines.
     *
     * @param roadObject road object of OpenDRIVE
     * @param curveAffine affine transformation matrix from the curve
     * @return list of polyhedrons
     */
    fun buildPolyhedronsByLocalCorners(
        roadObject: RoadObjectsObject,
        curveAffine: Affine3D,
        numberTolerance: Double,
    ): ContextIssueList<List<Polyhedron3D>> {
        val issueList = DefaultIssueList()
        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)

        val (builderExceptions, polyhedronsWithContext) =
            roadObject
                .getPolyhedronsDefinedByLocalCorners()
                .map { buildPolyhedronByLocalCorners(it, numberTolerance) }
                .separateEither()

        issueList +=
            builderExceptions
                .map {
                    DefaultIssue.of(
                        "PolyhedronNotConstructableFromLocalCornerOutlines",
                        it.message,
                        it.location,
                        Severity.WARNING,
                        wasFixed = true,
                    )
                }.mergeToReport()
        val polyhedrons =
            polyhedronsWithContext
                .handleIssueList { issueList += it.issueList }
                .map { it.copy(affineSequence = affineSequence) }

        return ContextIssueList(polyhedrons, issueList)
    }

    /**
     * Builds a single polyhedron from an OpenDRIVE road object defined by local corner outlines.
     */
    private fun buildPolyhedronByLocalCorners(
        outline: RoadObjectsObjectOutlinesOutline,
        numberTolerance: Double,
    ): Either<GeometryBuilderException, ContextIssueList<Polyhedron3D>> =
        either {
            require(outline.isPolyhedronDefinedByLocalCorners()) { "Outline does not contain a polyhedron represented by local corners." }
            require(
                outline.cornerLocal.all {
                    it.height == 0.0 || numberTolerance <= it.height
                },
            ) { "All cornerLocal elements must have a height of either zero or above the tolerance threshold." }
            val outlineId =
                outline.additionalId
                    .toEither {
                        IllegalStateException(
                            "Additional outline ID must be available.",
                        )
                    }.getOrElse { throw it }

            val issueList = DefaultIssueList()

            val verticalOutlineElements =
                outline.cornerLocal
                    .map { Polyhedron3DFactory.VerticalOutlineElement.of(it.getBasePoint(), it.getHeadPoint(), None, numberTolerance) }
                    .handleIssueList { issueList += it.issueList }
                    .let { it.toNonEmptyListOrNull()!! }

            val polyhedronWithContextIssueList =
                Polyhedron3DFactory
                    .buildFromVerticalOutlineElements(
                        outlineId,
                        verticalOutlineElements,
                        numberTolerance,
                    ).bind()
            polyhedronWithContextIssueList
        }

    /**
     * Builds a list of polyhedrons from OpenDRIVE road objects defined by extruded top local corner outlines.
     *
     * @param roadObject road object of OpenDRIVE
     * @param curveAffine affine transformation matrix from the curve
     * @param extrusionHeight height by which the top local corners are to be extruded
     * @return list of polyhedrons
     */
    fun buildPolyhedronsByExtrudedTopLocalCorners(
        roadObject: RoadObjectsObject,
        curveAffine: Affine3D,
        extrusionHeight: Double,
        numberTolerance: Double,
    ): ContextIssueList<List<Polyhedron3D>> {
        val issueList = DefaultIssueList()
        val objectAffine = Affine3D.of(roadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)

        val (builderExceptions, polyhedronsWithContext) =
            (roadObject.getLinearRingsDefinedByLocalCorners() + roadObject.getPolyhedronsDefinedByLocalCorners())
                .map { buildPolyhedronByExtrudedTopLocalCorners(it, extrusionHeight, numberTolerance) }
                .separateEither()

        issueList +=
            builderExceptions
                .map {
                    DefaultIssue.of(
                        "PolyhedronNotConstructableFromLocalCornerOutlines",
                        it.message,
                        it.location,
                        Severity.WARNING,
                        wasFixed = true,
                    )
                }.mergeToReport()
        val polyhedrons =
            polyhedronsWithContext
                .handleIssueList { issueList += it.issueList }
                .map { it.copy(affineSequence = affineSequence) }

        return ContextIssueList(polyhedrons, issueList)
    }

    /**
     * Builds a single polyhedron from an OpenDRIVE road object defined by extruded top local corner outlines.
     */
    private fun buildPolyhedronByExtrudedTopLocalCorners(
        outline: RoadObjectsObjectOutlinesOutline,
        extrusionHeight: Double,
        numberTolerance: Double,
    ): Either<GeometryBuilderException, ContextIssueList<Polyhedron3D>> =
        either {
            require(
                outline.cornerLocal.all {
                    it.height == 0.0 || numberTolerance <= it.height
                },
            ) { "All cornerLocal elements must have a height of either zero or above the tolerance threshold." }
            val outlineId =
                outline.additionalId
                    .toEither {
                        IllegalStateException(
                            "Additional outline ID must be available.",
                        )
                    }.getOrElse { throw it }

            val issueList = DefaultIssueList()

            val verticalOutlineElements =
                outline.cornerLocal
                    .map { it.copy(z = it.z + it.height, height = extrusionHeight) }
                    .map { Polyhedron3DFactory.VerticalOutlineElement.of(it.getBasePoint(), it.getHeadPoint(), None, numberTolerance) }
                    .handleIssueList { issueList += it.issueList }
                    .let { it.toNonEmptyListOrNull()!! }

            val polyhedronWithContextIssueList =
                Polyhedron3DFactory
                    .buildFromVerticalOutlineElements(
                        outlineId,
                        verticalOutlineElements,
                        numberTolerance,
                    ).bind()
            polyhedronWithContextIssueList
        }

    /**
     * Builds a parametric sweep from OpenDRIVE road objects defined by the repeat entries.
     *
     * @param roadObjectRepeat repeated road object of OpenDRIVE
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     */
    fun buildParametricSweep(
        roadObjectRepeat: RoadObjectsObjectRepeat,
        roadReferenceLine: Curve3D,
        numberTolerance: Double,
    ): Option<ParametricSweep3D> {
        if (!roadObjectRepeat.containsParametricSweep()) return None

        // curve over which the object is moved
        val objectReferenceCurve2D = Curve2DBuilder.buildLateralTranslatedCurve(roadObjectRepeat, roadReferenceLine, numberTolerance)
        val objectReferenceHeight = FunctionBuilder.buildStackedHeightFunctionFromRepeat(roadObjectRepeat, roadReferenceLine)

        // dimensions of the sweep
        val heightFunction = roadObjectRepeat.getObjectHeightFunction()
        val widthFunction = roadObjectRepeat.getObjectWidthFunction()

        val parametricSweep3D =
            ParametricSweep3D(
                objectReferenceCurve2D,
                objectReferenceHeight,
                heightFunction,
                widthFunction,
                numberTolerance,
                ParametricSweep3D.DEFAULT_STEP_SIZE,
            )
        return parametricSweep3D.some()
    }
}
