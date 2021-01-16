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
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Circle3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.geometry.euclidean.threed.surface.Rectangle3D
import io.rtron.math.processing.LinearRing3DFactory
import io.rtron.math.transform.Affine3D
import io.rtron.math.transform.AffineSequence3D
import io.rtron.model.opendrive.road.objects.RoadObjectsObject
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectOutlinesOutline
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectOutlinesOutlineCornerRoad
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectRepeat
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObjectIdentifier
import io.rtron.std.ContextMessage
import io.rtron.std.handleAndRemoveFailure
import io.rtron.std.handleFailure
import io.rtron.std.handleMessage
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesParameters

/**
 * Builder for surface geometries in 3D from the OpenDRIVE data model.
 */
class Surface3DBuilder(
    private val reportLogger: Logger,
    private val parameters: Opendrive2RoadspacesParameters
) {

    // Methods

    /**
     * Builds a list of rectangles from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildRectangles(srcRoadObject: RoadObjectsObject, curveAffine: Affine3D): List<Rectangle3D> {
        val rectangleList = mutableListOf<Rectangle3D>()

        if (srcRoadObject.isRectangle()) {
            val objectAffine = Affine3D.of(srcRoadObject.referenceLinePointRelativePose)
            val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
            rectangleList += Rectangle3D(srcRoadObject.length, srcRoadObject.width, parameters.tolerance, affineSequence)
        }

        if (srcRoadObject.repeat.isRepeatedCuboid())
            this.reportLogger.infoOnce("Geometry RepeatedRectangle not implemented yet.")

        return rectangleList
    }

    /**
     * Builds a list of circles from the OpenDRIVE road object class ([RoadObjectsObject]) directly or from the
     * repeated entries defined in [RoadObjectsObjectRepeat].
     */
    fun buildCircles(srcRoadObject: RoadObjectsObject, curveAffine: Affine3D): List<Circle3D> {
        val circleList = mutableListOf<Circle3D>()

        if (srcRoadObject.isCircle()) {
            val objectAffine = Affine3D.of(srcRoadObject.referenceLinePointRelativePose)
            val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)
            circleList += Circle3D(srcRoadObject.radius, parameters.tolerance, affineSequence)
        }

        if (srcRoadObject.repeat.isRepeatCylinder())
            this.reportLogger.infoOnce("Geometry RepeatedCircle not implemented yet.")

        return circleList
    }

    /**
     * Builds a list of linear rings from an OpenDRIVE road object defined by road corner outlines.
     */
    fun buildLinearRingsByRoadCorners(
        id: RoadspaceObjectIdentifier,
        srcRoadObject: RoadObjectsObject,
        referenceLine: Curve3D
    ): List<LinearRing3D> {

        return srcRoadObject.getLinearRingsDefinedByRoadCorners()
            .map { buildLinearRingByRoadCorners(it, referenceLine) }
            .handleAndRemoveFailure { reportLogger.log(it, id.toString()) }
            .handleMessage { reportLogger.log(it, id.toString()) }
    }

    /**
     * Builds a single linear ring from an OpenDRIVE road object defined by road corner outlines.
     */
    private fun buildLinearRingByRoadCorners(srcOutline: RoadObjectsObjectOutlinesOutline, referenceLine: Curve3D):
        Result<ContextMessage<LinearRing3D>, IllegalArgumentException> {

            val vertices = srcOutline.cornerRoad
                .map { buildVertices(it, referenceLine) }
                .handleAndRemoveFailure { reportLogger.log(it) }

            return LinearRing3DFactory.buildFromVertices(vertices, parameters.tolerance)
        }

    /**
     * Builds a vertex from the OpenDRIVE road corner element.
     */
    private fun buildVertices(srcCornerRoad: RoadObjectsObjectOutlinesOutlineCornerRoad, referenceLine: Curve3D):
        Result<Vector3D, Exception> {
            val affine = referenceLine.calculateAffine(srcCornerRoad.curveRelativePosition)
                .handleFailure { return it }
            val basePoint = srcCornerRoad.getBasePoint()
                .handleFailure { return it }
                .let { affine.transform(it.getCartesianCurveOffset()) }
            return Result.success(basePoint)
        }

    /**
     * Builds a list of linear rings from an OpenDRIVE road object defined by local corner outlines.
     */
    fun buildLinearRingsByLocalCorners(
        id: RoadspaceObjectIdentifier,
        srcRoadObject: RoadObjectsObject,
        curveAffine: Affine3D
    ): List<LinearRing3D> {
        val objectAffine = Affine3D.of(srcRoadObject.referenceLinePointRelativePose)
        val affineSequence = AffineSequence3D.of(curveAffine, objectAffine)

        return srcRoadObject.getLinearRingsDefinedByLocalCorners()
            .map { buildLinearRingByLocalCorners(id, it) }
            .handleAndRemoveFailure { reportLogger.log(it, id.toString()) }
            .handleMessage { reportLogger.log(it, id.toString()) }
            .map { it.copy(affineSequence = affineSequence) }
    }

    /**
     * Builds a single linear ring from an OpenDRIVE road object defined by local corner outlines.
     */
    private fun buildLinearRingByLocalCorners(id: RoadspaceObjectIdentifier, srcOutline: RoadObjectsObjectOutlinesOutline):
        Result<ContextMessage<LinearRing3D>, IllegalArgumentException> {

            val vertices = srcOutline.cornerLocal
                .map { it.getBasePoint() }
                .handleAndRemoveFailure { reportLogger.log(it, id.toString(), "Removing outline point.") }

            return LinearRing3DFactory.buildFromVertices(vertices, parameters.tolerance)
        }
}
