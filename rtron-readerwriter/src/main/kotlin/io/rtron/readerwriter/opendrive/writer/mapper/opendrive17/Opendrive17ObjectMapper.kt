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

package io.rtron.readerwriter.opendrive.writer.mapper.opendrive17

import arrow.core.Option
import io.rtron.model.opendrive.lane.ELaneType
import io.rtron.model.opendrive.lane.ERoadMarkWeight
import io.rtron.model.opendrive.objects.EObjectType
import io.rtron.model.opendrive.objects.EOrientation
import io.rtron.model.opendrive.objects.EOutlineFillType
import io.rtron.model.opendrive.objects.ESideType
import io.rtron.model.opendrive.objects.RoadObjects
import io.rtron.model.opendrive.objects.RoadObjectsObject
import io.rtron.model.opendrive.objects.RoadObjectsObjectBorders
import io.rtron.model.opendrive.objects.RoadObjectsObjectMarkings
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlines
import io.rtron.model.opendrive.objects.RoadObjectsObjectParkingSpace
import io.rtron.readerwriter.opendrive.writer.mapper.common.OpendriveCommonMapper
import org.asam.opendrive17.E_LaneType
import org.asam.opendrive17.E_ObjectType
import org.asam.opendrive17.E_OutlineFillType
import org.asam.opendrive17.E_RoadMarkWeight
import org.asam.opendrive17.E_SideType
import org.asam.opendrive17.T_Road_Objects
import org.asam.opendrive17.T_Road_Objects_Object
import org.asam.opendrive17.T_Road_Objects_Object_Borders
import org.asam.opendrive17.T_Road_Objects_Object_Markings
import org.asam.opendrive17.T_Road_Objects_Object_Outlines
import org.asam.opendrive17.T_Road_Objects_Object_ParkingSpace
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueCheckStrategy

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = [OpendriveCommonMapper::class, Opendrive17CoreMapper::class])
abstract class Opendrive17ObjectMapper {

    //
    // Road objects
    //
    fun mapOptionRoadObjects(source: Option<RoadObjects>): T_Road_Objects? = source.fold({ null }, { mapRoadObjects(it) })

    @Mapping(source = "roadObject", target = "object")
    abstract fun mapRoadObjects(source: RoadObjects): T_Road_Objects

    //
    // Road object
    //
    abstract fun mapRoadObjectsObject(source: RoadObjectsObject): T_Road_Objects_Object

    fun mapOptionRoadObjectsObjectParkingSpace(source: Option<RoadObjectsObjectParkingSpace>): T_Road_Objects_Object_ParkingSpace? = source.fold({ null }, { mapRoadObjectsObjectParkingSpace(it) })
    abstract fun mapRoadObjectsObjectParkingSpace(source: RoadObjectsObjectParkingSpace): T_Road_Objects_Object_ParkingSpace

    fun mapOptionRoadObjectsObjectMarkings(source: Option<RoadObjectsObjectMarkings>): T_Road_Objects_Object_Markings? = source.fold({ null }, { mapRoadObjectsObjectMarkings(it) })
    abstract fun mapRoadObjectsObjectMarkings(source: RoadObjectsObjectMarkings): T_Road_Objects_Object_Markings

    fun mapOptionEOrientation(source: Option<EOrientation>): String? = source.fold({ null }, { mapEOrientation(it) })
    fun mapEOrientation(source: EOrientation): String = when (source) {
        EOrientation.PLUS -> "+"
        EOrientation.MINUS -> "-"
        EOrientation.NONE -> "none"
    }

    fun mapOptionFillType(source: Option<EOutlineFillType>): E_OutlineFillType? = source.fold({ null }, { mapFillType(it) })
    abstract fun mapFillType(source: EOutlineFillType): E_OutlineFillType

    fun mapOptionEObjectType(source: Option<EObjectType>): E_ObjectType? = source.fold({ null }, { mapEObjectType(it) })
    abstract fun mapEObjectType(source: EObjectType): E_ObjectType

    fun mapOptionRoadObjectsObjectBorders(source: Option<RoadObjectsObjectBorders>): T_Road_Objects_Object_Borders? = source.fold({ null }, { mapRoadObjectsObjectBorders(it) })
    abstract fun mapRoadObjectsObjectBorders(source: RoadObjectsObjectBorders): T_Road_Objects_Object_Borders

    //
    // Outlines
    //
    fun mapOptionRoadObjectsObjectOutlines(source: Option<RoadObjectsObjectOutlines>): T_Road_Objects_Object_Outlines? = source.fold({ null }, { mapRoadObjectsObjectOutlines(it) })
    abstract fun mapRoadObjectsObjectOutlines(source: RoadObjectsObjectOutlines): T_Road_Objects_Object_Outlines

    fun mapOptionELaneType(source: Option<ELaneType>): E_LaneType? = source.fold({ null }, { mapELaneType(it) })
    abstract fun mapELaneType(source: ELaneType): E_LaneType

    //
    // Markings
    //
    fun mapOptionESideType(source: Option<ESideType>): E_SideType? = source.fold({ null }, { mapESideType(it) })
    abstract fun mapESideType(source: ESideType): E_SideType

    fun mapOptionERoadMarkWeight(source: Option<ERoadMarkWeight>): E_RoadMarkWeight? = source.fold({ null }, { mapERoadMarkWeight(it) })
    abstract fun mapERoadMarkWeight(source: ERoadMarkWeight): E_RoadMarkWeight
}
