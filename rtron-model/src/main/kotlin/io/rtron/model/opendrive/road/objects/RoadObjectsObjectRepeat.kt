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

package io.rtron.model.opendrive.road.objects

import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.range.Range
import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData

data class RoadObjectsObjectRepeat(
    var userData: List<UserData> = listOf(),
    var include: List<Include> = listOf(),
    var dataQuality: DataQuality = DataQuality(),

    var s: Double = Double.NaN,
    var length: Double = Double.NaN,
    var distance: Double = Double.NaN,
    var tStart: Double = Double.NaN,
    var tEnd: Double = Double.NaN,
    var heightStart: Double = Double.NaN,
    var heightEnd: Double = Double.NaN,
    var zOffsetStart: Double = Double.NaN,
    var zOffsetEnd: Double = Double.NaN,
    var widthStart: Double = Double.NaN,
    var widthEnd: Double = Double.NaN,

    // variant 1
    var lengthStart: Double = Double.NaN,
    var lengthEnd: Double = Double.NaN,

    // variant 2
    var radiusStart: Double = Double.NaN,
    var radiusEnd: Double = Double.NaN
) {

    // Methods
    fun isSetStartPoint() = !s.isNaN() && tStart.isFinite() && zOffsetStart.isFinite()

    fun isContinuous() = distance == 0.0
    fun isDiscrete() = !isContinuous()

    fun isLengthNonZero() = length.isFinite() && length != 0.0

    fun isObjectWidthZero() = (widthStart.isNaN() || widthStart == 0.0) && (widthEnd.isNaN() || widthEnd == 0.0)
    fun isObjectLengthZero() = (lengthStart.isNaN() || lengthStart == 0.0) && (lengthEnd.isNaN() || lengthEnd == 0.0)
    fun isObjectHeightZero() = (heightStart.isNaN() || heightStart == 0.0) && (heightEnd.isNaN() || heightEnd == 0.0)
    fun isObjectRadiusZero() = (radiusStart.isNaN() || radiusStart == 0.0) && (radiusEnd.isNaN() || radiusEnd == 0.0)

    fun isObjectWidthNonZero() = (!widthStart.isNaN() && widthStart != 0.0) || (!widthEnd.isNaN() && widthEnd != 0.0)
    fun isObjectLengthNonZero() = (!lengthStart.isNaN() && lengthStart != 0.0) || (!lengthEnd.isNaN() && lengthEnd != 0.0)
    fun isObjectHeightNonZero() = (!heightStart.isNaN() && heightStart != 0.0) || (!heightEnd.isNaN() && heightEnd != 0.0)
    fun isObjectRadiusNonZero() = (!radiusStart.isNaN() && radiusStart != 0.0) || (!radiusEnd.isNaN() && radiusEnd != 0.0)

    fun getRoadReferenceLineParameterSection() = Range.closed(s, s + length)

    fun getLateralOffsetFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(tStart, length, tEnd)
    fun getHeightOffsetFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(zOffsetStart, length, zOffsetEnd)

    fun getObjectWidthFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(widthStart, length, widthEnd)
    fun getObjectLengthFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(lengthStart, length, lengthEnd)
    fun getObjectHeightFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(heightStart, length, heightEnd)
    fun getObjectRadiusFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(radiusStart, length, radiusEnd)

    fun isSet() = isParametricSweep() || isCurve() || isHorizontalLinearRing() || isVerticalLinearRing() ||
        isRepeatedCuboid() || isRepeatCylinder()

    fun isParametricSweep() = isContinuous() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectWidthNonZero() && isObjectHeightNonZero()

    fun isCurve() = isContinuous() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectWidthZero() && isObjectHeightZero()

    fun isHorizontalLinearRing() = isContinuous() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectWidthNonZero() && isObjectHeightZero()

    fun isVerticalLinearRing() = isContinuous() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectWidthZero() && isObjectHeightNonZero()

    fun isRepeatedCuboid() = isDiscrete() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectHeightNonZero() && isObjectLengthNonZero() && isObjectWidthNonZero()

    fun isRepeatCylinder() = isDiscrete() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectHeightNonZero() && isObjectRadiusNonZero()
}
