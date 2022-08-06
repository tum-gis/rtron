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
import arrow.core.continuations.either
import arrow.core.getOrHandle
import arrow.core.left
import io.rtron.io.files.getFileSizeToDisplay
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.readerwriter.opendrive.reader.OpendriveUnmarshaller
import io.rtron.readerwriter.opendrive.report.SchemaValidationReport
import io.rtron.readerwriter.opendrive.version.OpendriveVersion
import io.rtron.readerwriter.opendrive.version.OpendriveVersionUtils
import io.rtron.std.BaseException
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.isRegularFile

class OpendriveReader private constructor(
    val filePath: Path,
    private val opendriveVersion: OpendriveVersion,
    private val versionSpecificUnmarshaller: OpendriveUnmarshaller
) {

    // Properties and Initializers
    init {
        require(filePath.isRegularFile()) { "Path must point to a regular file." }
        require(supportedFilenameEndings.any { filePath.fileName.toString().endsWith(it) }) { "Path must point to a regular file." }
    }

    private val fallbackUnmarshaller by lazy {
        OpendriveUnmarshaller.FALLBACK
    }

    private val logger = KotlinLogging.logger {}

    // Methods
    fun runSchemaValidation(): SchemaValidationReport {

        val messageList = versionSpecificUnmarshaller.validate(filePath).getOrHandle {
            logger.warn("Schema validation was aborted due the following error: ${it.message}")
            return SchemaValidationReport(opendriveVersion, completedSuccessfully = false, validationAbortMessage = it.message)
        }
        if (!messageList.isEmpty()) logger.warn("Schema validation for OpenDRIVE $opendriveVersion found ${messageList.size} incidents.")
        else logger.info("Schema validation report for OpenDRIVE $opendriveVersion: Everything ok.")

        return SchemaValidationReport(opendriveVersion, messageList)
    }

    fun readModel(): Either<OpendriveReaderException, OpendriveModel> = either.eager {

        // read model
        val opendriveModel: OpendriveModel = versionSpecificUnmarshaller.readFromFile(filePath).fold({
            logger.warn("No dedicated reader available for OpenDRIVE $opendriveVersion. Using reader for OpenDRIVE ${fallbackUnmarshaller.opendriveVersion} as fallback.")
            val model = fallbackUnmarshaller.readFromFile(filePath).bind()
            model
        }, { it })

        opendriveModel.updateAdditionalIdentifiers()
        logger.info("Completed read-in of file ${filePath.fileName} (around ${filePath.getFileSizeToDisplay()}).")
        opendriveModel
    }

    companion object {
        enum class OpendriveFilenameEnding(val ending: String) {
            PURE(".xodr"),
            COMPRESSED(".xodr.zip"),
        }
        val supportedFilenameEndings: Set<String> = setOf(OpendriveFilenameEnding.PURE.ending, OpendriveFilenameEnding.COMPRESSED.ending)

        fun of(filePath: Path): Either<OpendriveReaderException, OpendriveReader> = either.eager {
            if (!filePath.isRegularFile())
                OpendriveReaderException.FileNotFound(filePath).left().bind<OpendriveReaderException>()

            val opendriveVersion = OpendriveVersionUtils.getOpendriveVersion(filePath).bind()
            val versionSpecificUnmarshaller = OpendriveUnmarshaller.of(opendriveVersion).getOrHandle { throw IllegalArgumentException(it.message) }

            OpendriveReader(filePath, opendriveVersion, versionSpecificUnmarshaller)
        }
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
