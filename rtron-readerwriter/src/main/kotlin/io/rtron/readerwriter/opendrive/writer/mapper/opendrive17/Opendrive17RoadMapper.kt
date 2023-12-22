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
import arrow.core.getOrElse
import io.rtron.model.opendrive.junction.EContactPoint
import io.rtron.model.opendrive.junction.EElementDir
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
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometryArc
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometryLine
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometryParamPoly3
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometryPoly3
import io.rtron.model.opendrive.road.planview.RoadPlanViewGeometrySpiral
import io.rtron.readerwriter.opendrive.writer.mapper.common.OpendriveCommonMapper
import org.asam.opendrive17.E_ContactPoint
import org.asam.opendrive17.E_Road_Link_ElementType
import org.asam.opendrive17.E_TrafficRule
import org.asam.opendrive17.T_Road
import org.asam.opendrive17.T_Road_ElevationProfile
import org.asam.opendrive17.T_Road_LateralProfile
import org.asam.opendrive17.T_Road_Link
import org.asam.opendrive17.T_Road_Link_PredecessorSuccessor
import org.asam.opendrive17.T_Road_PlanView_Geometry_Arc
import org.asam.opendrive17.T_Road_PlanView_Geometry_Line
import org.asam.opendrive17.T_Road_PlanView_Geometry_ParamPoly3
import org.asam.opendrive17.T_Road_PlanView_Geometry_Poly3
import org.asam.opendrive17.T_Road_PlanView_Geometry_Spiral
import org.asam.opendrive17.T_Road_Railroad
import org.asam.opendrive17.T_Road_Surface
import org.asam.opendrive17.T_Road_Type_Speed
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = [OpendriveCommonMapper::class, Opendrive17CoreMapper::class, Opendrive17LaneMapper::class, Opendrive17ObjectMapper::class, Opendrive17SignalMapper::class])
abstract class Opendrive17RoadMapper {

    //
    // Road
    //
    abstract fun mapRoad(source: Road): T_Road

    //
    // Road link
    //
    fun mapOptionRoadLink(source: Option<RoadLink>): T_Road_Link? = source.fold({ null }, { mapRoadLink(it) })
    abstract fun mapRoadLink(source: RoadLink): T_Road_Link

    fun mapOptionRoadLinkPredecessorSuccessor(source: Option<RoadLinkPredecessorSuccessor>): T_Road_Link_PredecessorSuccessor? = source.fold({ null }, { mapRoadLinkPredecessorSuccessor(it) })
    abstract fun mapRoadLinkPredecessorSuccessor(source: RoadLinkPredecessorSuccessor): T_Road_Link_PredecessorSuccessor

    fun mapOptionEContactPoint(source: Option<EContactPoint>): E_ContactPoint? = source.fold({ null }, { mapEContactPoint(it) })
    abstract fun mapEContactPoint(source: EContactPoint): E_ContactPoint

    fun mapOptionERoadLinkElementType(source: Option<ERoadLinkElementType>): E_Road_Link_ElementType? = source.fold({ null }, { mapERoadLinkElementType(it) })
    abstract fun mapERoadLinkElementType(source: ERoadLinkElementType): E_Road_Link_ElementType

    //
    // Road type
    //
    fun mapOptionRoadTypeSpeed(source: Option<RoadTypeSpeed>): T_Road_Type_Speed? = source.fold({ null }, { mapRoadTypeSpeed(it) })
    abstract fun mapRoadTypeSpeed(source: RoadTypeSpeed): T_Road_Type_Speed

    //
    // Plan view
    //
    fun mapOptionRoadPlanViewGeometryLine(source: Option<RoadPlanViewGeometryLine>): T_Road_PlanView_Geometry_Line? = source.fold({ null }, { mapRoadPlanViewGeometryLine(it) })
    abstract fun mapRoadPlanViewGeometryLine(source: RoadPlanViewGeometryLine): T_Road_PlanView_Geometry_Line

    fun mapOptionRoadPlanViewGeometrySpiral(source: Option<RoadPlanViewGeometrySpiral>): T_Road_PlanView_Geometry_Spiral? = source.fold({ null }, { mapRoadPlanViewGeometrySpiral(it) })
    abstract fun mapRoadPlanViewGeometrySpiral(source: RoadPlanViewGeometrySpiral): T_Road_PlanView_Geometry_Spiral

    fun mapOptionRoadPlanViewGeometryArc(source: Option<RoadPlanViewGeometryArc>): T_Road_PlanView_Geometry_Arc? = source.fold({ null }, { mapRoadPlanViewGeometryArc(it) })
    abstract fun mapRoadPlanViewGeometryArc(source: RoadPlanViewGeometryArc): T_Road_PlanView_Geometry_Arc

    fun mapOptionRoadPlanViewGeometryPoly3(source: Option<RoadPlanViewGeometryPoly3>): T_Road_PlanView_Geometry_Poly3? = source.fold({ null }, { mapRoadPlanViewGeometryPoly3(it) })
    abstract fun mapRoadPlanViewGeometryPoly3(source: RoadPlanViewGeometryPoly3): T_Road_PlanView_Geometry_Poly3

    fun mapOptionRoadPlanViewGeometryParamPoly3(source: Option<RoadPlanViewGeometryParamPoly3>): T_Road_PlanView_Geometry_ParamPoly3? = source.fold({ null }, { mapRoadPlanViewGeometryParamPoly3(it) })
    abstract fun mapRoadPlanViewGeometryParamPoly3(source: RoadPlanViewGeometryParamPoly3): T_Road_PlanView_Geometry_ParamPoly3

    //
    // Elevation profile
    //
    fun mapOptionRoadElevationProfile(source: Option<RoadElevationProfile>): T_Road_ElevationProfile? = source.fold({ null }, { mapRoadElevationProfile(it) })
    abstract fun mapRoadElevationProfile(source: RoadElevationProfile): T_Road_ElevationProfile

    //
    // Lateral profile
    //
    fun mapLateralProfile(source: Option<RoadLateralProfile>): T_Road_LateralProfile? = source.fold({ null }, { mapLateralProfile(it) })
    abstract fun mapLateralProfile(source: RoadLateralProfile): T_Road_LateralProfile

    //
    // Surface
    //
    fun mapOptionRoadSurface(source: Option<RoadSurface>): T_Road_Surface? = source.fold({ null }, { mapRoadSurface(it) })
    abstract fun mapRoadSurface(source: RoadSurface): T_Road_Surface

    //
    // Railroad
    //
    fun mapOptionRoadRailroad(source: Option<RoadRailroad>): T_Road_Railroad? = source.fold({ null }, { mapRoadRailroad(it) })
    abstract fun mapRoadRailroad(source: RoadRailroad): T_Road_Railroad

    //
    // Enumerations
    //
    fun mapOptionETrafficRule(source: Option<ETrafficRule>): E_TrafficRule? = source.fold({ null }, { mapETrafficRule(it) })
    fun mapETrafficRule(source: ETrafficRule): E_TrafficRule = when (source) {
        ETrafficRule.RIGHT_HAND_TRAFFIC -> E_TrafficRule.RHT
        ETrafficRule.LEFT_HAND_TRAFFIC -> E_TrafficRule.LHT
    }

    fun mapOptionEElementDir(source: Option<EElementDir>): String = when (source.getOrElse { EElementDir.PLUS }) {
        EElementDir.PLUS -> "+"
        EElementDir.MINUS -> "-"
    }
}
