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

import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.LevelOfDetail
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateGeometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateGeometryOrImplicitGeometry
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveLaneCenterLineGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveLeftLaneBoundaryGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveRightLaneBoundaryGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveRoadCenterLaneLineGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveRoadReferenceLineGmlIdentifier
import io.rtron.transformer.messages.roadspaces.of
import org.citygml4j.core.model.generics.GenericLogicalSpace
import org.citygml4j.core.model.generics.GenericOccupiedSpace

/**
 * Builder for city objects of the CityGML Generics module.
 */
class GenericsModuleBuilder(
    private val parameters: Roadspaces2CitygmlParameters
) {
    // Properties and Initializers
    private val relationAdder = RelationAdder(parameters)
    private val attributesAdder = AttributesAdder(parameters)

    // Methods
    fun createGenericOccupiedSpaceFeature(roadspaceObject: RoadspaceObject): ContextMessageList<GenericOccupiedSpace> {
        val messageList = DefaultMessageList()

        val geometryTransformer = GeometryTransformer.of(roadspaceObject, parameters)
        val genericOccupiedSpace =
            createGenericOccupiedSpaceFeature(roadspaceObject.id, geometryTransformer)
                .handleMessageList { messageList += it }

        // semantics
        IdentifierAdder.addIdentifier(roadspaceObject.id.deriveGmlIdentifier(parameters.gmlIdPrefix), genericOccupiedSpace)
        relationAdder.addBelongToRelations(roadspaceObject, genericOccupiedSpace)
        attributesAdder.addAttributes(roadspaceObject, genericOccupiedSpace)

        return ContextMessageList(genericOccupiedSpace, messageList)
    }

    fun createRoadReferenceLine(id: RoadspaceIdentifier, abstractGeometry: AbstractGeometry3D, attributes: AttributeList): ContextMessageList<GenericLogicalSpace> {
        val messageList = DefaultMessageList()

        val genericLogicalSpace = createLogicalOccupiedSpaceFeature(id, abstractGeometry)
            .handleMessageList { messageList += it }
        IdentifierAdder.addIdentifier(id.deriveRoadReferenceLineGmlIdentifier(parameters.gmlIdPrefix), "RoadReferenceLine", genericLogicalSpace)
        attributesAdder.addAttributes(
            id.toAttributes(parameters.identifierAttributesPrefix) + attributes,
            genericLogicalSpace
        )

        return ContextMessageList(genericLogicalSpace, messageList)
    }

    fun createRoadCenterLaneLine(id: LaneIdentifier, abstractGeometry: AbstractGeometry3D, attributes: AttributeList): ContextMessageList<GenericLogicalSpace> {
        val messageList = DefaultMessageList()

        val genericLogicalSpace = createLogicalOccupiedSpaceFeature(id, abstractGeometry)
            .handleMessageList { messageList += it }
        IdentifierAdder.addIdentifier(id.deriveRoadCenterLaneLineGmlIdentifier(parameters.gmlIdPrefix), "RoadCenterLaneLine", genericLogicalSpace)
        attributesAdder.addAttributes(
            id.toAttributes(parameters.identifierAttributesPrefix) + attributes,
            genericLogicalSpace
        )

        return ContextMessageList(genericLogicalSpace, messageList)
    }

    fun createCenterLaneLine(id: LaneIdentifier, abstractGeometry: AbstractGeometry3D): ContextMessageList<GenericLogicalSpace> {
        val messageList = DefaultMessageList()

        val genericLogicalSpace = createLogicalOccupiedSpaceFeature(id, abstractGeometry)
            .handleMessageList { messageList += it }
        IdentifierAdder.addIdentifier(id.deriveLaneCenterLineGmlIdentifier(parameters.gmlIdPrefix), "LaneCenterLine", genericLogicalSpace)
        attributesAdder.addAttributes(
            id.toAttributes(parameters.identifierAttributesPrefix),
            genericLogicalSpace
        )

        return ContextMessageList(genericLogicalSpace, messageList)
    }

    fun createLeftLaneBoundary(id: LaneIdentifier, abstractGeometry: AbstractGeometry3D): ContextMessageList<GenericLogicalSpace> {
        val messageList = DefaultMessageList()

        val genericLogicalSpace = createLogicalOccupiedSpaceFeature(id, abstractGeometry)
            .handleMessageList { messageList += it }
        IdentifierAdder.addIdentifier(id.deriveLeftLaneBoundaryGmlIdentifier(parameters.gmlIdPrefix), "LeftLaneBoundary", genericLogicalSpace)
        attributesAdder.addAttributes(
            id.toAttributes(parameters.identifierAttributesPrefix),
            genericLogicalSpace
        )

        return ContextMessageList(genericLogicalSpace, messageList)
    }

    fun createRightLaneBoundary(id: LaneIdentifier, abstractGeometry: AbstractGeometry3D): ContextMessageList<GenericLogicalSpace> {
        val messageList = DefaultMessageList()

        val genericLogicalSpace = createLogicalOccupiedSpaceFeature(id, abstractGeometry)
            .handleMessageList { messageList += it }
        IdentifierAdder.addIdentifier(id.deriveRightLaneBoundaryGmlIdentifier(parameters.gmlIdPrefix), "RightLaneBoundary", genericLogicalSpace)
        attributesAdder.addAttributes(
            id.toAttributes(parameters.identifierAttributesPrefix),
            genericLogicalSpace
        )

        return ContextMessageList(genericLogicalSpace, messageList)
    }

    private fun createLogicalOccupiedSpaceFeature(id: AbstractRoadspacesIdentifier, abstractGeometry: AbstractGeometry3D):
        ContextMessageList<GenericLogicalSpace> {
        val geometryTransformer = GeometryTransformer(parameters)
            .also { abstractGeometry.accept(it) }
        return createLogicalOccupiedSpaceFeature(id, geometryTransformer)
    }

    private fun createGenericOccupiedSpaceFeature(id: AbstractRoadspacesIdentifier, geometryTransformer: GeometryTransformer): ContextMessageList<GenericOccupiedSpace> {
        val messageList = DefaultMessageList()
        val genericOccupiedSpaceFeature = GenericOccupiedSpace()

        // geometry
        genericOccupiedSpaceFeature.populateGeometryOrImplicitGeometry(geometryTransformer, LevelOfDetail.TWO)
            .onLeft { messageList += DefaultMessage.of("", it.message, id, Severity.WARNING, wasFixed = true) }
        geometryTransformer.rotation.tap {
            attributesAdder.addRotationAttributes(it, genericOccupiedSpaceFeature)
        }

        return ContextMessageList(genericOccupiedSpaceFeature, messageList)
    }

    private fun createLogicalOccupiedSpaceFeature(id: AbstractRoadspacesIdentifier, geometryTransformer: GeometryTransformer): ContextMessageList<GenericLogicalSpace> {
        val messageList = DefaultMessageList()
        val genericLogicalSpaceFeature = GenericLogicalSpace()

        // geometry
        genericLogicalSpaceFeature.populateGeometry(geometryTransformer, LevelOfDetail.TWO)
            .onLeft { messageList += DefaultMessage.of("", it.message, id, Severity.WARNING, wasFixed = true) }

        return ContextMessageList(genericLogicalSpaceFeature, messageList)
    }
}
