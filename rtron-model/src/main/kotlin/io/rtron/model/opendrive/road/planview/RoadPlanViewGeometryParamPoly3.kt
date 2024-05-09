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

package io.rtron.model.opendrive.road.planview

import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.model.opendrive.road.ParamPoly3PRange

data class RoadPlanViewGeometryParamPoly3(
    var aU: Double = Double.NaN,
    var aV: Double = Double.NaN,
    var bU: Double = Double.NaN,
    var bV: Double = Double.NaN,
    var cU: Double = Double.NaN,
    var cV: Double = Double.NaN,
    var dU: Double = Double.NaN,
    var dV: Double = Double.NaN,
    var pRange: ParamPoly3PRange = ParamPoly3PRange.ARC_LENGTH,
) : OpendriveElement() {
    // Properties and Initializers
    val coefficientsU get() = doubleArrayOf(aU, bU, cU, dU)
    val coefficientsV get() = doubleArrayOf(aV, bV, cV, dV)

    // Methods
    fun isNormalized() = pRange == ParamPoly3PRange.NORMALIZED
}
