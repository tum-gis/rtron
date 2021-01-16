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

package io.rtron.transformer.roadspace2citygml.transformer

import io.rtron.io.logging.Logger
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObjectIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlParameters
import org.citygml4j.model.citygml.core.AbstractCityObject
import org.citygml4j.model.gml.basicTypes.Code
import org.citygml4j.util.gmlid.DefaultGMLIdManager
import java.util.UUID
import kotlin.collections.HashMap

/**
 * Adds object identifiers from the RoadSpaces model to an [AbstractCityObject] (CityGML model).
 */
class IdentifierAdder(
    private val parameters: Roadspaces2CitygmlParameters,
    private val reportLogger: Logger
) {

    // Properties and Initializers
    private val _checkedIdPrefix by lazy {
        if (!DefaultGMLIdManager.getInstance()!!.isValidPrefix(parameters.gmlIdPrefix))
            reportLogger.warnOnce("Unvalid ID prefix configured: ${parameters.gmlIdPrefix}")
        parameters.gmlIdPrefix
    }

    /** count index for already used identifier keys */
    private val _usedKeyCount = HashMap<String, Int>()

    // Methods

    /**
     * Adds a pseudo random hash id (hash based on the [id] and the [name]) to the [dstCityObject].
     */
    fun addIdentifier(id: RoadspaceIdentifier, name: String, dstCityObject: AbstractCityObject) {
        val hashKey = name + '_' + id.roadspaceId + '_' + id.modelIdentifier.fileHashSha256
        dstCityObject.id = generateHashUUID(hashKey)
        dstCityObject.addName(Code(name))
    }

    /**
     * Adds a pseudo random hash id (hash based on the [id]) to the [dstCityObject].
     */
    fun addIdentifier(id: RoadspaceObjectIdentifier, dstCityObject: AbstractCityObject) {
        val hashKey = id.roadspaceObjectId + '_' +
            id.roadspaceIdentifier.roadspaceId + '_' +
            id.roadspaceIdentifier.modelIdentifier.fileHashSha256

        dstCityObject.id = generateHashUUID(hashKey)
        dstCityObject.addName(Code(id.roadspaceObjectName))
    }

    /**
     * Adds a pseudo random hash id (hash based on the [id] and the [name]) to the [dstCityObject].
     */
    fun addIdentifier(id: LaneIdentifier, name: String, dstCityObject: AbstractCityObject) {
        val hashKey = name + '_' +
            id.laneId + '_' +
            id.laneSectionIdentifier.laneSectionId + '_' +
            id.laneSectionIdentifier.roadspaceIdentifier.roadspaceId + '_' +
            id.laneSectionIdentifier.roadspaceIdentifier.modelIdentifier.fileHashSha256

        dstCityObject.id = generateHashUUID(hashKey)
        dstCityObject.addName(Code(name))
    }

    /**
     * Returns a completely random id.
     */
    fun generateRandomUUID(): String = _checkedIdPrefix + UUID.randomUUID().toString()

    /**
     * Generates a unique UUID based on a hash of the [key] (even if the key has already been used).
     */
    private fun generateHashUUID(key: String): String {
        require(!key.isBlank()) { "The key for generating a hashed UUID must not be blank." }
        val countIndex = _usedKeyCount.getOrDefault(key, 0) + 1
        val keyWithCount = key + '_' + countIndex

        val uuid = UUID.nameUUIDFromBytes(keyWithCount.toByteArray()).toString()
        _usedKeyCount[key] = countIndex
        return _checkedIdPrefix + uuid
    }
}
