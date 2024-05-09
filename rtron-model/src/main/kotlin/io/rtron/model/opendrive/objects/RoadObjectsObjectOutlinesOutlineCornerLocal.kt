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

package io.rtron.model.opendrive.objects

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.model.opendrive.core.OpendriveElement

data class RoadObjectsObjectOutlinesOutlineCornerLocal(
    var height: Double = Double.NaN,
    var id: Option<Int> = None,
    var u: Double = Double.NaN,
    var v: Double = Double.NaN,
    var z: Double = Double.NaN,
) : OpendriveElement() {
    // Methods
    fun hasZeroHeight(): Boolean = !height.isFinite() || height == 0.0

    fun hasPositiveHeight(): Boolean = !hasZeroHeight() && height > 0.0

    fun getBasePoint() = Vector3D(u, v, z)

    fun getHeadPoint(): Option<Vector3D> =
        if (hasZeroHeight()) {
            None
        } else {
            Vector3D(u, v, z + height).some()
        }

    fun getPoints(): Pair<Vector3D, Option<Vector3D>> = Pair(getBasePoint(), getHeadPoint())
}
