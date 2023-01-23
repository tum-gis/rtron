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

package io.rtron.math.geometry.euclidean.twod.curve

import arrow.core.getOrElse
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.Range
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.range.shiftLowerEndpointTo

/**
 * Cuts out a section from the [completeCurve].
 * The resulting domain of the [SectionedCurve2D] starts at 0.0 and ends at the length of the section.
 *
 * @param completeCurve complete curve segment that is to be cut
 * @param section the range that is cut out from the [completeCurve]'s domain
 */
class SectionedCurve2D(
    private val completeCurve: AbstractCurve2D,
    section: Range<Double>
) : AbstractCurve2D() {

    // Properties and Initializers
    init {
        require(completeCurve.domain.fuzzyEncloses(section, tolerance)) { "The complete function must be defined everywhere where the section is also defined." }
    }

    override val domain: Range<Double> = section.shiftLowerEndpointTo(0.0)
    override val tolerance: Double get() = completeCurve.tolerance
    private val sectionStart = CurveRelativeVector1D(section.lowerEndpointResult().getOrElse { throw it })

    // Methods
    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Vector2D {

        val pointOnCompleteCurve = sectionStart + curveRelativePoint
        return completeCurve.calculatePointGlobalCSUnbounded(pointOnCompleteCurve)
    }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Rotation2D {

        val pointOnCompleteCurve = sectionStart + curveRelativePoint
        return completeCurve.calculateRotationGlobalCSUnbounded(pointOnCompleteCurve)
    }
}
