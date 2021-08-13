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

package io.rtron.readerwriter.opendrive

import io.rtron.io.logging.LogManager
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.common.CountryCode
import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.EAccessRestrictionType
import io.rtron.model.opendrive.common.EObjectType
import io.rtron.model.opendrive.common.EOrientation
import io.rtron.model.opendrive.common.ERoadMarkRule
import io.rtron.model.opendrive.common.ERoadMarkType
import io.rtron.model.opendrive.common.ERoadObjectsObjectParkingSpaceAccess
import io.rtron.model.opendrive.common.EUnit
import io.rtron.model.opendrive.common.EUnitSpeed
import io.rtron.model.opendrive.road.Road
import io.rtron.model.opendrive.road.lanes.ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSectionLCRLaneRoadMark
import io.rtron.model.opendrive.road.lateralprofile.RoadLateralProfile
import io.rtron.model.opendrive.road.objects.RoadObjects
import io.rtron.model.opendrive.road.objects.RoadObjectsObject
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectMaterial
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectOutlines
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectOutlinesOutline
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectOutlinesOutlineCornerLocal
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectOutlinesOutlineCornerRoad
import io.rtron.model.opendrive.road.objects.RoadObjectsObjectRepeat
import io.rtron.model.opendrive.road.planview.ParamPoly3PRange
import io.rtron.model.opendrive.road.signals.RoadSignalsSignal
import org.asam.opendrive14.Access
import org.asam.opendrive14.CenterLane
import org.asam.opendrive14.Dynamic
import org.asam.opendrive14.Include
import org.asam.opendrive14.Lane
import org.asam.opendrive14.LaneChange
import org.asam.opendrive14.OpenDRIVE
import org.asam.opendrive14.OpenDRIVE.Road.Objects.Object.Outline
import org.asam.opendrive14.OpenDRIVE.Road.Objects.Object.Outline.CornerLocal
import org.asam.opendrive14.OpenDRIVE.Road.Objects.Object.Outline.CornerRoad
import org.asam.opendrive14.OpenDRIVE.Road.Objects.Object.Repeat
import org.asam.opendrive14.OpenDRIVE.Road.Signals
import org.asam.opendrive14.PRange
import org.asam.opendrive14.Restriction
import org.asam.opendrive14.RoadmarkType
import org.asam.opendrive14.Rule
import org.asam.opendrive14.SingleSide
import org.asam.opendrive14.Unit
import org.asam.opendrive14.UserData
import org.mapstruct.AfterMapping
import org.mapstruct.BeforeMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.MappingTarget
import org.mapstruct.Mappings
import org.mapstruct.NullValueCheckStrategy
import org.mapstruct.ValueMapping
import org.mapstruct.ValueMappings

/**
 * Returns upper case string variations (with or without '_') of string.
 */
fun String.toUpperCaseVariations(): List<String> =
    listOf(uppercase(), replace("_", "").uppercase())

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
abstract class Opendrive14Mapper {

    var reportLogger = LogManager.getReportLogger("general")

    abstract fun mapModel(model: OpenDRIVE): OpendriveModel

    abstract fun mapRoad(road: OpenDRIVE.Road): Road
    @AfterMapping
    fun adjustJunctionId(@MappingTarget targetRoad: Road) {
        if (targetRoad.junction == "-1") targetRoad.junction = ""
    }

    //
    // Plan view mapping
    //
    fun mapPRange(prange: PRange): ParamPoly3PRange {
        return if (prange == PRange.NORMALIZED) ParamPoly3PRange.NORMALIZED else ParamPoly3PRange.ARCLENGTH
    }

    @BeforeMapping
    fun mapLateralProfileLogging(srcLateralProfile: OpenDRIVE.Road.LateralProfile) {
        if (srcLateralProfile.crossfall.isNotEmpty())
            reportLogger.infoOnce("Since crossfall is not in the OpenDRIVE standard from version 1.6, it is not supported.")
    }
    abstract fun mapLateralProfile(srcLateralProfile: OpenDRIVE.Road.LateralProfile): RoadLateralProfile

    //
    // Road objects mapping
    //
    @Mapping(source = "object", target = "roadObject")
    abstract fun mapRoadObjects(objects: OpenDRIVE.Road.Objects): RoadObjects
    @AfterMapping
    fun splitRepeats(
        srcObjects: OpenDRIVE.Road.Objects,
        @MappingTarget targetRoadObjects: RoadObjects
    ) {
        // OpenDRIVE 1.4 has a list of Repeat Records and OpenDRIVE 1.5 only one entry
        // Multiple new objects are copied, so that each road object contains only one repeat record

        data class SrcTargetObject(val srcObject: OpenDRIVE.Road.Objects.Object, val targetObject: RoadObjectsObject)
        val roadObjectPairs = srcObjects.`object`.zip(targetRoadObjects.roadObject).map { SrcTargetObject(it.first, it.second) }

        roadObjectPairs.forEach { currentObjectPair ->
            // remove old object, that has already been mapped
            targetRoadObjects.roadObject.toMutableList() -= currentObjectPair.targetObject
            // add repeat entries with dedicated objects and an extra index
            currentObjectPair.srcObject.repeat.forEachIndexed { _, currentRepeat ->
                val mappedId = currentObjectPair.srcObject.id
                // val mappedId = "${currentObjectPair.srcObject.id}_${index}"
                val mappedRepeat = mapRoadObjectsObjectRepeat(currentRepeat)
                targetRoadObjects.roadObject += currentObjectPair.targetObject.copy(id = mappedId, repeat = mappedRepeat)
            }
        }
    }

    @Mappings(
        Mapping(source = "outline", target = "outlines")
    )
    abstract fun mapRoadObjectsObject(objects: OpenDRIVE.Road.Objects.Object): RoadObjectsObject
    @AfterMapping
    fun mapOutlines(
        srcOutline: Outline,
        @MappingTarget targetOutline: RoadObjectsObjectOutlines
    ) {
        val outlineList: MutableList<RoadObjectsObjectOutlinesOutline> = ArrayList()
        outlineList.add(mapOutlinesOutline(srcOutline))
        targetOutline.outline = outlineList
        targetOutline.userData = mapUserDataList(srcOutline.userData)
        targetOutline.include = mapIncludeList(srcOutline.include)
        targetOutline.dataQuality = mapDataQuality()
    }
    @Mappings(
        Mapping(target = "dataQuality", ignore = true),
        Mapping(target = "closed", ignore = true),
        Mapping(target = "outer", ignore = true),
        Mapping(target = "id", ignore = true)
    )
    abstract fun mapOutlinesOutline(srcOutline: Outline): RoadObjectsObjectOutlinesOutline

    abstract fun mapRoadObjectsObjectOutlinesOutlineCornerRoad(
        srcOutlineCornerRoad: CornerRoad
    ): RoadObjectsObjectOutlinesOutlineCornerRoad

    abstract fun mapRoadObjectsObjectOutlinesOutlineCornerRoad(
        srcOutlineCornerLocal: CornerLocal
    ): RoadObjectsObjectOutlinesOutlineCornerLocal

    fun mapMaterialList(srcMaterial: OpenDRIVE.Road.Objects.Object.Material?): List<RoadObjectsObjectMaterial> {
        val materialList: MutableList<RoadObjectsObjectMaterial> = ArrayList()
        if (srcMaterial != null) materialList.add(mapMaterial(srcMaterial))
        return materialList
    }

    abstract fun mapMaterial(material: OpenDRIVE.Road.Objects.Object.Material): RoadObjectsObjectMaterial

    @ValueMappings(ValueMapping(source = MappingConstants.ANY_REMAINING, target = "UNKNOWN"))
    abstract fun mapRoadObjectsObjectParkingSpaceAccess(access: Access): ERoadObjectsObjectParkingSpaceAccess

    fun mapRoadObjectType(srcType: String): EObjectType =
        when (srcType.uppercase()) {
            in EObjectType.NONE.name.toUpperCaseVariations() -> EObjectType.NONE
            in EObjectType.OBSTACLE.name.toUpperCaseVariations() -> EObjectType.OBSTACLE
            in EObjectType.POLE.name.toUpperCaseVariations() -> EObjectType.POLE
            in EObjectType.TREE.name.toUpperCaseVariations() -> EObjectType.TREE
            in EObjectType.VEGETATION.name.toUpperCaseVariations() -> EObjectType.VEGETATION
            in EObjectType.BARRIER.name.toUpperCaseVariations() -> EObjectType.BARRIER
            in EObjectType.BUILDING.name.toUpperCaseVariations() -> EObjectType.BUILDING
            in EObjectType.PARKING_SPACE.name.toUpperCaseVariations() -> EObjectType.PARKING_SPACE
            in EObjectType.PATCH.name.toUpperCaseVariations() -> EObjectType.PATCH
            in EObjectType.RAILING.name.toUpperCaseVariations() -> EObjectType.RAILING
            in EObjectType.TRAFFIC_ISLAND.name.toUpperCaseVariations() -> EObjectType.TRAFFIC_ISLAND
            in EObjectType.CROSSWALK.name.toUpperCaseVariations() -> EObjectType.CROSSWALK
            in EObjectType.STREET_LAMP.name.toUpperCaseVariations() -> EObjectType.STREET_LAMP
            in EObjectType.GANTRY.name.toUpperCaseVariations() -> EObjectType.GANTRY
            in EObjectType.SOUND_BARRIER.name.toUpperCaseVariations() -> EObjectType.SOUND_BARRIER
            in EObjectType.ROAD_MARK.name.toUpperCaseVariations() -> EObjectType.ROAD_MARK
            else -> EObjectType.NONE
        }

    fun mapRoadObjectsObjectRepeat(srcRepeatList: List<Repeat>): RoadObjectsObjectRepeat {
        return if (srcRepeatList.isEmpty()) RoadObjectsObjectRepeat()
        else mapRoadObjectsObjectRepeat(srcRepeatList.first())
    }

    abstract fun mapRoadObjectsObjectRepeat(srcRepeat: Repeat): RoadObjectsObjectRepeat

    abstract fun mapUserDataList(userData: List<UserData>): List<io.rtron.model.opendrive.common.UserData>
    abstract fun mapIncludeList(include: List<Include>): List<io.rtron.model.opendrive.common.Include>
    fun mapDataQuality(): DataQuality { return DataQuality() }

    //
    // Lanes mapping
    //
    fun mapRoadLanesLaneSectionSingleSide(srcValue: SingleSide): Boolean {
        return srcValue == SingleSide.TRUE
    }

    fun mapRoadLanesLaneSectionCenterLanes(srcCenterLane: CenterLane): List<RoadLanesLaneSectionCenterLane> {
        val centerLanes: MutableList<RoadLanesLaneSectionCenterLane> = ArrayList()
        centerLanes.add(mapRoadLanesLaneSectionCenterLane(srcCenterLane))
        return centerLanes
    }

    abstract fun mapRoadLanesLaneSectionCenterLane(srcCenterLane: CenterLane): RoadLanesLaneSectionCenterLane

    fun mapRoadLanesLaneSectionLCRLaneLinkPredecessor(srcPredecessor: Lane.Link.Predecessor?): List<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> {
        val predecessors: MutableList<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> = ArrayList()
        if (srcPredecessor != null) predecessors.add(mapPredecessor(srcPredecessor))
        return predecessors
    }

    fun mapRoadLanesLaneSectionLCRLaneLinkPredecessor(srcPredecessor: CenterLane.Link.Predecessor?): List<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> {
        val predecessors: MutableList<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> = ArrayList()
        if (srcPredecessor != null) predecessors.add(mapPredecessor(srcPredecessor))
        return predecessors
    }

    fun mapRoadLanesLaneSectionLCRLaneLinkSuccessor(srcSuccessor: Lane.Link.Successor?): List<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> {
        val successors: MutableList<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> = ArrayList()
        if (srcSuccessor != null) successors.add(mapSuccessor(srcSuccessor))
        return successors
    }

    fun mapRoadLanesLaneSectionLCRLaneLinkSuccessor(srcSuccessor: CenterLane.Link.Successor?): List<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> {
        val successors: MutableList<RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor> = ArrayList()
        if (srcSuccessor != null) successors.add(mapSuccessor(srcSuccessor))
        return successors
    }

    abstract fun mapPredecessor(srcPredecessor: Lane.Link.Predecessor): RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor
    abstract fun mapPredecessor(srcPredecessor: CenterLane.Link.Predecessor): RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor
    abstract fun mapSuccessor(srcSuccessor: Lane.Link.Successor): RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor
    abstract fun mapSuccessor(srcSuccessor: CenterLane.Link.Successor): RoadLanesLaneSectionLCRLaneLinkPredecessorSuccessor

    fun mapLaneChange(srcLaneChange: LaneChange): ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange =
        when (srcLaneChange) {
            LaneChange.INCREASE -> ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.INCREASE
            LaneChange.DECREASE -> ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.DECREASE
            LaneChange.BOTH -> ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.BOTH
            LaneChange.NONE -> ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.NONE
        }

    @Mappings(
        Mapping(source = "typeAttribute", target = "type"),
        Mapping(source = "type", target = "typeAttribute")
    )
    abstract fun map(roadMark: Lane.RoadMark): RoadLanesLaneSectionLCRLaneRoadMark

    fun map(srcType: RoadmarkType): ERoadMarkType = when (srcType) {
        RoadmarkType.NONE -> ERoadMarkType.NONE
        RoadmarkType.SOLID -> ERoadMarkType.SOLID
        RoadmarkType.BROKEN -> ERoadMarkType.BROKEN
        RoadmarkType.SOLID___SOLID -> ERoadMarkType.SOLID_SOLID
        RoadmarkType.SOLID___BROKEN -> ERoadMarkType.SOLID_BROKEN
        RoadmarkType.BROKEN___SOLID -> ERoadMarkType.BROKEN_SOLID
        RoadmarkType.BROKEN___BROKEN -> ERoadMarkType.BROKEN_BROKEN
        RoadmarkType.BOTTS___DOTS -> ERoadMarkType.BOTTS_DOTS
        RoadmarkType.GRASS -> ERoadMarkType.GRASS
        RoadmarkType.CURB -> ERoadMarkType.CURB
    }

    fun map(srcRule: Rule): ERoadMarkRule = when (srcRule) {
        Rule.NO___PASSING -> ERoadMarkRule.NOPASSING
        Rule.CAUTION -> ERoadMarkRule.CAUTION
        Rule.NONE -> ERoadMarkRule.NONE
    }

    fun mapUnit(srcUnit: Unit): EUnitSpeed = when (srcUnit) {
        Unit.M___S -> EUnitSpeed.METER_PER_SECOND
        Unit.MPH -> EUnitSpeed.MILES_PER_HOUR
        Unit.KM___H -> EUnitSpeed.KILOMETER_PER_HOUR
        else -> EUnitSpeed.UNKNOWN
    }

    fun map(srcRestriction: Restriction): EAccessRestrictionType = when (srcRestriction) {
        Restriction.SIMULATOR -> EAccessRestrictionType.SIMULATOR
        Restriction.AUTONOMOUS___TRAFFIC -> EAccessRestrictionType.AUTONOMOUS_TRAFFIC
        Restriction.PEDESTRIAN -> EAccessRestrictionType.PEDESTRIAN
        Restriction.NONE -> EAccessRestrictionType.NONE
    }

    fun map(srcDynamic: Dynamic): Boolean = srcDynamic == Dynamic.YES

    fun map(srcOrientation: String): EOrientation = when (srcOrientation) {
        "+" -> EOrientation.PLUS
        "-" -> EOrientation.MINUS
        else -> EOrientation.NONE
    }

    fun map(srcUnit: Unit): EUnit = when (srcUnit) {
        Unit.M -> EUnit.METER
        Unit.KM -> EUnit.KILOMETER
        Unit.FT -> EUnit.FEET
        Unit.MILE -> EUnit.MILE

        Unit.M___S -> EUnit.METER_PER_SECOND
        Unit.MPH -> EUnit.MILES_PER_HOUR
        Unit.KM___H -> EUnit.KILOMETER_PER_HOUR

        Unit.KG -> EUnit.KILOGRAM
        Unit.T -> EUnit.TON

        Unit.PERCENT -> EUnit.PERCENT
    }

    @Mapping(source = "country", target = "countryCode")
    abstract fun mapRoadSignalsSignal(srcSignal: Signals.Signal): RoadSignalsSignal

    fun mapCountryCode(srcCountry: String): CountryCode =
        when (srcCountry.uppercase()) {
            "AUSTRALIA" -> CountryCode("AUT")
            "BRAZIL" -> CountryCode("BRA")
            "CHINA" -> CountryCode("CHN")
            "FRANCE" -> CountryCode("FRA")
            "GERMANY" -> CountryCode("DEU")
            "ITALY" -> CountryCode("ITA")
            "SWITZERLAND" -> CountryCode("CHE")
            "USA" -> CountryCode("USA")
            "OPENDRIVE" -> CountryCode(srcCountry)
            else -> CountryCode(srcCountry)
        }
}
