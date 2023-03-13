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

package io.rtron.model.opendrive.road.elevation

import arrow.optics.optics
import io.rtron.model.opendrive.core.OpendriveElement

@optics
data class RoadElevationProfileElevation(
    var a: Double = Double.NaN,
    var b: Double = Double.NaN,
    var c: Double = Double.NaN,
    var d: Double = Double.NaN,

    var s: Double = Double.NaN
) : OpendriveElement() {
    // Properties and Initializers
    val coefficients get() = doubleArrayOf(a, b, c, d)

    // Methods

    companion object
}
