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

package io.rtron.readerwriter.opendrive.reader.mapper.opendrive14

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.model.opendrive.core.EUnit
import io.rtron.model.opendrive.core.EUnitSpeed
import io.rtron.model.opendrive.core.HeaderGeoReference
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import org.asam.opendrive14.Dynamic
import org.asam.opendrive14.Unit
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    uses = [OpendriveCommonMapper::class],
    imports = [Option::class],
)
abstract class Opendrive14CoreMapper {
    //
    // Header
    //
    fun mapHeaderGeoreference(source: String): Option<HeaderGeoReference> =
        if (source.isBlank()) {
            None
        } else {
            HeaderGeoReference(content = source).some()
        }

    //
    // Enumerations
    //
    fun mapUnitToOptionEUnitSpeed(source: Unit?): Option<EUnitSpeed> = source?.let { mapUnit(it).some() } ?: None

    fun mapUnit(source: Unit): EUnitSpeed =
        when (source) {
            Unit.M___S -> EUnitSpeed.METER_PER_SECOND
            Unit.MPH -> EUnitSpeed.MILES_PER_HOUR
            Unit.KM___H -> EUnitSpeed.KILOMETER_PER_HOUR
            else -> EUnitSpeed.KILOMETER_PER_HOUR
        }

    fun mapUnitToOptionEUnit(source: Unit?): Option<EUnit> = source?.let { mapUnitToEUnit(it).some() } ?: None

    fun mapUnitToEUnit(source: Unit): EUnit =
        when (source) {
            Unit.M -> EUnit.METER
            Unit.KM -> EUnit.KILOMETER
            Unit.FT -> EUnit.FEET
            Unit.MILE -> EUnit.MILE

            Unit.M___S -> EUnit.METER_PER_SECOND
            Unit.MPH -> EUnit.MILES_PER_HOUR
            Unit.KM___H -> EUnit.KILOMETER_PER_HOUR

            Unit.KG -> EUnit.KILOGRAM
            Unit.T -> EUnit.TON

            Unit.PERCENT -> EUnit.PERCENT
        }

    fun mapDynamicToBoolean(source: Dynamic): Boolean = source == Dynamic.YES
}
