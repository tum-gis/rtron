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

package io.rtron.model.roadspaces

import io.rtron.model.AbstractModel
import io.rtron.model.roadspaces.roadspace.Roadspace


/**
 * The [RoadspacesModel] is a parametric implementation of the objects within a road space and is capable of generating
 * surface based representations. Therefore, it can serve as intermediate model, as it can read the parametric modeling
 * approach of OpenDRIVE and generate the surface based modeling approach of CityGML.
 */
data class RoadspacesModel(
        val header: Header,
        val roadspaces: List<Roadspace> = listOf()
) : AbstractModel()
