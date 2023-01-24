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

package io.rtron.math.geometry.curved.threed.surface

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.rtron.math.geometry.GeometryException
import io.rtron.math.geometry.curved.twod.point.CurveRelativeVector2D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.DefinableDomain
import io.rtron.math.range.Tolerable
import io.rtron.math.range.fuzzyContains
import io.rtron.math.range.length

/**
 * Abstract class for all geometric surface objects in an curve relative coordinate system in 3D.
 */
abstract class AbstractCurveRelativeSurface3D : DefinableDomain<Double>, Tolerable {

    // Properties and Initializers

    /** length of the surface along the curve */
    val length: Double get() = domain.length

    // Methods

    /**
     * Returns a point in the global cartesian coordinate system that is located on this surface and given by a
     * point in the curve relative coordinate system.
     * An error is returned, if the requested point is not within this curve's domain.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @param addHeightOffset adds an additional height offset to the surface
     * @return point in cartesian coordinates
     */
    fun calculatePointGlobalCS(curveRelativePoint: CurveRelativeVector2D, addHeightOffset: Double = 0.0):
        Either<GeometryException.ValueNotContainedInDomain, Vector3D> {
        if (!domain.fuzzyContains(curveRelativePoint.curvePosition, tolerance))
            return GeometryException.ValueNotContainedInDomain(curveRelativePoint.curvePosition).left()

        return calculatePointGlobalCSUnbounded(curveRelativePoint, addHeightOffset).right()
    }

    /**
     * Returns a point in the global cartesian coordinate system that is located on this surface and given by a
     * point in the curve relative coordinate system.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @param addHeightOffset adds an additional height offset to the surface
     * @return point in cartesian coordinates
     */
    abstract fun calculatePointGlobalCSUnbounded(curveRelativePoint: CurveRelativeVector2D, addHeightOffset: Double): Vector3D
}
