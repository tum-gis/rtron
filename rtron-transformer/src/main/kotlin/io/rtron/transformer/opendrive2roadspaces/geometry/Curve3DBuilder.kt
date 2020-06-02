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

import io.rtron.io.logging.Logger
import io.rtron.math.analysis.function.univariate.combination.ConcatenatedFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.transformer.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesParameters
import io.rtron.model.opendrive.road.RoadElevationProfileElevation
import io.rtron.model.opendrive.road.objects.RoadObjectsObject
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometry
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.std.handleMessage
import io.rtron.std.isSortedBy


/**
 * Builder for curves in 3D from the OpenDRIVE data model.
 */
class Curve3DBuilder(
        private val reportLogger: Logger,
        private val parameters: Opendrive2RoadspacesParameters
) {

    // Properties and Initializers
    private val _functionBuilder = FunctionBuilder(reportLogger, parameters)
    private val _curve2DBuilder = Curve2DBuilder(reportLogger, parameters)

    // Methods

    /**
     * Builds a curve in 3D from OpenDRIVE's plan view entries and the elevation profile.
     */
    fun buildCurve3D(id: RoadspaceIdentifier, srcPlanViewGeometries: List<RoadPlanViewGeometry>,
                     srcElevationProfiles: List<RoadElevationProfileElevation>): Curve3D {

        val planViewCurve2D = _curve2DBuilder.buildCurve2DFromPlanViewGeometries(srcPlanViewGeometries)

        val heightFunction =
                if (srcElevationProfiles.isSortedBy { it.s }) {
                    ConcatenatedFunction.ofPolynomialFunctions(
                            srcElevationProfiles.map { it.s },
                            srcElevationProfiles.map { it.coefficients })
                            .handleMessage { this.reportLogger.info(it, id.toString()) }
                } else {
                    reportLogger.warn("Elevation profile list is not sorted, therefore the elevation profile" +
                            " is set to zero.", id.toString())
                    LinearFunction.X_AXIS
                }
        return Curve3D(planViewCurve2D, heightFunction, tolerance = parameters.tolerance)
    }

    /**
     * Builds a curve in 3D from OpenDRIVE's road object entry [srcRoadObject].
     */
    fun buildCurve3D(srcRoadObject: RoadObjectsObject, roadReferenceLine: Curve3D): List<Curve3D> {
        if (!srcRoadObject.repeat.isCurve()) return emptyList()

        val curve2D = _curve2DBuilder
                .buildLateralTranslatedCurve(srcRoadObject.repeat, roadReferenceLine)
        val heightFunction = _functionBuilder
                .buildStackedHeightFunctionFromRepeat(srcRoadObject.repeat, roadReferenceLine)

        val curve3D = Curve3D(curve2D, heightFunction, tolerance = parameters.tolerance)
        return listOf(curve3D)
    }
}
