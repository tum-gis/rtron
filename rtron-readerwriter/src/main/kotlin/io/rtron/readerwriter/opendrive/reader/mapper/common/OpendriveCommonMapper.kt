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

package io.rtron.readerwriter.opendrive.reader.mapper.common

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.model.opendrive.core.ECountryCode
import io.rtron.model.opendrive.objects.EOrientation
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.NullValueCheckStrategy
import org.mapstruct.ValueMapping

/**
 * Returns upper case string variations (with or without '_') of string.
 */
fun String.toUpperCaseVariations(): List<String> =
    listOf(uppercase(), replace("_", "").uppercase())

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
abstract class OpendriveCommonMapper {

    fun <T> mapNullableToOption(source: T?): Option<T> = Option.fromNullable(source)

    fun mapCountryCodeStringToOption(source: String?): Option<ECountryCode> = source?.let { Option.fromNullable(mapCountryCodeString(it)) } ?: None
    @ValueMapping(source = "Austria", target = "AU")
    @ValueMapping(source = "Brazil", target = "BR")
    @ValueMapping(source = "China", target = "CN")
    @ValueMapping(source = "France", target = "FR")
    @ValueMapping(source = "Germany", target = "DE")
    @ValueMapping(source = "Italy", target = "IT")
    @ValueMapping(source = "Switzerland", target = "CH")
    @ValueMapping(source = "USA", target = "US")
    @ValueMapping(source = "AUT", target = "AU")
    @ValueMapping(source = "BRA", target = "BR")
    @ValueMapping(source = "CHN", target = "CN")
    @ValueMapping(source = "FRA", target = "FR")
    @ValueMapping(source = "DEU", target = "DE")
    @ValueMapping(source = "ITA", target = "IT")
    @ValueMapping(source = "CHE", target = "CH")
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = MappingConstants.NULL)
    abstract fun mapCountryCodeString(source: String): ECountryCode?

    fun mapOrientationStringToOption(source: String?): Option<EOrientation> = source?.let { mapOrientationString(it).some() } ?: None
    fun mapOrientationString(source: String): EOrientation = when (source) {
        "+" -> EOrientation.PLUS
        "-" -> EOrientation.MINUS
        else -> EOrientation.NONE
    }
}
