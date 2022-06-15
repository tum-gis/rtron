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

package io.rtron.transformer.converter.roadspaces2citygml

import arrow.core.Option
import arrow.core.flattenOption
import arrow.core.getOrHandle
import io.rtron.io.logging.LogManager
import io.rtron.io.logging.ProgressBar
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Report
import io.rtron.io.report.handleReport
import io.rtron.io.report.mergeReports
import io.rtron.math.projection.CoordinateReferenceSystem
import io.rtron.model.citygml.CitygmlModel
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.std.getValueEither
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.module.IdentifierAdder
import io.rtron.transformer.converter.roadspaces2citygml.transformer.RoadsTransformer
import io.rtron.transformer.converter.roadspaces2citygml.transformer.RoadspaceObjectTransformer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.citygml4j.core.model.core.AbstractCityObject
import org.citygml4j.core.model.transportation.Road
import org.citygml4j.core.model.transportation.TrafficSpaceReference
import org.xmlobjects.gml.model.feature.BoundingShape
import org.xmlobjects.gml.model.geometry.Envelope

/**
 * Transformer from the RoadSpaces data model to CityGML.
 *
 * @param configuration configuration for the transformation
 */
class Roadspaces2CitygmlTransformer(
    val configuration: Roadspaces2CitygmlConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    private val identifierAdder = IdentifierAdder(configuration)
    private val _roadObjectTransformer = RoadspaceObjectTransformer(configuration, identifierAdder)
    private val _roadLanesTransformer = RoadsTransformer(configuration, identifierAdder)

    // Methods

    /**
     * Execution of the transformation.
     *
     * @param roadspacesModel RoadSpaces model as input
     * @return generated CityGML model as output
     */
    fun transform(roadspacesModel: RoadspacesModel): CitygmlModel {

        val report = Report()

        // transformation of each road space
        _reportLogger.info("${Roadspaces2CitygmlTransformer::class.simpleName} with $configuration.")
        val abstractCityObjects =
            if (configuration.concurrentProcessing) transformRoadspacesConcurrently(roadspacesModel).handleReport { report += it }
            else transformRoadspacesSequentially(roadspacesModel).handleReport { report += it }

        report.write(configuration.outputReportFilePath)
        // create CityGML model
        val boundingShape = calculateBoundingShape(abstractCityObjects, roadspacesModel.header.coordinateReferenceSystem)
        _reportLogger.info("${Roadspaces2CitygmlTransformer::class.simpleName}: Completed transformation. ✔")
        return CitygmlModel(roadspacesModel.id.modelName, boundingShape, abstractCityObjects)
    }

    private fun transformRoadspacesSequentially(
        roadspacesModel: RoadspacesModel
    ): ContextReport<List<AbstractCityObject>> {

        val report = Report()

        // build objects
        val roadFeaturesProgressBar = ProgressBar("Transforming road", roadspacesModel.getAllRoadspaceNames().size)
        val roadFeatures = roadspacesModel
            .getAllRoadspaceNames()
            .map { _roadLanesTransformer.transformRoad(it, roadspacesModel).also { roadFeaturesProgressBar.step() } }
            .mergeReports()
            .handleReport { report += it }
            .flattenOption()

        val roadspaceObjectsProgressBar = ProgressBar("Transforming roadspace objects", roadspacesModel.numberOfRoadspaces)
        val roadspaceObjects: List<AbstractCityObject> = roadspacesModel
            .getAllRoadspaces()
            .map { _roadObjectTransformer.transformRoadspaceObjects(it.roadspaceObjects).also { roadspaceObjectsProgressBar.step() } }
            .mergeReports()
            .handleReport { report += it }
            .flatten()

        val additionalRoadLines: List<AbstractCityObject> = if (configuration.transformAdditionalRoadLines) {
            val additionalRoadLinesProgressBar = ProgressBar("Transforming additional road lines", roadspacesModel.numberOfRoadspaces)
            roadspacesModel.getAllRoadspaces().map {
                _roadLanesTransformer.transformAdditionalRoadLines(it).also { additionalRoadLinesProgressBar.step() }
            }.mergeReports().handleReport { report += it }.flatten()
        } else emptyList()

        addLaneTopology(roadspacesModel, roadFeatures)
        val cityObjects: List<AbstractCityObject> = roadFeatures + roadspaceObjects + additionalRoadLines
        return ContextReport(cityObjects, report)
    }

    private fun transformRoadspacesConcurrently(
        roadspacesModel: RoadspacesModel
    ): ContextReport<List<AbstractCityObject>> {
        val report = Report()

        // build objects
        val roadFeaturesProgressBar = ProgressBar("Transforming road", roadspacesModel.getAllRoadspaceNames().size)
        val roadFeaturesDeferred = roadspacesModel
            .getAllRoadspaceNames()
            .map {
                GlobalScope.async {
                    _roadLanesTransformer.transformRoad(it, roadspacesModel).also { roadFeaturesProgressBar.step() }
                }
            }

        val roadspaceObjectsProgressBar = ProgressBar("Transforming roadspace objects", roadspacesModel.numberOfRoadspaces)
        val roadspaceObjectsDeferred = roadspacesModel.getAllRoadspaces().map {
            GlobalScope.async {
                _roadObjectTransformer.transformRoadspaceObjects(it.roadspaceObjects)
                    .also { roadspaceObjectsProgressBar.step() }
            }
        }

        val additionalRoadLinesDeferred = if (configuration.transformAdditionalRoadLines) {
            val additionalRoadLinesProgressBar = ProgressBar("Transforming additional road lines", roadspacesModel.numberOfRoadspaces)
            roadspacesModel.getAllRoadspaces().map {
                GlobalScope.async {
                    _roadLanesTransformer.transformAdditionalRoadLines(it).also { additionalRoadLinesProgressBar.step() }
                }
            }
        } else emptyList()

        val roadFeatures = runBlocking {
            roadFeaturesDeferred.map { currentRoadFeature -> currentRoadFeature.await().handleReport { report += it } }.flattenOption()
        }
        val roadspaceObjects = runBlocking {
            roadspaceObjectsDeferred.map { currentRoadSpaceObject -> currentRoadSpaceObject.await().handleReport { report += it } }.flatten()
        }
        val additionalRoadLines = runBlocking {
            additionalRoadLinesDeferred.flatMap { currentRoadLines -> currentRoadLines.await().handleReport { report += it } }
        }

        addLaneTopology(roadspacesModel, roadFeatures)
        val cityObjects: List<AbstractCityObject> = roadFeatures + roadspaceObjects + additionalRoadLines
        return ContextReport(cityObjects, report)
    }

    private fun addLaneTopology(roadspacesModel: RoadspacesModel, dstTransportationSpaces: List<Road>) {
        val trafficSpaceProperties = dstTransportationSpaces.flatMap { it.trafficSpaces } + dstTransportationSpaces.flatMap { it.sections }.flatMap { it.`object`.trafficSpaces }
        val trafficSpacePropertiesAdjusted = trafficSpaceProperties.filter { it.`object`.id != null }

        val lanesMap = roadspacesModel.getAllLeftRightLanes().associateBy { configuration.gmlIdPrefix + it.id.hashedId }
        trafficSpacePropertiesAdjusted.forEach { currentTrafficSpace ->
            val currentLane = lanesMap.getValueEither(currentTrafficSpace.`object`.id).getOrHandle { throw it.toIllegalArgumentException() }
            val predecessorLaneIds = roadspacesModel.getPredecessorLaneIdentifiers(currentLane.id).getOrHandle { throw it }
            val successorLaneIds = roadspacesModel.getSuccessorLaneIdentifiers(currentLane.id).getOrHandle { throw it }

            currentTrafficSpace.`object`.predecessors = predecessorLaneIds.map { TrafficSpaceReference(configuration.gmlIdPrefix + it.hashedId) }
            currentTrafficSpace.`object`.successors = successorLaneIds.map { TrafficSpaceReference(configuration.gmlIdPrefix + it.hashedId) }
        }
    }

    private fun calculateBoundingShape(abstractCityObjects: List<AbstractCityObject>, crs: Option<CoordinateReferenceSystem>): BoundingShape {
        val envelope = Envelope()
        crs.tap { envelope.srsName = it.srsName }
        abstractCityObjects.forEach { envelope.include(it.computeEnvelope()) }
        return BoundingShape(envelope)
    }
}
