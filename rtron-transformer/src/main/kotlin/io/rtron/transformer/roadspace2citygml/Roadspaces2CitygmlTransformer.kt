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
import io.rtron.transformer.AbstractTransformer
import io.rtron.transformer.roadspace2citygml.adder.RoadObjectAdder
import io.rtron.transformer.roadspace2citygml.adder.RoadsAdder
import io.rtron.transformer.roadspace2citygml.adder.RoadspaceLineAdder
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import org.citygml4j.model.citygml.core.CityModel
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

    private val _roadLineAdder = RoadspaceLineAdder(configuration)
    private val _roadObjectAdder = RoadObjectAdder(configuration)
    private val _lanesAdder = RoadsAdder(configuration)

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
        roadspacesModel.roadspaces.forEach { currentRoadspace ->
            progressBar.step()
            transform(currentRoadspace, cityModel)
        }

        // create CityGML model
        this.calculateBoundedBy(roadspacesModel.header.coordinateReferenceSystem, cityModel)
        _reportLogger.info("Completed transformation: RoadspacesModel -> CitygmlModel. ✔")
        return CitygmlModel(cityModel)
    }

    /**
     * Transform a single [Roadspace] and add the objects to the [dstCityModel].
     */
    private fun transform(srcRoadspace: Roadspace, dstCityModel: CityModel) {
        _roadLineAdder.addRoadReferenceLine(srcRoadspace, dstCityModel)
        _roadLineAdder.addLaneReferenceLine(srcRoadspace, dstCityModel)

        _roadObjectAdder.addRoadspaceObjects(srcRoadspace.roadspaceObjects, dstCityModel)
        _lanesAdder.addLaneSections(srcRoadspace.road, dstCityModel)
    }


    private fun calculateBoundedBy(srcCrs: Result<CoordinateReferenceSystem, Exception>, dstCityModel: CityModel) {
        if (dstCityModel.cityObjectMember.isEmpty()) return

        dstCityModel.boundedBy = dstCityModel.calcBoundedBy(BoundingBoxOptions.defaults())
        if (dstCityModel.boundedBy != null)
            srcCrs.success { dstCityModel.boundedBy.envelope.srsName = it.srsName }
        else _reportLogger.warn("BoundedBy was not calculated correctly.")
    }
}
