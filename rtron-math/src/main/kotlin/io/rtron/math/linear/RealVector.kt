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
    private val entries: DoubleArray
) {

    // Properties and Initializers

    /** adapted Apache Commons Math vector */
    private val _vector by lazy { CMArrayRealVector(entries) }

    /** dimension of the vector */
    val dimension by lazy { _vector.dimension }

    /** L_2 norm of the vector */
    val norm by lazy { _vector.norm }

    // Secondary Constructors
    constructor(entries: List<Double>) : this(entries.toDoubleArray())

    // Operators

    operator fun get(index: Int): Double = _vector.getEntry(index)
    operator fun plus(v: RealVector) = _vector.add(v._vector).toRealVector()
    operator fun minus(v: RealVector) = _vector.subtract(v._vector).toRealVector()

    // Methods

    /** Returns true, if any entry of this vector is NaN. */
    fun containsNaN(): Boolean = _vector.isNaN

    /** Returns true, if any entry of this vector is Infinite. */
    fun containsInfinite(): Boolean = _vector.isInfinite

    /** Returns the dot product of this with the [other] [RealVector]. */
    fun dotProduct(other: RealVector): Double = _vector.dotProduct(other.toVectorCM())

    /** Multiplies each entry with [factor] and returns the [RealVector]. */
    fun mapMultiply(factor: Double) = _vector.mapMultiply(factor).toRealVector()

    /** Returns the element-by-element product of this with the [other] [RealVector]. */
    fun ebeMultiply(other: RealVector) = _vector.ebeMultiply(other.toVectorCM()).toRealVector()

    /** Returns the element-by-element division of this with the [other] [RealVector]. */
    fun ebeDivide(other: RealVector) = _vector.ebeDivide(other.toVectorCM()).toRealVector()

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

    fun toDoubleArray(): DoubleArray = _vector.toArray()!!
    fun toDoubleList(): List<Double> = toDoubleArray().toList()

    /** Conversion to adapted Real Vector class from Apache Commons Math. */
    fun toVectorCM(): CMArrayRealVector = _vector

    companion object {
        fun of(vararg entries: Double) = RealVector(entries)
    }
}
