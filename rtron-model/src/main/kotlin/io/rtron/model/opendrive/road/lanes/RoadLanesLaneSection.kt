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

package io.rtron.model.opendrive.road.lanes

import arrow.core.Either
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData
import io.rtron.std.ContextMessage

data class RoadLanesLaneSection(

    var left: RoadLanesLaneSectionLeft = RoadLanesLaneSectionLeft(),
    var center: RoadLanesLaneSectionCenter = RoadLanesLaneSectionCenter(),
    var right: RoadLanesLaneSectionRight = RoadLanesLaneSectionRight(),

    var userData: List<UserData> = listOf(),
    var include: List<Include> = listOf(),
    var dataQuality: DataQuality = DataQuality(),

    var s: Double = Double.NaN,
    var singleSide: Boolean = false
) {
    // Properties and Initializers
    val laneSectionStart get() = CurveRelativeVector1D(s)

    // Methods
    fun getNumberOfLanes() = left.getNumberOfLanes() + center.getNumberOfLanes() + right.getNumberOfLanes()

    fun getCenterLane() = center.lane.first()
    fun getLeftRightLanes(): Map<Int, RoadLanesLaneSectionLRLane> = left.getLanes() + right.getLanes()

    fun isProcessable(): Either<IllegalStateException, ContextMessage<Unit>> {

        if (center.getNumberOfLanes() != 1)
            return Either.Left(IllegalStateException("Lane section should contain exactly one center lane."))

        if (left.isEmpty() && right.isEmpty())
            return Either.Left(IllegalStateException("Lane section should contain lanes at least on the left or on the right side."))

        val infos = mutableListOf<String>()

        if (left.isNotEmpty()) {
            val leftLaneIds = left.lane.map { it.id }
            val expectedIds = (left.getNumberOfLanes() downTo 1).toList()

            if (!leftLaneIds.containsAll(expectedIds))
                return Either.Left(IllegalStateException("Left lanes have missing IDs."))
            if (leftLaneIds != leftLaneIds.sortedDescending())
                infos += "Left lanes should be ordered in a descending manner."
        }

        if (right.isNotEmpty()) {
            val rightLaneIds = right.lane.map { it.id }
            val expectedIds = (-1 downTo -right.getNumberOfLanes()).toList()

            if (!rightLaneIds.containsAll(expectedIds))
                return Either.Left(IllegalStateException("Right lanes have missing IDs."))
            if (rightLaneIds != rightLaneIds.sortedDescending())
                infos += "Right lanes should be ordered in a descending manner."
        }

        return Either.Right(ContextMessage(Unit, infos))
    }
}
