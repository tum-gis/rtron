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

package io.rtron.transformer.roadspaces2citygml.module

import io.rtron.model.roadspaces.junction.JunctionIdentifier
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObjectIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.transformer.roadspaces2citygml.parameter.Roadspaces2CitygmlParameters
import org.citygml4j.model.core.AbstractCityObject
import org.xmlobjects.gml.model.basictypes.Code
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Adds object identifiers from the RoadSpaces model to an [AbstractCityObject] (CityGML model).
 */
class IdentifierAdder(
    private val parameters: Roadspaces2CitygmlParameters
) {

    // Properties and Initializers

    /** count index for already used identifier keys */
    private val usedKeyCount = ConcurrentHashMap<String, Int>()

    /** records of already used identifier (to ensure uniqueness) */
    private val usedUniqueIdentifiers = ConcurrentHashMap<String, Boolean>()

    // Methods

    /** Returns the GML identifier (with prefix) of the [id]. */
    fun getGmlIdentifier(id: JunctionIdentifier) = parameters.gmlIdPrefix + id.hashedId

    /** Returns the GML identifier (with prefix) of the [id]. */
    fun getGmlIdentifier(id: RoadspaceObjectIdentifier) = parameters.gmlIdPrefix + id.hashedId

    /** Returns the GML identifier (with prefix) of the [id]. */
    fun getGmlIdentifier(id: LaneIdentifier) = parameters.gmlIdPrefix + id.hashedId

    /**
     * Adds the hashed id and the name of the [RoadspaceIdentifier] to the [dstCityObject], whereby the [id] can only be
     * assigned once (otherwise an [IllegalArgumentException] is thrown).
     *
     * @param id identifier to be added to the [dstCityObject]
     */
    fun addUniqueIdentifier(id: RoadspaceObjectIdentifier, dstCityObject: AbstractCityObject) {
        dstCityObject.id = getGmlIdentifier(id).also { addUniqueIdentifierUsageRecord(it) }
        dstCityObject.names = listOf(Code(id.roadspaceObjectName))
    }

    /**
     * Adds the hashed id of the [LaneIdentifier] to the [dstCityObject], whereby the [id] can only be
     * assigned once (otherwise an [IllegalArgumentException] is thrown).
     *
     * @param id identifier to be added to the [dstCityObject]
     */
    fun addUniqueIdentifier(id: LaneIdentifier, dstCityObject: AbstractCityObject) {
        dstCityObject.id = getGmlIdentifier(id).also { addUniqueIdentifierUsageRecord(it) }
    }

    /** Adds a pseudo random hash id (hash based on the [id] and the [name]) to the [dstCityObject]. */
    fun addIdentifier(id: RoadspaceIdentifier, name: String, dstCityObject: AbstractCityObject) {
        val hashKey = name + '_' + id.hashKey
        dstCityObject.id = generateHashUUID(hashKey)
        dstCityObject.names = listOf(Code(name))
    }

    /** Adds a pseudo random hash id (hash based on the [id] and the [name]) to the [dstCityObject]. */
    fun addIdentifier(id: LaneIdentifier, name: String, dstCityObject: AbstractCityObject) {
        val hashKey = name + '_' + id.hashedId
        dstCityObject.id = generateHashUUID(hashKey)
        dstCityObject.names = listOf(Code(name))
    }

    /**
     * Adds the hashed id and the name of the [RoadspaceObjectIdentifier] to the [dstCityObject], whereby the [id] can
     * only be assigned once (otherwise an [IllegalArgumentException] is thrown).
     *
     * @param id identifier to be added to the [dstCityObject]
     */
    fun addIdentifier(id: RoadspaceObjectIdentifier, name: String, dstCityObject: AbstractCityObject) {
        val hashKey = name + '_' + id.hashedId
        dstCityObject.id = generateHashUUID(hashKey)
        dstCityObject.names = listOf(Code(name))
    }

    /** Returns a completely random id. */
    fun generateRandomUUID(): String = parameters.gmlIdPrefix + UUID.randomUUID().toString()

    /** Generates a unique UUID based on a hash of the [key] (even if the key has already been used). */
    private fun generateHashUUID(key: String): String {
        require(key.isNotBlank()) { "Key for generating a hashed UUID must not be blank." }
        val countIndex = usedKeyCount.getOrDefault(key, 0) + 1
        val keyWithCount = key + '_' + countIndex

        val uuid = UUID.nameUUIDFromBytes(keyWithCount.toByteArray()).toString()
        usedKeyCount[key] = countIndex
        return (parameters.gmlIdPrefix + uuid).also { addUniqueIdentifierUsageRecord(it) }
    }

    /** Adds the [id] to the usage record and throws an [IllegalArgumentException], when the [id] was already used. */
    private fun addUniqueIdentifierUsageRecord(id: String) {
        require(id.isNotBlank()) { "Identifier must not be blank." }
        require(!usedUniqueIdentifiers.containsKey(id)) { "Id must not have been used yet." }
        usedUniqueIdentifiers[id] = true
    }
}
