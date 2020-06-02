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

package io.rtron.math.processing

import io.rtron.math.geometry.euclidean.threed.curve.Line3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Plane3D
import io.rtron.math.linear.RealMatrix
import io.rtron.math.linear.SingularValueDecomposition
import io.rtron.math.std.DBL_EPSILON
import io.rtron.math.std.DBL_EPSILON_7
import io.rtron.std.filterWindowedEnclosing
import io.rtron.std.zipWithNextEnclosing
import io.rtron.std.zipWithNextToTriples
import kotlin.math.abs

/**
 * Returns true, if all [Vector3D] within the list are located on a line.
 *
 * @param tolerance tolerated distance between the line and the points
 */
fun List<Vector3D>.isColinear(tolerance: Double = DBL_EPSILON_7): Boolean =
        this.zipWithNextToTriples().all { it.isColinear(tolerance) }

/**
 * Returns true, if the second [Vector3D] is located on the line defined by the first and third [Vector3D].
 *
 * @param tolerance tolerated distance between the line and second [Vector3D]
 */
fun Triple<Vector3D, Vector3D, Vector3D>.isColinear(tolerance: Double = DBL_EPSILON_7): Boolean =
        Line3D(first, third).distance(second) <= tolerance

/**
 * Removes the linearly dependent vertices of a list of vertices that are sequentially interpreted.
 *
 * @receiver list of vertices that are evaluated in an enclosing way
 */
fun List<Vector3D>.removeLinearlyRedundantVertices(): List<Vector3D> =
    if (this.size < 3) this
    else filterWindowedEnclosing(listOf(false, true, false)) { it.isColinear() }

/**
 * Calculates the best fitting plane that lies in a list of [Vector3D].
 * See [StackExchange](https://math.stackexchange.com/a/99317) for more information.
 */
fun List<Vector3D>.calculateBestFittingPlane(): Plane3D {
    require(this.size >= 3) { "Calculating the best fitting plane requires at least three points." }

    val centroid = this.calculateCentroid()
    val offsetRealVectors = this.map { (it - centroid).toRealVector() }

    val singularValueDecomposition = SingularValueDecomposition(RealMatrix(offsetRealVectors))

    val matrixV = singularValueDecomposition.matrixV
    val normal = matrixV.getColumnVector(2)

    return Plane3D(centroid, Vector3D(normal[0], normal[1], normal[2]))
}

/**
 * Returns true, if all [Vector3D]s are located within a plane.
 *
 * @param tolerance tolerated distance between points and the plane
 * @param dynamicToleranceAdjustment increases the tolerance when numbers are greater
 */
fun List<Vector3D>.isPlanar(tolerance: Double = DBL_EPSILON_7, dynamicToleranceAdjustment: Boolean = true): Boolean {
    require(size >= 3)
    { "Planarity check requires the provision of at least three points." }

    val adjustedTolerance = if (dynamicToleranceAdjustment) {
        val u = Math.ulp(this.flatMap { it.toDoubleList() }.map(::abs).max()!!)
        val dynamicFactor = u / DBL_EPSILON
        tolerance * dynamicFactor
    } else tolerance

    val bestFittingPlane = this.calculateBestFittingPlane()
    return this.all { bestFittingPlane.getOffset(it) <= adjustedTolerance }
}

/**
 * Calculates the normal of a vertex list by means of
 * (Newell’s method)[https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal#Newell.27s_Method].
 */
fun List<Vector3D>.calculateNormal(): Vector3D =
        this.zipWithNextEnclosing().fold(Vector3D.ZERO) { normalVector, vertexPair ->
            normalVector + (vertexPair.first - vertexPair.second).crossProduct(vertexPair.first + vertexPair.second)
        }

/**
 * Calculates the centroid of a list of [Vector3D].
 * See the wikipedia article of [Centroid](https://en.wikipedia.org/wiki/Centroid).
 */
fun List<Vector3D>.calculateCentroid(): Vector3D =
        this.reduce { sum, point -> sum + point }.div(this.size.toDouble())

/**
 * Conversion to a string of coordinates.
 */
fun List<Vector3D>.toCoordinatesString(): String {
    val x = "x=[" + this.joinToString { "%.4f".format(it.x) } + "]; "
    val y = "y=[" + this.joinToString { "%.4f".format(it.y) } + "]; "
    val z = "z=[" + this.joinToString { "%.4f".format(it.z) } + "]"
    return x + y + z
}

/**
 * Removes consecutively following side duplicates.
 * For example, (A, B, C, B, D) will yield (A, B, C, D).
 *
 * @receiver list of sequentially following vectors
 * @return list of vectors without consecutively following side duplicates
 */
fun List<Vector3D>.removeConsecutiveSideDuplicates(): List<Vector3D> =
        if (this.size < 3) this
        else filterWindowedEnclosing(listOf(false, true, true)) { it[0] == it[2] }
