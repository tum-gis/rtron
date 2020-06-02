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

package io.rtron.math.transform

import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.linear.RealMatrix
import io.rtron.math.linear.RealVector
import kotlin.math.atan2
import org.joml.Matrix4d as JOMLMatrix4d
import org.joml.Matrix4dc as JOMLMatrix4dc
import org.joml.Vector3d as JOMLVector3d

fun JOMLVector3d.toVector2D() = Vector2D(this.x, this.y)
fun JOMLVector3d.toRealVector() = RealVector.of(this.x, this.y, this.z)

/**
 * Affine transformation matrix and operations in 2D.
 *
 * @param _matrix internal matrix of adapting library
 */
class Affine2D(
        private val _matrix: JOMLMatrix4dc
) : AbstractAffine() {

    // Properties and Initializers
    init {
        require(_matrix.isAffine)
        { "Matrix must be affine." }
        require(_matrix.m02() == 0.0 && _matrix.m12() == 0.0 && _matrix.m22() == 1.0 && _matrix.m32() == 0.0)
        { "Matrix transformations must only be applied to the xy plane." }
        require(_matrix.isFinite)
        { "Matrix must contain only finite values." }
    }

    private val _matrixTransposed by lazy { JOMLMatrix4d(_matrix).transpose() }
    private val _matrixInverse by lazy { JOMLMatrix4d(_matrix).invertAffine() }

    // Methods: Transformation
    fun transform(point: Vector2D) =
            _matrix.transformPosition(point.toVector3D(0.0).toVector3DJOML()).toVector2D()

    fun inverseTransform(point: Vector2D) =
            _matrixInverse.transformPosition(point.toVector3D(0.0).toVector3DJOML()).toVector2D()

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
    fun extractTranslation() = _matrix.getTranslation(JOMLVector3d()).toVector2D()

    /**
     * Extracts the scale vector of the [Affine2D] transformation matrix.
     * See the wikipedia article of [scaling (geometry)](https://en.wikipedia.org/wiki/Scaling_(geometry)).
     *
     * @return scaling vector
     */
    fun extractScaling(): RealVector {
        val scale = _matrix.getScale(JOMLVector3d())
        return RealVector.of(scale.x, scale.y)
    }

    /**
     * Extracts the rotation of the [Affine2D] transformation matrix.
     *
     * @return rotation
     */
    fun extractRotation() = Rotation2D(atan2(_matrix.m01(), _matrix.m11()))

    /**
     * Appends an[other] [Affine2D] transformation matrix.
     *
     * @param other transformation matrix for appending
     * @return new [Affine2D] transformation matrix
     */
    fun append(other: Affine2D): Affine2D {
        val result = this.toMatrix4JOML().mul(other.toMatrix4JOML())
        return Affine2D(result)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Affine2D

        if (_matrix != other._matrix) return false

        return true
    }

    override fun hashCode(): Int {
        return _matrix.hashCode()
    }

    // Conversions
    fun toMatrix4JOML() = JOMLMatrix4d(this._matrix)
    fun toMatrix() = RealMatrix(toDoubleArray(), 4)
    fun toDoubleArray(): DoubleArray = this._matrixTransposed.get(DoubleArray(16))

    companion object {
        /**
         * [UNIT] transformation matrix.
         */
        val UNIT = Affine2D(JOMLMatrix4d())

        /**
         * Creates an [Affine2D] transformation matrix from a [translation] vector.
         */
        fun of(translation: Vector2D): Affine2D {
            val matrix = JOMLMatrix4d().translate(translation.x, translation.y, 0.0)
            return Affine2D(matrix)
        }

        /**
         * Creates an [Affine2D] transformation matrix from a [scaling] vector.
         */
        fun of(scaling: RealVector): Affine2D {
            require(scaling.dimension == 2) { "Wrong dimension ${scaling.dimension}." }
            val matrix = JOMLMatrix4d().scale(scaling[0], scaling[1], 1.0)
            return Affine2D(matrix)
        }

        /**
         * Creates an [Affine2D] transformation matrix from a [rotation].
         */
        fun of(rotation: Rotation2D): Affine2D {
            val matrix = JOMLMatrix4d().rotateZ(rotation.angle)
            return Affine2D(matrix)
        }

        /**
         * Creates an [Affine2D] transformation matrix from a list of sequentially applied affine
         * transformation matrices.
         */
        fun of(affineList: List<Affine2D>): Affine2D {
            return affineList.fold(UNIT) { acc, affine -> acc.append(affine) }
        }

        /**
         * Creates an [Affine2D] transformation matrix from a list of sequentially applied affine
         * transformation matrices.
         */
        fun of(vararg affines: Affine2D) = of(affines.asList())

        /**
         * Creates an [Affine2D] transformation matrix from first applying a [translation] and then a [rotation].
         */
        fun of(translation: Vector2D, rotation: Rotation2D): Affine2D {
            val matrix = JOMLMatrix4d()
                    .translate(translation.x, translation.y, 0.0)
                    .rotateZ(rotation.angle)
            return Affine2D(matrix)
        }

        /**
         * Creates an [Affine2D] transformation matrix from a [pose].
         */
        fun of(pose: Pose2D): Affine2D {
            val matrix = JOMLMatrix4d()
                    .translate(pose.point.x, pose.point.y, 0.0)
                    .rotateZ(pose.rotation.angle)
            return Affine2D(matrix)
        }

    }

}
