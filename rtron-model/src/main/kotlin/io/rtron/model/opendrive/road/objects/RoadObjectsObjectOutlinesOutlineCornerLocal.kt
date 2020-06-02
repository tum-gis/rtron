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
import io.rtron.math.geometry.euclidean.threed.curve.LineSegment3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData
import io.rtron.std.handleFailure


class RoadObjectsObjectOutlinesOutlineCornerLocal(
        var userData: List<UserData> = listOf(),
        var include: List<Include> = listOf(),
        var dataQuality: DataQuality = DataQuality(),

        var u: Double = Double.NaN,
        var v: Double = Double.NaN,
        var z: Double = Double.NaN,
        var height: Double = Double.NaN,
        var id: Int = Int.MIN_VALUE
) {

    // Methods
    fun getBasePoint() = Vector3D.of(u, v, z)
    fun isSetBasePoint() = getBasePoint() is Result.Success
    fun getHeadPoint() = Vector3D.of(u, v, z + height)
    fun hasNonZeroHeight() = height.isFinite() && height != 0.0

    fun getVerticalBar(): Result<LineSegment3D, Exception> {
        val basePoint = getBasePoint().handleFailure { return it }
        val headPoint = getHeadPoint().handleFailure { return it }

        return Result.success(LineSegment3D(basePoint, headPoint))
    }

}
