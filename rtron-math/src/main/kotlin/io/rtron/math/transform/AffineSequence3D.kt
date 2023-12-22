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

package io.rtron.math.transform

/**
 * Represents a sequence of affine transformation matrices in 3D.
 *
 * @param affineList list of consecutively applied [AffineSequence3D] transformation matrices
 */
data class AffineSequence3D(
    val affineList: List<Affine3D>
) {
    // Properties and Initializers

    /** number of [Affine3D] matrices contained in list */
    val size get() = affineList.size

    // Methods
    fun isEmpty() = affineList.isEmpty()
    fun isNotEmpty() = affineList.isNotEmpty()

    /**
     * Solves all contained [Affine3D] by multiplication and returns the resulting transformation matrix.
     *
     * @return transformation matrix that applies the sequence of [Affine3D] matrices consecutively
     */
    fun solve() = if (isNotEmpty()) Affine3D.of(affineList) else Affine3D.UNIT

    companion object {
        val EMPTY = AffineSequence3D(emptyList())

        /**
         * Creates an [AffineSequence2D] by provided [affines] list.
         */
        fun of(vararg affines: Affine3D) = AffineSequence3D(affines.toList())
    }
}
