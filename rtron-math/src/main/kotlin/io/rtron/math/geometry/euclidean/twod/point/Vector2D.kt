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

package io.rtron.math.geometry.euclidean.twod.point

import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.linear.RealVector
import io.rtron.math.std.fuzzyEquals as doubleFuzzyEquals
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D as CMVector2D
import org.joml.Vector2d as JOMLVector2D

/** Conversion from adapted Vector class from Apache Commons Math. */
fun CMVector2D.toVector2D() = Vector2D(this.x, this.y)

/** Conversion from adapted Vector class from JOML. */
fun JOMLVector2D.toVector2D() = Vector2D(this.x, this.y)

/**
 * Represents a vector in two-dimensional space.
 *
 * @param x x component (abscissa) of the vector
 * @param y y component (ordinate) of the vector
 */
data class Vector2D(
    val x: Double,
    val y: Double,
) : AbstractPoint2D() {
    // Properties and Initializers

    init {
        require(x.isFinite()) { "X value must be finite." }
        require(y.isFinite()) { "Y value must be finite." }
    }

    private val vector2D: CMVector2D by lazy { CMVector2D(x, y) }

    /** L_2 norm of the vector */
    val norm by lazy { vector2D.norm }

    // Secondary Constructors

    constructor(v: Pair<Double, Double>) : this(v.first, v.second)

    // Operators

    operator fun plus(v: Vector2D) = vector2D.add(v.vector2D).toVector2D()

    operator fun minus(v: Vector2D) = vector2D.subtract(v.vector2D).toVector2D()

    operator fun times(m: Double): Vector2D = scalarMultiply(m)

    operator fun div(m: Double): Vector2D = scalarDivide(m)

    operator fun unaryPlus() = Vector2D(x, y)

    operator fun unaryMinus() = Vector2D(-x, -y)

    fun fuzzyEquals(
        o: Vector2D,
        tolerance: Double,
    ) = doubleFuzzyEquals(this.x, o.x, tolerance) &&
        doubleFuzzyEquals(this.y, o.y, tolerance)

    fun fuzzyUnequals(
        o: Vector2D,
        tolerance: Double,
    ) = !fuzzyEquals(o, tolerance)

    // Methods

    /** Returns the scalar product of this with the [factor]. */
    fun scalarMultiply(factor: Double): Vector2D = vector2D.scalarMultiply(factor).toVector2D()

    /** Returns the scalar division of this with the [divisor]. */
    fun scalarDivide(divisor: Double): Vector2D {
        require(divisor != 0.0) { "Divisor must not be zero." }
        return scalarMultiply(1.0 / divisor)
    }

    /** Returns the dot product of this with the [other] [Vector2D]. */
    fun dotProduct(other: Vector2D): Double = vector2D.dotProduct(other.vector2D)

    /** Returns the normalized vector. */
    fun normalized(): Vector2D = vector2D.normalize().toVector2D()

    /** Returns the angle between the [other] vector and this vector. */
    fun angle(other: Vector2D) = Rotation2D.of(other) - Rotation2D.of(this)

    /** Returns the distance between the [other] vector and this vector. */
    fun distance(other: Vector2D): Double = vector2D.distance(other.vector2D)

    // Conversions
    fun toVector3D(z: Double = 0.0) = Vector3D(this.x, this.y, z)

    fun toVector2DCm() = this.vector2D

    fun toVector2DJOML() = JOMLVector2D(this.x, this.y)

    fun toRealVector() = RealVector(doubleArrayOf(this.x, this.y))

    companion object {
        val ZERO = Vector2D(0.0, 0.0)
        val X_AXIS = Vector2D(1.0, 0.0)
        val Y_AXIS = Vector2D(0.0, 1.0)
    }
}
