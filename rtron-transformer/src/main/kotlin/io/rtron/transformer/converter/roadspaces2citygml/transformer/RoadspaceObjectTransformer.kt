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

package io.rtron.transformer.converter.roadspaces2citygml.transformer

import arrow.core.None
import arrow.core.Option
import arrow.core.flattenOption
import arrow.core.some
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.MessageList
import io.rtron.io.messages.mergeMessageLists
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.module.BuildingModuleBuilder
import io.rtron.transformer.converter.roadspaces2citygml.module.CityFurnitureModuleBuilder
import io.rtron.transformer.converter.roadspaces2citygml.module.GenericsModuleBuilder
import io.rtron.transformer.converter.roadspaces2citygml.module.IdentifierAdder
import io.rtron.transformer.converter.roadspaces2citygml.module.VegetationModuleBuilder
import io.rtron.transformer.converter.roadspaces2citygml.router.RoadspaceObjectRouter
import org.citygml4j.core.model.core.AbstractCityObject
import org.citygml4j.core.model.core.CityModel

/**
 * Transforms [RoadspaceObject] classes (RoadSpaces model) to the [CityModel] (CityGML model).
 */
class RoadspaceObjectTransformer(
    private val configuration: Roadspaces2CitygmlConfiguration,
    private val identifierAdder: IdentifierAdder
) {

    // Properties and Initializers
    private val _genericsModuleBuilder = GenericsModuleBuilder(configuration, identifierAdder)
    private val _buildingModuleBuilder = BuildingModuleBuilder(configuration, identifierAdder)
    private val _cityFurnitureModuleBuilder = CityFurnitureModuleBuilder(configuration, identifierAdder)
    private val _vegetationModuleBuilder = VegetationModuleBuilder(configuration, identifierAdder)

    // Methods

    /**
     * Transforms a list of [roadspaceObjects] (RoadSpaces model) to the [AbstractCityObject] (CityGML model).
     */
    fun transformRoadspaceObjects(roadspaceObjects: List<RoadspaceObject>): ContextMessageList<List<AbstractCityObject>> {
        return roadspaceObjects
            .map { transformSingleRoadspaceObject(it) }
            .mergeMessageLists()
            .map { it.flattenOption() }
    }

    /**
     * Creates a city object (CityGML model) from the [RoadspaceObject] and it's geometry.
     * Contains the rules which determine the CityGML feature types from the [RoadspaceObject].
     *
     * @param roadspaceObject road space object from the RoadSpaces model
     * @return city object (CityGML model)
     */
    private fun transformSingleRoadspaceObject(roadspaceObject: RoadspaceObject): ContextMessageList<Option<AbstractCityObject>> {
        val messageList = MessageList()

        val cityObjects: Option<AbstractCityObject> = when (RoadspaceObjectRouter.route(roadspaceObject)) {
            RoadspaceObjectRouter.CitygmlTargetFeatureType.BUILDING_BUILDING -> _buildingModuleBuilder.createBuildingFeature(roadspaceObject).handleMessageList { messageList += it }.some()
            RoadspaceObjectRouter.CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE -> _cityFurnitureModuleBuilder.createCityFurnitureFeature(roadspaceObject).handleMessageList { messageList += it }.some()
            RoadspaceObjectRouter.CitygmlTargetFeatureType.GENERICS_GENERICOCCUPIEDSPACE -> _genericsModuleBuilder.createGenericOccupiedSpaceFeature(roadspaceObject).handleMessageList { messageList += it }.some()
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> None
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> None
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_MARKING -> None
            RoadspaceObjectRouter.CitygmlTargetFeatureType.VEGETATION_SOLITARYVEGEATIONOBJECT -> _vegetationModuleBuilder.createSolitaryVegetationFeature(roadspaceObject).handleMessageList { messageList += it }.some()
        }

        return ContextMessageList(cityObjects, messageList)
    }
}
