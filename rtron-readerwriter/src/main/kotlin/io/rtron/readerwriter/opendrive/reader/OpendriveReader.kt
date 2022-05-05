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
import arrow.core.NonEmptyList
import arrow.core.computations.either
import arrow.core.left
import arrow.core.nonEmptyListOf
import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.opendrive.OpendriveVersion.Companion.ofRevision
import io.rtron.readerwriter.opendrive.configuration.OpendriveReaderConfiguration
import io.rtron.readerwriter.opendrive.reader.OpendriveUnmarshaller
import io.rtron.std.BaseException
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory

class OpendriveReader(
    val configuration: OpendriveReaderConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    // Methods
    fun read(filePath: Path): Either<OpendriveReaderException, OpendriveModel> = either.eager {
        if (!filePath.exists())
            OpendriveReaderException.FileNotFound(filePath).left().bind<OpendriveReaderException>()

        // schema validation
        val opendriveVersion = getOpendriveVersion(filePath).bind()
        val versionSpecificUnmarshaller = OpendriveUnmarshaller.of(opendriveVersion).bind()
        val schemaValidationReport = versionSpecificUnmarshaller.validate(filePath).bind()

        configuration.outputSchemaValidationReportDirectoryPath.tap {
            val schemaValidationReportFilePath = it.resolve(Path("reports/reader/opendrive/schemaValidationReport.json"))
            schemaValidationReport.write(schemaValidationReportFilePath)
        }
        if (!schemaValidationReport.isEmpty()) _reportLogger.warn("Schema validation for OpenDRIVE $opendriveVersion found ${schemaValidationReport.getTextSummary()}.")
        else _reportLogger.info("Schema validation report for OpenDRIVE $opendriveVersion: Everything ok.")

        // read model
        val opendriveModel: OpendriveModel = versionSpecificUnmarshaller.readFromFile(filePath).fold({
            val fallbackUnmarshaller = OpendriveUnmarshaller.FALLBACK
            _reportLogger.warn("No dedicated reader available for OpenDRIVE $opendriveVersion. Using reader for OpenDRIVE ${fallbackUnmarshaller.opendriveVersion} as fallback.")
            val model = fallbackUnmarshaller.readFromFile(filePath).bind()
            model
        }, { it })

        _reportLogger.info("Completed read-in of file ${filePath.fileName} (around ${filePath.getFileSizeToDisplay()}). ✔")
        opendriveModel
    }

    private fun getOpendriveVersion(file: Path): Either<OpendriveReaderException, OpendriveVersion> = either.eager {

        val xmlDoc: Document = Either.catch { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.toFileJ()) }
            .mapLeft { OpendriveReaderException.MalformedXmlDocument(it.message ?: "") }
            .bind()

        val header = xmlDoc.getElementsByTagName("header")
        if (header.length <= 0)
            OpendriveReaderException.HeaderElementNotFound("No header element available").left().bind()
        if (header.length > 1)
            OpendriveReaderException.HeaderElementNotFound("Multiple header elements available").left().bind()

        val revMajor = Either.catch { header.item(0).attributes.getNamedItem("revMajor").nodeValue.toInt() }
            .mapLeft { OpendriveReaderException.VersionNotIdentifiable("Major version is not identifiable") }
            .bind()
        val revMinor = Either.catch { header.item(0).attributes.getNamedItem("revMinor").nodeValue.toInt() }
            .mapLeft { OpendriveReaderException.VersionNotIdentifiable("Minor version is not identifiable") }
            .bind()

        ofRevision(revMajor, revMinor).mapLeft { OpendriveReaderException.VersionNotIdentifiable(it.message) }.bind()
    }

    companion object {
        val supportedFileExtensions: NonEmptyList<String> = nonEmptyListOf("xodr", "xodrz")
    }
}

sealed class OpendriveReaderException(message: String) : BaseException(message) {
    data class FileNotFound(val path: Path) : OpendriveReaderException("File not found at $path")
    data class MalformedXmlDocument(val reason: String) : OpendriveReaderException("OpenDRIVE file cannot be parsed: $reason")
    data class HeaderElementNotFound(val reason: String) : OpendriveReaderException("Header element of OpenDRIVE dataset not found: $reason")
    data class VersionNotIdentifiable(val reason: String) : OpendriveReaderException("Version of OpenDRIVE dataset not deducible: $reason")

    data class NoDedicatedSchemaAvailable(val version: OpendriveVersion) : OpendriveReaderException("No dedicated schema available for $version")
    data class FatalSchemaValidationError(val reason: String) : OpendriveReaderException("Fatal error during schema validation: $reason")
    data class NoDedicatedReaderAvailable(val version: OpendriveVersion) : OpendriveReaderException("No dedicated reader available for $version")
}
