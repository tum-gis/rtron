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

package io.rtron.readerwriter

import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import io.rtron.model.AbstractModel
import io.rtron.readerwriter.citygml.CitygmlReaderWriter
import io.rtron.readerwriter.citygml.CitygmlReaderWriterConfiguration
import io.rtron.readerwriter.opendrive.OpendriveReaderWriter
import io.rtron.readerwriter.opendrive.OpendriveReaderWriterConfiguration
import io.rtron.std.handleFailure

class ReadWriteManager(projectId: String = "", readerWriterConfigurations: List<AbstractReaderWriterConfiguration>) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(projectId)

    private val registeredReaderWriter = readerWriterConfigurations.map(::getConcreteReaderWriter)

    // Methods
    fun isSupported(model: AbstractModel) = registeredReaderWriter.any { it.isSupported(model) }

    fun read(filePath: Path): AbstractModel {
        val readers = registeredReaderWriter.filter { it.isSupported(filePath.extension) }
        require(readers.isNotEmpty()) { "No adequate reader found." }
        require(readers.size <= 1) { "Multiple adequate readers found." }

        return readers.first().read(filePath)
            .also { _reportLogger.info("Completed read-in of file ${filePath.fileName} (around ${filePath.getFileSizeToDisplay()}). ✔") }
    }

    fun write(model: AbstractModel, directoryPath: Path) {
        val writers = registeredReaderWriter.filter { it.isSupported(model) }
        require(writers.isNotEmpty()) { "No adequate writer found." }
        require(writers.size <= 1) { "Multiple adequate writers found." }

        writers.first().write(model, directoryPath)
            .handleFailure { throw it.error }
            .forEach { _reportLogger.info("Completed writing of file ${it.fileName} (around ${it.getFileSizeToDisplay()}). ✔") }
    }

    private fun getConcreteReaderWriter(configuration: AbstractReaderWriterConfiguration): AbstractReaderWriter =
        when (configuration) {
            is CitygmlReaderWriterConfiguration -> CitygmlReaderWriter(configuration)
            is OpendriveReaderWriterConfiguration -> OpendriveReaderWriter(configuration)
            // register new ReaderWriter classes here
            else -> throw IllegalArgumentException("Unknown ReaderWriterConfiguration. Registration is required if it is a new one.")
        }

    companion object {

        val supportedFileExtensions: List<String> =
            CitygmlReaderWriter.supportedFileExtensions +
                OpendriveReaderWriter.supportedFileExtensions
        // register new ReaderWriter classes here

        init {
            val multiplyHandledExtensions = supportedFileExtensions.groupingBy { it }.eachCount().filter { it.value > 1 }
            require(multiplyHandledExtensions.isEmpty()) { "A file extensions ${multiplyHandledExtensions.keys} must have a unique ReaderWriter it is handled by." }
        }

        fun of(projectId: String, vararg configuration: AbstractReaderWriterConfiguration) =
            ReadWriteManager(projectId, configuration.toList())
    }
}
