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

import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1Geometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1ImplicitGeometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2Geometry
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveLaneCenterLineGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveLeftLaneBoundaryGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveRightLaneBoundaryGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveRoadCenterLaneLineGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveRoadReferenceLineGmlIdentifier
import io.rtron.transformer.issues.roadspaces.of
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
    fun createGenericOccupiedSpaceFeature(roadspaceObject: RoadspaceObject): ContextIssueList<GenericOccupiedSpace> {
        val issueList = DefaultIssueList()

        val genericOccupiedSpaceFeature = GenericOccupiedSpace()

        // geometry
        val pointGeometryTransformer = GeometryTransformer.of(roadspaceObject.pointGeometry, parameters)
        genericOccupiedSpaceFeature.populateLod1ImplicitGeometry(pointGeometryTransformer)

        roadspaceObject.boundingBoxGeometry.onSome { currentBoundingBoxGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentBoundingBoxGeometry, parameters)
            genericOccupiedSpaceFeature.populateLod1Geometry(geometryTransformer)
                .mapLeft {
                    issueList += DefaultIssue.of(
                        "NoSuitableGeometryForGenericOccupiedSpaceLod1",
                        it.message,
                        roadspaceObject.id,
                        Severity.WARNING,
                        wasFixed = true
                    )
                }
        }

        roadspaceObject.complexGeometry.onSome { currentComplexGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentComplexGeometry, parameters)
            genericOccupiedSpaceFeature.populateLod2Geometry(geometryTransformer)
                .onLeft {
                    issueList += DefaultIssue.of(
                        "NoSuitableGeometryForGenericOccupiedSpaceLod2",
                        it.message,
                        roadspaceObject.id,
                        Severity.WARNING,
                        wasFixed = true
                    )
                }
            geometryTransformer.rotation.onSome {
                attributesAdder.addRotationAttributes(it, genericOccupiedSpaceFeature)
            }
        }

        // semantics
        IdentifierAdder.addIdentifier(
            roadspaceObject.id.deriveGmlIdentifier(parameters.gmlIdPrefix),
            genericOccupiedSpaceFeature
        )
        relationAdder.addBelongToRelations(roadspaceObject, genericOccupiedSpaceFeature)
        attributesAdder.addAttributes(roadspaceObject, genericOccupiedSpaceFeature)

        return ContextIssueList(genericOccupiedSpaceFeature, issueList)
    }

    fun createRoadReferenceLine(
        id: RoadspaceIdentifier,
        abstractGeometry: AbstractGeometry3D,
        attributes: AttributeList
    ): ContextIssueList<GenericLogicalSpace> {
        val issueList = DefaultIssueList()

        val genericLogicalSpace = createLogicalOccupiedSpaceFeature(id, abstractGeometry)
            .handleIssueList { issueList += it }
        IdentifierAdder.addIdentifier(
            id.deriveRoadReferenceLineGmlIdentifier(parameters.gmlIdPrefix),
            "RoadReferenceLine",
            genericLogicalSpace
        )
        attributesAdder.addAttributes(
            id.toAttributes(parameters.identifierAttributesPrefix) + attributes,
            genericLogicalSpace
        )

        return ContextIssueList(genericLogicalSpace, issueList)
    }

    fun createRoadCenterLaneLine(
        id: LaneIdentifier,
        abstractGeometry: AbstractGeometry3D,
        attributes: AttributeList
    ): ContextIssueList<GenericLogicalSpace> {
        val issueList = DefaultIssueList()

        val genericLogicalSpace = createLogicalOccupiedSpaceFeature(id, abstractGeometry)
            .handleIssueList { issueList += it }
        IdentifierAdder.addIdentifier(
            id.deriveRoadCenterLaneLineGmlIdentifier(parameters.gmlIdPrefix),
            "RoadCenterLaneLine",
            genericLogicalSpace
        )
        attributesAdder.addAttributes(
            id.toAttributes(parameters.identifierAttributesPrefix) + attributes,
            genericLogicalSpace
        )

        return ContextIssueList(genericLogicalSpace, issueList)
    }

    fun createCenterLaneLine(
        id: LaneIdentifier,
        abstractGeometry: AbstractGeometry3D
    ): ContextIssueList<GenericLogicalSpace> {
        val issueList = DefaultIssueList()

        val genericLogicalSpace = createLogicalOccupiedSpaceFeature(id, abstractGeometry)
            .handleIssueList { issueList += it }
        IdentifierAdder.addIdentifier(
            id.deriveLaneCenterLineGmlIdentifier(parameters.gmlIdPrefix),
            "LaneCenterLine",
            genericLogicalSpace
        )
        attributesAdder.addAttributes(
            id.toAttributes(parameters.identifierAttributesPrefix),
            genericLogicalSpace
        )

        return ContextIssueList(genericLogicalSpace, issueList)
    }

    fun createLeftLaneBoundary(
        id: LaneIdentifier,
        abstractGeometry: AbstractGeometry3D
    ): ContextIssueList<GenericLogicalSpace> {
        val issueList = DefaultIssueList()

        val genericLogicalSpace = createLogicalOccupiedSpaceFeature(id, abstractGeometry)
            .handleIssueList { issueList += it }
        IdentifierAdder.addIdentifier(
            id.deriveLeftLaneBoundaryGmlIdentifier(parameters.gmlIdPrefix),
            "LeftLaneBoundary",
            genericLogicalSpace
        )
        attributesAdder.addAttributes(
            id.toAttributes(parameters.identifierAttributesPrefix),
            genericLogicalSpace
        )

        return ContextIssueList(genericLogicalSpace, issueList)
    }

    fun createRightLaneBoundary(
        id: LaneIdentifier,
        abstractGeometry: AbstractGeometry3D
    ): ContextIssueList<GenericLogicalSpace> {
        val issueList = DefaultIssueList()

        val genericLogicalSpace = createLogicalOccupiedSpaceFeature(id, abstractGeometry)
            .handleIssueList { issueList += it }
        IdentifierAdder.addIdentifier(
            id.deriveRightLaneBoundaryGmlIdentifier(parameters.gmlIdPrefix),
            "RightLaneBoundary",
            genericLogicalSpace
        )
        attributesAdder.addAttributes(
            id.toAttributes(parameters.identifierAttributesPrefix),
            genericLogicalSpace
        )

        return ContextIssueList(genericLogicalSpace, issueList)
    }

    private fun createLogicalOccupiedSpaceFeature(
        id: AbstractRoadspacesIdentifier,
        abstractGeometry: AbstractGeometry3D
    ):
        ContextIssueList<GenericLogicalSpace> {
        val issueList = DefaultIssueList()
        val genericLogicalSpaceFeature = GenericLogicalSpace()

        val geometryTransformer = GeometryTransformer(parameters)
            .also { abstractGeometry.accept(it) }
        // geometry
        genericLogicalSpaceFeature.populateLod2Geometry(geometryTransformer)
            .onLeft {
                issueList += DefaultIssue.of(
                    "NoSuitableGeometryForLogicalOccupiedSpaceFeatureLod2",
                    it.message,
                    id,
                    Severity.WARNING,
                    wasFixed = true
                )
            }

        return ContextIssueList(genericLogicalSpaceFeature, issueList)
    }
}
