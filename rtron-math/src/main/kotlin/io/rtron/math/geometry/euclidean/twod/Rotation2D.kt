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

package io.rtron.math.geometry.euclidean.twod

import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.std.DEG_TO_RAD
import io.rtron.math.std.RAD_TO_DEG
import io.rtron.math.std.normalizeAngle
import kotlin.math.atan2

/**
 * Represents a rotation anticlockwise in 2D.
 *
 * @param angle angle in radians, whereas the angle 0.0 corresponds to the x-axis
 */
class Rotation2D(
    angle: Double
) {

    // Properties and Initializers

    init {
        require(angle.isFinite()) { "Rotation angle must be finite." }
    }

    /** angle in radians normalized to the interval of [0, 2PI) */
    val angle: Double = normalizeAngle(angle)

    // Operators

    operator fun plus(v: Rotation2D) = Rotation2D(angle + v.angle)
    operator fun minus(v: Rotation2D) = Rotation2D(angle - v.angle)
    operator fun times(m: Rotation2D) = Rotation2D(angle * m.angle)
    operator fun div(m: Rotation2D) = Rotation2D(angle * m.angle)

    operator fun unaryPlus() = Rotation2D(this.angle)
    operator fun unaryMinus() = Rotation2D(-this.angle)

    // Methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rotation2D

        if (angle != other.angle) return false

        return true
    }

    override fun hashCode(): Int {
        return angle.hashCode()
    }

    // Conversions

    fun toAngleRadians() = this.angle
    fun toAngleDegree() = this.angle * RAD_TO_DEG

    fun toRotation3D(pitch: Double = 0.0, roll: Double = 0.0) = Rotation3D(angle, pitch, roll)

    companion object {
        val ZERO = Rotation2D(0.0)

        /**
         * Create a [Rotation2D] from an angle provided in degrees.
         */
        fun of(angleDegree: Double) = Rotation2D(angleDegree * DEG_TO_RAD)

        /**
         * Creates a [Rotation2D] which is defined from the x axis to the given [direction] vector.
         */
        fun of(direction: Vector2D) = Rotation2D(atan2(direction.y, direction.x))
    }
}
