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

package io.rtron.transformer.roadspaces2citygml

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.success
import io.rtron.io.logging.ProgressBar
import io.rtron.math.projection.CoordinateReferenceSystem
import io.rtron.model.citygml.CitygmlModel
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.topology.LaneTopology
import io.rtron.transformer.AbstractTransformer
import io.rtron.transformer.roadspaces2citygml.parameter.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.roadspaces2citygml.transformer.RoadsTransformer
import io.rtron.transformer.roadspaces2citygml.transformer.RoadspaceLineTransformer
import io.rtron.transformer.roadspaces2citygml.transformer.RoadspaceObjectTransformer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.citygml4j.model.core.AbstractCityObject
import org.xmlobjects.gml.model.feature.BoundingShape
import org.xmlobjects.gml.model.geometry.Envelope

/**
 * Transformer from the RoadSpaces data model to CityGML.
 *
 * @param configuration configuration for the transformation
 */
class Roadspaces2CitygmlTransformer(
    val configuration: Roadspaces2CitygmlConfiguration
) : AbstractTransformer() {

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    private val _roadspaceLineTransformer = RoadspaceLineTransformer(configuration)
    private val _roadObjectTransformer = RoadspaceObjectTransformer(configuration)
    private val _roadLanesTransformer = RoadsTransformer(configuration)

    // Methods

    /**
     * Execution of the transformation.
     *
     * @param roadspacesModel RoadSpaces model as input
     * @return generated CityGML model as output
     */
    fun transform(roadspacesModel: RoadspacesModel): CitygmlModel {

        // transformation of each road space
        _reportLogger.info("Transforming roads spaces with ${configuration.parameters}.")
        val progressBar = ProgressBar("Transforming road spaces", roadspacesModel.roadspaces.size)
        val abstractCityObjects = if (configuration.concurrentProcessing)
            transformRoadspacesConcurrently(roadspacesModel.roadspaces.values.toList(), roadspacesModel.laneTopology, progressBar)
        else transformRoadspacesSequentially(roadspacesModel.roadspaces.values.toList(), roadspacesModel.laneTopology, progressBar)

        // create CityGML model
        val boundingShape = calculateBoundingShape(abstractCityObjects, roadspacesModel.header.coordinateReferenceSystem)
        _reportLogger.info("Completed transformation: RoadspacesModel -> CitygmlModel. ✔")
        return CitygmlModel(roadspacesModel.id.modelName, boundingShape, abstractCityObjects)
    }

    private fun transformRoadspacesSequentially(
        srcRoadspaces: List<Roadspace>,
        srcLaneTopology: LaneTopology,
        progressBar: ProgressBar
    ): List<AbstractCityObject> =
        srcRoadspaces.flatMap {
            transform(it, srcLaneTopology).also { progressBar.step() }
        }

    private fun transformRoadspacesConcurrently(
        srcRoadspaces: List<Roadspace>,
        srcLaneTopology: LaneTopology,
        progressBar: ProgressBar
    ): List<AbstractCityObject> {

        val resultsDeferred = srcRoadspaces.map {
            GlobalScope.async {
                transform(it, srcLaneTopology).also { progressBar.step() }
            }
        }
        return runBlocking { resultsDeferred.flatMap { it.await() } }
    }

    /**
     * Transform a single [Roadspace] and add the objects to the [AbstractCityObject].
     */
    private fun transform(srcRoadspace: Roadspace, srcLaneTopology: LaneTopology): List<AbstractCityObject> =
        _roadspaceLineTransformer.transformRoadReferenceLine(srcRoadspace).toList() +
            _roadLanesTransformer.transformRoadCenterLaneLines(srcRoadspace.road) +
            _roadLanesTransformer.transformLaneLines(srcRoadspace.road) +
            _roadLanesTransformer.transformLaneSurfaces(srcRoadspace.road) +
            _roadLanesTransformer.transformLateralFillerSurfaces(srcRoadspace.road) +
            _roadLanesTransformer.transformLongitudinalFillerSurfaces(srcRoadspace.road, srcLaneTopology) +
            _roadLanesTransformer.transformRoadMarkings(srcRoadspace.road) +
            _roadObjectTransformer.transformRoadspaceObjects(srcRoadspace.roadspaceObjects)

    private fun calculateBoundingShape(srcAbstractCityObjects: List<AbstractCityObject>, srcCrs: Result<CoordinateReferenceSystem, Exception>): BoundingShape {
        val envelope = Envelope()
        srcCrs.success { envelope.srsName = it.srsName }
        srcAbstractCityObjects.forEach { envelope.include(it.computeEnvelope()) }
        return BoundingShape(envelope)
    }
}
