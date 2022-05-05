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

package io.rtron.math.transform

import io.rtron.math.geometry.euclidean.threed.Pose3D
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.point.toVector3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import io.rtron.math.linear.RealMatrix
import io.rtron.math.linear.RealVector
import org.joml.Matrix4d as JOMLMatrix4d
import org.joml.Matrix4dc as JOMLMatrix4dc
import org.joml.Quaterniond as JOMLQuaterniond
import org.joml.Vector3d as JOMLVector3d

fun JOMLMatrix4dc.toRealMatrix(): RealMatrix {
    val values = DoubleArray(16)
    return RealMatrix(this.get(values), 4)
}

/**
 * Affine transformation matrix and operations in 3D.
 *
 * @param _matrix internal matrix of adapting library
 */
class Affine3D(
    private val _matrix: JOMLMatrix4dc
) : AbstractAffine() {

    // Properties and Initializers
    init {
        require(_matrix.isAffine) { "Matrix must be affine." }
        require(_matrix.isFinite) { "Matrix must contain only finite values." }
    }

    private val _matrixTransposed by lazy { JOMLMatrix4d(_matrix).transpose() }
    private val _matrixInverse by lazy { JOMLMatrix4d(_matrix).invertAffine() }

    // Methods: Transformation
    fun transform(point: Vector3D) = _matrix.transformPosition(point.toVector3DJOML()).toVector3D()
    fun inverseTransform(point: Vector3D) = _matrixInverse.transformPosition(point.toVector3DJOML()).toVector3D()

    @JvmName("transformOfListVector3D")
    fun transform(points: List<Vector3D>): List<Vector3D> = points.map { transform(it) }

    @JvmName("inverseTransformOfListVector3D")
    fun inverseTransform(points: List<Vector3D>) = points.map { inverseTransform(it) }

    fun transform(polygon: Polygon3D) =
        Polygon3D(polygon.vertices.map { transform(it) }, polygon.tolerance)
    fun inverseTransform(polygon: Polygon3D) =
        Polygon3D(polygon.vertices.map { inverseTransform(it) }, polygon.tolerance)

    @JvmName("transformOfListPolygon3D")
    fun transform(polygons: List<Polygon3D>) = polygons.map { transform(it) }

    @JvmName("inverseTransformOfListPolygon3D")
    fun inverseTransform(polygons: List<Polygon3D>) = polygons.map { inverseTransform(it) }

    // Methods: Extraction

    /**
     * Extracts the translation vector of the [Affine3D] transformation matrix.
     *
     * @return translation vector
     */
    fun extractTranslation() = _matrix.getTranslation(JOMLVector3d()).toVector3D()

    /**
     * Extracts the scale vector of the [Affine3D] transformation matrix.
     * See the wikipedia article of [scaling (geometry)](https://en.wikipedia.org/wiki/Scaling_(geometry)).
     *
     * @return scaling vector
     */
    fun extractScaling(): RealVector = _matrix.getScale(JOMLVector3d()).toRealVector()

    /**
     * Extracts the rotation of the [Affine3D] transformation matrix.
     *
     * @return rotation
     */
    fun extractRotationAffine(): Affine3D {
        val rotation = _matrix.getUnnormalizedRotation(JOMLQuaterniond())!!
        return Affine3D(JOMLMatrix4d().rotate(rotation))
    }

    /**
     * Extracts the rotation of the [Affine3D] transformation matrix to a [Rotation3D].
     *
     * @return rotation angles
     */
    fun extractRotation(): Rotation3D {
        val rotationQuaternion = _matrix.getUnnormalizedRotation(JOMLQuaterniond())!!
        val rotationEuler = rotationQuaternion.getEulerAnglesXYZ(JOMLVector3d())!!
        return Rotation3D(rotationEuler.z, rotationEuler.y, rotationEuler.x)
    }

    /**
     * Appends an[other] [Affine3D] transformation matrix.
     *
     * @param other transformation matrix for appending
     * @return new [Affine3D] transformation matrix
     */
    fun append(other: Affine3D): Affine3D {
        val result = this.toMatrix4JOML().mul(other.toMatrix4JOML())
        return Affine3D(result)
    }

    // Methods: Generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Affine3D

        if (_matrix != other._matrix) return false

        return true
    }

    override fun hashCode(): Int {
        return _matrix.hashCode()
    }

    // Conversions
    fun toMatrix4JOML() = JOMLMatrix4d(this._matrix)
    fun toRealMatrix() = this._matrixTransposed.toRealMatrix()
    fun toDoubleArray(): DoubleArray = this._matrixTransposed.get(DoubleArray(16))
    fun toDoubleList(): List<Double> = toDoubleArray().toList()

    override fun toString(): String {
        return "Affine3D(_matrix=$_matrix)"
    }

    companion object {
        val UNIT = Affine3D(JOMLMatrix4d())

        /**
         * Creates an [Affine3D] transformation matrix from a 4x4 [RealMatrix].
         */
        fun of(matrix: RealMatrix): Affine3D {
            require(matrix.dimension == 4 to 4) { "Matrix must have dimensions of 4x4." }

            val jomlMatrix = JOMLMatrix4d().set(matrix.transpose().toDoubleArray())
            return Affine3D(jomlMatrix)
        }

        /**
         * Creates an [Affine3D] transformation matrix from a [translation] vector.
         */
        fun of(translation: Vector3D): Affine3D {
            val matrix = JOMLMatrix4d().translation(translation.toVector3DJOML())
            return Affine3D(matrix)
        }

        /**
         * Creates an [Affine3D] transformation matrix from a [scaling] vector.
         */
        fun of(scaling: RealVector): Affine3D {
            require(scaling.dimension == 3) { "Wrong dimension ${scaling.dimension}." }
            val matrix = JOMLMatrix4d().scale(scaling[0], scaling[1], scaling[2])
            return Affine3D(matrix)
        }

        /**
         * Creates an [Affine3D] transformation matrix from a [rotation].
         */
        fun of(rotation: Rotation3D): Affine3D {
            val matrix = JOMLMatrix4d()
                .rotateZ(rotation.heading)
                .rotateY(rotation.pitch)
                .rotateX(rotation.roll)

            return Affine3D(matrix)
        }

        /**
         * Creates an [Affine3D] transformation matrix from a list of sequentially applied affine
         * transformation matrices.
         */
        fun of(affineList: List<Affine3D>): Affine3D {
            return affineList.fold(UNIT) { acc, affine -> acc.append(affine) }
        }

        /**
         * Creates an [Affine3D] transformation matrix from a list of sequentially applied affine
         * transformation matrices.
         */
        fun of(vararg affines: Affine3D) = of(affines.asList())

        /**
         * Creates an [Affine3D] transformation matrix from a [pose].
         */
        fun of(pose: Pose3D): Affine3D {
            val translationAffine = of(pose.point)
            val rotationAffine = of(pose.rotation)
            return of(listOf(translationAffine, rotationAffine))
        }

        /**
         * Creates an [Affine3D] transformation matrix by means of a new coordinate system basis.
         *
         * @param basisX x axis of new basis
         * @param basisY y axis of new basis
         * @param basisZ z axis of new basis
         */
        fun of(basisX: Vector3D, basisY: Vector3D, basisZ: Vector3D): Affine3D {
            val matrix = JOMLMatrix4d().set(
                basisX.toVector4DJOML(),
                basisY.toVector4DJOML(),
                basisZ.toVector4DJOML(),
                Vector3D.ZERO.toVector4DJOML()
            )!!
            return Affine3D(matrix.invertAffine())
        }
    }
}
