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

package io.rtron.transformer.roadspace2citygml.adder

import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.toAttributes
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.model.roadspaces.topology.LaneTopology
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspace2citygml.module.GenericsModuleBuilder
import io.rtron.transformer.roadspace2citygml.module.TransportationModuleBuilder
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import org.citygml4j.model.citygml.core.CityModel
import org.citygml4j.model.citygml.core.CityObjectMember


/**
 * Adds [Road] classes (RoadSpaces model) to the [CityModel] (CityGML model).
 */
class RoadsAdder(
        private val configuration: Roadspaces2CitygmlConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    private val _identifierAdder = IdentifierAdder(configuration.parameters, _reportLogger)
    private val _attributesAdder = AttributesAdder(configuration.parameters)
    private val _genericsModuleBuilder = GenericsModuleBuilder(configuration)
    private val _transportationModuleBuilder = TransportationModuleBuilder(configuration)

    // Methods

    /**
     * Adds lane surfaces of a [Road] class (RoadSpaces model) to the [CityModel] (CityGML model).
     */
    fun addLaneSurfaces(srcRoad: Road, dstCityModel: CityModel) {

        srcRoad.getAllLanes(configuration.parameters.discretizationStepSize)
                .forEach { addLaneSurface(it.first, "LaneSurface", it.second, it.third, dstCityModel) }
    }

    /**
     * Adds the relevant lines (center line and lane boundaries) of a [Road] class (RoadSpaces model) to
     * the [CityModel] (CityGML model).
     */
    fun addLaneLines(srcRoad: Road, dstCityModel: CityModel) {

        srcRoad.getAllLeftLaneBoundaries()
                .forEach { addLaneBoundary(it.first, "LeftLaneBoundary", it.second, dstCityModel) }
        srcRoad.getAllRightLaneBoundaries()
                .forEach { addLaneBoundary(it.first, "RightLaneBoundary", it.second, dstCityModel) }
        srcRoad.getAllCurvesOnLanes(0.5)
                .forEach { addLaneBoundary(it.first, "LaneCenterLine", it.second, dstCityModel) }
    }

    /**
     * Adds lateral filler surfaces of a [Road] to the [CityModel] (CityGML model).
     * Lateral filler surfaces are between two adjacent lanes located within the same lane section.
     * This usually addresses vertical height offsets, which are caused for example by sidewalks.
     */
    fun addLateralFillerSurfaces(srcRoad: Road, dstCityModel: CityModel) {
        srcRoad.getAllLateralFillerSurfaces(configuration.parameters.discretizationStepSize)
                .forEach {
                    addLaneSurface(it.first, "LateralLaneFillerSurface", it.second, AttributeList.EMPTY,
                            dstCityModel)
                }
    }

    /**
     * Adds longitudinal filler surfaces to the [CityModel] (CityGML model).
     * Longitudinal filler surfaces are between two successive lanes, which can be located in the same street or in
     * successive streets.
     */
    fun addLongitudinalFillerSurfaces(srcRoad: Road, srcLaneTopology: LaneTopology, dstCityModel: CityModel) {

        srcRoad.getAllLaneIdentifiers()
                .flatMap { srcLaneTopology.getLongitudinalFillerSurfaces(it) }
                .forEach {
                    val name = if (it.laneId.isWithinSameRoad(it.successorLaneId))
                        "LongitudinalLaneFillerSurfaceWithinRoad"
                    else "LongitudinalLaneFillerSurfaceBetweenRoads"

                    val attributes = it.successorLaneId
                            .toAttributes(configuration.parameters.identifierAttributesPrefix + "to_")

                    addLaneSurface(it.laneId, name, it.surface, attributes, dstCityModel)
                }
    }

    private fun addLaneSurface(id: LaneIdentifier, name: String, surface: AbstractSurface3D, attributes: AttributeList,
                               dstCityModel: CityModel) {

        val roadObject = _transportationModuleBuilder
                .createLaneSurface(surface)
                .handleFailure { _reportLogger.log(it); return }

        _identifierAdder.addIdentifier(id, name, roadObject)
        _attributesAdder.addAttributes(id.toAttributes(configuration.parameters.identifierAttributesPrefix) +
                attributes, roadObject)
        dstCityModel.addCityObjectMember(CityObjectMember(roadObject))
    }

    private fun addLaneBoundary(id: LaneIdentifier, name: String, curve: AbstractCurve3D, dstCityModel: CityModel) {

        val roadObject = _genericsModuleBuilder
                .createGenericObject(curve)
                .handleFailure { _reportLogger.log(it); return }

        _identifierAdder.addIdentifier(id, name, roadObject)
        _attributesAdder.addAttributes(id.toAttributes(configuration.parameters.identifierAttributesPrefix), roadObject)
        dstCityModel.addCityObjectMember(CityObjectMember(roadObject))
    }
}
