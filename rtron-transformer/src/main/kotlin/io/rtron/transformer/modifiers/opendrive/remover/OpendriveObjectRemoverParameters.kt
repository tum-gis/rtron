/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.modifiers.opendrive.remover

import io.rtron.model.opendrive.objects.EObjectType
import kotlinx.serialization.Serializable

@Serializable
data class OpendriveObjectRemoverParameters(
    /** remove road objects without type */
    val removeRoadObjectsWithoutType: Boolean,
    /** remove road objects of type */
    val removeRoadObjectsOfTypes: Set<EObjectType>,
) {
    companion object {
        val DEFAULT_REMOVE_ROAD_OBJECTS_WITHOUT_TYPE = false
        val DEFAULT_REMOVE_ROAD_OBJECTS_OF_TYPES = emptySet<EObjectType>()
    }
}
