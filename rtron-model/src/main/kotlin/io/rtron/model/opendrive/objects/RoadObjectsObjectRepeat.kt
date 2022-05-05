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

package io.rtron.model.opendrive.objects

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.range.Range
import io.rtron.model.opendrive.core.OpendriveElement

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
) : OpendriveElement() {

    // Methods
    fun isSetStartPoint() = !s.isNaN() && tStart.isFinite() && zOffsetStart.isFinite()

    fun isContinuous() = distance == 0.0
    fun isDiscrete() = !isContinuous()

    fun isLengthNonZero() = length.isFinite() && length != 0.0

    fun isObjectWidthZero() = (widthStart.getOrElse { Double.NaN }.isNaN() || widthStart.getOrElse { Double.NaN } == 0.0) && (widthEnd.getOrElse { Double.NaN }.isNaN() || widthEnd.getOrElse { Double.NaN } == 0.0)
    fun isObjectLengthZero() = (lengthStart.getOrElse { Double.NaN }.isNaN() || lengthStart.getOrElse { Double.NaN } == 0.0) && (lengthEnd.getOrElse { Double.NaN }.isNaN() || lengthEnd.getOrElse { Double.NaN } == 0.0)
    fun isObjectHeightZero() = (heightStart.isNaN() || heightStart == 0.0) && (heightEnd.isNaN() || heightEnd == 0.0)
    fun isObjectRadiusZero() = (radiusStart.getOrElse { Double.NaN }.isNaN() || radiusStart.getOrElse { Double.NaN } == 0.0) && (radiusEnd.getOrElse { Double.NaN }.isNaN() || radiusEnd.getOrElse { Double.NaN } == 0.0)

    fun isObjectWidthNonZero() = (!widthStart.getOrElse { Double.NaN }.isNaN() && widthStart.getOrElse { Double.NaN } != 0.0) || (!widthEnd.getOrElse { Double.NaN }.isNaN() && widthEnd.getOrElse { Double.NaN } != 0.0)
    fun isObjectLengthNonZero() = (!lengthStart.getOrElse { Double.NaN }.isNaN() && lengthStart.getOrElse { Double.NaN } != 0.0) || lengthEnd.exists { it != 0.0 } // TODO: validate that it is not 0
    fun isObjectHeightNonZero() = (!heightStart.isNaN() && heightStart != 0.0) || (!heightEnd.isNaN() && heightEnd != 0.0)
    fun isObjectRadiusNonZero() = (!radiusStart.getOrElse { Double.NaN }.isNaN() && radiusStart.getOrElse { Double.NaN } != 0.0) || (!radiusEnd.getOrElse { Double.NaN }.isNaN() && radiusEnd.getOrElse { Double.NaN } != 0.0)

    fun getRoadReferenceLineParameterSection() = Range.closed(s, s + length)

    fun getLateralOffsetFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(tStart, length, tEnd)
    fun getHeightOffsetFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(zOffsetStart, length, zOffsetEnd)

    fun getObjectWidthFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(widthStart.getOrElse { Double.NaN }, length, widthEnd.getOrElse { Double.NaN })
    fun getObjectLengthFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(lengthStart.getOrElse { Double.NaN }, length, lengthEnd.getOrElse { Double.NaN }) // TODO: adjust function
    fun getObjectHeightFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(heightStart, length, heightEnd)
    fun getObjectRadiusFunction() = LinearFunction.ofInclusiveInterceptAndPointWithoutNaN(radiusStart.getOrElse { Double.NaN }, length, radiusEnd.getOrElse { Double.NaN })

    fun isSet() = isParametricSweep() || isCurve() || isHorizontalParametricBoundedSurface() || isVerticalParametricBoundedSurface() ||
        isRepeatedCuboid() || isRepeatCylinder()

    fun isParametricSweep() = isContinuous() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectWidthNonZero() && isObjectHeightNonZero()

    fun isCurve() = isContinuous() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectWidthZero() && isObjectHeightZero()

    fun isHorizontalParametricBoundedSurface() = isContinuous() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectWidthNonZero() && isObjectHeightZero()

    fun isVerticalParametricBoundedSurface() = isContinuous() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectWidthZero() && isObjectHeightNonZero()

    fun isRepeatedCuboid() = isDiscrete() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectHeightNonZero() && isObjectLengthNonZero() && isObjectWidthNonZero()

    fun isRepeatCylinder() = isDiscrete() && isSetStartPoint() &&
        isLengthNonZero() &&
        isObjectHeightNonZero() && isObjectRadiusNonZero()
}
