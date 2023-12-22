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

package io.rtron.transformer.converter.roadspaces2citygml.module

import org.citygml4j.core.model.core.AbstractCityObject
import org.xmlobjects.gml.model.basictypes.Code

/**
 * Adds object identifiers from the RoadSpaces model to an [AbstractCityObject] (CityGML model).
 */
object IdentifierAdder {

    // Methods

    /** Adds the [gmlId] to the [dstCityObject]. */
    fun addIdentifier(gmlId: String, dstCityObject: AbstractCityObject) {
        dstCityObject.id = gmlId
    }

    /** Adds the [gmlId] and the [name] to the [dstCityObject]. */
    fun addIdentifier(gmlId: String, name: String, dstCityObject: AbstractCityObject) {
        dstCityObject.id = gmlId
        dstCityObject.names = listOf(Code(name))
    }
}
