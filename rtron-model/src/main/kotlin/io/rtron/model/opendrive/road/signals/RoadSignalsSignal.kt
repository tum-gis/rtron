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

package io.rtron.model.opendrive.road.signals

import io.rtron.math.geometry.curved.threed.point.CurveRelativePoint3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.model.opendrive.common.*
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectLaneValidity


data class RoadSignalsSignal(
        var validity: List<RoadObjectsObjectLaneValidity> = listOf(),
        var dependency: List<RoadSignalsSignalDependency> = listOf(),
        var reference: List<RoadSignalsSignalReference> = listOf(),

        var positionRoad: RoadSignalsSignalPositionRoad = RoadSignalsSignalPositionRoad(),
        var positionInertial: RoadSignalsSignalPositionInertial = RoadSignalsSignalPositionInertial(),

        var userData: List<UserData> = listOf(),
        var include: List<Include> = listOf(),
        var dataQuality: DataQuality = DataQuality(),

        var s: Double = Double.NaN,
        var t: Double = Double.NaN,
        var id: String = "",
        var name: String = "",
        var dynamic: Boolean = false,
        var orientation: EOrientation = EOrientation.NONE,
        var zOffset: Double = Double.NaN,
        var countryCode: CountryCode = CountryCode(),
        var countryRevision: String = "",
        var type: String = "",
        var subtype: String = "",
        var value: Double = Double.NaN,
        var unit: EUnit = EUnit.UNKNOWN,
        var height: Double = Double.NaN,
        var width: Double = Double.NaN,
        var text: String = "",
        var hOffset: Double = Double.NaN,
        var pitch: Double = Double.NaN,
        var roll: Double = Double.NaN
) {
    // Properties and Initializers
    val curveRelativePosition get() = CurveRelativePoint3D(s, t, zOffset)

    /** Position of the object relative to the point on the road reference line */
    val referenceLinePointRelativePosition get() = Vector3D(0.0, t, zOffset)

    // Methods
    fun isPolygon() = !width.isNaN() && width != 0.0 && !height.isNaN() && height != 0.0
    fun isVerticalLine() = (width.isNaN() || width == 0.0) && !height.isNaN() && height != 0.0
    fun isHorizontalLine() = !width.isNaN() && width != 0.0 && (height.isNaN() || height == 0.0)
    fun isPoint() = !isPolygon() && !isVerticalLine() && !isHorizontalLine()
}
