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

package io.rtron.math.geometry.euclidean.twod.curve

import arrow.core.Either
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.AbstractGeometry2D
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.BoundType
import io.rtron.math.range.DefinableDomain
import io.rtron.math.range.Tolerable
import io.rtron.math.range.fuzzyContainsResult
import io.rtron.math.range.length
import io.rtron.std.handleFailure
import io.rtron.std.toResult

/**
 * Abstract class for all geometric curve objects in 2D.
 */
abstract class AbstractCurve2D : AbstractGeometry2D(), DefinableDomain<Double>, Tolerable {

    // Properties

    /** length of the curve */
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
    fun calculatePointLocalCS(curveRelativePoint: CurveRelativeVector1D): Either<Exception, Vector2D> {

        this.domain.fuzzyContainsResult(curveRelativePoint.curvePosition, tolerance).toResult().handleFailure { return Either.Left(it.error) }
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
        Either<Exception, Vector2D>

    /**
     * Returns the orientation in the local cartesian coordinate system that is tangential to this curve at a given
     * point which is given in a curve relative coordinate system.
     * An error is returned, if the requested point is not within this curve's domain.
     *
     * @param curveRelativePoint point in curve relative coordinates for which the orientation is to be calculated
     * @return orientation tangential to this curve
     */
    fun calculateRotationLocalCS(curveRelativePoint: CurveRelativeVector1D): Either<Exception, Rotation2D> {

        this.domain.fuzzyContainsResult(curveRelativePoint.curvePosition, tolerance).toResult().handleFailure { return Either.Left(it.error) }
        return calculateRotationLocalCSUnbounded(curveRelativePoint)
    }

    /**
     * Returns the orientation in the local cartesian coordinate system that is tangential to this curve at a given
     * point which is given in a curve relative coordinate system.
     *
     * @param curveRelativePoint point in curve relative coordinates for which the orientation is to be calculated
     * @return orientation tangential to this curve
     */
    protected abstract fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D):
        Either<Exception, Rotation2D>

    /**
     * Returns the global point in the global cartesian coordinate system that is located on this curve and given by a
     * point in the curve relative coordinate system.
     * An error is returned, if the requested point is not within this curve's domain.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @return point in cartesian coordinates
     */
    fun calculatePointGlobalCS(curveRelativePoint: CurveRelativeVector1D): Either<Exception, Vector2D> =
        calculatePointLocalCS(curveRelativePoint).map { affineSequence.solve().transform(it) }

    /**
     * Returns the orientation in the global cartesian coordinate system that is tangential to this curve at a given
     * point which is given in a curve relative coordinate system.
     *
     * @param curveRelativePoint point in curve relative coordinates for which the orientation is to be calculated
     * @return orientation tangential to this curve
     */
    fun calculateRotationGlobalCS(curveRelativePoint: CurveRelativeVector1D): Either<Exception, Rotation2D> =
        calculateRotationLocalCS(curveRelativePoint).map { affineSequence.solve().transform(it) }

    /**
     * Returns a pose at the position along the curve [curveRelativePoint].
     *
     * @param curveRelativePoint pose is calculated on the [curveRelativePoint]
     * @return pose whereby the orientation is tangential to this curve
     */
    fun calculatePoseGlobalCS(curveRelativePoint: CurveRelativeVector1D): Either<Exception, Pose2D> {
        val point = calculatePointGlobalCS(curveRelativePoint).toResult().handleFailure { return Either.Left(it.error) }
        val rotation = calculateRotationGlobalCS(curveRelativePoint).toResult().handleFailure { return Either.Left(it.error) }
        return Either.Right(Pose2D(point, rotation))
    }
}
