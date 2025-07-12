/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

import arrow.core.Option
import arrow.core.some
import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.mergeIssueLists
import io.rtron.io.logging.ProgressBar
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.model.opendrive.junction.EJunctionType
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.transformer.converter.opendrive2roadspaces.header.HeaderBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.junction.JunctionBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.report.Opendrive2RoadspacesReport
import io.rtron.transformer.converter.opendrive2roadspaces.roadspaces.RoadMarkRepresentationRegistry
import io.rtron.transformer.converter.opendrive2roadspaces.roadspaces.RoadspaceBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * Transformer from OpenDRIVE data model to the RoadSpaces data model.
 *
 * @param parameters parameters for the transformation
 */
class Opendrive2RoadspacesTransformer(
    val parameters: Opendrive2RoadspacesParameters,
) {
    // Properties and Initializers
    private val logger = KotlinLogging.logger {}

    private val headerBuilder = HeaderBuilder(parameters)
    private val roadspaceBuilder = RoadspaceBuilder(parameters)
    private val junctionBuilder = JunctionBuilder(parameters)

    // Methods

    /**
     * Execution of the transformation.
     *
     * @param opendriveModel OpenDRIVE model as input
     * @return transformed RoadSpaces model as output
     */
    fun transform(opendriveModel: OpendriveModel): Pair<Option<RoadspacesModel>, Opendrive2RoadspacesReport> {
        logger.info { "Parameters: $parameters." }
        opendriveModel.updateAdditionalIdentifiers()
        val report = Opendrive2RoadspacesReport(parameters)

        // general model information
        val header = headerBuilder.buildHeader(opendriveModel.header).handleIssueList { report.conversion += it }

        // transformation of each road
        val roadMarkRepresentationRegistry = RoadMarkRepresentationRegistry.fromHighestOccurrence(opendriveModel)
        val progressBar = ProgressBar("Transforming roads", opendriveModel.road.size)
        val roadspacesWithContextReports =
            if (parameters.concurrentProcessing) {
                transformRoadspacesConcurrently(opendriveModel, roadMarkRepresentationRegistry, progressBar)
            } else {
                transformRoadspacesSequentially(opendriveModel, roadMarkRepresentationRegistry, progressBar)
            }

        val roadspaces = roadspacesWithContextReports.mergeIssueLists().handleIssueList { report.conversion += it }

        val junctions =
            opendriveModel.junction
                .filter { it.typeValidated == EJunctionType.DEFAULT }
                .map { junctionBuilder.buildDefaultJunction(it, roadspaces) }

        val roadspacesModel = RoadspacesModel(header, roadspaces, junctions)

        logger.info { "Completed transformation with ${report.getTextSummary()}." }
        return roadspacesModel.some() to report
    }

    private fun transformRoadspacesSequentially(
        opendriveModel: OpendriveModel,
        roadMarkRepresentationRegistry: RoadMarkRepresentationRegistry,
        progressBar: ProgressBar,
    ): List<ContextIssueList<Roadspace>> =
        opendriveModel.roadAsNonEmptyList.map {
            roadspaceBuilder.buildRoadspace(it, roadMarkRepresentationRegistry).also { progressBar.step() }
        }

    @OptIn(DelicateCoroutinesApi::class)
    private fun transformRoadspacesConcurrently(
        opendriveModel: OpendriveModel,
        roadMarkRepresentationRegistry: RoadMarkRepresentationRegistry,
        progressBar: ProgressBar,
    ): List<ContextIssueList<Roadspace>> {
        val roadspacesDeferred =
            opendriveModel.roadAsNonEmptyList.map {
                GlobalScope.async {
                    roadspaceBuilder.buildRoadspace(it, roadMarkRepresentationRegistry).also { progressBar.step() }
                }
            }
        return runBlocking { roadspacesDeferred.map { it.await() } }
    }
}
