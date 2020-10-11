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

import io.rtron.model.roadspaces.roadspace.attribute.*
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlParameters
import org.citygml4j.model.citygml.core.AbstractCityObject
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute
import org.citygml4j.model.gml.basicTypes.Measure
import org.citygml4j.model.citygml.generics.DoubleAttribute as GmlDoubleAttribute
import org.citygml4j.model.citygml.generics.GenericAttributeSet as GmlGenericAttributeSet
import org.citygml4j.model.citygml.generics.IntAttribute as GmlIntAttribute
import org.citygml4j.model.citygml.generics.MeasureAttribute as GmlMeasureAttribute
import org.citygml4j.model.citygml.generics.StringAttribute as GmlStringAttribute


/**
 * Adds [Attribute] and [AttributeList] classes (RoadSpaces model) to an [AbstractCityObject] (CityGML model).
 */
class AttributesAdder(
        private val parameters: Roadspaces2CitygmlParameters
) {

    // Methods

    /**
     * Adds an [attributeList] to the [dstCityObject].
     */
    fun addAttributes(attributeList: AttributeList, dstCityObject: AbstractCityObject) {
        attributeList.attributes
                .filter { it.isNotEmpty() }
                .flatMap { convertAttribute(it) }
                .forEach { dstCityObject.addGenericAttribute(it) }
    }

    /**
     * Converts a list of attributes from the RoadSpaces model to CityGML representation.
     * If flattenGenericAttributeSets is true, the lists are flattened out to a non-nested list.
     *
     * @param attribute attribute to be converted a
     * @return list of CityGML attributes
     */
    private fun convertAttribute(attribute: Attribute): List<AbstractGenericAttribute> =
            when (attribute) {
                is StringAttribute -> listOf(GmlStringAttribute(attribute.name, attribute.value))
                is IntAttribute -> listOf(GmlIntAttribute(attribute.name, attribute.value))
                is DoubleAttribute -> listOf(GmlDoubleAttribute(attribute.name, attribute.value))
                is BooleanAttribute -> listOf(GmlStringAttribute(attribute.name, attribute.value.toString()))
                is MeasureAttribute -> {
                    val measure = Measure(attribute.value).apply { uom = attribute.uom.toGmlString() }
                    val measureAttribute = GmlMeasureAttribute(attribute.name, measure)
                    val doubleAttribute = GmlDoubleAttribute(attribute.name, attribute.value)
                    listOf(measureAttribute, doubleAttribute)
                }
                is AttributeList -> {
                    val attributes = attribute.attributes.flatMap { convertAttribute(it) }

                    if (parameters.flattenGenericAttributeSets) attributes
                    else listOf(GmlGenericAttributeSet(attribute.name, attributes))
                }
            }
}

/**
 * Returns a unit of measurement as string.
 */
fun UnitOfMeasure.toGmlString(): String = when (this) {
    UnitOfMeasure.METER -> "#m"
    UnitOfMeasure.KILOMETER -> "km"
    UnitOfMeasure.METER_PER_SECOND -> "mps"
    UnitOfMeasure.KILOMETER_PER_HOUR -> "kmph"
    UnitOfMeasure.MILES_PER_HOUR -> "mph"
    else -> TODO("Conversion of $this is not yet implemented.")
}
