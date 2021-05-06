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

package io.rtron.readerwriter.citygml.parameter

import io.rtron.readerwriter.AbstractReaderWriterParameters
import io.rtron.readerwriter.ReaderWriterConfiguration
import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.std.Property

typealias CitygmlReaderWriterConfiguration = ReaderWriterConfiguration<CitygmlReaderWriterParameters>

class CitygmlReaderWriterParameters(
    private val writeVersionsProperty: Property<Set<CitygmlVersion>> = Property(setOf(CitygmlVersion.V2_0), true),
) : AbstractReaderWriterParameters() {

    // Properties and Initializers
    init {
        require(writeVersionsProperty.value.isNotEmpty()) { "At least one CitGML version must be set." }
    }

    /**
     * citygml version for writing dateset
     */
    val writeVersions: Set<CitygmlVersion> by writeVersionsProperty

    /**
     * Merges the [other] parameters into this. See [Property.leftMerge] for the prioritization rules.
     */
    infix fun leftMerge(other: CitygmlReaderWriterParameters) = CitygmlReaderWriterParameters(
        this.writeVersionsProperty leftMerge other.writeVersionsProperty
    )

    override fun toString() = "CitygmlReaderWriterParameters(writeVersions=$writeVersions)"
}