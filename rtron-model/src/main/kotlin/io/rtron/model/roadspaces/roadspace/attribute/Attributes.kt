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

package io.rtron.model.roadspaces.roadspace.attribute

/**
 * Abstract class of an attribute.
 */
sealed class Attribute(val name: String) {
    abstract fun isEmpty(): Boolean
    fun isNotEmpty() = !isEmpty()
}

/**
 * Attribute with a [name] containing a certain string [value].
 */
class StringAttribute(name: String, val value: String) : Attribute(name) {
    override fun isEmpty() = value.isEmpty()
}

/**
 * Attribute with a [name] containing a certain integer [value].
 */
class IntAttribute(name: String, val value: Int) : Attribute(name) {
    override fun isEmpty() = value == Int.MIN_VALUE
}

/**
 * Attribute with a [name] containing a certain double [value].
 */
class DoubleAttribute(name: String, val value: Double) : Attribute(name) {
    override fun isEmpty() = value.isNaN()
}

/**
 * Attribute with a [name] containing a certain boolean [value].
 */
class BooleanAttribute(name: String, val value: Boolean) : Attribute(name) {
    override fun isEmpty() = false
}

/**
 * Attribute with a [name] containing a certain double [value] with a [UnitOfMeasure].
 *
 * @param uom unit of measure of the value
 */
class MeasureAttribute(name: String, val value: Double, val uom: UnitOfMeasure) : Attribute(name) {
    override fun isEmpty() = value.isNaN()
}

/**
 * List of attributes.
 *
 * @param name name of the list
 */
class AttributeList(val attributes: List<Attribute> = listOf(), name: String = "") : Attribute(name) {

    // Operators
    operator fun plus(other: AttributeList): AttributeList {
        require(this.name == other.name) { "Merging of attribute lists requires the same name." }
        return AttributeList(attributes + other.attributes, name)
    }

    // Methods
    override fun isEmpty() = attributes.all { it.isEmpty() }

    companion object {
        val EMPTY = AttributeList(emptyList())

        fun of(vararg attributes: Attribute) = AttributeList(attributes.toList())
    }
}
