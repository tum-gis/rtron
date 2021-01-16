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

package io.rtron.math.geometry.euclidean.threed

import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.twod.Pose2D

/**
 * A pose in 3D consists of a position and an orientation.
 * See the wikipedia article on [pose](https://en.wikipedia.org/wiki/Pose_(computer_vision)).
 *
 * @param point position in 3D
 * @param rotation orientation
 */
data class Pose3D(
    val point: Vector3D = Vector3D.ZERO,
    val rotation: Rotation3D = Rotation3D.ZERO
) {

    // Conversions
    fun toPose2D(dropAxis: Vector3D = Vector3D.Z_AXIS) =
        Pose2D(point.toVector2D(dropAxis), rotation.toRotation2D(dropAxis))

    override fun toString(): String {
        return "Pose3D(position=$point, rotation=$rotation)"
    }

    companion object {
        val ZERO = Pose3D(Vector3D.ZERO, Rotation3D.ZERO)
    }
}
