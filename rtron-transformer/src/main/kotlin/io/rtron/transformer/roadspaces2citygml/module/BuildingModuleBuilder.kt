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
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObjectIdentifier
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.roadspaces2citygml.geometry.LevelOfDetail
import io.rtron.transformer.roadspaces2citygml.geometry.populateGeometryOrImplicitGeometry
import org.citygml4j.model.building.Building
import org.citygml4j.model.construction.GroundSurface
import org.citygml4j.model.construction.RoofSurface
import org.citygml4j.model.construction.WallSurface
import org.citygml4j.model.core.AbstractSpaceBoundaryProperty

/**
 * Builder for city objects of the CityGML Building module.
 */
class BuildingModuleBuilder(
    private val configuration: Roadspaces2CitygmlConfiguration,
    private val identifierAdder: IdentifierAdder
) {
    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)
    private val _attributesAdder = AttributesAdder(configuration)

    // Methods
    fun createBuildingFeature(roadspaceObject: RoadspaceObject): Result<Building, Exception> {

        // geometry
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)

        val buildingFeatureResult = if (geometryTransformer.isSetSolid()) createLod2Building(roadspaceObject.id, geometryTransformer) else createLod1Building(geometryTransformer)
        val buildingFeature = buildingFeatureResult.handleFailure { return it }

        // semantics
        identifierAdder.addIdentifier(roadspaceObject.id, roadspaceObject.name, buildingFeature)
        _attributesAdder.addAttributes(roadspaceObject, buildingFeature)

        return Result.success(buildingFeature)
    }

    /**
     * Creates a building feature with individual roof, ground and wall surfaces.
     * In order to cut out the respective geometries of the roof, ground and wall, a solid must be set in the [geometryTransformer].
     */
    private fun createLod2Building(id: RoadspaceObjectIdentifier, geometryTransformer: GeometryTransformer): Result<Building, Exception> {
        require(geometryTransformer.isSetSolid()) { "Solid geometry is required to create an LoD2 building." }
        val buildingFeature = Building()

        val roofSurfaceFeature = RoofSurface()
        roofSurfaceFeature.lod2MultiSurface = geometryTransformer.getSolidCutout(GeometryTransformer.FaceType.TOP).handleFailure { return it }
        buildingFeature.addBoundary(AbstractSpaceBoundaryProperty(roofSurfaceFeature))
        identifierAdder.addDetailedIdentifier(id, id.roadspaceObjectName, "RoofSurface", dstCityObject = roofSurfaceFeature)

        val groundSurfaceFeature = GroundSurface()
        groundSurfaceFeature.lod2MultiSurface = geometryTransformer.getSolidCutout(GeometryTransformer.FaceType.BASE).handleFailure { return it }
        buildingFeature.addBoundary(AbstractSpaceBoundaryProperty(groundSurfaceFeature))
        identifierAdder.addDetailedIdentifier(id, id.roadspaceObjectName, "FloorSurface", dstCityObject = groundSurfaceFeature)

        geometryTransformer.getIndividualSolidCutouts(GeometryTransformer.FaceType.SIDE)
            .handleFailure { return it }
            .forEachIndexed { index, multiSurfaceProperty ->
                val wallSurfaceFeature = WallSurface()
                wallSurfaceFeature.lod2MultiSurface = multiSurfaceProperty
                buildingFeature.addBoundary(AbstractSpaceBoundaryProperty(wallSurfaceFeature))
                identifierAdder.addDetailedIdentifier(id, id.roadspaceObjectName, "WallSurface", index, wallSurfaceFeature)
            }

        return Result.success(buildingFeature)
    }

    private fun createLod1Building(geometryTransformer: GeometryTransformer): Result<Building, Exception> {
        val buildingFeature = Building()
        buildingFeature.populateGeometryOrImplicitGeometry(geometryTransformer, LevelOfDetail.ONE).handleFailure { return it }

        return Result.success(buildingFeature)
    }
}
