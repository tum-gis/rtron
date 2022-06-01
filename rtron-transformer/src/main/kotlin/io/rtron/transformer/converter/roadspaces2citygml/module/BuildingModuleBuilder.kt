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
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceObjectIdentifier
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.LevelOfDetail
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateGeometryOrImplicitGeometry
import io.rtron.transformer.report.of
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
    private val _attributesAdder = AttributesAdder(configuration)

    // Methods
    fun createBuildingFeature(roadspaceObject: RoadspaceObject): ContextReport<Building> {
        val report = Report()

        // geometry
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)

        val buildingFeature =
            if (geometryTransformer.getSolid().isDefined()) createLod2Building(roadspaceObject.id, geometryTransformer).handleReport { report += it }
            else createLod1Building(roadspaceObject.id, geometryTransformer).handleReport { report += it }

        // semantics
        identifierAdder.addIdentifier(roadspaceObject.id, roadspaceObject.name.getOrElse { "" }, buildingFeature) // TODO fix option
        _attributesAdder.addAttributes(roadspaceObject, buildingFeature)

        return ContextReport(buildingFeature, report)
    }

    /**
     * Creates a building feature with individual roof, ground and wall surfaces.
     * In order to cut out the respective geometries of the roof, ground and wall, a solid must be set in the [geometryTransformer].
     */
    private fun createLod2Building(id: RoadspaceObjectIdentifier, geometryTransformer: GeometryTransformer): ContextReport<Building> {
        require(geometryTransformer.getSolid().isDefined()) { "Solid geometry is required to create an LoD2 building." }
        val report = Report()
        val buildingFeature = Building()

        val roofSurfaceFeature = RoofSurface()
        geometryTransformer.getSolidCutout(GeometryTransformer.FaceType.TOP).tap {
            roofSurfaceFeature.lod2MultiSurface = it
        }.tapNone {
            report += Message.of("No LoD2 MultiSurface for roof feature available.", id, isFatal = false, wasHealed = true)
        }
        buildingFeature.addBoundary(AbstractSpaceBoundaryProperty(roofSurfaceFeature))
        identifierAdder.addDetailedIdentifier(id, id.roadspaceObjectName, "RoofSurface", dstCityObject = roofSurfaceFeature)

        val groundSurfaceFeature = GroundSurface()
        geometryTransformer.getSolidCutout(GeometryTransformer.FaceType.BASE).tap {
            groundSurfaceFeature.lod2MultiSurface = it
        }.tapNone {
            report += Message.of("No LoD2 MultiSurface for ground feature available.", id, isFatal = false, wasHealed = true)
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
            report += Message.of("No LoD2 MultiSurface for wall feature available.", id, isFatal = false, wasHealed = true)
        }

        return ContextReport(buildingFeature, report)
    }

    private fun createLod1Building(id: AbstractRoadspacesIdentifier, geometryTransformer: GeometryTransformer): ContextReport<Building> {
        val report = Report()
        val buildingFeature = Building()
        buildingFeature.populateGeometryOrImplicitGeometry(geometryTransformer, LevelOfDetail.ONE)
            .tapLeft { report += Message.of(it.message, id, isFatal = false, wasHealed = true) }

        return ContextReport(buildingFeature, report)
    }
}
