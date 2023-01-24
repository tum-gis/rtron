/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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

import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.std.reshapeByColumnDimension
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix as CMRealMatrix

/**
 * Real matrix of double values.
 *
 * @param rows the rows of the matrix represented as [DoubleArray]
 */
class RealMatrix(
    rows: Array<DoubleArray>
) {

    // Properties and Initializers

    init {
        require(rows.isNotEmpty()) { "Cannot create matrix without any rows." }
        require(rows.all { it.isNotEmpty() }) { "Cannot create matrix with empty columns." }
    }

    /** adapted matrix class of Commons Math */
    private val matrix: Array2DRowRealMatrix = Array2DRowRealMatrix(rows)

    /** entry values of matrix whereby each [DoubleArray] represents a row */
    val entries: Array<DoubleArray> by lazy { matrix.data }

    /** entry values of the matrix flattened to one [DoubleArray] */
    val entriesFlattened: DoubleArray get() = entries.flatMap { it.toList() }.toDoubleArray()

    /** number of rows */
    val rowDimension = matrix.rowDimension

    /** number of columns */
    val columnDimension = matrix.columnDimension

    /** dimension of matrix as (rowDimension, columnDimension) */
    val dimension get() = Pair(rowDimension, columnDimension)

    // Secondary Constructors

    constructor(matrix: CMRealMatrix) : this(matrix.data)
    constructor(matrix: DoubleArray, columnDimension: Int) : this(matrix.reshapeByColumnDimension(columnDimension))
    constructor(rowVectors: List<RealVector>) : this(rowVectors.map { it.toDoubleArray() }.toTypedArray())

    // Operators

    operator fun get(rowIndex: Int) = getRow(rowIndex)

    // Methods

    /** Returns true, if any value of the vector is NaN. */
    fun containsNaN() = toDoubleArray().any { it.isNaN() }

    /** Returns the row of [rowIndex] as double array. */
    fun getRow(rowIndex: Int): DoubleArray = matrix.getRow(rowIndex)!!

    /** Returns the row of [rowIndex] as a real matrix. */
    fun getRowMatrix(rowIndex: Int): RealMatrix = RealMatrix(matrix.getRowMatrix(rowIndex))

    /** Returns the row of [rowIndex] as real vector. */
    fun getRowVector(rowIndex: Int): RealVector = matrix.getRowVector(rowIndex).toRealVector()

    /** Returns the column of [columnIndex] as double array. */
    fun getColumn(columnIndex: Int): DoubleArray = matrix.getColumn(columnIndex)!!

    /** Returns the column of [columnIndex] as a real matrix. */
    fun getColumnMatrix(columnIndex: Int): RealMatrix = RealMatrix(matrix.getColumnMatrix(columnIndex))

    /** Returns the column of [columnIndex] as real vector. */
    fun getColumnVector(columnIndex: Int): RealVector = matrix.getColumnVector(columnIndex).toRealVector()

    /**
     * Returns a submatrix of the complete matrix by only selecting the [selectedRows] and [selectedColumns].
     *
     * @param selectedRows selected row indices
     * @param selectedColumns selected column indices
     * @return submatrix
     */
    fun getSubMatrix(selectedRows: IntArray, selectedColumns: IntArray) =
        RealMatrix(matrix.getSubMatrix(selectedRows, selectedColumns))

    /**
     * Returns a submatrix of the complete matrix by only selecting the [selectedRows] and [selectedColumns].
     *
     * @param selectedRows selected row indices
     * @param selectedColumns selected column indices
     * @return submatrix
     */
    fun getSubMatrix(selectedRows: IntRange, selectedColumns: IntRange) =
        getSubMatrix(selectedRows.toList().toIntArray(), selectedColumns.toList().toIntArray())

    /** Returns this matrix multiplied with [other] (return = this x [other]). */
    fun multiply(other: RealMatrix) = RealMatrix(matrix.multiply(other.toRealMatrixCM()))

    /** Returns the [other] matrix multiplied with this (return = [other] x this). */
    fun preMultiply(other: RealMatrix) = RealMatrix(matrix.preMultiply(other.toRealMatrixCM()))

    /** Returns this matrix multiplied with a [vector] (return = this x [vector]). */
    fun multiply(vector: RealVector): RealVector = this.transpose().preMultiply(vector)

    /** Returns a [vector] multiplied with this matrix (return = [vector] x this). */
    fun preMultiply(vector: RealVector) = matrix.preMultiply(vector.toVectorCM()).toRealVector()

    /** Returns this matrix multiplied with a scalar [factor]. */
    fun scalarMultiply(factor: Double) = RealMatrix(matrix.scalarMultiply(factor))

    /** Returns this matrix multiplied with a scalar [summand]. */
    fun scalarAdd(summand: Double) = RealMatrix(matrix.scalarAdd(summand))

    /** Returns the transposed matrix of this matrix. */
    fun transpose() = RealMatrix(matrix.transpose())

    /** Returns the inverse matrix of this matrix. */
    fun inverse() = RealMatrix(MatrixUtils.inverse(matrix))

    fun normalize(rowIndex: Int, columnIndex: Int): RealMatrix {
        require(this[rowIndex][columnIndex] != 0.0) { "Normalizing element must not be zero." }
        return scalarMultiply(1.0 / this[rowIndex][columnIndex])
    }

    override fun hashCode(): Int {
        return matrix.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RealMatrix

        if (dimension != other.dimension) return false
        if (!entries.contentEquals(other.entries)) return false

        return true
    }

    // Conversions
    fun toDoubleArray(): DoubleArray = matrix.data.flatMap { it.asIterable() }.toDoubleArray()
    fun toDoubleList(): List<Double> = matrix.data.flatMap { it.asIterable() }.toList()

    /** Conversion to adapted Real Matrix class from Apache Commons Math. */
    fun toRealMatrixCM(): CMRealMatrix = matrix

    override fun toString(): String {
        return "Matrix(_matrix=$matrix)"
    }

    companion object {

        /**
         * Creates a [RealMatrix] from a list of column [RealVector].
         *
         * @param columnVectors list of column vectors that must have the same dimension
         */
        @JvmName("ofListRealVector")
        fun of(columnVectors: List<RealVector>): RealMatrix {
            val rowDimension = columnVectors.first().dimension
            require(columnVectors.isNotEmpty()) { "No column vectors provided for building a matrix." }
            require(columnVectors.all { it.dimension == rowDimension }) { "Provided column vectors have different dimensions." }

            val matrixValues = (0 until rowDimension).fold(emptyList<DoubleArray>()) { acc, currentRowIndex -> acc + columnVectors.map { it[currentRowIndex] }.toDoubleArray() }

            return RealMatrix(matrixValues.toTypedArray())
        }

        /**
         * Creates a [RealMatrix] from a list of 3D vectors
         *
         * @param vectors list of 3D vectors whereby each vector will be represented as a row
         */
        @JvmName("ofListVector3D")
        fun of(vectors: List<Vector3D>): RealMatrix =
            RealMatrix(vectors.map { it.toRealVector() })

        /**
         * Creates an identity [RealMatrix] of the [dimension].
         */
        fun ofIdentity(dimension: Int): RealMatrix {
            require(dimension > 0) { "Dimension must be greater than 0." }

            val cmMatrix = MatrixUtils.createRealIdentityMatrix(dimension)
            return RealMatrix(cmMatrix)
        }

        /**
         * Creates a [RealMatrix] with [diagonal] entries.
         *
         * @param diagonal entry values for matrix diagonal
         */
        fun ofDiagonal(diagonal: RealVector): RealMatrix {
            require(diagonal.dimension > 0) { "Dimension of diagonal must be greater than 0." }

            val cmMatrix = MatrixUtils.createRealDiagonalMatrix(diagonal.toDoubleArray())
            return RealMatrix(cmMatrix)
        }
    }
}
