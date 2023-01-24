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

package io.rtron.model.opendrive.road

import arrow.core.None
import arrow.core.Option
import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.model.opendrive.junction.ERoadSurfaceCrgMode
import io.rtron.model.opendrive.junction.ERoadSurfaceCrgPurpose

data class RoadSurfaceCrg(
    var file: String = "",
    var hOffset: Option<Double> = None,
    var mode: ERoadSurfaceCrgMode = ERoadSurfaceCrgMode.ATTACHED,
    var orientation: EDirection = EDirection.SAME,
    var purpose: Option<ERoadSurfaceCrgPurpose> = None,
    var sEnd: Double = Double.NaN,
    var sOffset: Option<Double> = None,
    var sStart: Double = Double.NaN,
    var tOffset: Option<Double> = None,
    var zOffset: Option<Double> = None,
    var zScale: Option<Double> = None
) : OpendriveElement()
