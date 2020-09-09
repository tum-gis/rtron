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

import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.attribute.toAttributes
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspace2citygml.module.GenericsModuleBuilder
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import org.citygml4j.model.citygml.core.CityModel
import org.citygml4j.model.citygml.core.CityObjectMember


/**
 * Adds lines, such as lane boundaries and center lines (RoadSpaces model), to the [CityModel] (CityGML model).
 */
class RoadspaceLineAdder(
        private val configuration: Roadspaces2CitygmlConfiguration
) {
    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    private val _identifierAdder = IdentifierAdder(configuration.parameters, _reportLogger)
    private val _attributesAdder = AttributesAdder(configuration.parameters)
    private val _genericsModuleBuilder = GenericsModuleBuilder(configuration)

    // Methods

    /**
     * Adds the reference line of the road to the [CityModel].
     */
    fun addRoadReferenceLine(srcRoadspace: Roadspace, dstCityModel: CityModel) {
        val abstractCityObject = _genericsModuleBuilder.createGenericObject(srcRoadspace.referenceLine)
                .handleFailure { _reportLogger.log(it); return }

        _identifierAdder.addIdentifier(srcRoadspace.id, "RoadReferenceLine", abstractCityObject)
        _attributesAdder.addAttributes(srcRoadspace.id.toAttributes(configuration.parameters.identifierAttributesPrefix) +
                srcRoadspace.attributes, abstractCityObject)
        dstCityModel.addCityObjectMember(CityObjectMember(abstractCityObject))
    }

}
