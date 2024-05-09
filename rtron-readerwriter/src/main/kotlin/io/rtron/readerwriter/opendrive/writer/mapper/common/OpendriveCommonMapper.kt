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

package io.rtron.readerwriter.opendrive.writer.mapper.common

import arrow.core.Option
import arrow.core.getOrElse
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
abstract class OpendriveCommonMapper {
    fun <T> mapNullableToOption(source: T?): Option<T> = Option.fromNullable(source)

    fun mapOptionDoubleToNullableDouble(source: Option<Double>): Double? = source.getOrElse { return null }

    fun mapOptionStringToNullableString(source: Option<String>): String? = source.getOrElse { return null }

    fun mapOptionIntegerToNullableInteger(source: Option<Int>): Int? = source.getOrElse { return null }
}
