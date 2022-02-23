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

package io.rtron.transformer.roadspaces2citygml

import arrow.core.Either
import com.github.kittinunf.result.success
import io.rtron.io.logging.LogManager
import io.rtron.io.logging.ProgressBar
import io.rtron.math.projection.CoordinateReferenceSystem
import io.rtron.model.citygml.CitygmlModel
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.std.getValueResult
import io.rtron.std.handleFailure
import io.rtron.std.toResult
import io.rtron.std.unwrapValues
import io.rtron.transformer.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.roadspaces2citygml.module.IdentifierAdder
import io.rtron.transformer.roadspaces2citygml.module.TransportationModuleBuilder
import io.rtron.transformer.roadspaces2citygml.transformer.RoadsTransformer
import io.rtron.transformer.roadspaces2citygml.transformer.RoadspaceObjectTransformer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.citygml4j.model.core.AbstractCityObject
import org.citygml4j.model.transportation.Road
import org.citygml4j.model.transportation.TrafficSpaceReference
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
    private val _transportationModuleBuilder = TransportationModuleBuilder(configuration, identifierAdder)
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

        // transformation of each road space
        _reportLogger.info("${this.javaClass.simpleName} with $configuration.")
        val abstractCityObjects = if (configuration.concurrentProcessing)
            transformRoadspacesConcurrently(roadspacesModel)
        else transformRoadspacesSequentially(roadspacesModel)

        // create CityGML model
        val boundingShape = calculateBoundingShape(abstractCityObjects, roadspacesModel.header.coordinateReferenceSystem)
        _reportLogger.info("${this.javaClass.simpleName}: Completed transformation. ✔")
        return CitygmlModel(roadspacesModel.id.modelName, boundingShape, abstractCityObjects)
    }

    private fun transformRoadspacesSequentially(
        roadspacesModel: RoadspacesModel
    ): List<AbstractCityObject> {

        // build objects
        val roadFeaturesProgressBar = ProgressBar("Transforming road", roadspacesModel.getAllRoadspaceNames().size)
        val roadFeatures = roadspacesModel
            .getAllRoadspaceNames()
            .map { _roadLanesTransformer.transformRoad(it, roadspacesModel).also { roadFeaturesProgressBar.step() } }
            .unwrapValues()

        val roadspaceObjectsProgressBar = ProgressBar("Transforming roadspace objects", roadspacesModel.numberOfRoadspaces)
        val roadspaceObjects = roadspacesModel.getAllRoadspaces().flatMap { _roadObjectTransformer.transformRoadspaceObjects(it.roadspaceObjects).also { roadspaceObjectsProgressBar.step() } }

        val additionalRoadLines = if (configuration.transformAdditionalRoadLines) {
            val additionalRoadLinesProgressBar = ProgressBar("Transforming additional road lines", roadspacesModel.numberOfRoadspaces)
            roadspacesModel.getAllRoadspaces().flatMap { _roadLanesTransformer.transformAdditionalRoadLines(it).also { additionalRoadLinesProgressBar.step() } }
        } else emptyList()

        addLaneTopology(roadspacesModel, roadFeatures)
        return roadFeatures + roadspaceObjects + additionalRoadLines
    }

    private fun transformRoadspacesConcurrently(
        roadspacesModel: RoadspacesModel
    ): List<AbstractCityObject> {

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

        val roadFeatures = runBlocking { roadFeaturesDeferred.map { it.await() }.unwrapValues() }
        val roadspaceObjects = runBlocking { roadspaceObjectsDeferred.flatMap { it.await() } }
        val additionalRoadLines = runBlocking { additionalRoadLinesDeferred.flatMap { it.await() } }

        addLaneTopology(roadspacesModel, roadFeatures)
        return roadFeatures + roadspaceObjects + additionalRoadLines
    }

    private fun addLaneTopology(roadspacesModel: RoadspacesModel, dstTransportationSpaces: List<Road>) {
        val trafficSpaceProperties = dstTransportationSpaces.flatMap { it.trafficSpaces } + dstTransportationSpaces.flatMap { it.sections }.flatMap { it.`object`.trafficSpaces }
        val trafficSpacePropertiesAdjusted = trafficSpaceProperties.filter { it.`object`.id != null }

        val lanesMap = roadspacesModel.getAllLeftRightLanes().map { configuration.gmlIdPrefix + it.id.hashedId to it }.toMap()
        trafficSpacePropertiesAdjusted.forEach { currentTrafficSpace ->
            val currentLane = lanesMap.getValueResult(currentTrafficSpace.`object`.id).handleFailure { throw it.error }
            val predecessorLaneIds = roadspacesModel.getPredecessorLaneIdentifiers(currentLane.id).toResult().handleFailure { throw it.error }
            val successorLaneIds = roadspacesModel.getSuccessorLaneIdentifiers(currentLane.id).toResult().handleFailure { throw it.error }

            currentTrafficSpace.`object`.predecessors = predecessorLaneIds.map { TrafficSpaceReference(configuration.gmlIdPrefix + it.hashedId) }
            currentTrafficSpace.`object`.successors = successorLaneIds.map { TrafficSpaceReference(configuration.gmlIdPrefix + it.hashedId) }
        }
    }

    private fun calculateBoundingShape(
        abstractCityObjects: List<AbstractCityObject>,
        crs: Either<Exception, CoordinateReferenceSystem>
    ): BoundingShape {
        val envelope = Envelope()
        crs.toResult().success { envelope.srsName = it.srsName }
        abstractCityObjects.forEach { envelope.include(it.computeEnvelope()) }
        return BoundingShape(envelope)
    }
}
