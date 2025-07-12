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

package io.rtron.math.geometry.euclidean.threed.curve

import arrow.core.NonEmptyList
import arrow.core.getOrElse
import io.rtron.math.container.ConcatenationContainer
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.Range

data class CompositeCurve3D(
    val curveMembers: NonEmptyList<AbstractCurve3D>,
    private val absoluteDomains: NonEmptyList<Range<Double>>,
    private val absoluteStarts: NonEmptyList<Double>,
) : AbstractCurve3D() {
    // Properties and Initializers
    init {
        require(curveMembers.all { it.tolerance == this.tolerance }) { "All curveMembers must have the same tolerance." }
        require(length > tolerance) { "Length must be greater than zero as well as the tolerance threshold." }
    }

    private val container = ConcatenationContainer(curveMembers, absoluteDomains, absoluteStarts, tolerance)
    override val domain get() = container.domain
    override val tolerance get() = curveMembers.first().tolerance

    // Methods

    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Vector3D {
        val localMember =
            container
                .fuzzySelectMember(curveRelativePoint.curvePosition, tolerance)
                .getOrElse { throw it }
        val localPoint = CurveRelativeVector1D(localMember.localParameter)

        return localMember.member.calculatePointGlobalCSUnbounded(localPoint)
    }
}
