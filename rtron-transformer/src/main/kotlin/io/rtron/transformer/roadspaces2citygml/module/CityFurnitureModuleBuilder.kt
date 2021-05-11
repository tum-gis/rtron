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

package io.rtron.transformer.roadspaces2citygml.module

import com.github.kittinunf.result.Result
import io.rtron.io.logging.LogManager
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.roadspaces2citygml.geometry.LevelOfDetail
import io.rtron.transformer.roadspaces2citygml.geometry.populateGeometryOrImplicitGeometry
import org.citygml4j.model.cityfurniture.CityFurniture

/**
 * Builder for city objects of the CityGML CityFurniture module.
 */
class CityFurnitureModuleBuilder(
    private val configuration: Roadspaces2CitygmlConfiguration,
    private val identifierAdder: IdentifierAdder
) {
    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)
    private val _attributesAdder = AttributesAdder(configuration)

    // Methods
    fun createCityFurnitureFeature(roadspaceObject: RoadspaceObject): Result<CityFurniture, Exception> {
        val cityFurnitureFeature = CityFurniture()

        // geometry
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        cityFurnitureFeature.populateGeometryOrImplicitGeometry(geometryTransformer, LevelOfDetail.TWO)
            .handleFailure { return it }
        if (geometryTransformer.isSetRotation())
            geometryTransformer.getRotation()
                .handleFailure { return it }
                .also { _attributesAdder.addRotationAttributes(it, cityFurnitureFeature) }

        // semantics
        identifierAdder.addIdentifier(roadspaceObject.id, roadspaceObject.name, cityFurnitureFeature)
        _attributesAdder.addAttributes(roadspaceObject, cityFurnitureFeature)

        return Result.success(cityFurnitureFeature)
    }
}
