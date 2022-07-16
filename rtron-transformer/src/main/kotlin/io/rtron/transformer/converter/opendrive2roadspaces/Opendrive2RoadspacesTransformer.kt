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
import arrow.core.None
import arrow.core.Option
import arrow.core.separateEither
import arrow.core.some
import io.rtron.io.logging.ProgressBar
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.Message
import io.rtron.io.messages.mergeMessageLists
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.model.opendrive.junction.EJunctionType
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.identifier.ModelIdentifier
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.converter.opendrive2roadspaces.header.HeaderBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.junction.JunctionBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.report.Opendrive2RoadspacesReport
import io.rtron.transformer.converter.opendrive2roadspaces.roadspaces.RoadspaceBuilder
import io.rtron.transformer.report.of
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

/**
 * Transformer from OpenDRIVE data model to the RoadSpaces data model.
 *
 * @param configuration configuration for the transformation
 */
class Opendrive2RoadspacesTransformer(
    val configuration: Opendrive2RoadspacesConfiguration
) {

    // Properties and Initializers
    private val logger = KotlinLogging.logger {}

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
    fun transform(opendriveModel: OpendriveModel): Pair<Option<RoadspacesModel>, Opendrive2RoadspacesReport> {
        logger.info("Configuration: $configuration.")
        opendriveModel.updateAdditionalIdentifiers()
        val report = Opendrive2RoadspacesReport()

        // general model information
        val header = _headerBuilder.buildHeader(opendriveModel.header).handleMessageList { report.conversion += it }
        val modelIdentifier = ModelIdentifier(
            modelName = opendriveModel.header.name,
            modelDate = opendriveModel.header.date,
            modelVendor = opendriveModel.header.vendor,
            sourceFileIdentifier = configuration.sourceFileIdentifier
        )

        // transformation of each road
        val progressBar = ProgressBar("Transforming roads", opendriveModel.road.size)
        val (roadspaceExceptions, roadspacesWithContextReports) =
            if (configuration.concurrentProcessing) transformRoadspacesConcurrently(modelIdentifier, opendriveModel, progressBar).separateEither()
            else transformRoadspacesSequentially(modelIdentifier, opendriveModel, progressBar).separateEither()

        if (roadspaceExceptions.isNotEmpty()) {
            roadspaceExceptions.forEach {
                report.conversion += Message.of(it.message, it.location, isFatal = true, wasHealed = false)
            }
            return None to report
        }
        val roadspaces = roadspacesWithContextReports.mergeMessageLists().handleMessageList { report.conversion += it }

        val junctions = opendriveModel.junction
            .filter { it.typeValidated == EJunctionType.DEFAULT }
            .map { _junctionBuilder.buildDefaultJunction(modelIdentifier, it, roadspaces) }

        val roadspacesModel = RoadspacesModel(modelIdentifier, header, roadspaces, junctions)

        logger.info("Completed transformation with ${report.getTextSummary()}.")
        return roadspacesModel.some() to report
    }

    private fun transformRoadspacesSequentially(
        modelIdentifier: ModelIdentifier,
        opendriveModel: OpendriveModel,
        progressBar: ProgressBar
    ): List<Either<Opendrive2RoadspacesTransformationException, ContextMessageList<Roadspace>>> =
        opendriveModel.road.map {
            _roadspaceBuilder.buildRoadspace(modelIdentifier, it).also { progressBar.step() }
        }

    @OptIn(DelicateCoroutinesApi::class)
    private fun transformRoadspacesConcurrently(
        modelIdentifier: ModelIdentifier,
        opendriveModel: OpendriveModel,
        progressBar: ProgressBar
    ): List<Either<Opendrive2RoadspacesTransformationException, ContextMessageList<Roadspace>>> {
        val roadspacesDeferred = opendriveModel.road.map {
            GlobalScope.async {
                _roadspaceBuilder.buildRoadspace(modelIdentifier, it).also { progressBar.step() }
            }
        }
        return runBlocking { roadspacesDeferred.map { it.await() } }
    }
}
