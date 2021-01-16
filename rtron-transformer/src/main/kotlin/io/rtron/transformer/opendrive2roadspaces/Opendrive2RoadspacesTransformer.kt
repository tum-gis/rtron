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

package io.rtron.transformer.opendrive2roadspaces

import com.github.kittinunf.result.Result
import io.rtron.io.logging.ProgressBar
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.roadspaces.ModelIdentifier
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.topology.LaneTopology
import io.rtron.std.handleAndRemoveFailureIndexed
import io.rtron.transformer.AbstractTransformer
import io.rtron.transformer.TransformerConfiguration
import io.rtron.transformer.opendrive2roadspaces.header.HeaderBuilder
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesParameters
import io.rtron.transformer.opendrive2roadspaces.roadspaces.RoadspaceBuilder
import io.rtron.transformer.opendrive2roadspaces.topology.TopologyBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * Transformer from OpenDRIVE data model to the RoadSpaces data model.
 *
 * @param configuration configuration for the transformation
 */
class Opendrive2RoadspacesTransformer(
    val configuration: TransformerConfiguration<Opendrive2RoadspacesParameters>
) : AbstractTransformer() {

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    private val _headerBuilder = HeaderBuilder(configuration)
    private val _roadspaceBuilder = RoadspaceBuilder(configuration)
    private val _topologyBuilder = TopologyBuilder(configuration)

    // Methods

    /**
     * Execution of the transformation.
     *
     * @param opendriveModel OpenDRIVE model as input
     * @return transformed RoadSpaces model as output
     */
    fun transform(opendriveModel: OpendriveModel): RoadspacesModel {
        _reportLogger.info("Transforming roads with ${configuration.parameters}.")

        // general model information
        val header = _headerBuilder.buildHeader(opendriveModel.header)
        val modelIdentifier = ModelIdentifier(
            modelName = opendriveModel.header.name,
            modelDate = opendriveModel.header.date,
            modelVendor = opendriveModel.header.vendor,
            sourceFileIdentifier = configuration.sourceFileIdentifier
        )

        // transformation of each road
        val progressBar = ProgressBar("Transforming roads", opendriveModel.road.size)
        val roadspacesResults =
            if (configuration.concurrentProcessing) transformRoadspacesConcurrently(modelIdentifier, opendriveModel, progressBar)
            else transformRoadspacesSequentially(modelIdentifier, opendriveModel, progressBar)

        val roadspaces = roadspacesResults.handleAndRemoveFailureIndexed { index, failure ->
            _reportLogger.log(failure, "RoadId=${opendriveModel.road[index].id}", "Removing road.")
        }.map { it.id to it }.toMap()

        val junctions = opendriveModel.junction
            .map { _topologyBuilder.buildJunction(modelIdentifier, it) }
            .map { it.id to it }.toMap()
        val laneTopology = LaneTopology(roadspaces, junctions)

        return RoadspacesModel(modelIdentifier, header, roadspaces, laneTopology)
            .also { _reportLogger.info("Completed transformation: OpenDRIVE -> RoadspacesModel. ✔") }
    }

    private fun transformRoadspacesSequentially(
        modelIdentifier: ModelIdentifier,
        opendriveModel: OpendriveModel,
        progressBar: ProgressBar
    ): List<Result<Roadspace, Exception>> =
        opendriveModel.road.map {
            _roadspaceBuilder.buildRoadspace(modelIdentifier, it).also { progressBar.step() }
        }

    private fun transformRoadspacesConcurrently(
        modelIdentifier: ModelIdentifier,
        opendriveModel: OpendriveModel,
        progressBar: ProgressBar
    ): List<Result<Roadspace, Exception>> {
        val roadspacesDeferred = opendriveModel.road.map {
            GlobalScope.async {
                _roadspaceBuilder.buildRoadspace(modelIdentifier, it).also { progressBar.step() }
            }
        }
        return runBlocking { roadspacesDeferred.map { it.await() } }
    }
}
