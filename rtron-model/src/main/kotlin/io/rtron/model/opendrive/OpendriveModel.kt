/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.toNonEmptyListOrNull
import arrow.core.toOption
import arrow.optics.optics
import io.rtron.model.AbstractModel
import io.rtron.model.opendrive.core.Header
import io.rtron.model.opendrive.junction.Junction
import io.rtron.model.opendrive.road.Road
import io.rtron.model.opendrive.signal.Controller

/**
 * Implementation of the OpenDRIVE data model according to version 1.7.
 * See the [official page](https://www.asam.net/standards/detail/opendrive/) from ASAM for more.
 */
@optics
data class OpendriveModel(
    var header: Header = Header(),
    var road: List<Road> = emptyList(),
    var controller: List<Controller> = emptyList(),
    var junction: List<Junction> = emptyList()
) : AbstractModel() {

    // Properties and Initializers
    val roadAsNonEmptyList: NonEmptyList<Road>
        get() = road.toNonEmptyListOrNull()!!

    fun getRoad(id: String): Option<Road> = roadAsNonEmptyList.find { it.id == id }.toOption()

    companion object
}
