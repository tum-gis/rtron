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

package io.rtron.transformer.converter.roadspaces2citygml.module

import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.toAttributes
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.LevelOfDetail
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateGeometryOrImplicitGeometry
import io.rtron.transformer.report.of
import org.citygml4j.model.generics.GenericOccupiedSpace

/**
 * Builder for city objects of the CityGML Generics module.
 */
class GenericsModuleBuilder(
    private val configuration: Roadspaces2CitygmlConfiguration,
    private val identifierAdder: IdentifierAdder
) {
    // Properties and Initializers
    private val _attributesAdder = AttributesAdder(configuration)

    // Methods
    fun createGenericOccupiedSpaceFeature(roadspaceObject: RoadspaceObject): ContextReport<GenericOccupiedSpace> {
        val report = Report()

        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        val genericOccupiedSpace =
            createGenericOccupiedSpaceFeature(roadspaceObject.id, geometryTransformer)
                .handleReport { report += it }

        // semantics
        identifierAdder.addUniqueIdentifier(roadspaceObject.id, genericOccupiedSpace)
        _attributesAdder.addAttributes(roadspaceObject, genericOccupiedSpace)

        return ContextReport(genericOccupiedSpace, report)
    }

    fun createGenericOccupiedSpaceFeature(id: LaneIdentifier, name: String, abstractGeometry: AbstractGeometry3D, attributes: AttributeList): ContextReport<GenericOccupiedSpace> {
        val report = Report()

        val genericOccupiedSpace = createGenericOccupiedSpaceFeature(id, abstractGeometry)
            .handleReport { report += it }

        identifierAdder.addIdentifier(id, name, genericOccupiedSpace)
        _attributesAdder.addAttributes(
            id.toAttributes(configuration.identifierAttributesPrefix) +
                attributes,
            genericOccupiedSpace
        )

        return ContextReport(genericOccupiedSpace, report)
    }

    fun createGenericOccupiedSpaceFeature(id: RoadspaceIdentifier, name: String, abstractGeometry: AbstractGeometry3D, attributes: AttributeList): ContextReport<GenericOccupiedSpace> {
        val report = Report()
        val genericOccupiedSpace = createGenericOccupiedSpaceFeature(id, abstractGeometry)
            .handleReport { report += it }

        identifierAdder.addIdentifier(id, name, genericOccupiedSpace)
        _attributesAdder.addAttributes(
            id.toAttributes(configuration.identifierAttributesPrefix) +
                attributes,
            genericOccupiedSpace
        )
        return ContextReport(genericOccupiedSpace, report)
    }

    private fun createGenericOccupiedSpaceFeature(id: AbstractRoadspacesIdentifier, abstractGeometry: AbstractGeometry3D):
        ContextReport<GenericOccupiedSpace> {
        val geometryTransformer = GeometryTransformer(configuration)
            .also { abstractGeometry.accept(it) }
        return createGenericOccupiedSpaceFeature(id, geometryTransformer)
    }

    private fun createGenericOccupiedSpaceFeature(id: AbstractRoadspacesIdentifier, geometryTransformer: GeometryTransformer): ContextReport<GenericOccupiedSpace> {
        val report = Report()

        val genericOccupiedSpaceFeature = GenericOccupiedSpace()

        // geometry
        genericOccupiedSpaceFeature.populateGeometryOrImplicitGeometry(geometryTransformer, LevelOfDetail.TWO)
            .tapLeft { report += Message.of(it.message, id, isFatal = false, wasHealed = true) }

        geometryTransformer.rotation.tap {
            _attributesAdder.addRotationAttributes(it, genericOccupiedSpaceFeature)
        }

        return ContextReport(genericOccupiedSpaceFeature, report)
    }
}
