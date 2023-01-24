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

import arrow.core.getOrElse
import io.rtron.math.geometry.curved.twod.point.CurveRelativeVector2D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.Range
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.range.shiftLowerEndpointTo

/**
 * Cuts out a section from the [completeCurveRelativeSurface].
 * The resulting domain of the [SectionedCurveRelativeParametricSurface3D] starts at 0.0 and ends at the length of
 * the section.
 *
 * @param completeCurveRelativeSurface complete curve relative surface that is to be cut
 * @param section the range that is cut out from the [completeCurveRelativeSurface]'s domain
 */
class SectionedCurveRelativeParametricSurface3D(
    private val completeCurveRelativeSurface: AbstractCurveRelativeSurface3D,
    section: Range<Double>
) : AbstractCurveRelativeSurface3D() {

    // Properties and Initializers
    init {
        require(completeCurveRelativeSurface.domain.fuzzyEncloses(section, tolerance)) { "The complete surface must be defined everywhere where the section is also defined." }
    }

    override val domain: Range<Double> = section.shiftLowerEndpointTo(0.0)
    override val tolerance: Double get() = completeCurveRelativeSurface.tolerance
    private val sectionStart = section.lowerEndpointResult().getOrElse { throw it }

    init {
        require(length > tolerance) { "Length must be greater than zero as well as the tolerance threshold." }
    }

    // Methods
    override fun calculatePointGlobalCSUnbounded(curveRelativePoint: CurveRelativeVector2D, addHeightOffset: Double): Vector3D {

        val pointOnCompleteSurface = CurveRelativeVector2D(
            sectionStart + curveRelativePoint.curvePosition,
            curveRelativePoint.lateralOffset
        )
        return completeCurveRelativeSurface.calculatePointGlobalCSUnbounded(pointOnCompleteSurface, addHeightOffset)
    }
}
