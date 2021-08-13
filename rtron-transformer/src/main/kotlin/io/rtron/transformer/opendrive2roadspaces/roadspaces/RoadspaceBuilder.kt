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

package io.rtron.transformer.opendrive2roadspaces.roadspaces

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import io.rtron.io.logging.LogManager
import io.rtron.math.analysis.function.bivariate.BivariateFunction
import io.rtron.math.analysis.function.bivariate.pure.PlaneFunction
import io.rtron.math.analysis.function.bivariate.pure.ShapeFunction
import io.rtron.math.geometry.curved.threed.surface.CurveRelativeParametricSurface3D
import io.rtron.model.opendrive.road.lateralprofile.RoadLateralProfileShape
import io.rtron.model.roadspaces.ModelIdentifier
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.std.handleFailure
import io.rtron.transformer.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.opendrive2roadspaces.geometry.Curve3DBuilder
import io.rtron.model.opendrive.road.Road as OpendriveRoad

/**
 * Builder of [Roadspace] (RoadSpaces data model) to the Road class of the OpenDRIVE data model.
 */
class RoadspaceBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {
    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    private val _curve3DBuilder = Curve3DBuilder(_reportLogger, configuration)
    private val _functionBuilder = FunctionBuilder(_reportLogger, configuration)

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
    fun buildRoadspace(modelId: ModelIdentifier, road: OpendriveRoad): Result<Roadspace, Exception> {

        // check whether source model is processable
        val roadspaceId = RoadspaceIdentifier(road.id, modelId)
        road.isProcessable(configuration.tolerance)
            .map { _reportLogger.log(it, roadspaceId.toString()) }
            .handleFailure { return it }

        // build up road reference line
        val roadReferenceLine = _curve3DBuilder.buildCurve3D(
            roadspaceId,
            road.planView.geometry,
            road.elevationProfile.elevation
        ).handleFailure { return it }
        val torsionFunction = _functionBuilder.buildCurveTorsion(
            roadspaceId,
            road.lateralProfile.superelevation
        )

        // build attributes for the road
        val attributes = buildAttributes(road)

        // build up road's surface geometries
        val lateralProfileRoadShape = buildLateralRoadShape(roadspaceId, road.lateralProfile.shape)
        val roadSurface = CurveRelativeParametricSurface3D(
            roadReferenceLine.copy(torsionFunction = torsionFunction),
            lateralProfileRoadShape
        )
        val roadSurfaceWithoutTorsion = CurveRelativeParametricSurface3D(roadReferenceLine, lateralProfileRoadShape)

        // build up the road containing only lane sections, lanes (no road side objects)
        val roadspaceRoad = _roadBuilder
            .buildRoad(roadspaceId, road, roadSurface, roadSurfaceWithoutTorsion, attributes)
            .handleFailure { return it }

        // build up the road space objects (OpenDRIVE: road objects & signals)
        val roadspaceObjects =
            _roadObjectBuilder.buildRoadspaceObjects(roadspaceId, road.objects, roadReferenceLine, attributes) +
                _roadObjectBuilder.buildRoadspaceObjects(roadspaceId, road.signals, roadReferenceLine, attributes)

        // combine the models into a road space object
        val roadspace = Roadspace(
            id = roadspaceId,
            name = road.name,
            referenceLine = roadReferenceLine,
            road = roadspaceRoad,
            roadspaceObjects = roadspaceObjects,
            attributes = attributes
        )
        return Result.success(roadspace)
    }

    private fun buildLateralRoadShape(id: RoadspaceIdentifier, lateralProfileShapeList: List<RoadLateralProfileShape>):
        BivariateFunction {

        if (lateralProfileShapeList.isEmpty()) return PlaneFunction.ZERO

        val lateralFunctions = lateralProfileShapeList
            .groupBy { it.s }
            .mapValues { _functionBuilder.buildLateralShape(id, it.value) }
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
            attribute("junction", road.getJunction())
            attribute("rule", road.rule.toString())

            attribute("predecessor_road", road.link.predecessor.getRoadPredecessorSuccessor().map { it.first })
            attribute(
                "predecessor_contactPoint",
                road.link.predecessor.getRoadPredecessorSuccessor().map { it.second.toString() }
            )
            attribute("predecessor_junction", road.link.predecessor.getJunctionPredecessorSuccessor())

            attribute("successor_road", road.link.successor.getRoadPredecessorSuccessor().map { it.first })
            attribute(
                "successor_contactPoint",
                road.link.successor.getRoadPredecessorSuccessor().map { it.second.toString() }
            )
            attribute("successor_junction", road.link.successor.getJunctionPredecessorSuccessor())
        }
}
