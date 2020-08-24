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

package io.rtron.transformer.roadspace2citygml.parameter

import io.rtron.std.SettableProperty


/**
 * DSL Environment for building up [Roadspaces2CitygmlParameters].
 */
class Roadspaces2CitygmlParametersBuilder {

    private val defaultParameters = Roadspaces2CitygmlParameters()

    private val gmlIdPrefixProperty = SettableProperty(defaultParameters.gmlIdPrefix)
    var gmlIdPrefix by gmlIdPrefixProperty

    private val identifierAttributesPrefixProperty = SettableProperty(defaultParameters.identifierAttributesPrefix)
    var identifierAttributesPrefix by identifierAttributesPrefixProperty

    private val flattenGenericAttributeSetsProperty = SettableProperty(defaultParameters.flattenGenericAttributeSets)
    var flattenGenericAttributeSets by flattenGenericAttributeSetsProperty

    private val discretizationStepSizeProperty = SettableProperty(defaultParameters.discretizationStepSize)
    var discretizationStepSize by discretizationStepSizeProperty

    private val sweepDiscretizationStepSizeProperty = SettableProperty(defaultParameters.sweepDiscretizationStepSize)
    var sweepDiscretizationStepSize by sweepDiscretizationStepSizeProperty

    private val circleSlicesProperty = SettableProperty(defaultParameters.circleSlices)
    var circleSlices by circleSlicesProperty

    fun build() = Roadspaces2CitygmlParameters(
            gmlIdPrefixProperty,
            identifierAttributesPrefixProperty,
            flattenGenericAttributeSetsProperty,
            discretizationStepSizeProperty,
            sweepDiscretizationStepSizeProperty,
            circleSlicesProperty)
}

/**
 * Environment for building up [Roadspaces2CitygmlParameters].
 *
 * @param setup DSL environment for defining the parameters
 */
fun roadspaces2CitygmlParameters(setup: Roadspaces2CitygmlParametersBuilder.() -> Unit): Roadspaces2CitygmlParameters {
    val builder = Roadspaces2CitygmlParametersBuilder()
    return builder.apply(setup).build()
}
