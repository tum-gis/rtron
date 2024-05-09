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

import io.rtron.math.analysis.Fresnel
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.PI
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Represents a spiral of the form:
 * x(l) = A * sqrt(pi) * int_0^l cos( (pi*t^2) / 2 ) dt
 * y(l) = A * sqrt(pi) * int_0^l sin( (pi*t^2) / 2 ) dt
 * Asymptotic points at (A*sqrt(pi)/2, A*sqrt(pi)/2) and (-A*sqrt(pi)/2, -A*sqrt(pi)/2).
 *
 * @param cDot first derivative of curvature
 */
data class Spiral2D(
    val cDot: Double,
) {
    // Properties and Initializers
    val constantA: Double = 1.0 / sqrt(abs(cDot))
    val constantAuxiliaryA: Double = constantA * sqrt(PI)

    // Methods

    /**
     * Returns a point in cartesian coordinates at the spiral position [l].
     * The point is calculated by using a Fresnel integral implementation.
     *
     * @param l spiral position
     * @return point in cartesian coordinate
     */
    fun calculatePoint(l: Double): Vector2D {
        val fresnelPoint = Fresnel.calculatePoint(l / constantAuxiliaryA)
        val spiralPoint = Vector2D(fresnelPoint).scalarMultiply(constantAuxiliaryA)

        return when {
            0.0 <= cDot -> spiralPoint
            else -> Vector2D(spiralPoint.x, -spiralPoint.y)
        }
    }

    /** Returns the rotation of the tangent at the spiral position [l]. */
    fun calculateRotation(l: Double) = Rotation2D(l * l * cDot * 0.5)

    /** Returns the pose at the spiral position [l]. */
    fun calculatePose(l: Double) = Pose2D(calculatePoint(l), calculateRotation(l))

    /** Returns the curvature at the spiral position [l]. */
    fun calculateCurvature(l: Double) = sqrt(PI) * l / constantA
}
