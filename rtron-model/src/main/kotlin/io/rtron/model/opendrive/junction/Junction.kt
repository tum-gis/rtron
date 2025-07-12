/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.model.opendrive.junction

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toNonEmptyListOrNull
import arrow.optics.optics
import io.rtron.model.opendrive.additions.identifier.AdditionalJunctionIdentifier
import io.rtron.model.opendrive.additions.identifier.JunctionIdentifier
import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.model.opendrive.objects.EOrientation
import io.rtron.model.opendrive.road.RoadSurface

@optics
data class Junction(
    var connection: List<JunctionConnection> = emptyList(),
    var priority: List<JunctionPriority> = emptyList(),
    var controller: List<JunctionController> = emptyList(),
    var surface: Option<RoadSurface> = None,
    var id: String = "",
    var mainRoad: Option<String> = None,
    var name: Option<String> = None,
    var orientation: Option<EOrientation> = None,
    var sEnd: Option<Double> = None,
    var sStart: Option<Double> = None,
    var type: Option<EJunctionType> = None,
    override var additionalId: Option<JunctionIdentifier> = None,
) : OpendriveElement(),
    AdditionalJunctionIdentifier {
    // Properties and Initializers
    val connectionAsNonEmptyList: NonEmptyList<JunctionConnection>
        get() = connection.toNonEmptyListOrNull()!!

    val typeValidated: EJunctionType
        get() = type.getOrElse { EJunctionType.DEFAULT }

    // Methods
    fun getConnectingRoadIds(): Set<String> = connection.flatMap { it.incomingRoad.toList() }.toSet()

    fun getIncomingRoadIds(): Set<String> = connection.flatMap { it.incomingRoad.toList() }.toSet()

    fun getNumberOfIncomingRoads(): Int = getIncomingRoadIds().size

    companion object
}
