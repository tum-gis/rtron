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

import io.rtron.std.SettableProperty

/**
 * DSL Environment for building up [CitygmlReaderWriterParameters].
 */
class CitygmlReaderWriterParametersBuilder {

    private val defaultParameters = CitygmlReaderWriterParameters()

    private val writeVersionsProperty = SettableProperty(defaultParameters.writeVersions)
    var writeVersions by writeVersionsProperty

    fun build() = CitygmlReaderWriterParameters(
        writeVersionsProperty
    )
}

/**
 * Environment for building up [CitygmlReaderWriterParameters].
 *
 * @param setup DSL environment for defining the parameters
 */
fun citygmlReaderWriterParameters(setup: CitygmlReaderWriterParametersBuilder.() -> Unit): CitygmlReaderWriterParameters {
    val builder = CitygmlReaderWriterParametersBuilder()
    return builder.apply(setup).build()
}
