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
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlParameters

data class ProjectUserConfiguration(
    val opendrive2RoadspacesParameters: Opendrive2RoadspacesParameters = Opendrive2RoadspacesParameters(),
    val roadspaces2CitygmlParameters: Roadspaces2CitygmlParameters = Roadspaces2CitygmlParameters()
) {

    // Methods
    infix fun leftMerge(other: ProjectUserConfiguration) =
        ProjectUserConfiguration(
            opendrive2RoadspacesParameters = this.opendrive2RoadspacesParameters leftMerge other.opendrive2RoadspacesParameters,
            roadspaces2CitygmlParameters = this.roadspaces2CitygmlParameters leftMerge other.roadspaces2CitygmlParameters
        )
}
