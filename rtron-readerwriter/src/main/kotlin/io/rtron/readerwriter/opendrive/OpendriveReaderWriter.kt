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

package io.rtron.readerwriter.opendrive

import com.github.kittinunf.result.Result
import io.rtron.io.files.Path
import io.rtron.model.AbstractModel
import io.rtron.readerwriter.AbstractReaderWriter
import io.rtron.readerwriter.opendrive.parameter.OpendriveReaderWriterConfiguration
import io.rtron.std.handleFailure
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory

class OpendriveReaderWriter(
    val configuration: OpendriveReaderWriterConfiguration
) : AbstractReaderWriter() {

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    private val _opendrive14Reader by lazy { Opendrive14Reader(configuration) }
    private val _opendrive15Reader by lazy { Opendrive15Reader(configuration) }

    // Methods
    override fun isSupported(fileExtension: String) = fileExtension in supportedFileExtensions
    override fun isSupported(model: AbstractModel): Boolean = false

    override fun read(filePath: Path): Result<AbstractModel, Exception> {
        val opendriveVersion = getOpendriveVersion(filePath).handleFailure { return it }

        val model = when (opendriveVersion) {
            OpendriveVersion(1, 4) -> _opendrive14Reader.createOpendriveModel(filePath)
            OpendriveVersion(1, 5) -> _opendrive14Reader.createOpendriveModel(filePath)
            else -> {
                _reportLogger.warn("Detected OpenDRIVE version ($opendriveVersion) for which no dedicated reader is available. Experimentally continuing.")
                _opendrive14Reader.createOpendriveModel(filePath)
            }
        }

        return Result.success(model)
    }

    override fun write(model: AbstractModel, directoryPath: Path): Result<List<Path>, Exception> {
        return Result.error(UnsupportedOperationException("Not implemented"))
    }

    data class OpendriveVersion(val revMajor: Int = 0, val revMinor: Int = 0)
    private fun getOpendriveVersion(file: Path): Result<OpendriveVersion, Exception> {

        val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.toFileJ())
        val header = xmlDoc.getElementsByTagName("header").item(0)

        val revMajor = header.attributes.getNamedItem("revMajor") ?: return Result.error(IllegalStateException("Major version of OpenDRIVE dataset is not identifiable."))
        val revMinor = header.attributes.getNamedItem("revMinor") ?: return Result.error(IllegalStateException("Minor version of OpenDRIVE dataset is not identifiable."))

        val opendriveVersion = OpendriveVersion(
            revMajor = revMajor.nodeValue.toInt(),
            revMinor = revMinor.nodeValue.toInt()
        )
        return Result.success(opendriveVersion)
    }

    companion object {
        val supportedFileExtensions = listOf("xodr", "xodrz")
    }
}
