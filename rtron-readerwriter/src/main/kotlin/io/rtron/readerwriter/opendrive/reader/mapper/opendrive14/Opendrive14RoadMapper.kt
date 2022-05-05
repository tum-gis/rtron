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
import io.rtron.io.logging.LogManager
import io.rtron.model.opendrive.junction.EContactPoint
import io.rtron.model.opendrive.railroad.RoadRailroad
import io.rtron.model.opendrive.road.ERoadLinkElementType
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
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import org.asam.opendrive14.ContactPoint
import org.asam.opendrive14.ElementType
import org.asam.opendrive14.OpenDRIVE
import org.mapstruct.BeforeMapping
import org.mapstruct.InjectionStrategy
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = [OpendriveCommonMapper::class, Opendrive14CoreMapper::class, Opendrive14LaneMapper::class, Opendrive14ObjectMapper::class, Opendrive14SignalMapper::class], imports = [Option::class], injectionStrategy = InjectionStrategy.CONSTRUCTOR)
abstract class Opendrive14RoadMapper {

    var reportLogger = LogManager.getReportLogger("general")

    //
    // Road Link
    //
    abstract fun mapRoadLink(source: OpenDRIVE.Road.Link): RoadLink

    abstract fun mapRoadLinkPredecessor(source: OpenDRIVE.Road.Link.Predecessor): RoadLinkPredecessorSuccessor
    abstract fun mapRoadLinkSuccessor(source: OpenDRIVE.Road.Link.Successor): RoadLinkPredecessorSuccessor

    //
    // Plan View
    //
    abstract fun mapPlanViewGeometryLine(source: OpenDRIVE.Road.PlanView.Geometry.Line): RoadPlanViewGeometryLine
    abstract fun mapPlanViewGeometrySpiral(source: OpenDRIVE.Road.PlanView.Geometry.Spiral): RoadPlanViewGeometrySpiral
    abstract fun mapPlanViewGeometryArc(source: OpenDRIVE.Road.PlanView.Geometry.Arc): RoadPlanViewGeometryArc
    abstract fun mapPlanViewGeometryPoly3(source: OpenDRIVE.Road.PlanView.Geometry.Poly3): RoadPlanViewGeometryPoly3
    abstract fun mapPlanViewGeometryParamPoly3(source: OpenDRIVE.Road.PlanView.Geometry.ParamPoly3): RoadPlanViewGeometryParamPoly3

    //
    // Profiles
    //
    abstract fun mapElevationProfile(source: OpenDRIVE.Road.ElevationProfile): RoadElevationProfile
    abstract fun mapLateralProfile(source: OpenDRIVE.Road.LateralProfile): RoadLateralProfile

    @BeforeMapping
    fun mapLateralProfileLogging(source: OpenDRIVE.Road.LateralProfile?) {
        if (source == null) return

        if (source.crossfall != null && source.crossfall.isNotEmpty())
            reportLogger.infoOnce("Since crossfall is not in the OpenDRIVE standard from version 1.6, it is not supported.")
    }

    //
    // Other Road Attributes
    //
    abstract fun mapRoadTypeSpeed(source: OpenDRIVE.Road.Type.Speed): RoadTypeSpeed
    abstract fun mapRoadSurface(source: OpenDRIVE.Road.Surface): RoadSurface
    abstract fun mapRoadRailroad(source: OpenDRIVE.Road.Railroad): RoadRailroad

    //
    // Enumerations
    //
    fun mapContactPointToOption(source: ContactPoint?): Option<EContactPoint> = source?.let { mapContactPoint(it).some() } ?: None
    abstract fun mapContactPoint(source: ContactPoint): EContactPoint

    fun mapERoadLinkElementTypeToOption(source: ElementType?): Option<ERoadLinkElementType> = source?.let { mapERoadLinkElementType(it).some() } ?: None
    abstract fun mapERoadLinkElementType(source: ElementType): ERoadLinkElementType
}
