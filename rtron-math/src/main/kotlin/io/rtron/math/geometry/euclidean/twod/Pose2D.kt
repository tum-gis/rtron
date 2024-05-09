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

package io.rtron.math.geometry.euclidean.twod

import io.rtron.math.geometry.euclidean.twod.point.Vector2D

/**
 * A pose in 2D consists of a position and an orientation.
 * See the wikipedia article on [pose](https://en.wikipedia.org/wiki/Pose_(computer_vision)).
 *
 * @param point position in 2D
 * @param rotation orientation
 */
data class Pose2D(
    val point: Vector2D,
    val rotation: Rotation2D,
) {
    // Operators
    fun fuzzyEquals(
        o: Pose2D,
        distanceTolerance: Double,
        angleTolerance: Double,
    ) = this.point.fuzzyEquals(o.point, distanceTolerance) &&
        this.rotation.fuzzyEquals(o.rotation, angleTolerance)

    fun fuzzyUnequals(
        o: Pose2D,
        distanceTolerance: Double,
        angleTolerance: Double,
    ) = !fuzzyEquals(o, distanceTolerance, angleTolerance)

    companion object {
        val ZERO = Pose2D(Vector2D.ZERO, Rotation2D.ZERO)
    }
}
