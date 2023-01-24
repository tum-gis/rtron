/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.converter.roadspaces2citygml.module

import arrow.core.getOrElse
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.LevelOfDetail
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateGeometryOrImplicitGeometry
import io.rtron.transformer.messages.roadspaces.of
import org.citygml4j.core.model.cityfurniture.CityFurniture

/**
 * Builder for city objects of the CityGML CityFurniture module.
 */
class CityFurnitureModuleBuilder(
    private val parameters: Roadspaces2CitygmlParameters,
    private val identifierAdder: IdentifierAdder
) {
    // Properties and Initializers
    private val attributesAdder = AttributesAdder(parameters)

    // Methods
    fun createCityFurnitureFeature(roadspaceObject: RoadspaceObject): ContextMessageList<CityFurniture> {
        val cityFurnitureFeature = CityFurniture()
        val messageList = DefaultMessageList()

        // geometry
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, parameters)
        cityFurnitureFeature.populateGeometryOrImplicitGeometry(geometryTransformer, LevelOfDetail.TWO)
            .onLeft { messageList += DefaultMessage.of("", it.message, roadspaceObject.id, Severity.WARNING, wasFixed = true) }

        geometryTransformer.rotation.tap {
            attributesAdder.addRotationAttributes(it, cityFurnitureFeature)
        }

        // semantics
        identifierAdder.addIdentifier(roadspaceObject.id, roadspaceObject.name.getOrElse { "" }, cityFurnitureFeature) // TODO fix option
        attributesAdder.addAttributes(roadspaceObject, cityFurnitureFeature)

        return ContextMessageList(cityFurnitureFeature, messageList)
    }
}
