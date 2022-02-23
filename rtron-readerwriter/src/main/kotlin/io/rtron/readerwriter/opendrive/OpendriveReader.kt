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

package io.rtron.readerwriter.opendrive

import arrow.core.Either
import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.opendrive.configuration.OpendriveReaderConfiguration
import io.rtron.std.handleFailure
import io.rtron.std.toResult
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory

class OpendriveReader(
    val configuration: OpendriveReaderConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    private val _opendrive14Reader by lazy { Opendrive14Reader(configuration) }
    private val _opendrive15Reader by lazy { Opendrive15Reader(configuration) }

    // Methods

    fun read(filePath: Path): OpendriveModel {

        val opendriveVersion = getOpendriveVersion(filePath).toResult().handleFailure { throw it.error }

        val model = when (opendriveVersion) {
            OpendriveVersion(1, 4) -> _opendrive14Reader.createOpendriveModel(filePath)
            else -> {
                _reportLogger.warn("Detected OpenDRIVE version ($opendriveVersion) for which no dedicated reader is implemented (yet). Experimentally continuing.")
                _opendrive14Reader.createOpendriveModel(filePath)
            }
        }
        _reportLogger.info("Completed read-in of file ${filePath.fileName} (around ${filePath.getFileSizeToDisplay()}). ✔")
        return model
    }

    private data class OpendriveVersion(val revMajor: Int = 0, val revMinor: Int = 0)
    private fun getOpendriveVersion(file: Path): Either<IllegalStateException, OpendriveVersion> {

        val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.toFileJ())
        val header = xmlDoc.getElementsByTagName("header").item(0)

        val revMajor = header.attributes.getNamedItem("revMajor") ?: return Either.Left(IllegalStateException("Major version of OpenDRIVE dataset is not identifiable."))
        val revMinor = header.attributes.getNamedItem("revMinor") ?: return Either.Left(IllegalStateException("Minor version of OpenDRIVE dataset is not identifiable."))

        val opendriveVersion = OpendriveVersion(
            revMajor = revMajor.nodeValue.toInt(),
            revMinor = revMinor.nodeValue.toInt()
        )
        return Either.Right(opendriveVersion)
    }

    companion object {
        val supportedFileExtensions = listOf("xodr", "xodrz")
    }
}
