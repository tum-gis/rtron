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

package io.rtron.math.geometry.euclidean.threed.point

import arrow.core.Either
import arrow.core.nonEmptyListOf
import io.rtron.math.geometry.euclidean.threed.Geometry3DVisitor
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.linear.RealVector
import io.rtron.math.transform.AffineSequence3D
import io.rtron.std.hasSameSizeAs
import kotlin.math.atan2
import io.rtron.math.std.fuzzyEquals as doubleFuzzyEquals
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D as CMVector3D
import org.joml.Vector3d as JOMLVector3D
import org.joml.Vector4d as JOMLVector4D

/** Conversion from adapted Vector class from Apache Commons Math. */
fun CMVector3D.toVector3D() = Vector3D(this.x, this.y, this.z)

/** Conversion from adapted Vector class from JOML. */
fun JOMLVector3D.toVector3D() = Vector3D(this.x, this.y, this.z)

/**
 * Represents a vector in three-dimensional space, whereby its values must be finite.
 *
 * @param x x component (abscissa) of the vector
 * @param y y component (ordinate) of the vector
 * @param z z component (applicate) of the vector
 */
data class Vector3D(
    val x: Double,
    val y: Double,
    val z: Double,
    override val affineSequence: AffineSequence3D = AffineSequence3D.EMPTY
) : AbstractPoint3D() {

    // Properties and Initializers
    init {
        require(x.isFinite()) { "X value must be finite." }
        require(y.isFinite()) { "Y value must be finite." }
        require(z.isFinite()) { "Z value must be finite." }
    }

    private val vector3D: CMVector3D by lazy { CMVector3D(x, y, z) }

    /** L_2 norm of the vector */
    val norm by lazy { vector3D.norm }

    /** square norm of the vector */
    val normSq by lazy { vector3D.normSq }

    // Operators
    operator fun plus(v: Vector3D) = vector3D.add(v.vector3D).toVector3D()
    operator fun minus(v: Vector3D) = vector3D.subtract(v.vector3D).toVector3D()
    operator fun times(m: Double) = scalarMultiply(m)
    operator fun div(m: Double) = scalarDivide(m)
    operator fun unaryPlus() = Vector3D(x, y, z)
    operator fun unaryMinus() = Vector3D(-x, -y, -z)

    fun fuzzyEquals(o: Vector3D, tolerance: Double) = doubleFuzzyEquals(this.x, o.x, tolerance) &&
        doubleFuzzyEquals(this.y, o.y, tolerance) &&
        doubleFuzzyEquals(this.z, o.z, tolerance)
    fun fuzzyUnequals(o: Vector3D, tolerance: Double) = !fuzzyEquals(o, tolerance)

    // Methods

    /** Returns the scalar product of this with the [factor]. */
    fun scalarMultiply(factor: Double): Vector3D = vector3D.scalarMultiply(factor).toVector3D()

    /** Returns the scalar division of this with the [divisor]. */
    fun scalarDivide(divisor: Double): Vector3D {
        require(divisor != 0.0) { "Divisor must not be zero." }
        return scalarMultiply(1 / divisor)
    }

    /** Returns the dot product of this with the [other] [Vector2D]. */
    fun dotProduct(other: Vector3D): Double = vector3D.dotProduct(other.vector3D)

    /** Returns the cross product of this with the [other] [Vector2D]. */
    fun crossProduct(other: Vector3D): Vector3D = vector3D.crossProduct(other.vector3D).toVector3D()

    /** Returns the normalized vector. */
    fun normalized(): Vector3D {
        require(norm != 0.0) { "Vector normalization requires a vector with non-zero length." }
        return vector3D.normalize().toVector3D()
    }

    /** Returns the angle between the [other] vector and this vector. */
    fun angle(other: Vector3D): Double = atan2(this.crossProduct(other).norm, this.dotProduct(other))

    /** Returns the distance between the [other] vector and this vector. */
    fun distance(other: Vector3D): Double = vector3D.distance(other.toVector3DCm())

    /** Returns true, if each component is zero. */
    fun isZero(): Boolean = x == 0.0 && y == 0.0 && z == 0.0

    override fun calculatePointLocalCS(): Vector3D = this

    override fun accept(visitor: Geometry3DVisitor) = visitor.visit(this)

    // Conversions
    fun toDoubleArray() = doubleArrayOf(x, y, z)
    fun toDoubleList() = nonEmptyListOf(x, y, z)
    fun toRealVector() = RealVector(doubleArrayOf(x, y, z))
    fun toVector3DCm() = this.vector3D
    fun toVector3DJOML() = JOMLVector3D(this.x, this.y, this.z)
    fun toVector4DJOML(w: Double = 0.0) = JOMLVector4D(this.x, this.y, this.z, w)

    /**
     * Conversion to a vector in 2D.
     *
     * @param dropAxis axis to be dropped for 2D; selecting the z axis will take the vector (x, y)
     */
    fun toVector2D(dropAxis: Vector3D = Z_AXIS): Vector2D =
        when (dropAxis.normalized()) {
            X_AXIS -> Vector2D(y, z)
            Y_AXIS -> Vector2D(x, z)
            Z_AXIS -> Vector2D(x, y)
            else -> throw IllegalArgumentException("Unknown axis selected to drop.")
        }

    companion object {
        val ZERO = Vector3D(0.0, 0.0, 0.0)
        val X_AXIS = Vector3D(1.0, 0.0, 0.0)
        val Y_AXIS = Vector3D(0.0, 1.0, 0.0)
        val Z_AXIS = Vector3D(0.0, 0.0, 1.0)

        /**
         * Creates a [Vector3D], if each component is finite. Otherwise it will return a Result.Error.
         *
         */
        fun of(x: Double, y: Double, z: Double): Either<IllegalArgumentException, Vector3D> =
            if (!x.isFinite() || !y.isFinite() || !z.isFinite())
                Either.Left(IllegalArgumentException("Values for x, y, z must be finite."))
            else Either.Right(Vector3D(x, y, z))
    }
}

/**
 * Returns true, if each vector of this list is fuzzily equal to the [other] vector's elements (on the same index).
 *
 * @param other other list of vectors to be compared
 * @param tolerance allowed tolerance for fuzzy equal evaluation
 */
fun List<Vector3D>.fuzzyEquals(other: List<Vector3D>, tolerance: Double): Boolean {
    require(this.hasSameSizeAs(other)) { "Lists must have the same size." }
    return this.zip(other).all { it.first.fuzzyEquals(it.second, tolerance) }
}
