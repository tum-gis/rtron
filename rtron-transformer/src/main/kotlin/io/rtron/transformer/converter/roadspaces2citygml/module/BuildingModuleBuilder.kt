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
import io.rtron.model.roadspaces.identifier.RoadspaceObjectIdentifier
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1Geometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1ImplicitGeometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2Geometry
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveLod2GroundGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveLod2RoofGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveLod2WallGmlIdentifier
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
    private val parameters: Roadspaces2CitygmlParameters
) {
    // Properties and Initializers
    private val relationAdder = RelationAdder(parameters)
    private val attributesAdder = AttributesAdder(parameters)

    // Methods
    fun createBuildingFeature(roadspaceObject: RoadspaceObject): ContextMessageList<Building> {
        val messageList = DefaultMessageList()

        val buildingFeature = Building()

        // geometry
        val pointGeometryTransformer = GeometryTransformer.of(roadspaceObject.pointGeometry, parameters)
        buildingFeature.populateLod1ImplicitGeometry(pointGeometryTransformer)

        roadspaceObject.boundingBoxGeometry.tap { currentBoundingBoxGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentBoundingBoxGeometry, parameters)
            buildingFeature.populateLod1Geometry(geometryTransformer)
                .mapLeft { messageList += DefaultMessage.of("NoSuitableGeometryForBuildingLod1", it.message, roadspaceObject.id, Severity.WARNING, wasFixed = true) }
        }

        roadspaceObject.complexGeometry.tap { currentComplexGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentComplexGeometry, parameters)
            addLod2BuildingInformation(roadspaceObject.id, geometryTransformer, buildingFeature)
        }

        // semantics
        IdentifierAdder.addIdentifier(roadspaceObject.id.deriveGmlIdentifier(parameters.gmlIdPrefix), buildingFeature)
        relationAdder.addBelongToRelations(roadspaceObject, buildingFeature)
        attributesAdder.addAttributes(roadspaceObject, buildingFeature)

        return ContextMessageList(buildingFeature, messageList)
    }

    /**
     * Creates a building feature with individual roof, ground and wall surfaces.
     * In order to cut out the respective geometries of the roof, ground and wall, a solid must be set in the [geometryTransformer].
     */
    private fun addLod2BuildingInformation(id: RoadspaceObjectIdentifier, geometryTransformer: GeometryTransformer, dstBuildingFeature: Building): ContextMessageList<Building> {
        require(geometryTransformer.getSolid().isDefined()) { "Solid geometry is required to create an LOD2 building." }
        val messageList = DefaultMessageList()

        dstBuildingFeature.populateLod2Geometry(geometryTransformer)
            .onLeft { messageList += DefaultMessage.of("", it.message, id, Severity.WARNING, wasFixed = true) }

        val roofSurfaceFeature = RoofSurface()
        geometryTransformer.getSolidCutout(GeometryTransformer.FaceType.TOP).tap {
            roofSurfaceFeature.lod2MultiSurface = it
        }.tapNone {
            messageList += DefaultMessage.of("NoSuitableGeometryForRoofSurfaceLod2", "No LOD2 MultiSurface for roof feature available.", id, Severity.WARNING, wasFixed = true)
        }
        dstBuildingFeature.addBoundary(AbstractSpaceBoundaryProperty(roofSurfaceFeature))
        IdentifierAdder.addIdentifier(id.deriveLod2RoofGmlIdentifier(parameters.gmlIdPrefix), "RoofSurface", dstCityObject = roofSurfaceFeature)

        val groundSurfaceFeature = GroundSurface()
        geometryTransformer.getSolidCutout(GeometryTransformer.FaceType.BASE).tap {
            groundSurfaceFeature.lod2MultiSurface = it
        }.tapNone {
            messageList += DefaultMessage.of("NoSuitableGeometryForGroundSurfaceLod2", "No LOD2 MultiSurface for ground feature available.", id, Severity.WARNING, wasFixed = true)
        }
        dstBuildingFeature.addBoundary(AbstractSpaceBoundaryProperty(groundSurfaceFeature))
        IdentifierAdder.addIdentifier(id.deriveLod2GroundGmlIdentifier(parameters.gmlIdPrefix), "GroundSurface", dstCityObject = groundSurfaceFeature)

        geometryTransformer.getIndividualSolidCutouts(GeometryTransformer.FaceType.SIDE).tap { wallSurfaceResult ->
            wallSurfaceResult.forEachIndexed { index, multiSurfaceProperty ->
                val wallSurfaceFeature = WallSurface()
                wallSurfaceFeature.lod2MultiSurface = multiSurfaceProperty
                dstBuildingFeature.addBoundary(AbstractSpaceBoundaryProperty(wallSurfaceFeature))
                IdentifierAdder.addIdentifier(id.deriveLod2WallGmlIdentifier(parameters.gmlIdPrefix, index), "WallSurface", dstCityObject = wallSurfaceFeature)
            }
        }.tapNone {
            messageList += DefaultMessage.of("NoSuitableGeometryForWallSurfaceLod2", "No LOD2 MultiSurface for wall feature available.", id, Severity.WARNING, wasFixed = true)
        }

        return ContextMessageList(dstBuildingFeature, messageList)
    }
}
