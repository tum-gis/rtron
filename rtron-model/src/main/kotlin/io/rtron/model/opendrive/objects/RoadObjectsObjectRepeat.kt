/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.model.opendrive.objects

import arrow.core.None
import arrow.core.Option
import arrow.optics.optics
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.range.Range
import io.rtron.model.opendrive.additions.identifier.AdditionalRoadObjectRepeatIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadObjectRepeatIdentifier
import io.rtron.model.opendrive.core.OpendriveElement

@optics
data class RoadObjectsObjectRepeat(
    var distance: Double = Double.NaN,
    var heightEnd: Double = Double.NaN,
    var heightStart: Double = Double.NaN,
    var length: Double = Double.NaN,
    var lengthEnd: Option<Double> = None,
    var lengthStart: Option<Double> = None,
    var radiusEnd: Option<Double> = None,
    var radiusStart: Option<Double> = None,
    var s: Double = Double.NaN,
    var tEnd: Double = Double.NaN,
    var tStart: Double = Double.NaN,
    var widthEnd: Option<Double> = None,
    var widthStart: Option<Double> = None,
    var zOffsetEnd: Double = Double.NaN,
    var zOffsetStart: Double = Double.NaN,

    override var additionalId: Option<RoadObjectRepeatIdentifier> = None
) : OpendriveElement(), AdditionalRoadObjectRepeatIdentifier {

    // Properties and Initializers
    val curveRelativeStartPosition get() = CurveRelativeVector1D(s)

    /** position of the object relative to the point on the road reference line */
    val referenceLinePointRelativePosition get() = Vector3D(0.0, tStart, zOffsetStart)

    // Methods
    fun isContinuous() = distance == 0.0
    fun isDiscrete() = !isContinuous()

    fun isLengthNonZero() = length != 0.0

    fun isObjectWidthZero() = widthStart.isNone() && widthEnd.isNone()
    fun isObjectLengthZero() = lengthStart.isNone() && lengthEnd.isNone()
    fun isObjectHeightZero() = heightStart == 0.0 && heightEnd == 0.0
    fun isObjectRadiusZero() = radiusStart.isNone() && radiusEnd.isNone()

    fun isObjectWidthNonZero() = widthStart.isSome() || widthEnd.isSome()
    fun isObjectLengthNonZero() = lengthStart.isSome() || lengthEnd.isSome()
    fun isObjectHeightNonZero() = heightStart != 0.0 || heightEnd != 0.0
    fun isObjectRadiusNonZero() = radiusStart.isSome() || radiusEnd.isSome()

    fun getRoadReferenceLineParameterSection() = Range.closed(s, s + length)

    fun getLateralOffsetFunction() = LinearFunction.ofInclusiveInterceptAndPoint(tStart, length, tEnd)
    fun getHeightOffsetFunction() = LinearFunction.ofInclusiveInterceptAndPoint(zOffsetStart, length, zOffsetEnd)

    fun getObjectWidthFunction() = LinearFunction.ofInclusiveInterceptAndPoint(widthStart, length, widthEnd)
    fun getObjectLengthFunction() = LinearFunction.ofInclusiveInterceptAndPoint(lengthStart, length, lengthEnd)
    fun getObjectHeightFunction() = LinearFunction.ofInclusiveInterceptAndPoint(heightStart, length, heightEnd)
    fun getObjectRadiusFunction() = LinearFunction.ofInclusiveInterceptAndPoint(radiusStart, length, radiusEnd)

    fun containsParametricSweep() = isContinuous() &&
        isLengthNonZero() &&
        isObjectWidthNonZero() && isObjectHeightNonZero()

    fun containsCurve() = isContinuous() &&
        isLengthNonZero() &&
        isObjectWidthZero() && isObjectHeightZero()

    fun containsHorizontalParametricBoundedSurface() = isContinuous() &&
        isLengthNonZero() &&
        isObjectWidthNonZero() && isObjectHeightZero()

    fun containsVerticalParametricBoundedSurface() = isContinuous() &&
        isLengthNonZero() &&
        isObjectWidthZero() && isObjectHeightNonZero()

    fun containsRepeatedCuboid() = isDiscrete() &&
        isLengthNonZero() &&
        isObjectHeightNonZero() && isObjectLengthNonZero() && isObjectWidthNonZero()

    fun containsRepeatedRectangle() = isDiscrete() &&
        isLengthNonZero() &&
        isObjectHeightZero() && isObjectLengthNonZero() && isObjectWidthNonZero()

    fun containsRepeatCylinder() = isDiscrete() &&
        isLengthNonZero() &&
        isObjectHeightNonZero() && isObjectRadiusNonZero()

    fun containsRepeatCircle() = isDiscrete() &&
        isLengthNonZero() &&
        isObjectHeightNonZero() && isObjectRadiusNonZero()

    companion object
}
