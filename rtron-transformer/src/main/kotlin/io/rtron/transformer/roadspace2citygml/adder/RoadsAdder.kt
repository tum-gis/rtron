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
import io.rtron.std.handleAndRemoveFailure
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
     * Adds the lines of the center lane (id=0) of a [Road] class (RoadSpaces model) to the [CityModel] (CityGML model).
     */
    fun addRoadCenterLaneLines(srcRoad: Road, dstCityModel: CityModel) {
        srcRoad.getAllCenterLanes()
                .forEach { addLaneLine(it.first, "RoadCenterLaneLine", it.second, it.third, dstCityModel) }
    }

    /**
     * Adds lane surfaces of a [Road] class (RoadSpaces model) to the [CityModel] (CityGML model).
     */
    fun addLaneSurfaces(srcRoad: Road, dstCityModel: CityModel) {

        srcRoad.getAllLanes(configuration.parameters.discretizationStepSize)
                .handleAndRemoveFailure { _reportLogger.log(it) }
                .forEach { addLaneSurface(it.first, "LaneSurface", it.second, it.third, dstCityModel) }
    }

    /**
     * Adds the relevant lines (center line and lane boundaries) of a [Road] class (RoadSpaces model) to
     * the [CityModel] (CityGML model).
     */
    fun addLaneLines(srcRoad: Road, dstCityModel: CityModel) {

        srcRoad.getAllLeftLaneBoundaries()
                .forEach { addLaneLine(it.first, "LeftLaneBoundary", it.second, AttributeList.EMPTY, dstCityModel) }
        srcRoad.getAllRightLaneBoundaries()
                .forEach { addLaneLine(it.first, "RightLaneBoundary", it.second, AttributeList.EMPTY, dstCityModel) }
        srcRoad.getAllCurvesOnLanes(0.5)
                .forEach { addLaneLine(it.first, "LaneCenterLine", it.second, AttributeList.EMPTY, dstCityModel) }
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

    /**
     * Adds road markings of a [Road] class (RoadSpaces model) to the [CityModel] (CityGML model).
     */
    fun addRoadMarkings(srcRoad: Road, dstCityModel: CityModel) {
        srcRoad.getAllRoadMarkings(configuration.parameters.discretizationStepSize)
                .handleAndRemoveFailure { _reportLogger.log(it) }
                .forEach {
            val genericCityObject = _genericsModuleBuilder
                    .createGenericObject(it.second)
                    .handleFailure { _reportLogger.log(it); return }

            _identifierAdder.addIdentifier(it.first, "RoadMark", genericCityObject)
            _attributesAdder.addAttributes(it.first.toAttributes(configuration.parameters.identifierAttributesPrefix) +
                    it.third, genericCityObject)
            dstCityModel.addCityObjectMember(CityObjectMember(genericCityObject))
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

    private fun addLaneLine(id: LaneIdentifier, name: String, curve: AbstractCurve3D, attributes: AttributeList,
                            dstCityModel: CityModel) {

        val roadObject = _genericsModuleBuilder
                .createGenericObject(curve)
                .handleFailure { _reportLogger.log(it); return }

        _identifierAdder.addIdentifier(id, name, roadObject)
        _attributesAdder.addAttributes(id.toAttributes(configuration.parameters.identifierAttributesPrefix) +
                attributes, roadObject)
        dstCityModel.addCityObjectMember(CityObjectMember(roadObject))
    }

}
