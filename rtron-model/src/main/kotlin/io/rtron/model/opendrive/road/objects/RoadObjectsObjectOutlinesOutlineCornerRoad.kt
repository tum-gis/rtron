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

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData
import io.rtron.std.Optional

class RoadObjectsObjectOutlinesOutlineCornerRoad(
    var userData: List<UserData> = listOf(),
    var include: List<Include> = listOf(),
    var dataQuality: DataQuality = DataQuality(),

    var s: Double = Double.NaN,
    var t: Double = Double.NaN,
    var dz: Double = Double.NaN,
    var height: Double = Double.NaN,
    var id: Int = Int.MIN_VALUE
) {
    // Properties and Initializers
    val curveRelativePosition get() = CurveRelativeVector1D(s)

    // Methods
    fun hasZeroHeight(): Boolean = !height.isFinite() || height == 0.0
    fun hasPositiveHeight(): Boolean = !hasZeroHeight() && height > 0.0

    fun getBasePoint() = CurveRelativeVector3D.of(s, t, dz)
    fun isSetBasePoint(): Boolean = getBasePoint() is Result.Success
    fun getHeadPoint(): Optional<CurveRelativeVector3D> =
        if (hasZeroHeight()) Optional.empty()
        else Optional.of(CurveRelativeVector3D.of(s, t, dz + height))

    fun getPoints(): Result<Pair<CurveRelativeVector3D, Optional<CurveRelativeVector3D>>, Exception> =
        getBasePoint().map { Pair(it, getHeadPoint()) }
}
