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

import arrow.core.Option

/**
 * Environment for describing and building attribute lists.
 */
class AttributeListBuilder(
    private var namePrefix: String = ""
) {

    private val attributes = mutableListOf<Attribute>()

    operator fun Attribute.unaryPlus() {
        attributes += this
    }

    fun attribute(name: String, value: String) {
        StringAttribute.of(namePrefix + name, value).tap { attributes += it }
    }

    fun attribute(name: String, value: Int) {
        attributes += IntAttribute(namePrefix + name, value)
    }

    fun attribute(name: String, value: Double) {
        DoubleAttribute.of(namePrefix + name, value).tap { attributes += it }
    }

    fun attribute(name: String, value: Boolean) {
        attributes += BooleanAttribute(namePrefix + name, value)
    }

    fun attribute(name: String, value: Double, unitOfMeasure: UnitOfMeasure) {
        MeasureAttribute.of(namePrefix + name, value, unitOfMeasure).tap { attributes += it }
    }

    @JvmName("OptionalDoubleAttribute")
    fun attribute(name: String, optionalValue: Option<Double>) {
        optionalValue.tap {
            attributes += DoubleAttribute(namePrefix + name, it)
        }
    }

    @JvmName("OptionalStringAttribute")
    fun attribute(name: String, optionalValue: Option<String>) {
        optionalValue.tap {
            if (it.isNotBlank())
                attributes += StringAttribute(namePrefix + name, it)
        }
    }

    /**
     * Environment for building a nested attribute list within this attribute list.
     */
    fun attributes(name: String = "", setup: AttributeListBuilder.() -> Unit) {
        val attributeListBuilder = AttributeListBuilder(namePrefix + name)
        attributeListBuilder.setup()
        attributes += attributeListBuilder.toAttributeList(namePrefix + name)
    }

    fun build() = AttributeList(attributes)
    private fun toAttributeList(name: String) = AttributeList(attributes, name)
}

/**
 * Environment for building up an [AttributeList] with an attribute's [namePrefix].
 *
 * @param namePrefix each attribute's name is prefixed with this variable
 * @param setup DSL environment for describing attribute lists
 */
fun attributes(namePrefix: String = "", setup: AttributeListBuilder.() -> Unit): AttributeList {
    val attributeListBuilder = AttributeListBuilder(namePrefix)
    attributeListBuilder.setup()
    return attributeListBuilder.build()
}
