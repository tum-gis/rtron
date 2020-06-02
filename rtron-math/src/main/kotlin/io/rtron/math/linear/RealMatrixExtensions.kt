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

/**
 * Appends a [newColumn] to a [RealMatrix].
 */
fun RealMatrix.appendColumn(newColumn: DoubleArray): RealMatrix {
    require(this.rowDimension == newColumn.size) {"Wrong dimensions: Matrix has ${this.rowDimension} rows and" +
            " ${this.columnDimension} and double array has a size of ${newColumn.size}"}

    val newData = this.entries.foldIndexed(emptyList<DoubleArray>())
    { index, acc, currentRow -> acc + (currentRow + newColumn[index])}

    return RealMatrix(newData.toTypedArray())
}

/**
 * Appends a [newRow] to a [RealMatrix].
 */
fun RealMatrix.appendRow(newRow: DoubleArray): RealMatrix {
    require(this.columnDimension == newRow.size)
    { "Wrong dimensions: Matrix has ${this.rowDimension} rows and ${this.columnDimension} and " +
            "double array has a size of ${newRow.size}" }

    val newData = this.entries + newRow
    return RealMatrix(newData)
}
