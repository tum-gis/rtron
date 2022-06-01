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
import arrow.optics.optics
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
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

    // Methods
    fun isContinuous() = distance == 0.0
    fun isDiscrete() = !isContinuous()

    fun isLengthNonZero() = length != 0.0

    fun isObjectWidthZero() = widthStart.isEmpty() && widthEnd.isEmpty()
    fun isObjectLengthZero() = lengthStart.isEmpty() && lengthEnd.isEmpty()
    fun isObjectHeightZero() = heightStart == 0.0 && heightEnd == 0.0
    fun isObjectRadiusZero() = radiusStart.isEmpty() && radiusEnd.isEmpty()

    fun isObjectWidthNonZero() = widthStart.isDefined() || widthEnd.isDefined()
    fun isObjectLengthNonZero() = lengthStart.isDefined() || lengthEnd.isDefined()
    fun isObjectHeightNonZero() = heightStart != 0.0 || heightEnd != 0.0
    fun isObjectRadiusNonZero() = radiusStart.isDefined() || radiusEnd.isDefined()

    fun getRoadReferenceLineParameterSection() = Range.closed(s, s + length)

    fun getLateralOffsetFunction() = LinearFunction.ofInclusiveInterceptAndPoint(tStart, length, tEnd)
    fun getHeightOffsetFunction() = LinearFunction.ofInclusiveInterceptAndPoint(zOffsetStart, length, zOffsetEnd)

    fun getObjectWidthFunction() = LinearFunction.ofInclusiveInterceptAndPoint(widthStart, length, widthEnd)
    fun getObjectLengthFunction() = LinearFunction.ofInclusiveInterceptAndPoint(lengthStart, length, lengthEnd)
    fun getObjectHeightFunction() = LinearFunction.ofInclusiveInterceptAndPoint(heightStart, length, heightEnd)
    fun getObjectRadiusFunction() = LinearFunction.ofInclusiveInterceptAndPoint(radiusStart, length, radiusEnd)

    fun isSet() = isParametricSweep() || isCurve() || isHorizontalParametricBoundedSurface() || isVerticalParametricBoundedSurface() ||
        isRepeatedCuboid() || isRepeatCylinder()

    fun isParametricSweep() = isContinuous() &&
        isLengthNonZero() &&
        isObjectWidthNonZero() && isObjectHeightNonZero()

    fun isCurve() = isContinuous() &&
        isLengthNonZero() &&
        isObjectWidthZero() && isObjectHeightZero()

    fun isHorizontalParametricBoundedSurface() = isContinuous() &&
        isLengthNonZero() &&
        isObjectWidthNonZero() && isObjectHeightZero()

    fun isVerticalParametricBoundedSurface() = isContinuous() &&
        isLengthNonZero() &&
        isObjectWidthZero() && isObjectHeightNonZero()

    fun isRepeatedCuboid() = isDiscrete() &&
        isLengthNonZero() &&
        isObjectHeightNonZero() && isObjectLengthNonZero() && isObjectWidthNonZero()

    fun isRepeatCylinder() = isDiscrete() &&
        isLengthNonZero() &&
        isObjectHeightNonZero() && isObjectRadiusNonZero()

    companion object
}
