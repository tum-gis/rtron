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

import arrow.core.None
import arrow.core.Option
import arrow.optics.optics
import io.rtron.model.opendrive.core.OpendriveElement

@optics
data class RoadPlanViewGeometry(
    var line: Option<RoadPlanViewGeometryLine> = None,
    var spiral: Option<RoadPlanViewGeometrySpiral> = None,
    var arc: Option<RoadPlanViewGeometryArc> = None,
    var poly3: Option<RoadPlanViewGeometryPoly3> = None,
    var paramPoly3: Option<RoadPlanViewGeometryParamPoly3> = None,

    var hdg: Double = Double.NaN,
    var length: Double = Double.NaN,
    var s: Double = Double.NaN,
    var x: Double = Double.NaN,
    var y: Double = Double.NaN
) : OpendriveElement() {

    companion object
}
