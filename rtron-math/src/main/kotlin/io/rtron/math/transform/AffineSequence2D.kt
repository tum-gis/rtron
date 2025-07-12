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

package io.rtron.math.transform

/**
 * Represents a sequence of affine transformation matrices in 2D.
 *
 * @param affineList list of consecutively applied [Affine2D] transformation matrices
 */
data class AffineSequence2D(
    val affineList: List<Affine2D>,
) {
    // Properties and Initializers

    /** number of [Affine2D] matrices contained in list */
    val size get() = affineList.size

    // Secondary Constructors
    constructor(affine: Affine2D) : this(listOf(affine))

    // Methods
    fun isEmpty() = affineList.isEmpty()

    fun isNotEmpty() = affineList.isNotEmpty()

    /**
     * Solves all contained [Affine2D] by multiplication and returns the resulting transformation matrix.
     *
     * @return transformation matrix that applies the sequence of [Affine2D] matrices consecutively
     */
    fun solve() = Affine2D.of(affineList)

    companion object {
        val EMPTY = AffineSequence2D(emptyList())

        /**
         * Creates an [AffineSequence2D] by provided [affines] list.
         */
        fun of(vararg affines: Affine2D) = AffineSequence2D(affines.toList())
    }
}
