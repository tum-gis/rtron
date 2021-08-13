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

package io.rtron.transformer.roadspaces2citygml.module

import com.github.kittinunf.result.Result
import io.rtron.io.logging.LogManager
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.toAttributes
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.roadspaces2citygml.geometry.LevelOfDetail
import io.rtron.transformer.roadspaces2citygml.geometry.populateGeometryOrImplicitGeometry
import org.citygml4j.model.generics.GenericOccupiedSpace

/**
 * Builder for city objects of the CityGML Generics module.
 */
class GenericsModuleBuilder(
    private val configuration: Roadspaces2CitygmlConfiguration,
    private val identifierAdder: IdentifierAdder
) {
    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)
    private val _attributesAdder = AttributesAdder(configuration)

    // Methods
    fun createGenericOccupiedSpaceFeature(roadspaceObject: RoadspaceObject): Result<GenericOccupiedSpace, Exception> {
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        val genericOccupiedSpace = createGenericOccupiedSpaceFeature(geometryTransformer).handleFailure { return it }

        // semantics
        identifierAdder.addUniqueIdentifier(roadspaceObject.id, genericOccupiedSpace)
        _attributesAdder.addAttributes(roadspaceObject, genericOccupiedSpace)

        return Result.success(genericOccupiedSpace)
    }

    fun createGenericOccupiedSpaceFeature(id: LaneIdentifier, name: String, abstractGeometry: AbstractGeometry3D, attributes: AttributeList):
        Result<GenericOccupiedSpace, Exception> {

        val genericOccupiedSpace = createGenericOccupiedSpaceFeature(abstractGeometry).handleFailure { return it }

        identifierAdder.addIdentifier(id, name, genericOccupiedSpace)
        _attributesAdder.addAttributes(
            id.toAttributes(configuration.identifierAttributesPrefix) +
                attributes,
            genericOccupiedSpace
        )
        return Result.success(genericOccupiedSpace)
    }

    fun createGenericOccupiedSpaceFeature(id: RoadspaceIdentifier, name: String, abstractGeometry: AbstractGeometry3D, attributes: AttributeList):
        Result<GenericOccupiedSpace, Exception> {
        val genericOccupiedSpace = createGenericOccupiedSpaceFeature(abstractGeometry).handleFailure { return it }

        identifierAdder.addIdentifier(id, name, genericOccupiedSpace)
        _attributesAdder.addAttributes(
            id.toAttributes(configuration.identifierAttributesPrefix) +
                attributes,
            genericOccupiedSpace
        )
        return Result.success(genericOccupiedSpace)
    }

    private fun createGenericOccupiedSpaceFeature(abstractGeometry: AbstractGeometry3D):
        Result<GenericOccupiedSpace, Exception> {
        val geometryTransformer = GeometryTransformer(configuration)
            .also { abstractGeometry.accept(it) }
        return createGenericOccupiedSpaceFeature(geometryTransformer)
    }

    private fun createGenericOccupiedSpaceFeature(geometryTransformer: GeometryTransformer):
        Result<GenericOccupiedSpace, Exception> {
        val genericOccupiedSpaceFeature = GenericOccupiedSpace()

        // geometry
        genericOccupiedSpaceFeature.populateGeometryOrImplicitGeometry(geometryTransformer, LevelOfDetail.TWO)
        if (geometryTransformer.isSetRotation())
            geometryTransformer.getRotation()
                .handleFailure { return it }
                .also { _attributesAdder.addRotationAttributes(it, genericOccupiedSpaceFeature) }

        return Result.success(genericOccupiedSpaceFeature)
    }
}
