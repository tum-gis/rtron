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

package io.rtron.model.opendrive.road.elevation

import io.rtron.model.opendrive.core.OpendriveElement

class RoadElevationProfileElevation(
    var a: Double = Double.NaN,
    var b: Double = Double.NaN,
    var c: Double = Double.NaN,
    var d: Double = Double.NaN,

    var s: Double = Double.NaN,
) : OpendriveElement() {
    // Properties and Initializers
    val coefficients get() = doubleArrayOf(a, b, c, d)

    // Methods
    fun coefficientsWithOffset(
        offsetA: Double = 0.0,
        offsetB: Double = 0.0,
        offsetC: Double = 0.0,
        offsetD: Double = 0.0
    ) = doubleArrayOf(a + offsetA, b + offsetB, c + offsetC, d + offsetD)
}
