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
import arrow.core.some
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.model.opendrive.core.OpendriveElement

class RoadObjectsObjectOutlinesOutlineCornerRoad(
    var dz: Double = Double.NaN,
    var height: Double = Double.NaN,
    var id: Option<Int> = None,
    var s: Double = Double.NaN,
    var t: Double = Double.NaN
) : OpendriveElement() {
    // Properties and Initializers
    val curveRelativePosition get() = CurveRelativeVector1D(s)

    // Methods
    fun hasZeroHeight(): Boolean = !height.isFinite() || height == 0.0
    fun hasPositiveHeight(): Boolean = !hasZeroHeight() && height > 0.0

    fun getBasePoint() = CurveRelativeVector3D(s, t, dz)
    fun getHeadPoint(): Option<CurveRelativeVector3D> =
        if (hasZeroHeight()) None
        else CurveRelativeVector3D(s, t, dz + height).some()

    fun getPoints(): Pair<CurveRelativeVector3D, Option<CurveRelativeVector3D>> = Pair(getBasePoint(), getHeadPoint())
}
