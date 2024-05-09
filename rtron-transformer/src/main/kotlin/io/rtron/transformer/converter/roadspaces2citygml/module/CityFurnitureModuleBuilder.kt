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

package io.rtron.transformer.converter.roadspaces2citygml.module

import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1Geometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1ImplicitGeometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2Geometry
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveGmlIdentifier
import io.rtron.transformer.issues.roadspaces.of
import org.citygml4j.core.model.cityfurniture.CityFurniture

/**
 * Builder for city objects of the CityGML CityFurniture module.
 */
class CityFurnitureModuleBuilder(
    private val parameters: Roadspaces2CitygmlParameters,
) {
    // Properties and Initializers
    private val relationAdder = RelationAdder(parameters)
    private val attributesAdder = AttributesAdder(parameters)

    // Methods
    fun createCityFurnitureFeature(roadspaceObject: RoadspaceObject): ContextIssueList<CityFurniture> {
        val cityFurnitureFeature = CityFurniture()
        val issueList = DefaultIssueList()

        // geometry
        val pointGeometryTransformer = GeometryTransformer.of(roadspaceObject.pointGeometry, parameters)
        cityFurnitureFeature.populateLod1ImplicitGeometry(pointGeometryTransformer)
        pointGeometryTransformer.rotation.onSome {
            attributesAdder.addRotationAttributes(it, cityFurnitureFeature)
        }

        roadspaceObject.boundingBoxGeometry.onSome { currentBoundingBoxGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentBoundingBoxGeometry, parameters)
            cityFurnitureFeature.populateLod1Geometry(geometryTransformer)
                .mapLeft {
                    issueList +=
                        DefaultIssue.of(
                            "NoSuitableGeometryForCityFurnitureLod1",
                            it.message, roadspaceObject.id, Severity.WARNING, wasFixed = true,
                        )
                }
        }

        roadspaceObject.complexGeometry.onSome { currentComplexGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentComplexGeometry, parameters)
            cityFurnitureFeature.populateLod2Geometry(geometryTransformer)
                .onLeft {
                    issueList +=
                        DefaultIssue.of(
                            "NoSuitableGeometryForCityFurnitureLod2",
                            it.message, roadspaceObject.id, Severity.WARNING, wasFixed = true,
                        )
                }
        }

        // semantics
        IdentifierAdder.addIdentifier(
            roadspaceObject.id.deriveGmlIdentifier(parameters.gmlIdPrefix),
            roadspaceObject.name,
            cityFurnitureFeature,
        )
        relationAdder.addBelongToRelations(roadspaceObject, cityFurnitureFeature)
        attributesAdder.addAttributes(roadspaceObject, cityFurnitureFeature)

        return ContextIssueList(cityFurnitureFeature, issueList)
    }
}
