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

package io.rtron.transformer.roadspace2citygml.adder

import org.citygml4j.model.citygml.core.CityModel
import org.citygml4j.model.citygml.core.CityObjectMember
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.transformer.roadspace2citygml.module.GenericsModuleBuilder
import io.rtron.transformer.roadspace2citygml.module.TransportationModuleBuilder
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.std.handleFailure


/**
 * Adds [Road] classes (RoadSpaces model) to the [CityModel] (CityGML model).
 */
class RoadsAdder(
        val configuration: Roadspaces2CitygmlConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    private val _genericsModuleBuilder = GenericsModuleBuilder(configuration)
    private val _transportationModuleBuilder = TransportationModuleBuilder(configuration)

    private val _attributesAdder = AttributesAdder(configuration)

    // Methods

    /**
     * Adds lane surfaces, lane lines, filler surfaces of a [Road] classes (RoadSpaces model) to the [CityModel]
     * (CityGML model).
     */
    fun addLaneSections(srcRoad: Road, dstCityModel: CityModel) {

        srcRoad.getAllLanes(configuration.parameters.discretizationStepSize)
                .forEach { (_, surface, attributes) ->
            addLaneSurface(surface, attributes, dstCityModel)
        }

        srcRoad.getAllFillerSurfaces(configuration.parameters.discretizationStepSize)
                .forEach { addFillerSurfaces(it, dstCityModel) }
        srcRoad.getAllLeftLaneBoundaries()
                .forEach { addLeftRightLaneBoundary(it.second, "LeftLaneBoundary", it.third, dstCityModel) }
        srcRoad.getAllRightLaneBoundaries()
                .forEach { addLeftRightLaneBoundary(it.second, "RightLaneBoundary", it.third, dstCityModel) }
        srcRoad.getAllCurvesOnLanes(0.5)
                .forEach { addLeftRightLaneBoundary(it.second, "CenterLaneLine", it.third, dstCityModel) }
    }

    /**
     * Adds a filler [surface] to the [dstCityModel], which is required to close holes between the surfaces.
     */
    private fun addFillerSurfaces(surface: AbstractSurface3D, dstCityModel: CityModel) {

        val roadObject = _transportationModuleBuilder
                .createFillerSurface(surface)
                .handleFailure { _reportLogger.log(it); return }

        _attributesAdder.addIdName("FillerSurface", roadObject)
        dstCityModel.addCityObjectMember(CityObjectMember(roadObject))
    }

    private fun addLaneSurface(surface: AbstractSurface3D, attributes: AttributeList, dstCityModel: CityModel) {

        val roadObject = _transportationModuleBuilder
                .createLaneSurface(surface)
                .handleFailure { _reportLogger.log(it); return }

        _attributesAdder.addIdName("LaneSurface", roadObject)
        _attributesAdder.addAttributes(attributes, roadObject)
        dstCityModel.addCityObjectMember(CityObjectMember(roadObject))
    }

    private fun addLeftRightLaneBoundary(curve: AbstractCurve3D, name: String, attributes: AttributeList,
                                         dstCityModel: CityModel) {

        val roadObject = _genericsModuleBuilder
                .createGenericObject(curve)
                .handleFailure { _reportLogger.log(it); return }

        _attributesAdder.addIdName(name, roadObject)
        _attributesAdder.addAttributes(attributes, roadObject)
        dstCityModel.addCityObjectMember(CityObjectMember(roadObject))
    }
}
