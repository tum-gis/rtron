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

package io.rtron.math.geometry.euclidean.threed.curve

import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.point.toVector3D
import org.apache.commons.math3.geometry.euclidean.threed.Line as CMLine3D

/** Conversion from adapted Line class from Apache Commons Math. */
fun CMLine3D.toLine3D(tolerance: Double) =
    Line3D(this.origin.toVector3D(), this.origin.toVector3D() + this.direction.toVector3D(), tolerance)

/**
 * Represents a line in 3D.
 *
 * @param point1 first point on the line
 * @param point2 second point on the line
 */
class Line3D(
    point1: Vector3D,
    point2: Vector3D,
    private val tolerance: Double
) {

    // Properties and Initializers
    init {
        require(point1 != point2) { "Points must not be identical." }
        require(point1.fuzzyUnequals(point2, tolerance)) { "Points must be different by at least the tolerance threshold." }
    }

    /** adapted line class of Apache Commons Math */
    private val line3D by lazy { CMLine3D(point1.toVector3DCm(), point2.toVector3DCm(), tolerance) }

    /** line point closest to the origin */
    val origin by lazy { line3D.origin.toVector3D() }

    /** normalized direction vector */
    val direction by lazy { line3D.direction.toVector3D() }

    // Methods

    /**
     * Returns the distance between this line and a given [point].
     *
     * @param point point for which the distance shall be calculated
     * @return distance between this and the [point]
     */
    fun distance(point: Vector3D): Double = line3D.distance(point.toVector3DCm())

    // Conversions
    fun toLine3DCM() = line3D
}
