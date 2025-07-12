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

package io.rtron.readerwriter.opendrive.reader.mapper.opendrive16

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.model.opendrive.core.AdditionalData
import io.rtron.model.opendrive.core.EUnit
import io.rtron.model.opendrive.core.EUnitSpeed
import io.rtron.model.opendrive.core.HeaderGeoReference
import io.rtron.model.opendrive.core.HeaderOffset
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import org.asam.opendrive16.E_UnitDistance
import org.asam.opendrive16.E_UnitMass
import org.asam.opendrive16.E_UnitSlope
import org.asam.opendrive16.E_UnitSpeed
import org.asam.opendrive16.T_Header_GeoReference
import org.asam.opendrive16.T_Header_Offset
import org.asam.opendrive16.T_YesNo
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    uses = [OpendriveCommonMapper::class],
    imports = [Option::class],
)
abstract class Opendrive16CoreMapper {
    private val logger = KotlinLogging.logger {}

    //
    // Header
    //
    fun mapHeaderGeoreference(source: T_Header_GeoReference): Option<HeaderGeoReference> =
        HeaderGeoReference(content = source.content.joinToString()).some()

    fun mapHeaderOffsetToOptionHeaderOffset(source: T_Header_Offset?): Option<HeaderOffset> =
        source?.let { mapHeaderOffset(it).some() } ?: None

    abstract fun mapHeaderOffset(source: T_Header_Offset): HeaderOffset

    fun mapAdditionalData(source: List<Any>): List<AdditionalData> = emptyList()

    //
    // Enumerations
    //
    fun mapEUnitSpeedToOption(source: E_UnitSpeed?): Option<EUnitSpeed> = source?.let { mapEUnitSpeed(it).some() } ?: None

    fun mapEUnitSpeed(source: E_UnitSpeed): EUnitSpeed =
        when (source) {
            E_UnitSpeed.M___S -> EUnitSpeed.METER_PER_SECOND
            E_UnitSpeed.MPH -> EUnitSpeed.MILES_PER_HOUR
            E_UnitSpeed.KM___H -> EUnitSpeed.KILOMETER_PER_HOUR
        }

    fun mapUnitToOptionEUnit(source: String?): Option<EUnit> = source?.let { mapUnitToEUnit(it).some() } ?: None

    fun mapUnitToEUnit(source: String): EUnit =
        when (source) {
            E_UnitDistance.M.value() -> EUnit.METER
            E_UnitDistance.KM.value() -> EUnit.KILOMETER
            E_UnitDistance.FT.value() -> EUnit.FEET
            E_UnitDistance.MILE.value() -> EUnit.MILE

            E_UnitSpeed.M___S.value() -> EUnit.METER_PER_SECOND
            E_UnitSpeed.MPH.value() -> EUnit.MILES_PER_HOUR
            E_UnitSpeed.KM___H.value() -> EUnit.KILOMETER_PER_HOUR

            E_UnitMass.KG.value() -> EUnit.KILOGRAM
            E_UnitMass.T.value() -> EUnit.TON

            E_UnitSlope.PERCENT.value() -> EUnit.PERCENT
            else -> {
                logger.error { "Unknown mapping of $source to EUnit (falling back to PERCENT)" }
                EUnit.PERCENT
            }
        }

    fun mapYesNoToOption(source: T_YesNo?): Option<Boolean> = source?.let { mapYesNo(it).some() } ?: None

    fun mapYesNo(source: T_YesNo): Boolean =
        when (source) {
            T_YesNo.YES -> true
            T_YesNo.NO -> false
        }
}
