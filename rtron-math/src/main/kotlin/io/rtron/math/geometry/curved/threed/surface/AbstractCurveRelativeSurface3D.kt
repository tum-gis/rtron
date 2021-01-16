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

package io.rtron.math.geometry.curved.threed.surface

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.curved.twod.point.CurveRelativeVector2D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.DefinableDomain
import io.rtron.math.range.Tolerable
import io.rtron.math.range.fuzzyContainsResult
import io.rtron.std.handleFailure

/**
 * Abstract class for all geometric surface objects in an curve relative coordinate system in 3D.
 */
abstract class AbstractCurveRelativeSurface3D : DefinableDomain<Double>, Tolerable {

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
        Result<Vector3D, Exception> {

            this.domain.fuzzyContainsResult(curveRelativePoint.curvePosition, tolerance).handleFailure { return it }
            return calculatePointGlobalCSUnbounded(curveRelativePoint, addHeightOffset)
        }

    /**
     * Returns a point in the global cartesian coordinate system that is located on this surface and given by a
     * point in the curve relative coordinate system.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @param addHeightOffset adds an additional height offset to the surface
     * @return point in cartesian coordinates
     */
    abstract fun calculatePointGlobalCSUnbounded(curveRelativePoint: CurveRelativeVector2D, addHeightOffset: Double):
        Result<Vector3D, Exception>
}
