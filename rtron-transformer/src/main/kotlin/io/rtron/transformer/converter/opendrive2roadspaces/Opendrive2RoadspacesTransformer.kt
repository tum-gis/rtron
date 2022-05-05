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

package io.rtron.transformer.converter.opendrive2roadspaces

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.sequenceEither
import io.rtron.io.logging.LogManager
import io.rtron.io.logging.ProgressBar
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
import io.rtron.model.roadspaces.ModelIdentifier
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.converter.opendrive2roadspaces.header.HeaderBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.junction.JunctionBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.roadspaces.RoadspaceBuilder
import io.rtron.transformer.evaluator.opendrive.report.of
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * Transformer from OpenDRIVE data model to the RoadSpaces data model.
 *
 * @param configuration configuration for the transformation
 */
class Opendrive2RoadspacesTransformer(
    val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    private val _headerBuilder = HeaderBuilder(configuration)
    private val _roadspaceBuilder = RoadspaceBuilder(configuration)
    private val _junctionBuilder = JunctionBuilder(configuration)

    // Methods

    /**
     * Execution of the transformation.
     *
     * @param opendriveModel OpenDRIVE model as input
     * @return transformed RoadSpaces model as output
     */
    fun transform(opendriveModel: OpendriveModel): Either<Opendrive2RoadspacesTransformerException, RoadspacesModel> = either.eager {
        _reportLogger.info("${this.javaClass.simpleName} with $configuration.")
        val report = Report()

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

        val roadspaces: List<Roadspace> = roadspacesResults.mapIndexed { index, element ->
            element.tapLeft {
                report += Message.of(it.toString(), RoadIdentifier(opendriveModel.road[index].id).toStringMap(), isFatal = true, wasHealed = false)
                _reportLogger.log(it, "RoadId=${opendriveModel.road[index].id}", "Ignoring road.")
            }
        }.sequenceEither().mapLeft {
            report.write(configuration.outputReportFilePath)
            Opendrive2RoadspacesTransformerException.FatalError(it.toString())
        }.bind()

        val junctions = opendriveModel.junction
            .map { _junctionBuilder.buildJunction(modelIdentifier, it, roadspaces) }

        val roadspacesModel = RoadspacesModel(modelIdentifier, header, roadspaces, junctions)

        report.write(configuration.outputReportFilePath)
        _reportLogger.info("${this.javaClass.simpleName}: Completed transformation. ✔")
        roadspacesModel
    }

    private fun transformRoadspacesSequentially(
        modelIdentifier: ModelIdentifier,
        opendriveModel: OpendriveModel,
        progressBar: ProgressBar
    ): List<Either<Exception, Roadspace>> =
        opendriveModel.road.map {
            _roadspaceBuilder.buildRoadspace(modelIdentifier, it).also { progressBar.step() }
        }

    @OptIn(DelicateCoroutinesApi::class)
    private fun transformRoadspacesConcurrently(
        modelIdentifier: ModelIdentifier,
        opendriveModel: OpendriveModel,
        progressBar: ProgressBar
    ): List<Either<Exception, Roadspace>> {
        val roadspacesDeferred = opendriveModel.road.map {
            GlobalScope.async {
                _roadspaceBuilder.buildRoadspace(modelIdentifier, it).also { progressBar.step() }
            }
        }
        return runBlocking { roadspacesDeferred.map { it.await() } }
    }
}
