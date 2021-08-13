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

package io.rtron.transformer.opendrive2roadspaces.geometry

import com.github.kittinunf.result.Result
import io.rtron.io.logging.Logger
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.ConcatenatedFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.model.opendrive.road.RoadElevationProfileElevation
import io.rtron.model.opendrive.road.objects.RoadObjectsObject
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometry
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.std.filterToStrictSortingBy
import io.rtron.std.handleFailure
import io.rtron.transformer.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration

/**
 * Builder for curves in 3D from the OpenDRIVE data model.
 */
class Curve3DBuilder(
    private val reportLogger: Logger,
    private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _functionBuilder = FunctionBuilder(reportLogger, configuration)
    private val _curve2DBuilder = Curve2DBuilder(reportLogger, configuration)

    // Methods

    /**
     * Builds a curve in 3D from OpenDRIVE's plan view entries and the elevation profile.
     */
    fun buildCurve3D(
        id: RoadspaceIdentifier,
        planViewGeometries: List<RoadPlanViewGeometry>,
        elevationProfiles: List<RoadElevationProfileElevation>
    ): Result<Curve3D, IllegalArgumentException> {

        val planViewCurve2D =
            _curve2DBuilder.buildCurve2DFromPlanViewGeometries(id, planViewGeometries, configuration.offsetXY).handleFailure { return it }
        val heightFunction = buildHeightFunction(id, elevationProfiles)

        return Result.success(Curve3D(planViewCurve2D, heightFunction))
    }

    /**
     * Builds the height function of the OpenDRIVE's elevation profile.
     */
    private fun buildHeightFunction(id: RoadspaceIdentifier, elevationProfiles: List<RoadElevationProfileElevation>):
        UnivariateFunction {
        if (elevationProfiles.isEmpty()) return LinearFunction.X_AXIS

        val elevationEntriesAdjusted = elevationProfiles
            .filterToStrictSortingBy { it.s }
        if (elevationEntriesAdjusted.size < elevationProfiles.size)
            this.reportLogger.info(
                "Removing elevation entries which are not placed in strict order " +
                    "according to s.",
                id.toString()
            )

        return ConcatenatedFunction.ofPolynomialFunctions(
            elevationEntriesAdjusted.map { it.s },
            elevationEntriesAdjusted.map { it.coefficientsWithOffset(offsetA = configuration.offsetZ) },
            prependConstant = true,
            prependConstantValue = 0.0
        )
    }

    /**
     * Builds a curve in 3D from OpenDRIVE's road object entry [roadObject].
     */
    fun buildCurve3D(roadObject: RoadObjectsObject, roadReferenceLine: Curve3D): List<Curve3D> {
        if (!roadObject.repeat.isCurve()) return emptyList()

        val curve2D = _curve2DBuilder
            .buildLateralTranslatedCurve(roadObject.repeat, roadReferenceLine)
            .handleFailure { reportLogger.log(it, roadObject.id); return emptyList() }
        val heightFunction = _functionBuilder
            .buildStackedHeightFunctionFromRepeat(roadObject.repeat, roadReferenceLine)

        val curve3D = Curve3D(curve2D, heightFunction)
        return listOf(curve3D)
    }
}
