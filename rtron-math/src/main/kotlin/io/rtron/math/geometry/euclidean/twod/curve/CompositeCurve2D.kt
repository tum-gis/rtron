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

import arrow.core.Either
import arrow.core.computations.ResultEffect.bind
import arrow.core.computations.either
import arrow.core.left
import io.rtron.math.container.ConcatenationContainer
import io.rtron.math.geometry.GeometryException
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

    init {
        require(length > tolerance) { "Length value must be greater than zero and the tolerance threshold." }
    }

    // Methods
    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Either<Exception, Vector2D> = either.eager {

        val localMember = container.fuzzySelectMember(curveRelativePoint.curvePosition, tolerance).bind()
        val localPoint = CurveRelativeVector1D(localMember.localParameter)

        localMember.member.calculatePointGlobalCS(localPoint).bind()
    }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Either<Exception, Rotation2D> = either.eager {

        val localMember = container.fuzzySelectMember(curveRelativePoint.curvePosition, tolerance).bind()
        val localPoint = CurveRelativeVector1D(localMember.localParameter)

        localMember.member.calculatePoseGlobalCS(localPoint).map { it.rotation }.bind()
    }

    // Conversions
    override fun toString(): String {
        return "CompositeCurve2D(curveMembers=$curveMembers)"
    }

    companion object {
        fun of(curveMembers: List<AbstractCurve2D>, absoluteDomains: List<Range<Double>>, absoluteStarts: List<Double>, distanceTolerance: Double, angleTolerance: Double): Either<GeometryException, CompositeCurve2D> = either.eager {

            curveMembers.zipWithNext().forEach {
                val frontCurveMemberEndPose = it.first.calculatePoseGlobalCS(CurveRelativeVector1D(it.first.length)).bind()
                val backCurveMemberStartPose = it.second.calculatePoseGlobalCS(CurveRelativeVector1D.ZERO).bind()

                if (frontCurveMemberEndPose.point.fuzzyUnequals(backCurveMemberStartPose.point, distanceTolerance)) {
                    val distance = frontCurveMemberEndPose.point.distance(backCurveMemberStartPose.point)
                    val suffix = "Transition location: From ${frontCurveMemberEndPose.point} to ${backCurveMemberStartPose.point} with an euclidean distance of $distance."
                    GeometryException.OverlapOrGapInCurve(suffix).left().bind<GeometryException.OverlapOrGapInCurve>()
                }

                if (frontCurveMemberEndPose.rotation.fuzzyUnequals(backCurveMemberStartPose.rotation, angleTolerance)) {
                    val angleDifference = frontCurveMemberEndPose.rotation - backCurveMemberStartPose.rotation
                    val suffix = "Transition location: From ${frontCurveMemberEndPose.point} to ${backCurveMemberStartPose.point} with an angle difference: ${angleDifference.toAngleRadians()} radians."
                    GeometryException.KinkInCurve(suffix).left().bind<GeometryException.KinkInCurve>()
                }
            }

            val compositeCurve2D = CompositeCurve2D(curveMembers, absoluteDomains, absoluteStarts)
            compositeCurve2D
        }
    }
}
