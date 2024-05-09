/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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
import arrow.core.left
import arrow.core.right
import io.rtron.math.geometry.GeometryException
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.twod.AbstractGeometry2D
import io.rtron.math.geometry.euclidean.twod.Pose2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.BoundType
import io.rtron.math.range.DefinableDomain
import io.rtron.math.range.Tolerable
import io.rtron.math.range.fuzzyContains
import io.rtron.math.range.length

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
    fun calculatePointLocalCS(curveRelativePoint: CurveRelativeVector1D): Either<GeometryException.ValueNotContainedInDomain, Vector2D> {
        if (!domain.fuzzyContains(curveRelativePoint.curvePosition, tolerance)) {
            return GeometryException.ValueNotContainedInDomain(curveRelativePoint.curvePosition).left()
        }

        return calculatePointLocalCSUnbounded(curveRelativePoint).right()
    }

    /**
     * Returns the point in the cartesian coordinate system that is located on this curve and given by a point in the
     * curve relative coordinate system.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @return point in cartesian coordinates
     */
    protected abstract fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Vector2D

    /**
     * Returns the orientation in the local cartesian coordinate system that is tangential to this curve at a given
     * point which is given in a curve relative coordinate system.
     * An error is returned, if the requested point is not within this curve's domain.
     *
     * @param curveRelativePoint point in curve relative coordinates for which the orientation is to be calculated
     * @return orientation tangential to this curve
     */
    fun calculateRotationLocalCS(
        curveRelativePoint: CurveRelativeVector1D,
    ): Either<GeometryException.ValueNotContainedInDomain, Rotation2D> {
        if (!domain.fuzzyContains(curveRelativePoint.curvePosition, tolerance)) {
            return GeometryException.ValueNotContainedInDomain(curveRelativePoint.curvePosition).left()
        }

        return calculateRotationLocalCSUnbounded(curveRelativePoint).right()
    }

    /**
     * Returns the orientation in the local cartesian coordinate system that is tangential to this curve at a given
     * point which is given in a curve relative coordinate system.
     *
     * @param curveRelativePoint point in curve relative coordinates for which the orientation is to be calculated
     * @return orientation tangential to this curve
     */
    protected abstract fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Rotation2D

    /**
     * Returns the global point in the global cartesian coordinate system that is located on this curve and given by a
     * point in the curve relative coordinate system.
     * An error is returned, if the requested point is not within this curve's domain.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @return point in cartesian coordinates
     */
    fun calculatePointGlobalCS(curveRelativePoint: CurveRelativeVector1D): Either<GeometryException.ValueNotContainedInDomain, Vector2D> =
        calculatePointLocalCS(curveRelativePoint).map { affineSequence.solve().transform(it) }

    /**
     * Returns the global point in the global cartesian coordinate system that is located on this curve and given by a
     * point in the curve relative coordinate system.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @return point in cartesian coordinates
     */
    fun calculatePointGlobalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Vector2D {
        return calculatePointLocalCSUnbounded(curveRelativePoint).let { affineSequence.solve().transform(it) }
    }

    /**
     * Returns the orientation in the global cartesian coordinate system that is tangential to this curve at a given
     * point which is given in a curve relative coordinate system.
     *
     * @param curveRelativePoint point in curve relative coordinates for which the orientation is to be calculated
     * @return orientation tangential to this curve
     */
    fun calculateRotationGlobalCS(
        curveRelativePoint: CurveRelativeVector1D,
    ): Either<GeometryException.ValueNotContainedInDomain, Rotation2D> {
        if (!domain.fuzzyContains(curveRelativePoint.curvePosition, tolerance)) {
            return GeometryException.ValueNotContainedInDomain(curveRelativePoint.curvePosition).left()
        }

        return calculateRotationGlobalCSUnbounded(curveRelativePoint).right()
    }

    /**
     * Returns the orientation in the global cartesian coordinate system that is tangential to this curve at a given
     * point which is given in a curve relative coordinate system.
     * An error is returned, if the requested point is not within this curve's domain.
     *
     * @param curveRelativePoint point in curve relative coordinates for which the orientation is to be calculated
     * @return orientation tangential to this curve
     */
    fun calculateRotationGlobalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Rotation2D {
        return calculateRotationLocalCSUnbounded(curveRelativePoint).let { affineSequence.solve().transform(it) }
    }

    /**
     * Returns a pose at the position along the curve [curveRelativePoint].
     *
     * @param curveRelativePoint pose is calculated on the [curveRelativePoint]
     * @return pose whereby the orientation is tangential to this curve
     */
    fun calculatePoseGlobalCS(curveRelativePoint: CurveRelativeVector1D): Either<GeometryException.ValueNotContainedInDomain, Pose2D> {
        if (!domain.fuzzyContains(curveRelativePoint.curvePosition, tolerance)) {
            return GeometryException.ValueNotContainedInDomain(curveRelativePoint.curvePosition).left()
        }

        return calculatePoseGlobalCSUnbounded(curveRelativePoint).right()
    }

    /**
     * Returns a pose at the position along the curve [curveRelativePoint].
     * An error is returned, if the requested point is not within this curve's domain.
     *
     * @param curveRelativePoint pose is calculated on the [curveRelativePoint]
     * @return pose whereby the orientation is tangential to this curve
     */
    fun calculatePoseGlobalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Pose2D {
        val point = calculatePointGlobalCSUnbounded(curveRelativePoint)
        val rotation = calculateRotationGlobalCSUnbounded(curveRelativePoint)
        return Pose2D(point, rotation)
    }
}
