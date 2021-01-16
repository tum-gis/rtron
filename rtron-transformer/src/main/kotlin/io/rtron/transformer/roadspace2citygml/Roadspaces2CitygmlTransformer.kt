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

package io.rtron.transformer.roadspace2citygml

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.success
import io.rtron.io.logging.ProgressBar
import io.rtron.math.projection.CoordinateReferenceSystem
import io.rtron.model.citygml.CitygmlModel
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.topology.LaneTopology
import io.rtron.transformer.AbstractTransformer
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.roadspace2citygml.transformer.RoadsTransformer
import io.rtron.transformer.roadspace2citygml.transformer.RoadspaceLineTransformer
import io.rtron.transformer.roadspace2citygml.transformer.RoadspaceObjectTransformer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.citygml4j.model.citygml.core.AbstractCityObject
import org.citygml4j.model.citygml.core.CityModel
import org.citygml4j.model.citygml.core.CityObjectMember
import org.citygml4j.model.gml.basicTypes.Code
import org.citygml4j.util.bbox.BoundingBoxOptions

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

        // general model setup
        val cityModel = CityModel()
        cityModel.name = listOf(Code(roadspacesModel.id.modelName))

        // transformation of each road space
        _reportLogger.info("Transforming roads spaces with ${configuration.parameters}.")
        val progressBar = ProgressBar("Transforming road spaces", roadspacesModel.roadspaces.size)
        val abstractCityObjects = if (configuration.concurrentProcessing)
            transformRoadspacesConcurrently(roadspacesModel.roadspaces.values.toList(), roadspacesModel.laneTopology, progressBar)
        else transformRoadspacesSequentially(roadspacesModel.roadspaces.values.toList(), roadspacesModel.laneTopology, progressBar)

        abstractCityObjects
            .map { CityObjectMember(it) }
            .forEach { cityModel.addCityObjectMember(it) }

        // create CityGML model
        this.calculateBoundedBy(roadspacesModel.header.coordinateReferenceSystem, cityModel)
        _reportLogger.info("Completed transformation: RoadspacesModel -> CitygmlModel. ✔")
        return CitygmlModel(cityModel)
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

    private fun calculateBoundedBy(srcCrs: Result<CoordinateReferenceSystem, Exception>, dstCityModel: CityModel) {
        if (dstCityModel.cityObjectMember.isEmpty()) return

        dstCityModel.boundedBy = dstCityModel.calcBoundedBy(BoundingBoxOptions.defaults())
        if (dstCityModel.boundedBy != null)
            srcCrs.success { dstCityModel.boundedBy.envelope.srsName = it.srsName }
        else _reportLogger.warn("BoundedBy was not calculated correctly.")
    }
}
