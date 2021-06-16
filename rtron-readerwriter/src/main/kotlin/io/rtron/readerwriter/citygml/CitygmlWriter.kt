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

package io.rtron.readerwriter.citygml

import com.github.kittinunf.result.Result
import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import io.rtron.model.AbstractModel
import io.rtron.model.citygml.CitygmlModel
import io.rtron.readerwriter.citygml.configuration.CitygmlWriterConfiguration
import io.rtron.std.handleAndRemoveFailure
import org.citygml4j.CityGMLContext
import org.citygml4j.xml.module.citygml.CoreModule
import java.nio.charset.StandardCharsets

class CitygmlWriter(
    val configuration: CitygmlWriterConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    private val _citygmlContext = CityGMLContext.newInstance()

    // Methods

    fun write(model: AbstractModel, directoryPath: Path): Result<List<Path>, Exception> {
        require(model is CitygmlModel) { "$this received not a CitygmlModel." }

        val versionSuffix = configuration.versions.size > 1
        val filePaths = configuration.versions.map { write(model, it, directoryPath, versionSuffix) }
            .handleAndRemoveFailure { _reportLogger.log(it) }

        return Result.success(filePaths)
    }

    private fun write(model: CitygmlModel, version: CitygmlVersion, directoryPath: Path, versionSuffix: Boolean): Result<Path, Exception> {
        val citygmlVersion = version.toGmlCitygml()
        val out = _citygmlContext.createCityGMLOutputFactory(citygmlVersion)!!

        val fileName = directoryPath.fileName.toString() + (if (versionSuffix) "_$version" else "") + ".gml"
        val filePath = directoryPath.resolve(Path(fileName))

        val writer = out.createCityGMLChunkWriter(filePath.toFileJ(), StandardCharsets.UTF_8.name())
        writer.apply {
            withIndentString("  ")
            withDefaultSchemaLocations()
            withDefaultPrefixes()
            withDefaultNamespace(CoreModule.of(citygmlVersion).namespaceURI)
            cityModelInfo.boundedBy = model.boundingShape
        }
        model.cityObjects.forEach {
            writer.writeMember(it)
        }

        writer.close()
        _reportLogger.info("Completed writing of file $fileName (around ${filePath.getFileSizeToDisplay()}). ✔")
        return Result.success(filePath)
    }

    companion object {
        val supportedFileExtensions = listOf("gml")
    }
}