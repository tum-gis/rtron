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

package io.rtron.transformer.opendrive2roadspaces.parameter

import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.transformer.AbstractTransformerParameters
import io.rtron.transformer.TransformerConfiguration
import io.rtron.std.Property


typealias Opendrive2RoadspacesConfiguration = TransformerConfiguration<Opendrive2RoadspacesParameters>


/**
 * Transformation parameters for the OpenDRIVE to RoadSpace transformer.
 */
class Opendrive2RoadspacesParameters(
        private val toleranceProperty: Property<Double> = Property(1E-7, true),
        private val attributesPrefixProperty: Property<String> = Property("opendrive_", true),
        private val crsEpsgProperty: Property<Int> = Property(0, true),
        private val offsetXProperty: Property<Double> = Property(0.0, true),
        private val offsetYProperty: Property<Double> = Property(0.0, true),
        private val offsetZProperty: Property<Double> = Property(0.0, true)
) : AbstractTransformerParameters() {

    // Properties and Initializers

    /**
     * allowed tolerance when comparing double values
     */
    val tolerance by toleranceProperty

    /**
     * prefix of attribute names
     */
    val attributesPrefix by attributesPrefixProperty

    /**
     * [EPSG code](https://en.wikipedia.org/wiki/EPSG_Geodetic_Parameter_Dataset) of the coordinate reference system
     */
    val crsEpsg by crsEpsgProperty

    /**
     * offset by which the model is translated along the x axis
     */
    val offsetX by offsetXProperty

    /**
     * offset by which the model is translated along the y axis
     */
    val offsetY by offsetYProperty

    /**
     * offset by which the model is translated along the z axis
     */
    val offsetZ by offsetZProperty


    /**
     * offset in the xy plane as vector
     */
    val offsetXY = Vector2D(offsetX, offsetY)


    // Methods

    /**
     * Merges the [other] parameters into this. See [Property.leftMerge] for the prioritization rules.
     */
    infix fun leftMerge(other: Opendrive2RoadspacesParameters) = Opendrive2RoadspacesParameters(
            this.toleranceProperty leftMerge other.toleranceProperty,
            this.attributesPrefixProperty leftMerge other.attributesPrefixProperty,
            this.crsEpsgProperty leftMerge other.crsEpsgProperty,
            this.offsetXProperty leftMerge other.offsetXProperty,
            this.offsetYProperty leftMerge other.offsetYProperty,
            this.offsetZProperty leftMerge other.offsetZProperty)

    override fun toString() =
            "Opendrive2RoadspacesParameters(tolerance=$tolerance, attributesPrefix=$attributesPrefix," +
                    " crsEpsg=$crsEpsg, offsetX=$offsetX, offsetY=$offsetY, offsetZ=$offsetZ)"
}
