/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.readerwriter.citygml

import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.io.files.getFileSizeToDisplay
import io.rtron.io.files.outputStreamDirectOrCompressed
import io.rtron.model.citygml.CitygmlModel
import org.citygml4j.xml.CityGMLContext
import org.citygml4j.xml.module.citygml.CoreModule
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path

object CitygmlWriter {
    // Properties and Initializers
    private val logger = KotlinLogging.logger {}

    private val citygmlContext = CityGMLContext.newInstance()

    // Methods

    fun writeToFile(
        model: CitygmlModel,
        version: CitygmlVersion,
        filePath: Path,
    ) {
        val outputStream = filePath.outputStreamDirectOrCompressed()
        writeToStream(model, version, outputStream)
        outputStream.close()

        logger.info { "Completed writing of file ${filePath.fileName} (around ${filePath.getFileSizeToDisplay()})." }
    }

    fun writeToStream(
        model: CitygmlModel,
        version: CitygmlVersion,
        outputStream: OutputStream,
    ) {
        val citygmlVersion = version.toGmlCitygml()
        val out = citygmlContext.createCityGMLOutputFactory(citygmlVersion)!!

        val writer = out.createCityGMLChunkWriter(outputStream, StandardCharsets.UTF_8.name())
        writer.apply {
            withIndent("  ")
            withDefaultSchemaLocations()
            withDefaultPrefixes()
            withDefaultNamespace(CoreModule.of(citygmlVersion).namespaceURI)
            cityModelInfo.boundedBy = model.boundingShape
        }
        model.cityObjects.forEach {
            writer.writeMember(it)
        }

        writer.close()
    }
}
