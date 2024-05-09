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

package io.rtron.math.linear

import org.apache.commons.math3.linear.ArrayRealVector as CMArrayRealVector
import org.apache.commons.math3.linear.RealVector as CMRealVector

/** Conversion from adapted Real Vector class from Apache Commons Math. */
fun CMRealVector.toRealVector() = RealVector(this.toArray()!!)

/**
 * Real vector of double values, whereby the number of provided values defines the [dimension] of the vector.
 *
 * @param entries entry values of the real vector
 */
class RealVector(
    private val entries: DoubleArray,
) {
    // Properties and Initializers

    /** adapted Apache Commons Math vector */
    private val vector by lazy { CMArrayRealVector(entries) }

    /** dimension of the vector */
    val dimension by lazy { vector.dimension }

    /** L_2 norm of the vector */
    val norm by lazy { vector.norm }

    // Secondary Constructors
    constructor(entries: List<Double>) : this(entries.toDoubleArray())

    // Operators

    operator fun get(index: Int): Double = vector.getEntry(index)

    operator fun plus(v: RealVector) = vector.add(v.vector).toRealVector()

    operator fun minus(v: RealVector) = vector.subtract(v.vector).toRealVector()

    // Methods

    /** Returns true, if any entry of this vector is NaN. */
    fun containsNaN(): Boolean = vector.isNaN

    /** Returns true, if any entry of this vector is Infinite. */
    fun containsInfinite(): Boolean = vector.isInfinite

    /** Returns the dot product of this with the [other] [RealVector]. */
    fun dotProduct(other: RealVector): Double = vector.dotProduct(other.toVectorCM())

    /** Multiplies each entry with [factor] and returns the [RealVector]. */
    fun mapMultiply(factor: Double) = vector.mapMultiply(factor).toRealVector()

    /** Returns the element-by-element product of this with the [other] [RealVector]. */
    fun ebeMultiply(other: RealVector) = vector.ebeMultiply(other.toVectorCM()).toRealVector()

    /** Returns the element-by-element division of this with the [other] [RealVector]. */
    fun ebeDivide(other: RealVector) = vector.ebeDivide(other.toVectorCM()).toRealVector()

    /** Returns the normalized vector. */
    fun normalize() = mapMultiply(1.0 / norm)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RealVector

        if (!entries.contentEquals(other.entries)) return false

        return true
    }

    override fun hashCode(): Int {
        return entries.contentHashCode()
    }

    // Conversions

    fun toDoubleArray(): DoubleArray = vector.toArray()!!

    fun toDoubleList(): List<Double> = toDoubleArray().toList()

    /** Conversion to adapted Real Vector class from Apache Commons Math. */
    fun toVectorCM(): CMArrayRealVector = vector

    companion object {
        fun of(vararg entries: Double) = RealVector(entries)
    }
}
