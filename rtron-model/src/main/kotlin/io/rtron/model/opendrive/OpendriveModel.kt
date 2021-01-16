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

package io.rtron.model.opendrive

import io.rtron.model.AbstractModel
import io.rtron.model.opendrive.controller.Controller
import io.rtron.model.opendrive.header.Header
import io.rtron.model.opendrive.junction.Junction
import io.rtron.model.opendrive.road.Road

/**
 * Implementation of the OpenDRIVE data model according to version 1.6.
 * See the [official page](https://www.asam.net/standards/detail/opendrive/) from ASAM for more.
 */
data class OpendriveModel(
    var header: Header = Header(),
    var road: List<Road> = listOf(),
    var controller: List<Controller> = listOf(),
    var junction: List<Junction> = listOf()
) : AbstractModel()
