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

package io.rtron.readerwriter.opendrive.reader.mapper.opendrive16

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.model.opendrive.railroad.RoadRailroad
import io.rtron.model.opendrive.road.ERoadLinkElementType
import io.rtron.model.opendrive.road.ETrafficRule
import io.rtron.model.opendrive.road.Road
import io.rtron.model.opendrive.road.RoadLink
import io.rtron.model.opendrive.road.RoadLinkPredecessorSuccessor
import io.rtron.model.opendrive.road.RoadSurface
import io.rtron.model.opendrive.road.RoadTypeSpeed
import io.rtron.model.opendrive.road.elevation.RoadElevationProfile
import io.rtron.model.opendrive.road.lateral.RoadLateralProfile
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometry
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometryArc
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometryLine
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometryParamPoly3
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometryPoly3
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometrySpiral
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import io.rtron.readerwriter.opendrive.reader.mapper.common.toUpperCaseVariations
import org.asam.opendrive16.E_TrafficRule
import org.asam.opendrive16.T_Road
import org.asam.opendrive16.T_Road_ElevationProfile
import org.asam.opendrive16.T_Road_LateralProfile
import org.asam.opendrive16.T_Road_Link
import org.asam.opendrive16.T_Road_Link_PredecessorSuccessor
import org.asam.opendrive16.T_Road_PlanView_Geometry
import org.asam.opendrive16.T_Road_PlanView_Geometry_Arc
import org.asam.opendrive16.T_Road_PlanView_Geometry_Line
import org.asam.opendrive16.T_Road_PlanView_Geometry_ParamPoly3
import org.asam.opendrive16.T_Road_PlanView_Geometry_Poly3
import org.asam.opendrive16.T_Road_PlanView_Geometry_Spiral
import org.asam.opendrive16.T_Road_Railroad
import org.asam.opendrive16.T_Road_Surface
import org.asam.opendrive16.T_Road_Type_Speed
import org.mapstruct.InjectionStrategy
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    uses = [OpendriveCommonMapper::class, Opendrive16CoreMapper::class, Opendrive16LaneMapper::class, Opendrive16ObjectMapper::class, Opendrive16JunctionMapper::class, Opendrive16SignalMapper::class],
    imports = [Option::class],
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
abstract class Opendrive16RoadMapper {

    abstract fun mapRoad(source: T_Road): Road

    //
    // Road Link
    //
    abstract fun mapRoadLink(source: T_Road_Link): RoadLink
    abstract fun mapRoadLinkPredecessorSuccessor(source: T_Road_Link_PredecessorSuccessor): RoadLinkPredecessorSuccessor

    //
    // Road Type
    //
    abstract fun mapRoadTypeSpeed(source: T_Road_Type_Speed): RoadTypeSpeed

    //
    // Plan View
    //
    abstract fun mapPlanViewGeometry(source: T_Road_PlanView_Geometry): RoadPlanViewGeometry

    abstract fun mapPlanViewGeometryLine(source: T_Road_PlanView_Geometry_Line): RoadPlanViewGeometryLine
    abstract fun mapPlanViewGeometrySpiral(source: T_Road_PlanView_Geometry_Spiral): RoadPlanViewGeometrySpiral
    abstract fun mapPlanViewGeometryArc(source: T_Road_PlanView_Geometry_Arc): RoadPlanViewGeometryArc
    abstract fun mapPlanViewGeometryPoly3(source: T_Road_PlanView_Geometry_Poly3): RoadPlanViewGeometryPoly3
    abstract fun mapPlanViewGeometryParamPoly3(source: T_Road_PlanView_Geometry_ParamPoly3): RoadPlanViewGeometryParamPoly3

    //
    // Profiles
    //
    abstract fun mapElevationProfile(source: T_Road_ElevationProfile): RoadElevationProfile
    abstract fun mapLateralProfile(source: T_Road_LateralProfile): RoadLateralProfile

    //
    // Surface
    //
    abstract fun mapRoadSurface(source: T_Road_Surface): RoadSurface

    //
    // Railroad
    //
    abstract fun mapRoadRailroad(source: T_Road_Railroad): RoadRailroad

    //
    // Enumerations
    //
    fun mapElementTypeToOption(source: String?): Option<ERoadLinkElementType> =
        source?.let { mapElementType(it).some() } ?: None

    fun mapElementType(source: String): ERoadLinkElementType = when (source.uppercase()) {
        in ERoadLinkElementType.ROAD.name.toUpperCaseVariations() -> ERoadLinkElementType.ROAD
        in ERoadLinkElementType.JUNCTION.name.toUpperCaseVariations() -> ERoadLinkElementType.JUNCTION
        else -> ERoadLinkElementType.ROAD
    }

    fun mapTrafficRuleToOption(source: E_TrafficRule?): Option<ETrafficRule> =
        source?.let { mapTrafficRule(it).some() } ?: None

    fun mapTrafficRule(source: E_TrafficRule): ETrafficRule = when (source) {
        E_TrafficRule.RHT -> ETrafficRule.RIGHT_HAND_TRAFFIC
        E_TrafficRule.LHT -> ETrafficRule.LEFT_HAND_TRAFFIC
    }
}
