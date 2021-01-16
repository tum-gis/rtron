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

package io.rtron.math.std

import kotlin.math.PI

/**
 * Reshapes a [DoubleArray] towards a matrix with a defined [columnDimension].
 *
 * @param columnDimension number of columns or size of returned [DoubleArray], respectively
 * @return matrix represented as an [Array] (columns) of [DoubleArray] (rows)
 */
fun DoubleArray.reshapeByColumnDimension(columnDimension: Int): Array<DoubleArray> {
    return this.toList().chunked(columnDimension).map { it.toDoubleArray() }.toTypedArray()
}

/**
 * Reshapes a [DoubleArray] towards a matrix with a defined [rowDimension].
 *
 * @param rowDimension number of rows or size of returned [Array], respectively
 * @return matrix represented as an [Array] (columns) of [DoubleArray] (rows)
 */
fun DoubleArray.reshapeByRowDimension(rowDimension: Int): Array<DoubleArray> {
    require(this.size.rem(rowDimension) == 0) { "Not fitting dimensions: Trying to reshape a DoubleArray of size ${this.size}) to rowDimension of $rowDimension." }

    PI
    return this.reshapeByColumnDimension(this.size / rowDimension)
}
