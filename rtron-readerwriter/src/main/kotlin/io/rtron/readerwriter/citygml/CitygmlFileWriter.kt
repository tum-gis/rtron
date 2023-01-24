/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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

import io.rtron.io.files.getFileSizeToDisplay
import io.rtron.io.files.outputStreamDirectOrCompressed
import io.rtron.model.citygml.CitygmlModel
import mu.KotlinLogging
import org.citygml4j.xml.CityGMLContext
import org.citygml4j.xml.module.citygml.CoreModule
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

class CitygmlFileWriter(
    val parameters: CitygmlWriterParameters
) {

    // Properties and Initializers
    private val logger = KotlinLogging.logger {}

    private val citygmlContext = CityGMLContext.newInstance()

    // Methods

    fun writeModel(model: CitygmlModel, directoryPath: Path, fileNameWithoutExtension: String): List<Path> {
        return parameters.versions.map { write(model, it, directoryPath, fileNameWithoutExtension) }
    }

    private fun write(model: CitygmlModel, version: CitygmlVersion, directoryPath: Path, fileNameWithoutExtension: String): Path {
        val citygmlVersion = version.toGmlCitygml()
        val out = citygmlContext.createCityGMLOutputFactory(citygmlVersion)!!

        val fileName = fileNameWithoutExtension + ".gml" + parameters.fileCompression.fold({ "" }, { it.extensionWithDot })
        val filePath = directoryPath / Path(fileName)
        val outputStream = filePath.outputStreamDirectOrCompressed()

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
        outputStream.close()
        logger.info("Completed writing of file $fileName (around ${filePath.getFileSizeToDisplay()}).")
        return filePath
    }

    companion object {
        val supportedFilenameEndings: Set<String> = setOf("gml")
    }
}
