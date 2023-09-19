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
import io.rtron.model.roadspaces.identifier.RoadSide
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.router.RoadspaceObjectRouter
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier
import org.citygml4j.core.model.core.AbstractCityObject
import org.citygml4j.core.model.core.CityObjectRelation
import org.citygml4j.core.model.core.CityObjectRelationProperty
import org.citygml4j.core.model.transportation.TrafficSpace
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
    fun addRelatedToRelation(roadspaceObject: RoadspaceObject, dstTrafficSpace: TrafficSpace) {
        val relationType = "related" + when (RoadspaceObjectRouter.route(roadspaceObject)) {
            RoadspaceObjectRouter.CitygmlTargetFeatureType.BUILDING_BUILDING -> "Building"
            RoadspaceObjectRouter.CitygmlTargetFeatureType.CITYFURNITURE_CITYFURNITURE -> "Furniture"
            RoadspaceObjectRouter.CitygmlTargetFeatureType.GENERICS_GENERICOCCUPIEDSPACE -> "OccupiedSpace"
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_TRAFFICSPACE -> return
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_AUXILIARYTRAFFICSPACE -> return
            RoadspaceObjectRouter.CitygmlTargetFeatureType.TRANSPORTATION_MARKING -> return
            RoadspaceObjectRouter.CitygmlTargetFeatureType.VEGETATION_SOLITARYVEGETATIONOBJECT -> "Vegetation"
        }

        val relation = createCityObjectRelation(roadspaceObject.id.deriveGmlIdentifier(parameters.gmlIdPrefix), relationType, roadspaceObject.id)
        dstTrafficSpace.relatedTo.add(relation)
    }

    fun addBelongToRelations(roadspaceObject: RoadspaceObject, dstCityObject: AbstractCityObject) {
        val relations = roadspaceObject.laneRelations
            .flatMap { it.getAllLeftRightLaneIdentifiers() }
            .map { createCityObjectRelation(it.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(parameters.gmlIdPrefix), "belongsTo", it) }

        dstCityObject.relatedTo.addAll(relations)
    }

    /**
     * Adds a lane change relation to the [dstTrafficSpace] object
     */
    fun addLaneChangeRelation(lane: Lane, direction: RoadSide, dstTrafficSpace: TrafficSpace) {
        val gmlId = lane.id.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(parameters.gmlIdPrefix)
        val relationType = when (direction) {
            RoadSide.LEFT -> "leftLaneChange"
            RoadSide.RIGHT -> "rightLaneChange"
            RoadSide.CENTER -> throw IllegalArgumentException("Direction of a laneChange relation must not be center.")
        }
        val relation: CityObjectRelationProperty = createCityObjectRelation(gmlId, relationType, lane.id)
        dstTrafficSpace.relatedTo.add(relation)
    }

    private fun createCityObjectRelation(gmlId: String, type: String, id: AbstractRoadspacesIdentifier): CityObjectRelationProperty {
        val relation = CityObjectRelation(parameters.xlinkPrefix + gmlId)
        relation.relationType = Code(type)
        attributesAdder.addAttributes(id, relation)

        return CityObjectRelationProperty(relation)
    }
}
