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

package io.rtron.transformer.opendrive2roadspaces.roadspaces

import io.rtron.model.opendrive.common.EUnitSpeed
import io.rtron.model.roadspaces.roadspace.attribute.UnitOfMeasure


/**
 * Transforms units of the OpenDRIVE data model to units of the RoadSpaces data model.
 */
fun EUnitSpeed.toUnitOfMeasure(): UnitOfMeasure = when (this) {
    EUnitSpeed.METER_PER_SECOND -> UnitOfMeasure.METER_PER_SECOND
    EUnitSpeed.MILES_PER_HOUR -> UnitOfMeasure.MILES_PER_HOUR
    EUnitSpeed.KILOMETER_PER_HOUR -> UnitOfMeasure.KILOMETER_PER_HOUR
    EUnitSpeed.UNKNOWN -> UnitOfMeasure.UNKNOWN
}
