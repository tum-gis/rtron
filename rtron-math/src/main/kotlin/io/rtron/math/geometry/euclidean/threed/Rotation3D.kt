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

package io.rtron.math.geometry.euclidean.threed

import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.linear.RealMatrix
import io.rtron.math.std.RAD_TO_DEG
import io.rtron.math.std.normalizeAngle
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import org.apache.commons.math3.geometry.euclidean.threed.Rotation as CMRotation

/**
 * Rotation in the three dimensional space given as Tait-Bryan angles.
 * See the wikipedia article on [Tait–Bryan angles](https://en.wikipedia.org/wiki/Euler_angles#Tait%E2%80%93Bryan_angles).
 * Further, see the wikipedia article on [aircraft principal axes](https://en.wikipedia.org/wiki/Aircraft_principal_axes).
 *
 * @param heading around z-axis, 0.0 = direction of x-axis / east (+π/2 = direction of y-axis / north)
 * @param pitch around y’-axis, 0.0 = level (in x’/y’ plane), ( +π/2 = direction of negative z-axis)
 * @param roll around x’’-axis, 0.0 = level (parallel to x’’/y’’ plane)
 */
class Rotation3D(
    heading: Double,
    pitch: Double = 0.0,
    roll: Double = 0.0
) {

    // Properties and Initializers
    init {
        require(heading.isFinite()) { "Heading angle must be finite." }
        require(pitch.isFinite()) { "Pitch angle must be finite." }
        require(roll.isFinite()) { "Roll angle must be finite." }
    }

    val heading = normalizeAngle(heading)
    val pitch = normalizeAngle(pitch)
    val roll = normalizeAngle(roll)

    private val _rotation3D by lazy {
        CMRotation(ROTATION_ORDER, ROTATION_CONVENTION, heading, pitch, roll)
    }

    val headingDegree get() = heading * RAD_TO_DEG
    val pitchDegree get() = pitch * RAD_TO_DEG
    val rollDegree get() = roll * RAD_TO_DEG

    // Methods
    fun getMatrix() = RealMatrix(_rotation3D.matrix)

    // Conversions

    /**
     * Conversion to a rotation in 2D.
     *
     * @param selectAxis axis to be selected for 2D; selecting the z axis will take the heading angle for 2D
     */
    fun toRotation2D(selectAxis: Vector3D = Vector3D.Z_AXIS): Rotation2D =
        when (selectAxis.normalized()) {
            Vector3D.Z_AXIS -> Rotation2D(heading)
            Vector3D.Y_AXIS -> Rotation2D(pitch)
            Vector3D.X_AXIS -> Rotation2D(roll)
            else -> throw IllegalArgumentException("Unknown axis.")
        }

    override fun toString(): String {
        return "Rotation3D(heading='$heading' pitch='$pitch' roll='$roll')"
    }

    companion object {
        private val ROTATION_ORDER = RotationOrder.ZYX
        private val ROTATION_CONVENTION = RotationConvention.VECTOR_OPERATOR

        val ZERO = Rotation3D(0.0, 0.0, 0.0)
    }
}
