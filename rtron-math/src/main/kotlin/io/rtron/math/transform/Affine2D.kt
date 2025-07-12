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

import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.linear.RealMatrix
import io.rtron.math.linear.RealVector
import kotlin.math.atan2
import org.joml.Matrix3d as JOMLMatrix3d
import org.joml.Matrix3dc as JOMLMatrix3dc
import org.joml.Vector3d as JOMLVector3d

fun JOMLVector3d.toVector2D() = Vector2D(this.x, this.y)

fun JOMLVector3d.toRealVector() = RealVector.of(this.x, this.y, this.z)

fun JOMLMatrix3dc.isAffine() = this.m02() == 0.0 && this.m12() == 0.0 && this.m22() == 1.0

/**
 * Affine transformation matrix and operations in 2D.
 *
 * @param matrix internal matrix of adapting library
 */
class Affine2D(
    private val matrix: JOMLMatrix3dc,
) : AbstractAffine() {
    // Properties and Initializers
    init {
        require(matrix.isAffine()) { "Matrix must be affine." }
        require(matrix.isFinite) { "Matrix must contain only finite values." }
    }

    private val matrixTransposed by lazy { JOMLMatrix3d(matrix).transpose() }
    private val matrixInverse by lazy { JOMLMatrix3d(matrix).invert() }

    // Methods: Transformation
    fun transform(point: Vector2D) = matrix.transform(point.x, point.y, 1.0, JOMLVector3d()).toVector2D()

    fun inverseTransform(point: Vector2D) = matrixInverse.transform(point.x, point.y, 1.0, JOMLVector3d()).toVector2D()

    fun transform(rotation: Rotation2D) = rotation + extractRotation()

    fun inverseTransform(rotation: Rotation2D) = rotation - extractRotation()

    fun transform(pose: Pose2D) = Pose2D(transform(pose.point), transform(pose.rotation))

    fun inverseTransform(pose: Pose2D) = Pose2D(inverseTransform(pose.point), inverseTransform(pose.rotation))

    // Methods: Extraction

    /**
     * Extracts the translation vector of the [Affine2D] transformation matrix.
     *
     * @return translation vector
     */
    fun extractTranslation(): Vector2D = Vector2D(matrix.m20(), matrix.m21())

    /**
     * Extracts the scale vector of the [Affine2D] transformation matrix.
     * See the wikipedia article of [scaling (geometry)](https://en.wikipedia.org/wiki/Scaling_(geometry)).
     *
     * @return scaling vector
     */
    fun extractScaling(): RealVector {
        val scale = matrix.getScale(JOMLVector3d())
        return RealVector.of(scale.x, scale.y)
    }

    /**
     * Extracts the rotation of the [Affine2D] transformation matrix.
     *
     * @return rotation
     */
    fun extractRotation() = Rotation2D(atan2(matrix.m01(), matrix.m11()))

    /**
     * Appends an[other] [Affine2D] transformation matrix.
     *
     * @param other transformation matrix for appending
     * @return new [Affine2D] transformation matrix
     */
    fun append(other: Affine2D): Affine2D {
        val result = this.toMatrix3JOML().mul(other.toMatrix3JOML())
        return Affine2D(result)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Affine2D

        if (matrix != other.matrix) return false

        return true
    }

    override fun hashCode(): Int = matrix.hashCode()

    // Conversions
    fun toMatrix3JOML() = JOMLMatrix3d(this.matrix)

    fun toMatrix() = RealMatrix(toDoubleArray(), 3)

    fun toDoubleArray(): DoubleArray = this.matrixTransposed.get(DoubleArray(9))

    companion object {
        /**
         * [UNIT] transformation matrix.
         */
        val UNIT = Affine2D(JOMLMatrix3d())

        /**
         * Creates an [Affine2D] transformation matrix from a [translation] vector.
         */
        fun of(translation: Vector2D): Affine2D {
            val matrix = JOMLMatrix3d() // .translate(translation.x, translation.y, 0.0)
            matrix.m20 = translation.x
            matrix.m21 = translation.y

            return Affine2D(matrix)
        }

        /**
         * Creates an [Affine2D] transformation matrix from a [scaling] vector.
         */
        fun of(scaling: RealVector): Affine2D {
            require(scaling.dimension == 2) { "Wrong dimension ${scaling.dimension}." }
            val matrix = JOMLMatrix3d().scale(scaling[0], scaling[1], 1.0)
            return Affine2D(matrix)
        }

        /**
         * Creates an [Affine2D] transformation matrix from a [matrix] with column and row dimension of 2.
         */
        fun of(matrix: RealMatrix): Affine2D {
            require(matrix.columnDimension == 2) { "Wrong column dimension ${matrix.columnDimension}." }
            require(matrix.rowDimension == 2) { "Wrong row dimension ${matrix.rowDimension}." }
            require(matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0] > 0.0) {
                "Determinant must be greater than zero, since it must not be reflection."
            }
            val jomlMatrix = JOMLMatrix3d()
            jomlMatrix.m00 = matrix[0][0]
            jomlMatrix.m01 = matrix[1][0]
            jomlMatrix.m10 = matrix[0][1]
            jomlMatrix.m11 = matrix[1][1]
            return Affine2D(jomlMatrix)
        }

        /**
         * Creates an [Affine2D] transformation matrix from a [rotation].
         */
        fun of(rotation: Rotation2D): Affine2D {
            val matrix = JOMLMatrix3d().rotateZ(rotation.angle)
            return Affine2D(matrix)
        }

        /**
         * Creates an [Affine2D] transformation matrix from a list of sequentially applied affine
         * transformation matrices.
         */
        fun of(affineList: List<Affine2D>): Affine2D = affineList.fold(UNIT) { acc, affine -> acc.append(affine) }

        /**
         * Creates an [Affine2D] transformation matrix from a list of sequentially applied affine
         * transformation matrices.
         */
        fun of(vararg affines: Affine2D) = of(affines.asList())

        /**
         * Creates an [Affine2D] transformation matrix from first applying a [translation] and then a [rotation].
         */
        fun of(
            translation: Vector2D,
            rotation: Rotation2D,
        ): Affine2D {
            val matrix =
                JOMLMatrix3d()
                    .apply {
                        m20 = translation.x
                        m21 = translation.y
                    }.rotateZ(rotation.angle)
            return Affine2D(matrix)
        }

        /**
         * Creates an [Affine2D] transformation matrix from a [pose].
         */
        fun of(pose: Pose2D): Affine2D {
            val matrix =
                JOMLMatrix3d()
                    .apply {
                        m20 = pose.point.x
                        m21 = pose.point.y
                    }.rotateZ(pose.rotation.angle)
            return Affine2D(matrix)
        }
    }
}
