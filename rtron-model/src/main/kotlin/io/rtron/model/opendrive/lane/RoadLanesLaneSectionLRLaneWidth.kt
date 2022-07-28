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

package io.rtron.model.opendrive.lane

import io.rtron.model.opendrive.core.OpendriveElement

class RoadLanesLaneSectionLRLaneWidth(
    var a: Double = Double.NaN,
    var b: Double = Double.NaN,
    var c: Double = Double.NaN,
    var d: Double = Double.NaN,
    var sOffset: Double = Double.NaN
) : OpendriveElement() {
    // Properties and Initializers
    val coefficients get() = doubleArrayOf(a, b, c, d)

    // Methods
    /*fun getAsResult(): Either<IllegalStateException, RoadLanesLaneSectionLRLaneWidth> {
        if (!sOffset.isFinite() || sOffset < 0.0)
            return Either.Left(IllegalStateException("Value of sOffset must be finite and positive."))
        if (coefficients.any { !it.isFinite() })
            return Either.Left(IllegalStateException("Coefficient values must be finite."))

        return Either.Right(this)
    }*/
}
