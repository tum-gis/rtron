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

package io.rtron.transformer.opendrive2roadspaces.parameter

import io.rtron.std.SettableProperty


/**
 * DSL Environment for building up [Opendrive2RoadspacesParameters].
 */
class Opendrive2RoadspacesParametersBuilder {

    private val defaultParameters = Opendrive2RoadspacesParameters()

    private val toleranceProperty = SettableProperty(defaultParameters.tolerance)
    var tolerance by toleranceProperty

    private val attributesPrefixProperty = SettableProperty(defaultParameters.attributesPrefix)
    var attributesPrefix by attributesPrefixProperty

    private val crsEpsgProperty = SettableProperty(defaultParameters.crsEpsg)
    var crsEpsg by crsEpsgProperty

    private val offsetXProperty = SettableProperty(defaultParameters.offsetX)
    var offsetX by offsetXProperty

    private val offsetYProperty = SettableProperty(defaultParameters.offsetY)
    var offsetY by offsetYProperty

    private val offsetZProperty = SettableProperty(defaultParameters.offsetZ)
    var offsetZ by offsetZProperty

    private val extrapolateLateralRoadShapesProperty = SettableProperty(defaultParameters.extrapolateLateralRoadShapes)
    var extrapolateLateralRoadShapes by extrapolateLateralRoadShapesProperty

    fun build() = Opendrive2RoadspacesParameters(toleranceProperty, attributesPrefixProperty, crsEpsgProperty,
            offsetXProperty, offsetYProperty, offsetZProperty, extrapolateLateralRoadShapesProperty)
}

/**
 * Environment for building up [Opendrive2RoadspacesParameters].
 *
 * @param setup DSL environment for defining the parameters
 */
fun opendrive2RoadspacesParameters(setup: Opendrive2RoadspacesParametersBuilder.() -> Unit):
        Opendrive2RoadspacesParameters {
    val builder = Opendrive2RoadspacesParametersBuilder()
    return builder.apply(setup).build()
}
