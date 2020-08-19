/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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
import io.rtron.math.analysis.function.bivariate.BivariateFunction
import io.rtron.math.analysis.function.bivariate.pure.PlaneFunction
import io.rtron.math.geometry.curved.threed.surface.CurveRelativeParametricSurface3D
import io.rtron.model.opendrive.road.lateralprofile.RoadLateralProfileShape
import io.rtron.model.roadspaces.ModelIdentifier
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.std.handleFailure
import io.rtron.transformer.opendrive2roadspaces.analysis.FunctionBuilder
import io.rtron.transformer.opendrive2roadspaces.geometry.Curve3DBuilder
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesConfiguration
import io.rtron.model.opendrive.road.Road as OpendriveModelRoad


/**
 * Builder of [Roadspace] (RoadSpaces data model) to the Road class of the OpenDRIVE data model.
 */
class RoadspaceBuilder(
        private val configuration: Opendrive2RoadspacesConfiguration
) {
    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    private val _curve3DBuilder = Curve3DBuilder(_reportLogger, configuration.parameters)
    private val _functionBuilder = FunctionBuilder(_reportLogger, configuration.parameters)

    private val _roadBuilder = RoadBuilder(configuration)
    private val _roadObjectBuilder = RoadspaceObjectBuilder(configuration)

    // Methods

    /**
     * Builds a [Roadspace] of the RoadSpaces data model from the OpenDRIVE road.
     *
     * @param modelId identifier of the actual model
     * @param srcRoad source OpenDRIVE model
     * @return transformed [Roadspace]
     */
    fun buildRoadspace(modelId: ModelIdentifier, srcRoad: OpendriveModelRoad): Result<Roadspace, Exception> {

        // check whether source model is processable
        val roadspaceId = RoadspaceIdentifier(srcRoad.name, srcRoad.id, modelId)
        srcRoad.isProcessable(configuration.parameters.tolerance)
                .map { _reportLogger.log(it, roadspaceId.toString()) }
                .handleFailure { return it }

        // build up road reference line
        val roadReferenceLine = _curve3DBuilder.buildCurve3D(roadspaceId, srcRoad.planView.geometry,
                srcRoad.elevationProfile.elevation)
        val torsionFunction = _functionBuilder.buildCurveTorsion(roadspaceId,
                srcRoad.lateralProfile.superelevation)

        // build attributes for the road
        val attributes = buildAttributes(srcRoad)

        // build up road's surface geometries
        val lateralProfileRoadShape = buildLateralRoadShape(srcRoad.lateralProfile.shape)
        val roadSurface = CurveRelativeParametricSurface3D(roadReferenceLine.copy(torsionFunction = torsionFunction),
                lateralProfileRoadShape)
        val roadSurfaceWithoutTorsion = CurveRelativeParametricSurface3D(roadReferenceLine, lateralProfileRoadShape)

        // build up the road containing only lane sections, lanes (no road side objects)
        val road = _roadBuilder
                .buildRoad(roadspaceId, srcRoad.lanes, roadSurface, roadSurfaceWithoutTorsion, attributes)
                .handleFailure { return it }

        // build up the road space objects (OpenDRIVE: road objects & signals)
        val roadspaceObjects =
                _roadObjectBuilder.buildRoadspaceObjects(roadspaceId, srcRoad.objects, roadReferenceLine, attributes) +
                        _roadObjectBuilder.buildRoadspaceObjects(roadspaceId, srcRoad.signals, roadReferenceLine, attributes)

        // combine the models into a road space object
        val roadspace = Roadspace(
                id = roadspaceId,
                referenceLine = roadReferenceLine,
                road = road,
                roadspaceObjects = roadspaceObjects,
                attributes = attributes)
        return Result.success(roadspace)
    }

    private fun buildLateralRoadShape(srcLateralProfileShapeList: List<RoadLateralProfileShape>): BivariateFunction {
        if (srcLateralProfileShapeList.isNotEmpty())
            _reportLogger.warnOnce("Lateral shape profile is not implemented yet.")
        return PlaneFunction.ZERO
    }

    private fun buildAttributes(srcRoad: OpendriveModelRoad) =
            attributes("${configuration.parameters.attributesPrefix}road_") {
                attribute("length", srcRoad.length)
                attribute("junction", srcRoad.junction)
                attribute("rule", srcRoad.rule.toString())
            }
}
