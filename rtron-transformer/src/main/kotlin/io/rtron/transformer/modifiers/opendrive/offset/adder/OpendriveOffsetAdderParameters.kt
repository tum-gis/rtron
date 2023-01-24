/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.modifiers.opendrive.offset.adder

import kotlinx.serialization.Serializable

@Serializable
data class OpendriveOffsetAdderParameters(
    /** offset by which the model is translated along the x-axis */
    val offsetX: Double,
    /** offset by which the model is translated along the y-axis */
    val offsetY: Double,
    /** offset by which the model is translated along the z-axis */
    val offsetZ: Double,
    /** offset by which the model is rotated */
    val offsetHeading: Double,
) {
    fun isZeroOffset() = offsetX == 0.0 && offsetY == 0.0 && offsetZ == 0.0 && offsetHeading == 0.0

    companion object {
        const val DEFAULT_OFFSET_X = 0.0
        const val DEFAULT_OFFSET_Y = 0.0
        const val DEFAULT_OFFSET_Z = 0.0
        const val DEFAULT_OFFSET_HEADING = 0.0
    }
}
