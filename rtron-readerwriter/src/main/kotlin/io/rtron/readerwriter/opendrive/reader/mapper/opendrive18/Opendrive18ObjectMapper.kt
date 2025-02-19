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
import arrow.core.toOption
import io.rtron.model.opendrive.objects.EObjectType
import io.rtron.model.opendrive.objects.EOutlineFillType
import io.rtron.model.opendrive.objects.ESideType
import io.rtron.model.opendrive.objects.RoadObjects
import io.rtron.model.opendrive.objects.RoadObjectsObject
import io.rtron.model.opendrive.objects.RoadObjectsObjectBorders
import io.rtron.model.opendrive.objects.RoadObjectsObjectMarkings
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlines
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlinesOutline
import io.rtron.model.opendrive.objects.RoadObjectsObjectParkingSpace
import io.rtron.model.opendrive.objects.RoadObjectsObjectSurface
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import org.asam.opendrive18.E_ObjectType
import org.asam.opendrive18.E_OutlineFillType
import org.asam.opendrive18.E_SideType
import org.asam.opendrive18.T_Road_Objects
import org.asam.opendrive18.T_Road_Objects_Object
import org.asam.opendrive18.T_Road_Objects_Object_Borders
import org.asam.opendrive18.T_Road_Objects_Object_Markings
import org.asam.opendrive18.T_Road_Objects_Object_Outlines
import org.asam.opendrive18.T_Road_Objects_Object_Outlines_Outline
import org.asam.opendrive18.T_Road_Objects_Object_ParkingSpace
import org.asam.opendrive18.T_Road_Objects_Object_Surface
import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueCheckStrategy
import org.mapstruct.ValueMapping

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    uses = [
        OpendriveCommonMapper::class, Opendrive18CoreMapper::class, Opendrive18LaneMapper::class,
    ],
    imports = [Option::class],
)
abstract class Opendrive18ObjectMapper {
    //
    // Road Objects
    //
    @Mapping(source = "object", target = "roadObject")
    abstract fun mapRoadObjects(source: T_Road_Objects): RoadObjects

    abstract fun mapRoadObjectsObject(source: T_Road_Objects_Object): RoadObjectsObject

    @AfterMapping
    open fun afterMappingRoadObjectsObject(
        source: T_Road_Objects_Object,
        @MappingTarget target: RoadObjectsObject,
    ) {
        // after mapping deprecated outline element by creating a new container or
        // appending it to preexisting outlines container
        source.outline.toOption().onSome { sourceOutline ->
            val outline = mapRoadObjectsObjectOutlinesOutline(sourceOutline)
            target.outlines =
                target.outlines.fold(
                    { RoadObjectsObjectOutlines(outline = listOf(outline)) },
                    { RoadObjectsObjectOutlines(outline = it.outline + outline) },
                ).some()
        }
    }

    abstract fun mapRoadObjectSurface(source: T_Road_Objects_Object_Surface): RoadObjectsObjectSurface

    //
    // Outline
    //
    abstract fun mapRoadObjectsObjectOutlines(source: T_Road_Objects_Object_Outlines): RoadObjectsObjectOutlines

    abstract fun mapRoadObjectsObjectOutlinesOutline(source: T_Road_Objects_Object_Outlines_Outline): RoadObjectsObjectOutlinesOutline

    //
    abstract fun mapRoadObjectsObjectParkingSpace(source: T_Road_Objects_Object_ParkingSpace): RoadObjectsObjectParkingSpace

    abstract fun mapRoadObjectsObjectMarkings(source: T_Road_Objects_Object_Markings): RoadObjectsObjectMarkings

    abstract fun mapRoadObjectsObjectBorders(source: T_Road_Objects_Object_Borders): RoadObjectsObjectBorders

    //
    // Enumerations
    //
    fun mapOutlineFileTypeToOption(source: E_OutlineFillType?): Option<EOutlineFillType> =
        source?.let { mapOutlineFileType(it).some() } ?: None

    abstract fun mapOutlineFileType(source: E_OutlineFillType): EOutlineFillType

    fun mapSideTypeToOption(source: E_SideType?): Option<ESideType> = source?.let { mapSideType(it).some() } ?: None

    abstract fun mapSideType(source: E_SideType): ESideType

    fun mapObjectTypeToOption(source: E_ObjectType?): Option<EObjectType> = source?.let { mapObjectType(it).some() } ?: None

    @ValueMapping(source = "CAR", target = "NONE")
    @ValueMapping(source = "VAN", target = "NONE")
    @ValueMapping(source = "BUS", target = "NONE")
    @ValueMapping(source = "TRAILER", target = "NONE")
    @ValueMapping(source = "BIKE", target = "NONE")
    @ValueMapping(source = "MOTORBIKE", target = "NONE")
    @ValueMapping(source = "TRAM", target = "NONE")
    @ValueMapping(source = "TRAIN", target = "NONE")
    @ValueMapping(source = "PEDESTRIAN", target = "NONE")
    @ValueMapping(source = "WIND", target = "NONE")
    abstract fun mapObjectType(source: E_ObjectType): EObjectType
}
