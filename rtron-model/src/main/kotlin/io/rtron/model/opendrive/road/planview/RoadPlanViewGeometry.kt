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

package io.rtron.model.opendrive.road.planview

import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData

data class RoadPlanViewGeometry(
    var line: RoadPlanViewGeometryLine = RoadPlanViewGeometryLine(),
    var spiral: RoadPlanViewGeometrySpiral = RoadPlanViewGeometrySpiral(),
    var arc: RoadPlanViewGeometryArc = RoadPlanViewGeometryArc(),
    var poly3: RoadPlanViewGeometryPoly3 = RoadPlanViewGeometryPoly3(),
    var paramPoly3: RoadPlanViewGeometryParamPoly3 = RoadPlanViewGeometryParamPoly3(),

    var userData: List<UserData> = listOf(),
    var include: List<Include> = listOf(),
    var dataQuality: DataQuality = DataQuality(),

    var s: Double = Double.NaN,
    var x: Double = Double.NaN,
    var y: Double = Double.NaN,
    var hdg: Double = Double.NaN,
    var length: Double = Double.NaN
) {

    // Methods
    fun isLine() = !line.isNaN()
    fun isSpiral() = !spiral.isNaN()
    fun isArc() = !arc.isNaN()
    fun isPoly3() = !poly3.isNaN()
    fun isParamPoly3() = !paramPoly3.isNaN()
}
