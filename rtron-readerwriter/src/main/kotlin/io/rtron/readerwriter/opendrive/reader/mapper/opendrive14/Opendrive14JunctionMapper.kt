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

package io.rtron.readerwriter.opendrive.reader.mapper.opendrive14

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.model.opendrive.junction.EContactPoint
import io.rtron.model.opendrive.junction.Junction
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import org.asam.opendrive14.ContactPoint
import org.asam.opendrive14.OpenDRIVE
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = [OpendriveCommonMapper::class, Opendrive14CoreMapper::class, Opendrive14ObjectMapper::class], imports = [Option::class])
abstract class Opendrive14JunctionMapper {

    abstract fun mapJunction(source: OpenDRIVE.Junction): Junction

    //
    // Enumerations
    //
    fun mapContactPointToOption(source: ContactPoint?): Option<EContactPoint> = source?.let { mapContactPoint(it).some() } ?: None
    abstract fun mapContactPoint(source: ContactPoint): EContactPoint
}
