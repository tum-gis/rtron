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

package io.rtron.transformer.opendrive2roadspaces.geometry

import com.github.kittinunf.result.Result
import io.rtron.io.logging.Logger
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.math.geometry.euclidean.threed.solid.Cuboid3D
import io.rtron.math.geometry.euclidean.threed.solid.Cylinder3D
import io.rtron.math.geometry.euclidean.threed.solid.ParametricSweep3D
import io.rtron.math.geometry.euclidean.threed.solid.Polyhedron3D
import io.rtron.math.processing.Polyhedron3DFactory
import io.rtron.math.transform.Affine3D
import io.rtron.math.transform.AffineSequence3D
import io.rtron.model.opendrive.road.objects.RoadObjectsObject
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectOutlinesOutline
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectOutlinesOutlineCornerRoad
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectRepeat
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObjectIdentifier
import io.rtron.std.ContextMessage
import io.rtron.std.Optional
import io.rtron.std.handleAndRemoveFailure
import io.rtron.std.handleFailure
import io.rtron.std.handleMessage
import io.rtron.std.map
import io.rtron.transformer.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesParameters

/**
 * Builder for solid geometries in 3D from the OpenDRIVE data model.
 */
class Solid3DBuilder(
    private val reportLogger: Logger,
    private val parameters: Opendrive2RoadspacesParameters
) {

    // Properties and Initializers
    private val _functionBuilder = FunctionBuilder(reportLogger, parameters)
    private val _curve2DBuilder = Curve2DBuilder(reportLogger, parameters)

    // Methods

    /**
     * Builds a list of cuboids from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildCuboids(srcRoadObject: RoadObjectsObject, curveAffine: Affine3D): List<Cuboid3D> {
        val cuboidList = mutableListOf<Cuboid3D>()

        if (srcRoadObject.isCuboid()) {
            val objectAffine = Affine3D.of(srcRoadObject.referenceLinePointRelativePose)
            val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
            cuboidList += Cuboid3D(srcRoadObject.length, srcRoadObject.width, srcRoadObject.height, parameters.tolerance, affineSequence)
        }

        if (srcRoadObject.repeat.isRepeatedCuboid())
            this.reportLogger.infoOnce("Geometry RepeatedCuboids not implemented yet.")

        return cuboidList
    }

    /**
     * Builds a list of cylinders from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildCylinders(srcRoadObject: RoadObjectsObject, curveAffine: Affine3D): List<Cylinder3D> {
        val cylinderList = mutableListOf<Cylinder3D>()

        if (srcRoadObject.isCylinder()) {
            val objectAffine = Affine3D.of(srcRoadObject.referenceLinePointRelativePose)
            val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
            cylinderList += Cylinder3D(srcRoadObject.radius, srcRoadObject.height, parameters.tolerance, affineSequence)
        }

        if (srcRoadObject.repeat.isRepeatCylinder())
            this.reportLogger.infoOnce("Geometry RepeatedCylinder not implemented yet.")

        return cylinderList
    }

    /**
     * Builds a list of polyhedrons from OpenDRIVE road objects defined by road corner outlines.
     *
     * @param id identifier of the road space object for error logging
     * @param srcRoadObject road object of OpenDRIVE
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     * @return list of polyhedrons
     */
    fun buildPolyhedronsByRoadCorners(
        id: RoadspaceObjectIdentifier,
        srcRoadObject: RoadObjectsObject,
        roadReferenceLine: Curve3D
    ): List<Polyhedron3D> {

        return srcRoadObject.getPolyhedronsDefinedByRoadCorners()
            .map { buildPolyhedronByRoadCorners(id, it, roadReferenceLine) }
            .handleAndRemoveFailure { reportLogger.log(it, id.toString()) }
            .handleMessage { reportLogger.log(it, id.toString()) }
    }

    /**
     * Builds a single polyhedron from an OpenDRIVE road object defined by road corner outlines.
     */
    private fun buildPolyhedronByRoadCorners(
        id: RoadspaceObjectIdentifier,
        srcOutline: RoadObjectsObjectOutlinesOutline,
        referenceLine: Curve3D
    ): Result<ContextMessage<Polyhedron3D>, Exception> {
        require(srcOutline.isPolyhedronDefinedByRoadCorners()) { "Outline does not contain a polyhedron represented by road corners." }

        val validCornerRoadElements = srcOutline.cornerRoad.filter { it.hasZeroHeight() || it.hasPositiveHeight() }
        if (validCornerRoadElements.size < srcOutline.cornerRoad.size)
            reportLogger.info(
                "Removing at least one outline element due to a negative height value.",
                id.toString()
            )

        val verticalOutlineElements = validCornerRoadElements
            .map { buildVerticalOutlineElement(it, referenceLine) }
            .handleAndRemoveFailure { reportLogger.log(it, id.toString(), "Removing outline element.") }

        return Polyhedron3DFactory.buildFromVerticalOutlineElements(verticalOutlineElements, parameters.tolerance)
    }

    /**
     * Builds a vertical outline element from OpenDRIVE's road corner element and it's height.
     *
     * @param srcCornerRoad road corner element of OpenDRIVE which defines one corner of a road object
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     */
    private fun buildVerticalOutlineElement(
        srcCornerRoad: RoadObjectsObjectOutlinesOutlineCornerRoad,
        roadReferenceLine: Curve3D
    ):
        Result<Polyhedron3DFactory.VerticalOutlineElement, Exception> {

            val curveRelativeOutlineElementGeometry = srcCornerRoad.getPoints()
                .handleFailure { return it }

            val basePoint = roadReferenceLine.transform(curveRelativeOutlineElementGeometry.first)
                .handleFailure { return it }
            val headPoint = curveRelativeOutlineElementGeometry.second
                .map { point -> roadReferenceLine.transform(point).handleFailure { return it } }

            val verticalOutlineElement = Polyhedron3DFactory.VerticalOutlineElement(basePoint, headPoint, tolerance = parameters.tolerance)
            return Result.success(verticalOutlineElement)
        }

    /**
     * Builds a list of polyhedrons from OpenDRIVE road objects defined by local corner outlines.
     *
     * @param id identifier of the road space object for error logging
     * @param srcRoadObject road object of OpenDRIVE
     * @param curveAffine affine transformation matrix from the curve
     * @return list of polyhedrons
     */
    fun buildPolyhedronsByLocalCorners(
        id: RoadspaceObjectIdentifier,
        srcRoadObject: RoadObjectsObject,
        curveAffine: Affine3D
    ): List<Polyhedron3D> {
        val objectAffine = Affine3D.of(srcRoadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)

        return srcRoadObject.getPolyhedronsDefinedByLocalCorners()
            .map { buildPolyhedronByLocalCorners(id, it) }
            .handleAndRemoveFailure { reportLogger.log(it, id.toString()) }
            .handleMessage { reportLogger.log(it, id.toString()) }
            .map { it.copy(affineSequence = affineSequence) }
    }

    /**
     * Builds a single polyhedron from an OpenDRIVE road object defined by local corner outlines.
     */
    private fun buildPolyhedronByLocalCorners(id: RoadspaceObjectIdentifier, srcOutline: RoadObjectsObjectOutlinesOutline):
        Result<ContextMessage<Polyhedron3D>, Exception> {
            require(srcOutline.isPolyhedronDefinedByLocalCorners()) { "Outline does not contain a polyhedron represented by local corners." }

            val validCornerLocalElements = srcOutline.cornerLocal.filter { it.hasZeroHeight() || it.hasPositiveHeight() }
            if (validCornerLocalElements.size < srcOutline.cornerLocal.size)
                reportLogger.info(
                    "Removing at least one outline element due to a negative height value.",
                    id.toString()
                )

            val verticalOutlineElements = validCornerLocalElements
                .map { it.getPoints() }
                .handleAndRemoveFailure { reportLogger.log(it, id.toString(), "Removing outline element.") }
                .map { Polyhedron3DFactory.VerticalOutlineElement.of(it.first, it.second, Optional.empty(), parameters.tolerance) }
                .handleMessage { reportLogger.log(it, id.toString(), "Removing outline element.") }

            return Polyhedron3DFactory.buildFromVerticalOutlineElements(verticalOutlineElements, parameters.tolerance)
        }

    /**
     * Builds a parametric sweep from OpenDRIVE road objects defined by the repeat entries.
     *
     * @param srcRoadObject road object of OpenDRIVE
     * @param roadReferenceLine road reference line for transforming curve relative coordinates
     */
    fun buildParametricSweeps(srcRoadObject: RoadObjectsObject, roadReferenceLine: Curve3D): List<ParametricSweep3D> {
        if (!srcRoadObject.repeat.isParametricSweep()) return emptyList()

        // curve over which the object is moved
        val sweepReferenceCurve2D =
            _curve2DBuilder.buildLateralTranslatedCurve(srcRoadObject.repeat, roadReferenceLine)
        val sweepReferenceHeight =
            _functionBuilder.buildStackedHeightFunctionFromRepeat(srcRoadObject.repeat, roadReferenceLine)

        // dimensions of the sweep
        val heightFunction = srcRoadObject.repeat.getObjectHeightFunction()
        val widthFunction = srcRoadObject.repeat.getObjectWidthFunction()

        val parametricSweep3D = ParametricSweep3D(
            sweepReferenceCurve2D,
            sweepReferenceHeight,
            heightFunction,
            widthFunction,
            parameters.tolerance
        )
        return listOf(parametricSweep3D)
    }
}
