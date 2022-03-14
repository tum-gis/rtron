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
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import io.rtron.io.files.File
import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.opendrive.OpendriveVersion.Companion.ofRevision
import io.rtron.readerwriter.opendrive.configuration.OpendriveReaderConfiguration
import io.rtron.readerwriter.opendrive.validation.OpendriveSchemaValidationReportEntry
import io.rtron.std.BaseException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory

class OpendriveReader(
    val configuration: OpendriveReaderConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)
    private val _jsonFormatter = Json { prettyPrint = true }

    private val _opendrive14Reader by lazy { Opendrive14Reader(configuration) }
    private val _opendrive15Reader by lazy { Opendrive15Reader(configuration) }
    private val _opendrive17Reader by lazy { Opendrive17Reader(configuration) }

    // Methods
    fun read(filePath: Path): Either<OpendriveReaderException, OpendriveModel> =
        either.eager {
            if (!filePath.exists()) OpendriveReaderException.FileNotFound(filePath).left().bind()

            val opendriveVersion = getOpendriveVersion(filePath).bind()
            val (opendriveVersionSpecificReader, isFallbackReader) = getOpendriveVersionSpecificReader(opendriveVersion)
                .map { Pair(it, false) }
                .getOrElse { Pair(_opendrive14Reader, true) }

            val (model, schemaValidationReport) = opendriveVersionSpecificReader.createOpendriveModel(filePath).bind()

            if (!isFallbackReader && !schemaValidationReport.isEmpty()) {
                val numberOfWarnings = schemaValidationReport.getNumberOfEntries(severity = OpendriveSchemaValidationReportEntry.Severity.WARNING)
                val numberOfErrors = schemaValidationReport.getNumberOfEntries(severity = OpendriveSchemaValidationReportEntry.Severity.ERROR)
                val numberOfFatalErrors = schemaValidationReport.getNumberOfEntries(severity = OpendriveSchemaValidationReportEntry.Severity.FATAL_ERROR)
                _reportLogger.warn("Schema validation for OpenDRIVE $opendriveVersion found $numberOfWarnings warnings, $numberOfErrors errors and $numberOfFatalErrors fatal errors.")

                configuration.outputSchemaValidationReportDirectoryPath.tap {
                    val schemaValidationReportFilePath = it.resolve(Path("schemaValidationReport.json"))
                    val jsonFileContent = _jsonFormatter.encodeToString(schemaValidationReport)
                    File(schemaValidationReportFilePath).writeText(jsonFileContent)
                    _reportLogger.info("Schema validation report written for OpenDRIVE version  ")
                }
            } else {
                _reportLogger.info("Schema validation report for OpenDRIVE $opendriveVersion: Everything ok.")
            }

            _reportLogger.info("Completed read-in of file ${filePath.fileName} (around ${filePath.getFileSizeToDisplay()}). ✔")
            model
        }

    private fun getOpendriveVersion(file: Path): Either<OpendriveReaderException, OpendriveVersion> =
        either.eager {

            val xmlDoc: Document = Either.catch { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.toFileJ()) }
                .mapLeft { OpendriveReaderException.MalformedXmlDocument(it.message ?: "") }
                .bind()

            val header = xmlDoc.getElementsByTagName("header")
            if (header.length <= 0)
                OpendriveReaderException.HeaderNotFound("No header element available").left().bind()
            if (header.length > 1)
                OpendriveReaderException.HeaderNotFound("Multiple header elements available").left().bind()

            val revMajor = Either.catch { header.item(0).attributes.getNamedItem("revMajor").nodeValue.toInt() }
                .mapLeft { OpendriveReaderException.VersionNotDeducible("Major version is not identifiable") }
                .bind()
            val revMinor = Either.catch { header.item(0).attributes.getNamedItem("revMinor").nodeValue.toInt() }
                .mapLeft { OpendriveReaderException.VersionNotDeducible("Major version is not identifiable") }
                .bind()

            ofRevision(revMajor, revMinor).mapLeft { OpendriveReaderException.VersionNotDeducible(it.message) }.bind()
        }

    private fun getOpendriveVersionSpecificReader(opendriveVersion: OpendriveVersion): Either<OpendriveReaderException.NoDedicatedReaderAvailable, AbstractOpendriveVersionSpecificReader> =
        when (opendriveVersion) {
            OpendriveVersion.V_1_4 -> _opendrive14Reader.right()
            OpendriveVersion.V_1_7 -> _opendrive17Reader.right()
            else -> OpendriveReaderException.NoDedicatedReaderAvailable("Detected OpenDRIVE version ($opendriveVersion) for which no dedicated reader is implemented (yet).").left()
        }

    companion object {
        val supportedFileExtensions: NonEmptyList<String> = nonEmptyListOf("xodr", "xodrz")
    }
}

sealed class OpendriveReaderException(message: String) : BaseException(message) {
    data class FileNotFound(val path: Path) : OpendriveReaderException("File not found at $path")
    data class MalformedXmlDocument(val reason: String) : OpendriveReaderException("OpenDRIVE file cannot be parsed: $reason")
    data class HeaderNotFound(val reason: String) : OpendriveReaderException("Header element of OpenDRIVE dataset not found: $reason")
    data class VersionNotDeducible(val reason: String) : OpendriveReaderException("Version of OpenDRIVE dataset not deducible: $reason")
    data class NoDedicatedReaderAvailable(val reason: String) : OpendriveReaderException("No dedicated reader: $reason")
}
