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

package io.rtron.model.roadspaces.roadspace.attribute

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.some

/**
 * Abstract class of an attribute.
 */
sealed class Attribute(val name: String)

/**
 * Attribute with a [name] containing a certain string [value].
 */
class StringAttribute(name: String, val value: String) : Attribute(name) {

    // Properties and Initializers
    init {
        require(value.isNotBlank()) { "String value must not be blank." }
    }

    companion object {
        fun of(name: String, value: String): Option<StringAttribute> =
            if (value.isEmpty()) None
            else StringAttribute(name, value).some()
    }
}

/**
 * Attribute with a [name] containing a certain integer [value].
 */
class IntAttribute(name: String, val value: Int) : Attribute(name)

/**
 * Attribute with a [name] containing a certain double [value].
 */
class DoubleAttribute(name: String, val value: Double) : Attribute(name) {

    // Properties and Initializers
    init {
        require(value.isFinite()) { "Value must be finite." }
    }

    companion object {
        fun of(name: String, value: Double): Option<DoubleAttribute> =
            if (!value.isFinite()) None
            else DoubleAttribute(name, value).some()
    }
}

/**
 * Attribute with a [name] containing a certain boolean [value].
 */
class BooleanAttribute(name: String, val value: Boolean) : Attribute(name)

/**
 * Attribute with a [name] containing a certain double [value] with a [UnitOfMeasure].
 *
 * @param uom unit of measure of the value
 */
class MeasureAttribute(name: String, val value: Double, val uom: UnitOfMeasure) : Attribute(name) {

    // Properties and Initializers
    init {
        require(value.isFinite()) { "Value must be finite." }
    }

    companion object {
        fun of(name: String, value: Double, uom: UnitOfMeasure): Option<MeasureAttribute> =
            if (value.isNaN()) None
            else Some(MeasureAttribute(name, value, uom))
    }
}

/**
 * List of attributes.
 *
 * @param name name of the list
 */
class AttributeList(val attributes: List<Attribute> = emptyList(), name: String = "") : Attribute(name) {

    // Operators
    operator fun plus(other: AttributeList): AttributeList {
        require(this.name == other.name) { "Merging of attribute lists requires the same name." }
        return AttributeList(attributes + other.attributes, name)
    }

    // Methods
    fun isEmpty() = attributes.isEmpty()

    companion object {
        val EMPTY = AttributeList(emptyList())

        fun of(vararg attributes: Attribute) = AttributeList(attributes.toList())
    }
}
