/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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
import io.rtron.math.container.ConcatenationContainer
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.Range

/**
 * Represents the sequential concatenation of the [curveMembers].
 *
 * @param curveMembers curves to be concatenated
 */
data class CompositeCurve2D(
    val curveMembers: List<AbstractCurve2D>,
    private val absoluteDomains: List<Range<Double>>,
    private val absoluteStarts: List<Double>,
) : AbstractCurve2D() {
    // Properties and Initializers
    init {
        require(curveMembers.isNotEmpty()) { "Must contain at least one curve member." }
        require(curveMembers.all { it.tolerance == this.tolerance }) { "All curveMembers must have the same tolerance." }
    }

    private val container = ConcatenationContainer(curveMembers, absoluteDomains, absoluteStarts, tolerance)
    override val domain get() = container.domain
    override val tolerance get() = curveMembers.first().tolerance

    init {
        require(length > tolerance) { "Length value must be greater than zero and the tolerance threshold." }
    }

    // Methods
    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Vector2D {
        val localMember = container.fuzzySelectMember(curveRelativePoint.curvePosition, tolerance).getOrElse { throw it }
        val localPoint = CurveRelativeVector1D(localMember.localParameter)

        return localMember.member.calculatePointGlobalCSUnbounded(localPoint)
    }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Rotation2D {
        val localMember = container.fuzzySelectMember(curveRelativePoint.curvePosition, tolerance).getOrElse { throw it }
        val localPoint = CurveRelativeVector1D(localMember.localParameter)

        return localMember.member.calculatePoseGlobalCSUnbounded(localPoint).rotation
    }

    // Conversions
    override fun toString(): String {
        return "CompositeCurve2D(curveMembers=$curveMembers)"
    }

    companion object {
        fun of(
            curveMembers: List<AbstractCurve2D>,
            absoluteDomains: List<Range<Double>>,
            absoluteStarts: List<Double>,
            distanceTolerance: Double,
            angleTolerance: Double,
        ): CompositeCurve2D {
            curveMembers.zipWithNext().forEach {
                val frontCurveMemberEndPose = it.first.calculatePoseGlobalCSUnbounded(CurveRelativeVector1D(it.first.length))
                val backCurveMemberStartPose = it.second.calculatePoseGlobalCSUnbounded(CurveRelativeVector1D.ZERO)

                val distance = frontCurveMemberEndPose.point.distance(backCurveMemberStartPose.point)
                check(distance <= distanceTolerance) {
                    "Cannot construct CompositeCurve2D due to gap between elements from ${frontCurveMemberEndPose.point} " +
                        "to ${backCurveMemberStartPose.point} with an euclidean distance of $distance " +
                        "above tolerance of $distanceTolerance."
                }

                val angleDifference = frontCurveMemberEndPose.rotation.difference(backCurveMemberStartPose.rotation)
                check(angleDifference <= angleTolerance) {
                    "Cannot construct CompositeCurve2D due to an angle difference between elements from " +
                        "${frontCurveMemberEndPose.point} to ${backCurveMemberStartPose.point} with an angle difference " +
                        "of $angleDifference radians above tolerance of $angleTolerance."
                }

                /*if (distance <= angleTolerance) {
                    val suffix = "Transition location: From ${frontCurveMemberEndPose.point} to ${backCurveMemberStartPose.point} with an euclidean distance of $distance."
                    GeometryException.OverlapOrGapInCurve(suffix).left().bind<GeometryException.OverlapOrGapInCurve>()
                }
                if (angleDifference > angleTolerance) {
                    val suffix = "Transition location: From ${frontCurveMemberEndPose.point} to ${backCurveMemberStartPose.point} with an angle difference: $angleDifference radians."
                    GeometryException.KinkInCurve(suffix).left().bind<GeometryException.KinkInCurve>()
                }*/
            }

            return CompositeCurve2D(curveMembers, absoluteDomains, absoluteStarts)
        }
    }
}
