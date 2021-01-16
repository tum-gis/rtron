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

package io.rtron.main.project.configuration

import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesParameters
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesParametersBuilder
import io.rtron.transformer.opendrive2roadspaces.parameter.opendrive2RoadspacesParameters
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlParameters
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlParametersBuilder
import io.rtron.transformer.roadspace2citygml.parameter.roadspaces2CitygmlParameters

class ConfigurationBuilder {

    // Properties and Initializers
    private var opendrive2RoadspacesParameters = Opendrive2RoadspacesParameters()
    private var roadspace2CitygmlParameters = Roadspaces2CitygmlParameters()

    // Methods
    fun opendrive2roadspaces(setup: Opendrive2RoadspacesParametersBuilder.() -> Unit) {
        opendrive2RoadspacesParameters = opendrive2RoadspacesParameters(setup)
    }
    fun roadspaces2citygml(setup: Roadspaces2CitygmlParametersBuilder.() -> Unit) {
        roadspace2CitygmlParameters = roadspaces2CitygmlParameters(setup)
    }

    fun build() = ProjectUserConfiguration(opendrive2RoadspacesParameters, roadspace2CitygmlParameters)
}

fun configure(setup: ConfigurationBuilder.() -> Unit): ProjectUserConfiguration {
    val builder = ConfigurationBuilder()
    builder.setup()
    return builder.build()
}
