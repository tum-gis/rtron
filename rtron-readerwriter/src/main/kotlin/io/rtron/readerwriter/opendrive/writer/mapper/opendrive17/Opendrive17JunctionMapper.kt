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

package io.rtron.readerwriter.opendrive.writer.mapper.opendrive17

import arrow.core.Option
import io.rtron.model.opendrive.junction.EConnectionType
import io.rtron.model.opendrive.junction.EContactPoint
import io.rtron.model.opendrive.junction.EJunctionType
import io.rtron.model.opendrive.junction.Junction
import io.rtron.model.opendrive.junction.JunctionPredecessorSuccessor
import io.rtron.model.opendrive.junction.JunctionSurface
import io.rtron.readerwriter.opendrive.writer.mapper.common.OpendriveCommonMapper
import org.asam.opendrive17.E_Connection_Type
import org.asam.opendrive17.E_ContactPoint
import org.asam.opendrive17.E_Junction_Type
import org.asam.opendrive17.T_Junction
import org.asam.opendrive17.T_Junction_PredecessorSuccessor
import org.asam.opendrive17.T_Junction_Surface
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy
import org.mapstruct.ValueMapping

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    uses = [OpendriveCommonMapper::class, Opendrive17CoreMapper::class, Opendrive17ObjectMapper::class],
)
abstract class Opendrive17JunctionMapper {
    //
    // Junction
    //
    abstract fun mapJunction(source: Junction): T_Junction

    fun mapOptionJunctionSurface(source: Option<JunctionSurface>): T_Junction_Surface? = source.fold({ null }, { mapJunctionSurface(it) })

    abstract fun mapJunctionSurface(source: JunctionSurface): T_Junction_Surface

    fun mapOptionEJunctionType(source: Option<EJunctionType>): E_Junction_Type? = source.fold({ null }, { mapEJunctionType(it) })

    @ValueMapping(source = "CROSSING", target = "DEFAULT")
    abstract fun mapEJunctionType(source: EJunctionType): E_Junction_Type

    //
    // Junction Connection
    //
    fun mapOptionJunctionPredecessorSuccessor(source: Option<JunctionPredecessorSuccessor>): T_Junction_PredecessorSuccessor? =
        source.fold({ null }, { mapJunctionPredecessorSuccessor(it) })

    abstract fun mapJunctionPredecessorSuccessor(source: JunctionPredecessorSuccessor): T_Junction_PredecessorSuccessor

    fun mapOptionEContactPoint(source: Option<EContactPoint>): E_ContactPoint? = source.fold({ null }, { mapEContactPoint(it) })

    fun mapEContactPoint(source: EContactPoint): E_ContactPoint =
        when (source) {
            EContactPoint.START -> E_ContactPoint.START
            EContactPoint.END -> E_ContactPoint.END
        }

    fun mapOptionConnectionType(source: Option<EConnectionType>): E_Connection_Type? = source.fold({ null }, { mapConnectionType(it) })

    abstract fun mapConnectionType(source: EConnectionType): E_Connection_Type
}
