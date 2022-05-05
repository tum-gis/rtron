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

package io.rtron.readerwriter.opendrive.writer.mapper.opendrive17

import arrow.core.Option
import io.rtron.model.opendrive.core.EUnit
import io.rtron.model.opendrive.core.EUnitSpeed
import io.rtron.model.opendrive.core.Header
import io.rtron.model.opendrive.core.HeaderGeoReference
import io.rtron.model.opendrive.core.HeaderOffset
import io.rtron.readerwriter.opendrive.writer.mapper.common.OpendriveCommonMapper
import org.asam.opendrive17.E_UnitDistance
import org.asam.opendrive17.E_UnitMass
import org.asam.opendrive17.E_UnitSlope
import org.asam.opendrive17.E_UnitSpeed
import org.asam.opendrive17.T_Bool
import org.asam.opendrive17.T_Header
import org.asam.opendrive17.T_Header_GeoReference
import org.asam.opendrive17.T_Header_Offset
import org.asam.opendrive17.T_YesNo
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = [OpendriveCommonMapper::class])
abstract class Opendrive17CoreMapper {

    //
    // Header
    //
    abstract fun mapHeader(source: Header): T_Header

    fun mapOptionGeoReference(source: Option<HeaderGeoReference>): T_Header_GeoReference? = source.fold({ null }, { mapGeoReference(it) })
    fun mapGeoReference(source: HeaderGeoReference): T_Header_GeoReference {
        return T_Header_GeoReference().apply {
            content.add("![CDATA[${source.content}]]") // TODO
        }
    }

    fun mapOptionHeaderOffset(source: Option<HeaderOffset>): T_Header_Offset? = source.fold({ null }, { mapHeaderOffset(it) })
    abstract fun mapHeaderOffset(source: HeaderOffset): T_Header_Offset

    //
    // Enumerations
    //
    fun mapOptionEUnitToString(source: Option<EUnit>): String? = source.fold({ null }, { mapEUnitToString(it) })
    fun mapEUnitToString(source: EUnit): String = when (source) {
        EUnit.METER -> E_UnitDistance.M.value()!!
        EUnit.KILOMETER -> E_UnitDistance.KM.value()!!
        EUnit.FEET -> E_UnitDistance.FT.value()!!
        EUnit.MILE -> E_UnitDistance.M.value()!!

        EUnit.METER_PER_SECOND -> E_UnitSpeed.M___S.value()!!
        EUnit.MILES_PER_HOUR -> E_UnitSpeed.MPH.value()!!
        EUnit.KILOMETER_PER_HOUR -> E_UnitSpeed.KM___H.value()!!

        EUnit.KILOGRAM -> E_UnitMass.KG.value()!!
        EUnit.TON -> E_UnitMass.T.value()!!

        EUnit.PERCENT -> E_UnitSlope.PERCENT.value()!!
    }

    fun mapOptionEUnitSpeed(source: Option<EUnitSpeed>): E_UnitSpeed? = source.fold({ null }, { mapEUnitSpeed(it) })
    fun mapEUnitSpeed(source: EUnitSpeed): E_UnitSpeed = when (source) {
        EUnitSpeed.METER_PER_SECOND -> E_UnitSpeed.M___S
        EUnitSpeed.MILES_PER_HOUR -> E_UnitSpeed.MPH
        EUnitSpeed.KILOMETER_PER_HOUR -> E_UnitSpeed.KM___H
    }

    fun mapOptionBooleanToBool(source: Option<Boolean>): T_Bool? = source.fold({ null }, { mapBooleanToBool(it) })
    fun mapBooleanToBool(source: Boolean): T_Bool = if (source) T_Bool.TRUE else T_Bool.FALSE

    fun mapOptionBooleanToYesNo(source: Option<Boolean>): T_YesNo? = source.fold({ null }, { mapBooleanToYesNo(it) })
    fun mapBooleanToYesNo(source: Boolean): T_YesNo = if (source) T_YesNo.YES else T_YesNo.NO
}
