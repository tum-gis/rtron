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

package io.rtron.transformer.converter.opendrive2roadspaces.roadspaces

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.continuations.either
import arrow.core.getOrHandle
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.MessageList
import io.rtron.math.analysis.function.bivariate.BivariateFunction
import io.rtron.math.analysis.function.bivariate.pure.PlaneFunction
import io.rtron.math.analysis.function.bivariate.pure.ShapeFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.threed.surface.CurveRelativeParametricSurface3D
import io.rtron.model.opendrive.additions.exceptions.toIllegalStateException
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileShape
import io.rtron.model.roadspaces.identifier.ModelIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesTransformationException
import io.rtron.transformer.converter.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.converter.opendrive2roadspaces.geometry.Curve3DBuilder
import io.rtron.model.opendrive.road.Road as OpendriveRoad

/**
 * Builder of [Roadspace] (RoadSpaces data model) to the Road class of the OpenDRIVE data model.
 */
class RoadspaceBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {
    // Properties and Initializers
    private val _curve3DBuilder = Curve3DBuilder(configuration)
    private val _functionBuilder = FunctionBuilder(configuration)

    private val _roadBuilder = RoadBuilder(configuration)
    private val _roadObjectBuilder = RoadspaceObjectBuilder(configuration)

    // Methods

    /**
     * Builds a [Roadspace] of the RoadSpaces data model from the OpenDRIVE road.
     *
     * @param modelId identifier of the actual model
     * @param road source OpenDRIVE model
     * @return transformed [Roadspace]
     */
    fun buildRoadspace(modelId: ModelIdentifier, road: OpendriveRoad): Either<Opendrive2RoadspacesTransformationException, ContextMessageList<Roadspace>> = either.eager {
        val messageList = MessageList()

        val opendriveRoadId = road.additionalId.toEither { IllegalStateException("Additional road ID must be available.") }.getOrHandle { throw it }
        val roadspaceId = RoadspaceIdentifier(road.id, modelId)

        // build up road reference line
        val planViewGeometries = road.planView.geometryValidated.toEither().getOrHandle { throw it.toIllegalStateException() }
        val roadReferenceLine = _curve3DBuilder.buildCurve3D(planViewGeometries, road.getElevationEntries())
            .mapLeft { Opendrive2RoadspacesTransformationException.PlanViewGeometryException(it.message, opendriveRoadId) }
            .bind()
        val torsionFunction = road.getSuperelevationEntries().fold(
            { LinearFunction.X_AXIS },
            { _functionBuilder.buildCurveTorsion(it) }
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

        // build up the road containing only lane sections, lanes (no road side objects)
        val roadspaceRoad = _roadBuilder
            .buildRoad(roadspaceId, road, roadSurface, roadSurfaceWithoutTorsion, attributes)
            .handleMessageList { messageList += it }

        // build up the road space objects (OpenDRIVE: road objects & signals)
        val roadspaceObjectsFromRoadObjects = road.objects.fold({ emptyList() }, { roadObjects ->
            _roadObjectBuilder.buildRoadspaceObjects(roadspaceId, roadObjects, roadReferenceLine, attributes)
                .handleMessageList { messageList += it }
        })
        val roadspaceObjectsFromRoadSignals = road.signals.fold({ emptyList() }, { roadSignals ->
            _roadObjectBuilder.buildRoadspaceObjects(roadspaceId, roadSignals, roadReferenceLine, attributes)
        })

        // combine the models into a road space object
        val roadspace = Roadspace(
            id = roadspaceId,
            name = road.name,
            referenceLine = roadReferenceLine,
            road = roadspaceRoad,
            roadspaceObjects = roadspaceObjectsFromRoadObjects + roadspaceObjectsFromRoadSignals,
            attributes = attributes
        )
        ContextMessageList(roadspace, messageList)
    }

    private fun buildLateralRoadShape(id: RoadspaceIdentifier, lateralProfileShapeList: NonEmptyList<RoadLateralProfileShape>):
        BivariateFunction {

        val lateralFunctions = lateralProfileShapeList
            .groupBy { it.s }
            .mapValues { NonEmptyList.fromListUnsafe(it.value) }
            .mapValues { _functionBuilder.buildLateralShape(it.value) }
            .toSortedMap()

        return ShapeFunction(
            lateralFunctions,
            extrapolateX = true,
            extrapolateY = configuration.extrapolateLateralRoadShapes
        )
    }

    private fun buildAttributes(road: OpendriveRoad) =
        attributes("${configuration.attributesPrefix}road_") {
            attribute("length", road.length)
            attribute("junction", road.getJunctionOption())
            attribute("rule", road.rule.toString())

            road.link.tap { roadLink ->
                roadLink.predecessor.tap { predecessor ->
                    attribute("predecessor_road", predecessor.getRoadPredecessorSuccessor().map { it.first })
                    attribute("predecessor_contactPoint", predecessor.getRoadPredecessorSuccessor().map { it.second.toString() })
                    attribute("predecessor_junction", predecessor.getJunctionPredecessorSuccessor())
                }

                roadLink.successor.tap { successor ->
                    attribute("successor_road", successor.getRoadPredecessorSuccessor().map { it.first })
                    attribute("successor_contactPoint", successor.getRoadPredecessorSuccessor().map { it.second.toString() })
                    attribute("successor_junction", successor.getJunctionPredecessorSuccessor())
                }
            }
        }
}
