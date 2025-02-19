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

package io.rtron.readerwriter.opendrive.reader.mapper.opendrive18

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.model.opendrive.junction.EContactPoint
import io.rtron.model.opendrive.junction.EElementDir
import io.rtron.model.opendrive.junction.EJunctionType
import io.rtron.model.opendrive.junction.JunctionPredecessorSuccessor
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import io.rtron.readerwriter.opendrive.reader.mapper.common.toUpperCaseVariations
import org.asam.opendrive18.E_Junction_Type
import org.asam.opendrive18.T_Junction_PredecessorSuccessor
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    uses = [OpendriveCommonMapper::class, Opendrive18CoreMapper::class],
    imports = [Option::class],
)
abstract class Opendrive18JunctionMapper {
    private val logger = KotlinLogging.logger {}

    fun mapJunctionPredecessorSuccessorToOption(source: T_Junction_PredecessorSuccessor?): Option<JunctionPredecessorSuccessor> =
        source?.let { mapJunctionPredecessorSuccessor(it).some() } ?: None

    abstract fun mapJunctionPredecessorSuccessor(source: T_Junction_PredecessorSuccessor): JunctionPredecessorSuccessor

    //
    // Enumerations
    //
    fun mapJunctionTypeToOptionEJunctionType(source: E_Junction_Type?): Option<EJunctionType> =
        source?.let { mapJunctionType(it).some() } ?: None

    abstract fun mapJunctionType(source: E_Junction_Type): EJunctionType

    fun mapContactPointToOption(source: String?): Option<EContactPoint> = source?.let { mapContactPoint(it).some() } ?: None

    fun mapContactPoint(source: String): EContactPoint =
        when (source.uppercase()) {
            in EContactPoint.START.name.toUpperCaseVariations() -> EContactPoint.START
            in EContactPoint.END.name.toUpperCaseVariations() -> EContactPoint.END
            else -> EContactPoint.START
        }

    fun mapElementDirToOption(source: String?): Option<EElementDir> = source?.let { mapElementDir(it).some() } ?: None

    fun mapElementDir(source: String): EElementDir =
        when (source.uppercase()) {
            in "+" -> EElementDir.PLUS
            in "-" -> EElementDir.MINUS
            else -> EElementDir.PLUS
        }
}
