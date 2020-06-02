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

package io.rtron.model.opendrive.road.objects

import io.rtron.model.opendrive.common.*


data class RoadObjectsObjectMarkingsMarking(
        var cornerReference: List<RoadObjectsObjectMarkingsMarkingCornerReference> = listOf(),

        var userData: List<UserData> = listOf(),
        var include: List<Include> = listOf(),
        var dataQuality: DataQuality = DataQuality(),

        var side: ESideType = ESideType.UNKNOWN,
        var weight: ERoadMarkWeight = ERoadMarkWeight.STANDARD,
        var width: Double = Double.NaN,
        var color: ERoadMarkColor = ERoadMarkColor.STANDARD,
        var zOffset: Double = Double.NaN,
        var spaceLength: Double = Double.NaN,
        var lineLength: Double = Double.NaN,
        var startOffset: Double = Double.NaN,
        var stopOffset: Double = Double.NaN
)
