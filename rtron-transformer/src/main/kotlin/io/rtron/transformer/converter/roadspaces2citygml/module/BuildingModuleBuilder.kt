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

import arrow.core.getOrElse
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceObjectIdentifier
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.LevelOfDetail
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateGeometryOrImplicitGeometry
import io.rtron.transformer.messages.roadspaces.of
import org.citygml4j.core.model.building.Building
import org.citygml4j.core.model.construction.GroundSurface
import org.citygml4j.core.model.construction.RoofSurface
import org.citygml4j.core.model.construction.WallSurface
import org.citygml4j.core.model.core.AbstractSpaceBoundaryProperty

/**
 * Builder for city objects of the CityGML Building module.
 */
class BuildingModuleBuilder(
    private val parameters: Roadspaces2CitygmlParameters,
    private val identifierAdder: IdentifierAdder
) {
    // Properties and Initializers
    private val _attributesAdder = AttributesAdder(parameters)

    // Methods
    fun createBuildingFeature(roadspaceObject: RoadspaceObject): ContextMessageList<Building> {
        val messageList = DefaultMessageList()

        // geometry
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, parameters)

        val buildingFeature =
            if (geometryTransformer.getSolid().isDefined()) createLod2Building(roadspaceObject.id, geometryTransformer).handleMessageList { messageList += it }
            else createLod1Building(roadspaceObject.id, geometryTransformer).handleMessageList { messageList += it }

        // semantics
        identifierAdder.addIdentifier(roadspaceObject.id, roadspaceObject.name.getOrElse { "" }, buildingFeature) // TODO fix option
        _attributesAdder.addAttributes(roadspaceObject, buildingFeature)

        return ContextMessageList(buildingFeature, messageList)
    }

    /**
     * Creates a building feature with individual roof, ground and wall surfaces.
     * In order to cut out the respective geometries of the roof, ground and wall, a solid must be set in the [geometryTransformer].
     */
    private fun createLod2Building(id: RoadspaceObjectIdentifier, geometryTransformer: GeometryTransformer): ContextMessageList<Building> {
        require(geometryTransformer.getSolid().isDefined()) { "Solid geometry is required to create an LoD2 building." }
        val messageList = DefaultMessageList()
        val buildingFeature = Building()

        val roofSurfaceFeature = RoofSurface()
        geometryTransformer.getSolidCutout(GeometryTransformer.FaceType.TOP).tap {
            roofSurfaceFeature.lod2MultiSurface = it
        }.tapNone {
            messageList += DefaultMessage.of("", "No LoD2 MultiSurface for roof feature available.", id, Severity.WARNING, wasFixed = true)
        }
        buildingFeature.addBoundary(AbstractSpaceBoundaryProperty(roofSurfaceFeature))
        identifierAdder.addDetailedIdentifier(id, id.roadspaceObjectName, "RoofSurface", dstCityObject = roofSurfaceFeature)

        val groundSurfaceFeature = GroundSurface()
        geometryTransformer.getSolidCutout(GeometryTransformer.FaceType.BASE).tap {
            groundSurfaceFeature.lod2MultiSurface = it
        }.tapNone {
            messageList += DefaultMessage.of("", "No LoD2 MultiSurface for ground feature available.", id, Severity.WARNING, wasFixed = true)
        }
        buildingFeature.addBoundary(AbstractSpaceBoundaryProperty(groundSurfaceFeature))
        identifierAdder.addDetailedIdentifier(id, id.roadspaceObjectName, "FloorSurface", dstCityObject = groundSurfaceFeature)

        geometryTransformer.getIndividualSolidCutouts(GeometryTransformer.FaceType.SIDE).tap { wallSurfaceResult ->
            wallSurfaceResult.forEachIndexed { index, multiSurfaceProperty ->
                val wallSurfaceFeature = WallSurface()
                wallSurfaceFeature.lod2MultiSurface = multiSurfaceProperty
                buildingFeature.addBoundary(AbstractSpaceBoundaryProperty(wallSurfaceFeature))
                identifierAdder.addDetailedIdentifier(id, id.roadspaceObjectName, "WallSurface", index, wallSurfaceFeature)
            }
        }.tapNone {
            messageList += DefaultMessage.of("", "No LoD2 MultiSurface for wall feature available.", id, Severity.WARNING, wasFixed = true)
        }

        return ContextMessageList(buildingFeature, messageList)
    }

    private fun createLod1Building(id: AbstractRoadspacesIdentifier, geometryTransformer: GeometryTransformer): ContextMessageList<Building> {
        val messageList = DefaultMessageList()
        val buildingFeature = Building()
        buildingFeature.populateGeometryOrImplicitGeometry(geometryTransformer, LevelOfDetail.ONE)
            .tapLeft { messageList += DefaultMessage.of("", it.message, id, Severity.WARNING, wasFixed = true) }

        return ContextMessageList(buildingFeature, messageList)
    }
}
