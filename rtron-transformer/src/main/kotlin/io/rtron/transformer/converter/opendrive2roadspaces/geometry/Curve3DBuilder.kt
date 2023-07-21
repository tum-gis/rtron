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

import arrow.core.NonEmptyList
import arrow.core.Option
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.ConcatenatedFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.model.opendrive.objects.RoadObjectsObject
import io.rtron.model.opendrive.road.elevation.RoadElevationProfileElevation
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometry
import io.rtron.std.isStrictlySortedBy
import io.rtron.transformer.converter.opendrive2roadspaces.analysis.FunctionBuilder

/**
 * Builder for curves in 3D from the OpenDRIVE data model.
 */
object Curve3DBuilder {

    // Methods

    /**
     * Builds a curve in 3D from OpenDRIVE's plan view entries and the elevation profile.
     */
    fun buildCurve3D(
        planViewGeometries: NonEmptyList<RoadPlanViewGeometry>,
        elevationProfiles: Option<NonEmptyList<RoadElevationProfileElevation>>,
        numberTolerance: Double,
        distanceTolerance: Double,
        angleTolerance: Double
    ): Curve3D {
        val planViewCurve2D = Curve2DBuilder.buildCurve2DFromPlanViewGeometries(
            planViewGeometries,
            numberTolerance,
            distanceTolerance,
            angleTolerance
        )
        val heightFunction = elevationProfiles.fold({ LinearFunction.X_AXIS }, { buildHeightFunction(it) })

        return Curve3D(planViewCurve2D, heightFunction)
    }

    /**
     * Builds the height function of the OpenDRIVE's elevation profile.
     */
    private fun buildHeightFunction(elevationProfiles: NonEmptyList<RoadElevationProfileElevation>): UnivariateFunction {
        require(elevationProfiles.isStrictlySortedBy { it.s }) { "Elevation entries must be sorted in strict order according to s." }

        return ConcatenatedFunction.ofPolynomialFunctions(
            elevationProfiles.map { it.s },
            elevationProfiles.map { it.coefficients },
            prependConstant = true,
            prependConstantValue = 0.0
        )
    }

    /**
     * Builds a curve in 3D from OpenDRIVE's road object entry [roadObject].
     */
    fun buildCurve3D(roadObject: RoadObjectsObject, roadReferenceLine: Curve3D, numberTolerance: Double): List<Curve3D> {
        if (roadObject.repeat.isEmpty()) return emptyList() // TODO fix repeat list handling
        if (!roadObject.repeat.first().containsCurve()) return emptyList() // TODO fix repeat list handling

        val curve2D = Curve2DBuilder.buildLateralTranslatedCurve(roadObject.repeat.first(), roadReferenceLine, numberTolerance) // TODO fix repeat list handling
        val heightFunction = FunctionBuilder
            .buildStackedHeightFunctionFromRepeat(roadObject.repeat.first(), roadReferenceLine) // TODO fix repeat list handling

        val curve3D = Curve3D(curve2D, heightFunction)
        return listOf(curve3D)
    }
}
