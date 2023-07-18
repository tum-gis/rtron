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

package io.rtron.transformer.converter.roadspaces2citygml.module

import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.model.roadspaces.common.LateralFillerSurface
import io.rtron.model.roadspaces.common.LongitudinalFillerSurface
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.Attribute
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.BooleanAttribute
import io.rtron.model.roadspaces.roadspace.attribute.DoubleAttribute
import io.rtron.model.roadspaces.roadspace.attribute.IntAttribute
import io.rtron.model.roadspaces.roadspace.attribute.MeasureAttribute
import io.rtron.model.roadspaces.roadspace.attribute.StringAttribute
import io.rtron.model.roadspaces.roadspace.attribute.UnitOfMeasure
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.RoadMarking
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import org.citygml4j.core.model.core.AbstractCityObject
import org.citygml4j.core.model.core.AbstractGenericAttribute
import org.citygml4j.core.model.core.AbstractGenericAttributeProperty
import org.citygml4j.core.model.core.CityObjectRelation
import org.xmlobjects.gml.model.basictypes.Measure
import org.citygml4j.core.model.generics.DoubleAttribute as GmlDoubleAttribute
import org.citygml4j.core.model.generics.GenericAttributeSet as GmlGenericAttributeSet
import org.citygml4j.core.model.generics.IntAttribute as GmlIntAttribute
import org.citygml4j.core.model.generics.MeasureAttribute as GmlMeasureAttribute
import org.citygml4j.core.model.generics.StringAttribute as GmlStringAttribute

/**
 * Adds [Attribute] and [AttributeList] classes (RoadSpaces model) to an [AbstractCityObject] (CityGML model).
 */
class AttributesAdder(
    private val parameters: Roadspaces2CitygmlParameters
) {

    // Methods

    /**
     * Adds the angle values of a [Rotation3D] in radians to the [dstCityObject].
     */
    fun addRotationAttributes(rotation: Rotation3D, dstCityObject: AbstractCityObject) {
        val attributeList = attributes("${parameters.geometryAttributesPrefix}rotation_") {
            attribute("z", rotation.heading)
            attribute("y", rotation.pitch)
            attribute("x", rotation.roll)
        }
        addAttributes(attributeList, dstCityObject)
    }

    fun addAttributes(lane: Lane, dstCityObject: AbstractCityObject) {
        val attributes = lane.id.toAttributes(parameters.identifierAttributesPrefix) + lane.attributes
        addAttributes(attributes, dstCityObject)
    }

    fun addAttributes(roadspaceObject: RoadspaceObject, dstCityObject: AbstractCityObject) {
        val attributes = roadspaceObject.id.toAttributes(parameters.identifierAttributesPrefix) + roadspaceObject.attributes
        addAttributes(attributes, dstCityObject)
    }

    fun addAttributes(longitudinalFillerSurface: LongitudinalFillerSurface, dstCityObject: AbstractCityObject) {
        val attributes = longitudinalFillerSurface.id.toAttributes(parameters.identifierAttributesPrefix)
        addAttributes(attributes, dstCityObject)
    }

    fun addAttributes(lateralFillerSurface: LateralFillerSurface, dstCityObject: AbstractCityObject) {
        val attributes = lateralFillerSurface.id.toAttributes(parameters.identifierAttributesPrefix)
        addAttributes(attributes, dstCityObject)
    }

    fun addAttributes(laneId: LaneIdentifier, roadMarking: RoadMarking, dstCityObject: AbstractCityObject) {
        val attributes = laneId.toAttributes(parameters.identifierAttributesPrefix) + roadMarking.attributes
        addAttributes(attributes, dstCityObject)
    }

    /**
     * Adds an [attributeList] to the [dstCityObject].
     */
    fun addAttributes(attributeList: AttributeList, dstCityObject: AbstractCityObject) {
        dstCityObject.genericAttributes = dstCityObject.genericAttributes + attributeList.attributes
            .flatMap { convertAttribute(it) }
            .map { AbstractGenericAttributeProperty(it) }
    }

    /**
     * Adds the attributes of an [id] to the [dstRelation].
     */
    fun addAttributes(id: AbstractRoadspacesIdentifier, dstRelation: CityObjectRelation) {
        dstRelation.genericAttributes = dstRelation.genericAttributes + id.toAttributes(parameters.identifierAttributesPrefix).attributes
            .flatMap { convertAttribute(it) }
            .map { AbstractGenericAttributeProperty(it) }
    }

    /**
     * Converts a list of attributes from the RoadSpaces model to CityGML representation.
     * If flattenGenericAttributeSets is true, the lists are flattened out to a non-nested list.
     *
     * @param attribute attribute to be converted a
     * @return list of CityGML attributes
     */
    private fun convertAttribute(attribute: Attribute): List<AbstractGenericAttribute<*>> =
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

                if (parameters.flattenGenericAttributeSets) {
                    attributes
                } else {
                    listOf(GmlGenericAttributeSet(attribute.name, attributes.map { AbstractGenericAttributeProperty(it) }))
                }
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
