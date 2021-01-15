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

package io.rtron.math.geometry.euclidean.threed.curve

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.Geometry3DVisitor
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.*
import io.rtron.std.handleFailure


/**
 * Abstract class for all geometric curve objects in 3D.
 */
abstract class AbstractCurve3D : AbstractGeometry3D(), DefinableDomain<Double>, Tolerable {

    // Properties

    /** Length of the curve */
    val length: Double get() = domain.length

    /** [BoundType] at the end of the curve */
    val endBoundType: BoundType get() = domain.upperBoundType()


    // Methods

    /**
     * Returns the point in the local cartesian coordinate system that is located on this curve and given by a point in
     * the curve relative coordinate system.
     * An error is returned, if the requested point is not within this curve's domain.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @return point in cartesian coordinates
     */
    fun calculatePointLocalCS(curveRelativePoint: CurveRelativeVector1D): Result<Vector3D, Exception> {
        this.domain.fuzzyContainsResult(curveRelativePoint.curvePosition, tolerance).handleFailure { return it }
        return calculatePointLocalCSUnbounded(curveRelativePoint)
    }

    /**
     * Returns the point in the cartesian coordinate system that is located on this curve and given by a point in the
     * curve relative coordinate system.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @return point in cartesian coordinates
     */
    protected abstract fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
            Result<Vector3D, Exception>

    /**
     * Returns the point in the global cartesian coordinate system that is located on this curve and given by a
     * point in the curve relative coordinate system.
     * An error is returned, if the requested point is not within this curve's domain.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @return point in cartesian coordinates
     */
    fun calculatePointGlobalCS(curveRelativePoint: CurveRelativeVector1D): Result<Vector3D, Exception> =
            calculatePointLocalCS(curveRelativePoint).map { affineSequence.solve().transform(it) }

    /**
     * Returns the start point in the global cartesian coordinate system that is located on this curve.
     */
    fun calculateStartPointGlobalCS() = calculatePointGlobalCS(CurveRelativeVector1D.ZERO)

    /**
     * Returns the end point in the global cartesian coordinate system that is located on this curve.
     */
    fun calculateEndPointGlobalCS() = calculatePointGlobalCS(CurveRelativeVector1D(length))

    /**
     * Returns a list of points on the curve with a step size of [step].
     *
     * @param step step size between the points
     * @param includeEndPoint true, if the endpoint shall be included
     * @return list of points on this curve
     */
    fun calculatePointListGlobalCS(step: Double, includeEndPoint: Boolean = true): Result<List<Vector3D>, Exception>
            = domain.arrange(step, includeEndPoint, tolerance)
            .map(::CurveRelativeVector1D)
            .map { calculatePointGlobalCS(it) }
            .handleFailure { return it }
            .let { Result.success(it) }
    
    override fun accept(visitor: Geometry3DVisitor) { visitor.visit(this) }
}
