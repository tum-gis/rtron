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

package io.rtron.transformer.roadspaces2citygml.transformer

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.toAttributes
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.model.roadspaces.topology.LaneTopology
import io.rtron.std.handleAndRemoveFailure
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspaces2citygml.module.GenericsModuleBuilder
import io.rtron.transformer.roadspaces2citygml.module.TransportationModuleBuilder
import io.rtron.transformer.roadspaces2citygml.parameter.Roadspaces2CitygmlConfiguration
import org.citygml4j.model.core.AbstractCityObject
import org.citygml4j.model.core.CityModel
import org.citygml4j.model.generics.GenericOccupiedSpace
import org.citygml4j.model.transportation.Road as CitygmlRoad

/**
 * Transforms [Road] classes (RoadSpaces model) to the [CityModel] (CityGML model).
 */
class RoadsTransformer(
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
     * Transforms the lines of the center lane (id=0) of a [Road] class (RoadSpaces model) to the [AbstractCityObject]
     * (CityGML model).
     */
    fun transformRoadCenterLaneLines(srcRoad: Road): List<AbstractCityObject> =
        srcRoad.getAllCenterLanes()
            .map { transformLaneLine(it.first, "RoadCenterLaneLine", it.second, it.third) }
            .handleAndRemoveFailure { _reportLogger.log(it) }

    /**
     * Transforms lane surfaces of a [Road] class (RoadSpaces model) to the [AbstractCityObject] (CityGML model).
     */
    fun transformLaneSurfaces(srcRoad: Road): List<AbstractCityObject> =
        srcRoad.getAllLanes(configuration.parameters.discretizationStepSize)
            .handleAndRemoveFailure { _reportLogger.log(it) }
            .map { transformLaneSurface(it.first, "LaneSurface", it.second, it.third) }
            .handleAndRemoveFailure { _reportLogger.log(it) }

    /**
     * Transforms the relevant lines (center line and lane boundaries) of a [Road] class (RoadSpaces model) to
     * the [AbstractCityObject] (CityGML model).
     */
    fun transformLaneLines(srcRoad: Road): List<AbstractCityObject> =
        srcRoad.getAllLeftLaneBoundaries()
            .map { transformLaneLine(it.first, "LeftLaneBoundary", it.second, AttributeList.EMPTY) }
            .handleAndRemoveFailure { _reportLogger.log(it) } +
            srcRoad.getAllRightLaneBoundaries()
                .map { transformLaneLine(it.first, "RightLaneBoundary", it.second, AttributeList.EMPTY) }
                .handleAndRemoveFailure { _reportLogger.log(it) } +
            srcRoad.getAllCurvesOnLanes(0.5)
                .map { transformLaneLine(it.first, "LaneCenterLine", it.second, AttributeList.EMPTY) }
                .handleAndRemoveFailure { _reportLogger.log(it) }

    /**
     * Transforms lateral filler surfaces of a [Road] to the [AbstractCityObject] (CityGML model).
     * Lateral filler surfaces are between two adjacent lanes located within the same lane section.
     * This usually addresses vertical height offsets, which are caused for example by sidewalks.
     */
    fun transformLateralFillerSurfaces(srcRoad: Road): List<AbstractCityObject> =
        srcRoad.getAllLateralFillerSurfaces(configuration.parameters.discretizationStepSize)
            .map { transformLaneSurface(it.first, "LateralLaneFillerSurface", it.second, AttributeList.EMPTY) }
            .handleAndRemoveFailure { _reportLogger.log(it) }

    /**
     * Transforms longitudinal filler surfaces to the [AbstractCityObject] (CityGML model).
     * Longitudinal filler surfaces are between two successive lanes, which can be located in the same street or in
     * successive streets.
     */
    fun transformLongitudinalFillerSurfaces(srcRoad: Road, srcLaneTopology: LaneTopology): List<AbstractCityObject> =
        srcRoad.getAllLaneIdentifiers()
            .flatMap { srcLaneTopology.getLongitudinalFillerSurfaces(it) }
            .map {
                val name = if (it.laneId.isWithinSameRoad(it.successorLaneId))
                    "LongitudinalLaneFillerSurfaceWithinRoad"
                else "LongitudinalLaneFillerSurfaceBetweenRoads"

                val attributes = it.successorLaneId
                    .toAttributes(configuration.parameters.identifierAttributesPrefix + "to_")

                transformLaneSurface(it.laneId, name, it.surface, attributes)
            }
            .handleAndRemoveFailure { _reportLogger.log(it) }

    /**
     * Transforms road markings of a [Road] class (RoadSpaces model) to the [GenericOccupiedSpace] (CityGML model).
     */
    fun transformRoadMarkings(srcRoad: Road): List<GenericOccupiedSpace> =
        srcRoad.getAllRoadMarkings(configuration.parameters.discretizationStepSize)
            .handleAndRemoveFailure { _reportLogger.log(it, srcRoad.id.toString(), "Removing such road markings.") }
            .map { transformRoadMarking(it.first, "RoadMarking", it.second, it.third) }
            .handleAndRemoveFailure { _reportLogger.log(it) }

    private fun transformRoadMarking(
        id: LaneIdentifier,
        name: String,
        geometry: AbstractGeometry3D,
        attributes: AttributeList
    ): Result<GenericOccupiedSpace, Exception> {

        val genericCityObject = _genericsModuleBuilder
            .createGenericObject(geometry)
            .handleFailure { return it }

        _identifierAdder.addIdentifier(id, name, genericCityObject)
        _attributesAdder.addAttributes(
            id.toAttributes(configuration.parameters.identifierAttributesPrefix) +
                attributes,
            genericCityObject
        )
        return Result.success(genericCityObject)
    }

    private fun transformLaneSurface(
        id: LaneIdentifier,
        name: String,
        surface: AbstractSurface3D,
        attributes: AttributeList
    ): Result<CitygmlRoad, Exception> {

        val roadObject = _transportationModuleBuilder
            .createLaneSurface(surface)
            .handleFailure { return it }

        _identifierAdder.addIdentifier(id, name, roadObject)
        _attributesAdder.addAttributes(
            id.toAttributes(configuration.parameters.identifierAttributesPrefix) +
                attributes,
            roadObject
        )
        return Result.success(roadObject)
    }

    private fun transformLaneLine(id: LaneIdentifier, name: String, curve: AbstractCurve3D, attributes: AttributeList):
        Result<GenericOccupiedSpace, Exception> {

            val roadObject = _genericsModuleBuilder
                .createGenericObject(curve)
                .handleFailure { return it }

            _identifierAdder.addIdentifier(id, name, roadObject)
            _attributesAdder.addAttributes(
                id.toAttributes(configuration.parameters.identifierAttributesPrefix) +
                    attributes,
                roadObject
            )
            return Result.success(roadObject)
        }
}
