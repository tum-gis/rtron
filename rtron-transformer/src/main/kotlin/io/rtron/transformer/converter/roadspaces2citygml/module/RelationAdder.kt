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

import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.router.RoadspaceObjectRouter
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier
import org.citygml4j.core.model.core.AbstractCityObject
import org.citygml4j.core.model.core.CityObjectRelation
import org.citygml4j.core.model.core.CityObjectRelationProperty
import org.xmlobjects.gml.model.basictypes.Code

/**
 * Adds relations to and from an object to an [AbstractCityObject] (CityGML model).
 */
class RelationAdder(
    private val parameters: Roadspaces2CitygmlParameters
) {
    // Properties and Initializers
    private val attributesAdder = AttributesAdder(parameters)

    // Methods
    fun addRelatedToRelation(roadspaceObject: RoadspaceObject, dstCityObject: AbstractCityObject) {
        val relationType = "related" + when (RoadspaceObjectRouter.route(roadspaceObject)) {
            RoadspaceObjectRouter.CitygmlTargetFeatureType.BUILDING_BUILDING -> "Building"
            RoadspaceObjectRouter.CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE -> "Furniture"
            RoadspaceObjectRouter.CitygmlTargetFeatureType.GENERICS_GENERICOCCUPIEDSPACE -> "OccupiedSpace"
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> return
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> return
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_MARKING -> return
            RoadspaceObjectRouter.CitygmlTargetFeatureType.VEGETATION_SOLITARYVEGETATIONOBJECT -> "Vegetation"
        }

        val relations: HashSet<LaneIdentifier> = roadspaceObject.laneRelations.flatMap { it.getAllLeftRightLaneIdentifiers() }.toHashSet()
        dstCityObject.relatedTo = relations.map { createCityObjectRelation(roadspaceObject.id.deriveGmlIdentifier(parameters.gmlIdPrefix), relationType, it) }
    }

    fun addBelongToRelations(roadspaceObject: RoadspaceObject, dstCityObject: AbstractCityObject) {
        dstCityObject.relatedTo = roadspaceObject.laneRelations.flatMap { it.getAllLeftRightLaneIdentifiers() }.map { createCityObjectRelation(it.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(parameters.gmlIdPrefix), "belongsTo", it) }
    }

    private fun createCityObjectRelation(gmlId: String, type: String, id: AbstractRoadspacesIdentifier): CityObjectRelationProperty {
        val relation = CityObjectRelation(parameters.xlinkPrefix + gmlId)
        relation.relationType = Code(type)
        attributesAdder.addAttributes(id, relation)

        return CityObjectRelationProperty(relation)
    }
}
