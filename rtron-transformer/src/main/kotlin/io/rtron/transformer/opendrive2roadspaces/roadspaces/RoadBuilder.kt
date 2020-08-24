/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.opendrive2roadspaces.roadspaces

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import io.rtron.math.geometry.curved.threed.surface.CurveRelativeParametricSurface3D
import io.rtron.model.opendrive.road.lanes.RoadLanes
import io.rtron.model.opendrive.road.lanes.RoadLanesLaneSection
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneSection
import io.rtron.model.roadspaces.roadspace.road.LaneSectionIdentifier
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.std.handleAndRemoveFailure
import io.rtron.std.handleFailure
import io.rtron.transformer.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesConfiguration


/**
 * Builder for [Road] objects of the RoadSpaces data model.
 */
class RoadBuilder(
        private val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    private val _functionBuilder = FunctionBuilder(_reportLogger, configuration.parameters)
    private val _laneBuilder = LaneBuilder(configuration)

    // Methods

    /**
     * Builds a single road from the OpenDRIVE data model.
     *
     * @param id identifier of the road space
     * @param srcLanes source lane models of OpenDRIVE
     * @param roadSurface road surface with torsion applied
     * @param roadSurfaceWithoutTorsion road surface without torsion applied (needed for lanes with true level entry)
     * @param baseAttributes attributes attached to each element of the road (e.g. lanes)
     */
    fun buildRoad(id: RoadspaceIdentifier, srcLanes: RoadLanes, roadSurface: CurveRelativeParametricSurface3D,
                  roadSurfaceWithoutTorsion: CurveRelativeParametricSurface3D, baseAttributes: AttributeList): Result<Road, Exception> {

        val laneOffset = _functionBuilder.buildLaneOffset(id, srcLanes)
        val laneSections = srcLanes.laneSection
                .mapIndexed { currentId, currentLaneSection ->
                    val laneSectionIdentifier = LaneSectionIdentifier(currentId, currentLaneSection.s, id)
                    buildLaneSection(laneSectionIdentifier, currentLaneSection, baseAttributes)
                }
                .handleAndRemoveFailure { _reportLogger.log(it, id.toString()) }

        if (laneSections.isEmpty())
            return Result.error(IllegalArgumentException("Road element contains no valid lane sections."))

        val road = Road(id, roadSurface, roadSurfaceWithoutTorsion, laneOffset, laneSections)
        return Result.success(road)
    }

    /**
     * Builds a [LaneSection] which corresponds to OpenDRIVE's concept of lane sections.
     */
    private fun buildLaneSection(laneSectionIdentifier: LaneSectionIdentifier, srcLaneSection: RoadLanesLaneSection,
                                 baseAttributes: AttributeList): Result<LaneSection, Exception> {

        // check whether source model is processable
        srcLaneSection.isProcessable()
                .map { _reportLogger.log(it, laneSectionIdentifier.toString()) }
                .handleFailure { return it }

        val laneSectionAttributes = buildAttributes(srcLaneSection)
        val lanes = srcLaneSection.getLeftRightLanes()
                .map { (currentLaneId, currentSrcLane) ->
                    val laneIdentifier = LaneIdentifier(currentLaneId, laneSectionIdentifier)
                    val attributes = baseAttributes + laneSectionAttributes
                    _laneBuilder.buildLane(laneIdentifier, currentSrcLane, attributes)
                }

        val laneSection = LaneSection(laneSectionIdentifier, srcLaneSection.laneSectionStart, lanes)
        return Result.success(laneSection)
    }

    private fun buildAttributes(srcLaneSection: RoadLanesLaneSection) =
            attributes("${configuration.parameters.attributesPrefix}laneSection_") {
                attribute("curvePositionStart", srcLaneSection.laneSectionStart.curvePosition)
            }
}
