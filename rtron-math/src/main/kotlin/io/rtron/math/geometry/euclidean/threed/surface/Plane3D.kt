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

package io.rtron.math.geometry.euclidean.threed.surface

import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.point.toVector3D
import io.rtron.math.std.DEFAULT_TOLERANCE
import org.apache.commons.math3.geometry.euclidean.threed.Plane as CMPlane
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D as CMVector3D

/**
 * Representation of a plane in 3D.
 * See the wikipedia article on a [plane](https://en.wikipedia.org/wiki/Plane_(geometry)).
 *
 * @param point point belonging to the plane
 * @param normal normal direction to the plane
 */
data class Plane3D(
    val point: Vector3D = Vector3D.ZERO,
    val normal: Vector3D,
    val tolerance: Double
) {

    // Properties and Initializers
    init {
        require(normal.norm > tolerance) { "Norm of normal must be greater than zero and the tolerance threshold." }
    }

    private val _plane = CMPlane(point.toVector3DCm(), normal.toVector3DCm(), tolerance)

    /** first canonical vector u of the plane */
    val vectorU by lazy { _plane.u.toVector3D() }

    /** second canonical vector v of the plane */
    val vectorV by lazy { _plane.v.toVector3D() }

    // Methods
    /** Returns the oriented distance between the [point] and this plane. */
    fun getOffset(point: Vector3D): Double = _plane.getOffset(point.toVector3DCm())

    /** Projects the [point] onto this plane and returns the projected point. */
    fun project(point: Vector3D): Vector3D = (_plane.project(point.toVector3DCm()) as CMVector3D).toVector3D()

    /** Returns true, if this plane is similar to the [other] plane. */
    fun isSimilarTo(other: Plane3D): Boolean = _plane.isSimilarTo(other.toPlane3DCm())

    // Conversions

    /** Returns adapted line plane class of Apache Commons Math. */
    fun toPlane3DCm() = this._plane

    companion object {
        val XY_PLANE = Plane3D(Vector3D.ZERO, Vector3D.Z_AXIS, DEFAULT_TOLERANCE)
        val XZ_PLANE = Plane3D(Vector3D.ZERO, Vector3D.Y_AXIS, DEFAULT_TOLERANCE)
        val YZ_PLANE = Plane3D(Vector3D.ZERO, Vector3D.X_AXIS, DEFAULT_TOLERANCE)
    }
}
