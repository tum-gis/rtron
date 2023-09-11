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

package io.rtron.transformer.converter.opendrive2roadspaces.roadspaces

import arrow.core.NonEmptyList
import arrow.core.getOrElse
import arrow.core.toNonEmptyListOrNull
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessageList
import io.rtron.math.analysis.function.bivariate.BivariateFunction
import io.rtron.math.analysis.function.bivariate.pure.PlaneFunction
import io.rtron.math.analysis.function.bivariate.pure.ShapeFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.threed.surface.CurveRelativeParametricSurface3D
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileShape
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.converter.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Curve3DBuilder
import io.rtron.model.opendrive.road.Road as OpendriveRoad

/**
 * Builder of [Roadspace] (RoadSpaces data model) to the Road class of the OpenDRIVE data model.
 */
class RoadspaceBuilder(
    private val parameters: Opendrive2RoadspacesParameters
) {
    // Properties and Initializers
    private val roadBuilder = RoadBuilder(parameters)
    private val roadObjectBuilder = RoadspaceObjectBuilder(parameters)

    // Methods

    /**
     * Builds a [Roadspace] of the RoadSpaces data model from the OpenDRIVE road.
     *
     * @param modelId identifier of the actual model
     * @param road source OpenDRIVE model
     * @return transformed [Roadspace]
     */
    fun buildRoadspace(road: OpendriveRoad): ContextMessageList<Roadspace> {
        val messageList = DefaultMessageList()
        val roadspaceId = RoadspaceIdentifier(road.id)

        // build up road reference line
        val roadReferenceLine = Curve3DBuilder.buildCurve3D(
            road.planView.geometryAsNonEmptyList,
            road.getElevationEntries(),
            parameters.numberTolerance,
            parameters.planViewGeometryDistanceTolerance,
            parameters.planViewGeometryAngleTolerance
        )
        val torsionFunction = road.getSuperelevationEntries().fold(
            { LinearFunction.X_AXIS },
            { FunctionBuilder.buildCurveTorsion(it) }
        )

        // build attributes for the road
        val attributes = buildAttributes(road)

        // build up road's surface geometries
        val lateralProfileRoadShape = road.getShapeEntries()
            .fold({ PlaneFunction.ZERO }, { buildLateralRoadShape(roadspaceId, it) })

        val roadSurface = CurveRelativeParametricSurface3D(
            roadReferenceLine.copy(torsionFunction = torsionFunction),
            lateralProfileRoadShape
        )
        val roadSurfaceWithoutTorsion = CurveRelativeParametricSurface3D(roadReferenceLine, lateralProfileRoadShape)

        // build up the road containing only lane sections, lanes (no roadside objects)
        val roadspaceRoad = roadBuilder
            .buildRoad(roadspaceId, road, roadSurface, roadSurfaceWithoutTorsion, attributes)
            .handleMessageList { messageList += it }

        // build up the road space objects (OpenDRIVE: road objects & signals)
        val roadspaceObjectsFromRoadObjects = road.objects.fold({ emptyList() }, { roadObjects ->
            roadObjectBuilder.buildRoadspaceObjects(roadspaceId, roadObjects, roadReferenceLine, roadspaceRoad, attributes)
                .handleMessageList { messageList += it }
        })
        val roadspaceObjectsFromRoadSignals: List<RoadspaceObject> = road.signals.fold({ emptyList() }, { roadSignals ->
            roadObjectBuilder.buildRoadspaceObjects(roadspaceId, roadSignals, roadReferenceLine, roadspaceRoad, attributes)
                .handleMessageList { messageList += it }
        })

        // combine the models into a road space object
        val roadspace = Roadspace(
            id = roadspaceId,
            name = road.name.getOrElse { "" },
            referenceLine = roadReferenceLine,
            road = roadspaceRoad,
            roadspaceObjects = roadspaceObjectsFromRoadObjects + roadspaceObjectsFromRoadSignals,
            attributes = attributes
        )
        return ContextMessageList(roadspace, messageList)
    }

    private fun buildLateralRoadShape(id: RoadspaceIdentifier, lateralProfileShapeList: NonEmptyList<RoadLateralProfileShape>):
        BivariateFunction {
        val lateralFunctions = lateralProfileShapeList
            .groupBy { it.s }
            .mapValues { it.value.toNonEmptyListOrNull()!! }
            .mapValues { FunctionBuilder.buildLateralShape(it.value) }
            .toSortedMap()

        return ShapeFunction(
            lateralFunctions,
            extrapolateX = true,
            extrapolateY = parameters.extrapolateLateralRoadShapes
        )
    }

    private fun buildAttributes(road: OpendriveRoad) =
        attributes("${parameters.attributesPrefix}road_") {
            attribute("length", road.length)
            attribute("junction", road.getJunctionOption())
            attribute("rule", road.rule.map { it.toString() })

            road.link.onSome { roadLink ->
                roadLink.predecessor.onSome { predecessor ->
                    attribute("predecessor_road", predecessor.getRoadPredecessorSuccessor().map { it.first })
                    attribute(
                        "predecessor_contactPoint",
                        predecessor.getRoadPredecessorSuccessor().map { it.second.toString() }
                    )
                    attribute("predecessor_junction", predecessor.getJunctionPredecessorSuccessor())
                }

                roadLink.successor.onSome { successor ->
                    attribute("successor_road", successor.getRoadPredecessorSuccessor().map { it.first })
                    attribute(
                        "successor_contactPoint",
                        successor.getRoadPredecessorSuccessor().map { it.second.toString() }
                    )
                    attribute("successor_junction", successor.getJunctionPredecessorSuccessor())
                }
            }
        }
}
