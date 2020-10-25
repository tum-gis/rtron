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

package io.rtron.transformer.roadspace2citygml.transformer

import com.github.kittinunf.result.Result
import io.rtron.io.logging.Logger
import io.rtron.model.roadspaces.roadspace.attribute.toAttributes
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.std.handleAndRemoveFailure
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspace2citygml.geometry.GeometryTransformer
import io.rtron.transformer.roadspace2citygml.module.*
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import org.citygml4j.model.citygml.core.AbstractCityObject
import org.citygml4j.model.citygml.core.CityModel


/**
 * Transforms [RoadspaceObject] classes (RoadSpaces model) to the [CityModel] (CityGML model).
 */
class RoadspaceObjectTransformer(
        private val configuration: Roadspaces2CitygmlConfiguration
) {

    // Properties and Initializers
    private val _reportLogger: Logger = configuration.getReportLogger()

    private val _identifierAdder = IdentifierAdder(configuration.parameters, _reportLogger)
    private val _attributesAdder = AttributesAdder(configuration.parameters)
    private val _genericsModuleBuilder = GenericsModuleBuilder(configuration)
    private val _buildingModuleBuilder = BuildingModuleBuilder(configuration)
    private val _cityFurnitureModuleBuilder = CityFurnitureModuleBuilder(configuration)
    private val _transportationModuleBuilder = TransportationModuleBuilder(configuration)
    private val _vegetationModuleBuilder = VegetationModuleBuilder(configuration)

    // Methods

    /**
     * Transforms a list of [srcRoadspaceObjects] (RoadSpaces model) to the [AbstractCityObject] (CityGML model).
     */
    fun transformRoadspaceObjects(srcRoadspaceObjects: List<RoadspaceObject>): List<AbstractCityObject> =
        srcRoadspaceObjects
                .map { transformSingleRoadspaceObject(it) }
                .handleAndRemoveFailure { _reportLogger.log(it) }

    private fun transformSingleRoadspaceObject(srcRoadspaceObject: RoadspaceObject): Result<AbstractCityObject, Exception> {
        val geometryTransformer = createGeometryTransformer(srcRoadspaceObject)
        val abstractCityObject = createAbstractCityObject(srcRoadspaceObject, geometryTransformer)
                .handleFailure { return it }

        _identifierAdder.addIdentifier(srcRoadspaceObject.id, abstractCityObject)
        _attributesAdder.addAttributes(
                srcRoadspaceObject.id.toAttributes(configuration.parameters.identifierAttributesPrefix) +
                        srcRoadspaceObject.attributes, abstractCityObject)

        return Result.success(abstractCityObject)
    }

    private fun createGeometryTransformer(srcRoadspaceObject: RoadspaceObject): GeometryTransformer {
        require(srcRoadspaceObject.geometry.size == 1)
        val currentGeometricPrimitive = srcRoadspaceObject.geometry.first()

        return GeometryTransformer(configuration.parameters, _reportLogger)
                .also { currentGeometricPrimitive.accept(it) }
    }

    /**
     * Creates a city object (CityGML model) from the [RoadspaceObject] and it's geometry.
     * Contains the rules which determine the CityGML feature types from the [RoadspaceObject].
     *
     * @param srcRoadspaceObject road space object from the RoadSpaces model
     * @param geometryTransformer transformed geometry
     * @return city object (CityGML model)
     */
    private fun createAbstractCityObject(srcRoadspaceObject: RoadspaceObject, geometryTransformer: GeometryTransformer):
            Result<AbstractCityObject, Exception> {

        // based on object name
        if (srcRoadspaceObject.name == "bench")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "bus")
            return _transportationModuleBuilder.createTransportationComplex(geometryTransformer,
                    TransportationModuleBuilder.Feature.ROAD)
        if (srcRoadspaceObject.name == "controllerBox")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "crossWalk")
            return _transportationModuleBuilder.createTransportationComplex(geometryTransformer,
                    TransportationModuleBuilder.Feature.ROAD)
        if (srcRoadspaceObject.name == "fence")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "noParkingArea")
            return _transportationModuleBuilder.createTransportationComplex(geometryTransformer,
                    TransportationModuleBuilder.Feature.ROAD)
        if (srcRoadspaceObject.name == "railing")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "raiseMedian")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "trafficLight")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "trafficSign")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "tree")
            return _vegetationModuleBuilder.createVegetationObject(geometryTransformer)
        if (srcRoadspaceObject.name == "unknown")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "wall")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)

        // based on object type
        if (srcRoadspaceObject.type == RoadObjectType.BARRIER)
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.TREE)
            return _vegetationModuleBuilder.createVegetationObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.VEGETATION)
            return _vegetationModuleBuilder.createVegetationObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.BUILDING)
            return _buildingModuleBuilder.createBuildingObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.STREET_LAMP)
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.SIGNAL)
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.POLE)
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)

        // if no rule for object name and type, create a generic city object
        return _genericsModuleBuilder.createGenericObject(geometryTransformer)
    }

}
