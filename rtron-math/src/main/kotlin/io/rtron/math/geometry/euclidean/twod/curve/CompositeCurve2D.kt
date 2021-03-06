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

package io.rtron.math.geometry.euclidean.twod.curve

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import io.rtron.math.container.ConcatenationContainer
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.Range
import io.rtron.std.handleFailure

/**
 * Represents the sequential concatenation of the [curveMembers].
 *
 * @param curveMembers curves to be concatenated
 */
data class CompositeCurve2D(
    val curveMembers: List<AbstractCurve2D>,
    private val absoluteDomains: List<Range<Double>>,
    private val absoluteStarts: List<Double>
) : AbstractCurve2D() {

    // Properties and Initializers
    init {
        require(curveMembers.isNotEmpty()) { "Must contain at least one curve member." }
        require(curveMembers.all { it.tolerance == this.tolerance }) { "All curveMembers must have the same tolerance." }
    }

    private val container = ConcatenationContainer(curveMembers, absoluteDomains, absoluteStarts, tolerance)
    override val domain get() = container.domain
    override val tolerance get() = curveMembers.first().tolerance

    // Methods
    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Result<Vector2D, Exception> {

            val localMember = container
                .fuzzySelectMember(curveRelativePoint.curvePosition, tolerance)
                .handleFailure { return it }
            val localPoint = CurveRelativeVector1D(localMember.localParameter)

            return localMember.member.calculatePointGlobalCS(localPoint)
        }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Result<Rotation2D, Exception> {

            val localMember = container
                .fuzzySelectMember(curveRelativePoint.curvePosition, tolerance)
                .handleFailure { return it }
            val localPoint = CurveRelativeVector1D(localMember.localParameter)

            return localMember.member.calculatePoseGlobalCS(localPoint).map { it.rotation }
        }

    // Conversions
    override fun toString(): String {
        return "CompositeCurve2D(curveMembers=$curveMembers)"
    }

    /*companion object {
        fun of(vararg curveMembers: AbstractCurve2D) = CompositeCurve2D(curveMembers.toList())
    }*/
}
