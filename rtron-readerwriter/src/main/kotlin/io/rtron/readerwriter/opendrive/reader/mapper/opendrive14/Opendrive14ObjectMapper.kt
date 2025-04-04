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

package io.rtron.readerwriter.opendrive.reader.mapper.opendrive14

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.model.opendrive.objects.EObjectType
import io.rtron.model.opendrive.objects.RoadObjects
import io.rtron.model.opendrive.objects.RoadObjectsObject
import io.rtron.model.opendrive.objects.RoadObjectsObjectMaterial
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlines
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlinesOutline
import io.rtron.model.opendrive.objects.RoadObjectsObjectParkingSpace
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import io.rtron.readerwriter.opendrive.reader.mapper.common.toUpperCaseVariations
import org.asam.opendrive14.OpenDRIVE
import org.asam.opendrive14.ParkingSpace
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueCheckStrategy

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    uses = [OpendriveCommonMapper::class],
    imports = [Option::class],
)
abstract class Opendrive14ObjectMapper {
    //
    // Road Objects
    //
    @Mapping(source = "object", target = "roadObject")
    abstract fun mapRoadObjects(source: OpenDRIVE.Road.Objects): RoadObjects

    //
    // Road Object
    //
    @Mapping(source = "outline", target = "outlines")
    abstract fun mapRoadObjectsObject(source: OpenDRIVE.Road.Objects.Object): RoadObjectsObject

    fun mapRoadObjectsObjectOutline(source: OpenDRIVE.Road.Objects.Object.Outline): RoadObjectsObjectOutlines {
        val outlines = listOf(mapRoadObjectsObjectOutlinesOutline(source))
        return RoadObjectsObjectOutlines(outlines)
    }

    abstract fun mapRoadObjectsObjectOutlinesOutline(source: OpenDRIVE.Road.Objects.Object.Outline): RoadObjectsObjectOutlinesOutline

    fun mapMaterialList(source: OpenDRIVE.Road.Objects.Object.Material?): List<RoadObjectsObjectMaterial> =
        source?.let { listOf(mapRoadObjectsObjectMaterial(it)) } ?: emptyList()

    abstract fun mapRoadObjectsObjectMaterial(source: OpenDRIVE.Road.Objects.Object.Material): RoadObjectsObjectMaterial

    abstract fun mapParkingSpace(source: ParkingSpace): RoadObjectsObjectParkingSpace

    //
    // Enumerations
    //

    fun mapRoadObjectTypeToOption(source: String?): Option<EObjectType> = source?.let { mapRoadObjectType(it).some() } ?: None

    fun mapRoadObjectType(source: String): EObjectType =
        when (source.uppercase()) {
            in EObjectType.NONE.name.toUpperCaseVariations() -> EObjectType.NONE
            in EObjectType.OBSTACLE.name.toUpperCaseVariations() -> EObjectType.OBSTACLE
            in EObjectType.POLE.name.toUpperCaseVariations() -> EObjectType.POLE
            in EObjectType.TREE.name.toUpperCaseVariations() -> EObjectType.TREE
            in EObjectType.VEGETATION.name.toUpperCaseVariations() -> EObjectType.VEGETATION
            in EObjectType.BARRIER.name.toUpperCaseVariations() -> EObjectType.BARRIER
            in EObjectType.BUILDING.name.toUpperCaseVariations() -> EObjectType.BUILDING
            in EObjectType.PARKING_SPACE.name.toUpperCaseVariations() -> EObjectType.PARKING_SPACE
            in "patch" -> EObjectType.ROAD_SURFACE
            in "railing" -> EObjectType.BARRIER
            in EObjectType.TRAFFIC_ISLAND.name.toUpperCaseVariations() -> EObjectType.TRAFFIC_ISLAND
            in EObjectType.CROSSWALK.name.toUpperCaseVariations() -> EObjectType.CROSSWALK
            in "streetLamp" -> EObjectType.POLE
            in EObjectType.GANTRY.name.toUpperCaseVariations() -> EObjectType.GANTRY
            in "soundBarrier" -> EObjectType.BARRIER
            in EObjectType.ROAD_MARK.name.toUpperCaseVariations() -> EObjectType.ROAD_MARK
            in "wind" -> EObjectType.POLE
            else -> EObjectType.NONE
        }
}
