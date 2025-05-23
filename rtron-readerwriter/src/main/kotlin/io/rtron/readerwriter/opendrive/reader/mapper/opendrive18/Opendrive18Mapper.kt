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

package io.rtron.readerwriter.opendrive.reader.mapper.opendrive17

import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import io.rtron.readerwriter.opendrive.reader.mapper.opendrive18.Opendrive18CoreMapper
import io.rtron.readerwriter.opendrive.reader.mapper.opendrive18.Opendrive18JunctionMapper
import io.rtron.readerwriter.opendrive.reader.mapper.opendrive18.Opendrive18RoadMapper
import org.asam.opendrive18.OpenDRIVE
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    uses = [OpendriveCommonMapper::class, Opendrive18CoreMapper::class, Opendrive18JunctionMapper::class, Opendrive18RoadMapper::class],
    imports = [arrow.core.Option::class],
)
abstract class Opendrive18Mapper {
    private val logger = KotlinLogging.logger {}

    abstract fun mapModel(source: OpenDRIVE): OpendriveModel
}
