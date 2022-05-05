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

import arrow.core.Either
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.curved.threed.curve.CurveRelativeLineSegment3D
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.math.geometry.euclidean.threed.Pose3D
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.twod.curve.AbstractCurve2D
import io.rtron.math.range.fuzzyContainsResult
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.transform.Affine3D
import io.rtron.std.handleFailure
import io.rtron.std.toResult

/**
 * A curve in 3D defined by a curve in 2D and a height function. Furthermore, the curve can have a torsion, which is
 * relevant for pose and transformation matrix calculations along the curve.
 * See the wikipedia article on [torsion of a curve](https://en.wikipedia.org/wiki/Torsion_of_a_curve).
 *
 * @param curveXY the curve in the xy plane
 * @param heightFunction the definition of the height, which must be defined where the [curveXY] is defined
 * @param torsionFunction the torsion of the curve, which must be defined where the [curveXY] is defined
 */
data class Curve3D(
    val curveXY: AbstractCurve2D,
    val heightFunction: UnivariateFunction,
    val torsionFunction: UnivariateFunction = LinearFunction.X_AXIS
) : AbstractCurve3D() {

    // Properties and Initializers
    init {
        require(heightFunction.domain.fuzzyEncloses(curveXY.domain, tolerance)) { "The height function must be defined everywhere where the curveXY is also defined." }
        require(torsionFunction.domain.fuzzyEncloses(curveXY.domain, tolerance)) { "The torsion function must be defined everywhere where the curveXY is also defined." }

        require(length > tolerance) { "Length must be greater than zero as well as the tolerance threshold." }
    }

    override val domain get() = curveXY.domain
    override val tolerance get() = curveXY.tolerance

    // Methods

    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Either<Exception, Vector3D> {

        val pointXY = curveXY.calculatePointGlobalCS(curveRelativePoint)
            .toResult()
            .handleFailure { throw it.error }
        val height = heightFunction.valueInFuzzy(curveRelativePoint.curvePosition, tolerance)
            .toResult()
            .handleFailure { throw it.error }
        val vector = Vector3D(pointXY.x, pointXY.y, height)
        return Either.Right(vector)
    }

    /**
     * Returns a pose at the position along the curve [curveRelativePoint].
     *
     * @param curveRelativePoint pose is calculated on the [curveRelativePoint]
     * @return pose whereby the orientation is tangential to this curve and its torsion
     */
    fun calculatePose(curveRelativePoint: CurveRelativeVector1D): Either<Exception, Pose3D> {
        this.domain.fuzzyContainsResult(curveRelativePoint.curvePosition, tolerance).toResult().handleFailure { return Either.Left(it.error) }

        val poseXY = curveXY.calculatePoseGlobalCS(curveRelativePoint)
            .toResult()
            .handleFailure { throw it.error }
        val height = heightFunction.value(curveRelativePoint.curvePosition)
            .toResult()
            .handleFailure { throw it.error }
        val torsion = torsionFunction.value(curveRelativePoint.curvePosition)
            .toResult()
            .handleFailure { throw it.error }

        val point = Vector3D(poseXY.point.x, poseXY.point.y, height)
        val rotation = Rotation3D(poseXY.rotation.angle, 0.0, torsion)

        return Either.Right(Pose3D(point, rotation))
    }

    /**
     * Returns an [Affine3D] at the position along the curve [curveRelativePoint].
     *
     * @param curveRelativePoint affine transformation matrix is calculated on the [curveRelativePoint]
     * @return affine transformation matrix whereby the orientation is tangential to this curve and its torsion
     */
    fun calculateAffine(curveRelativePoint: CurveRelativeVector1D): Either<Exception, Affine3D> =
        calculatePose(curveRelativePoint).map { Affine3D.of(it) }

    /**
     * Transforms the [curveRelativePoint] (relative to this curve) to a [Vector3D] in cartesian coordinates.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @return point in cartesian coordinates
     */
    fun transform(curveRelativePoint: CurveRelativeVector3D): Either<Exception, Vector3D> {
        val affine = calculateAffine(curveRelativePoint.toCurveRelative1D()).toResult().handleFailure { return Either.Left(it.error) }
        val vector = affine.transform(curveRelativePoint.getCartesianCurveOffset())
        return Either.Right(vector)
    }

    /**
     * Transforms the [curveRelativeLineSegment] (relative to this curve) to a [LineSegment3D] in cartesian coordinates.
     *
     * @param curveRelativeLineSegment line segment in curve relative coordinates
     * @return line segment in cartesian coordinates
     */
    fun transform(curveRelativeLineSegment: CurveRelativeLineSegment3D): Either<Exception, LineSegment3D> {
        val start = transform(curveRelativeLineSegment.start).toResult().handleFailure { return Either.Left(it.error) }
        val end = transform(curveRelativeLineSegment.end).toResult().handleFailure { return Either.Left(it.error) }
        return LineSegment3D.of(start, end, curveRelativeLineSegment.tolerance, endBoundType)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Curve3D) return false

        if (curveXY != other.curveXY) return false
        if (heightFunction != other.heightFunction) return false
        if (torsionFunction != other.torsionFunction) return false
        if (tolerance != other.tolerance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = curveXY.hashCode()
        result = 31 * result + heightFunction.hashCode()
        result = 31 * result + torsionFunction.hashCode()
        result = 31 * result + tolerance.hashCode()
        return result
    }
}
