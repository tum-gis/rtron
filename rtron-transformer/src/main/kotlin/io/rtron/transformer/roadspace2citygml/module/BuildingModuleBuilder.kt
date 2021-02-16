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

package io.rtron.transformer.roadspace2citygml.module

import com.github.kittinunf.result.Result
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspace2citygml.geometry.GeometryTransformer
import io.rtron.transformer.roadspace2citygml.geometry.populateLod1Geometries
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import org.citygml4j.model.building.Building

/**
 * Builder for city objects of the CityGML Building module.
 */
class BuildingModuleBuilder(
    val configuration: Roadspaces2CitygmlConfiguration
) {

    // Methods
    fun createBuildingObject(geometryTransformer: GeometryTransformer): Result<Building, Exception> {
        val building = Building()
        building.populateLod1Geometries(geometryTransformer).handleFailure { return it }

        return Result.success(building)
    }
}
