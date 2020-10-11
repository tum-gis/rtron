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

package io.rtron.math.geometry.euclidean.threed.solid

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getOrElse
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.StackedFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.euclidean.threed.Geometry3DVisitor
import io.rtron.math.geometry.euclidean.threed.curve.Curve3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import io.rtron.math.geometry.euclidean.twod.curve.LateralTranslatedCurve2D
import io.rtron.math.range.DefinableDomain
import io.rtron.math.range.Range
import io.rtron.math.range.Tolerable
import io.rtron.math.range.fuzzyEncloses
import io.rtron.std.handleFailure


/**
 * Represents a parametric sweep in 3D. This refers to a geometry solid, which is defined by a [referenceCurveXY].
 * The width and height of the solid is defined as functions along the reference curve.
 *
 * @param referenceCurveXY main curve along which the solid is build up
 * @param absoluteHeight absolute height function of the [referenceCurveXY]
 * @param objectHeightFunction height of the object as function of the curve relative position
 * @param objectWidthFunction width of the object as function of the curve relative position
 */
data class ParametricSweep3D(
        val referenceCurveXY: LateralTranslatedCurve2D,
        val absoluteHeight: UnivariateFunction,
        val objectHeightFunction: LinearFunction,
        val objectWidthFunction: LinearFunction,
        override val tolerance: Double,
        private val discretizationStepSize: Double = DEFAULT_STEP_SIZE
) : AbstractSolid3D(), DefinableDomain<Double>, Tolerable {

    // Properties and Initializers
    init {
        require(absoluteHeight.domain.fuzzyEncloses(referenceCurveXY.domain, tolerance))
        { "The absolute height function must be defined everywhere where the referenceCurveXY is also defined." }
        require(objectHeightFunction.domain.fuzzyEncloses(referenceCurveXY.domain, tolerance))
        { "The object height function must be defined everywhere where the referenceCurveXY is also defined." }
        require(objectWidthFunction.domain.fuzzyEncloses(referenceCurveXY.domain, tolerance))
        { "The object width function must be defined everywhere where the referenceCurveXY is also defined." }
    }

    override val domain: Range<Double>
        get() = referenceCurveXY.domain

    /** upper left curve of the sweep (perspective in the direction of the reference curve) */
    private val upperLeftCurve: Curve3D
        get() {
            val curveXY = referenceCurveXY.addLateralTranslation(objectWidthFunction, -0.5)
            val height = StackedFunction.ofSum(absoluteHeight, objectHeightFunction, defaultValue = 0.0)
            return Curve3D(curveXY, height)
        }

    /** upper right curve of the sweep (perspective in the direction of the reference curve) */
    private val upperRightCurve: Curve3D
        get() {
            val curveXY = referenceCurveXY.addLateralTranslation(objectWidthFunction, +0.5)
            val height = StackedFunction.ofSum(absoluteHeight, objectHeightFunction, defaultValue = 0.0)
            return Curve3D(curveXY, height)
        }

    /** lower left curve of the sweep (perspective in the direction of the reference curve) */
    private val lowerLeftCurve: Curve3D
        get() {
            val curveXY = referenceCurveXY.addLateralTranslation(objectWidthFunction, -0.5)
            return Curve3D(curveXY, absoluteHeight)
        }

    /** lower right curve of the sweep (perspective in the direction of the reference curve) */
    private val lowerRightCurve: Curve3D
        get() {
            val curveXY = referenceCurveXY.addLateralTranslation(objectWidthFunction, +0.5)
            return Curve3D(curveXY, absoluteHeight)
        }

    /** lower left curve of the sweep as a list of points */
    private val lowerLeftVertices by lazy {
        lowerLeftCurve.calculatePointListGlobalCS(discretizationStepSize).handleFailure { throw it.error }
    }

    /** lower right curve of the sweep as a list of points */
    private val lowerRightVertices by lazy {
        lowerRightCurve.calculatePointListGlobalCS(discretizationStepSize).handleFailure { throw it.error }
    }

    /** upper left curve of the sweep as a list of points */
    private val upperLeftVertices by lazy {
        upperLeftCurve.calculatePointListGlobalCS(discretizationStepSize).handleFailure { throw it.error }
    }

    /** upper right curve of the sweep as a list of points */
    private val upperRightVertices by lazy {
        upperRightCurve.calculatePointListGlobalCS(discretizationStepSize).handleFailure { throw it.error }
    }

    init {
        require(lowerLeftVertices.zipWithNext().all { it.first != it.second })
        { "Must not contain consecutively points." }
        require(lowerRightVertices.zipWithNext().all { it.first != it.second })
        { "Must not contain consecutively points." }
        require(upperLeftVertices.zipWithNext().all { it.first != it.second })
        { "Must not contain consecutively points." }
        require(upperRightVertices.zipWithNext().all { it.first != it.second })
        { "Must not contain consecutively points." }
    }

    // Methods
    override fun calculatePolygonsLocalCS(): Result<List<Polygon3D>, Exception> {

        // calculate the side faces
        val basePolygons = createPolygons(lowerLeftVertices, lowerRightVertices) getOrElse { emptyList() }
        val topPolygons = createPolygons(upperRightVertices, upperLeftVertices) getOrElse { emptyList() }
        val leftPolygons = createPolygons(upperLeftVertices, lowerLeftVertices) getOrElse { emptyList() }
        val rightPolygons = createPolygons(lowerRightVertices, upperRightVertices) getOrElse { emptyList() }

        // calculate the start and end faces
        val startPolygons = run {
            val linearRing = LinearRing3D.of(upperLeftVertices.first(), upperRightVertices.first(),
                    lowerRightVertices.first(), lowerLeftVertices.first())
            linearRing.calculatePolygonsGlobalCS() getOrElse { emptyList() }
        }
        val endPolygons = run {
            val linearRing = LinearRing3D.of(upperLeftVertices.last(), lowerLeftVertices.last(),
                    lowerRightVertices.last(), upperRightVertices.last())
            linearRing.calculatePolygonsGlobalCS() getOrElse { emptyList() }
        }

        // combine all polygons
        val allPolygons = basePolygons + topPolygons + leftPolygons + rightPolygons +
                startPolygons + endPolygons
        return Result.success(allPolygons)
    }

    private fun createPolygons(leftVertices: List<Vector3D>, rightVertices: List<Vector3D>):
            Result<List<Polygon3D>, Exception> =
            LinearRing3D.ofWithDuplicatesRemoval(leftVertices, rightVertices, tolerance)
                    .handleFailure { return it }
                    .map { it.calculatePolygonsGlobalCS() }
                    .handleFailure { return it }
                    .flatten()
                    .let { Result.success(it) }

    override fun accept(visitor: Geometry3DVisitor) = visitor.visit(this)

    companion object {
        const val DEFAULT_STEP_SIZE: Double = 0.3 // used for tesselation
    }

}
