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

package io.rtron.readerwriter.opendrive

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.io.files.CompressedFileExtension
import io.rtron.io.files.getFileSizeToDisplay
import io.rtron.io.files.inputStreamFromDirectOrCompressedFile
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.readerwriter.opendrive.reader.OpendriveUnmarshaller
import io.rtron.readerwriter.opendrive.reader.mapper.opendrive14.Opendrive14Mapper
import io.rtron.readerwriter.opendrive.reader.mapper.opendrive16.Opendrive16Mapper
import io.rtron.readerwriter.opendrive.version.OpendriveVersion
import io.rtron.readerwriter.opendrive.version.OpendriveVersionUtils
import io.rtron.std.BaseException
import org.mapstruct.factory.Mappers
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.isRegularFile

object OpendriveReader {
    // Properties and Initializers
    private val logger = KotlinLogging.logger {}

    // Methods
    fun readFromFile(filePath: Path): Either<OpendriveReaderException, OpendriveModel> =
        either {
            if (!filePath.isRegularFile()) {
                OpendriveReaderException.FileNotFound(filePath).left().bind<OpendriveReaderException>()
            }
            if (!supportedFilenameEndings.any { filePath.fileName.toString().endsWith(it) }) {
                OpendriveReaderException.FileEndingNotSupported(filePath).left().bind<OpendriveReaderException>()
            }

            val fileInputStreamForVersion = filePath.inputStreamFromDirectOrCompressedFile()
            val opendriveVersion = OpendriveVersionUtils.getOpendriveVersion(fileInputStreamForVersion).bind()
            fileInputStreamForVersion.close()

            val fileInputStream = filePath.inputStreamFromDirectOrCompressedFile()
            val opendriveModelResult = readFromStream(opendriveVersion, fileInputStream)
            fileInputStream.close()

            val opendriveModel = opendriveModelResult.bind()
            logger.info { "Completed read-in of file ${filePath.fileName} (around ${filePath.getFileSizeToDisplay()})." }
            opendriveModel
        }

    fun readFromStream(
        opendriveVersion: OpendriveVersion,
        inputStream: InputStream,
    ): Either<OpendriveReaderException, OpendriveModel> =
        either {
            val unmarshallerOpendriveVersion =
                when (opendriveVersion) {
                    OpendriveVersion.V0_7 -> OpendriveVersion.V1_4
                    OpendriveVersion.V1_1 -> OpendriveVersion.V1_4
                    OpendriveVersion.V1_2 -> OpendriveVersion.V1_4
                    OpendriveVersion.V1_3 -> OpendriveVersion.V1_4
                    OpendriveVersion.V1_4 -> OpendriveVersion.V1_4
                    OpendriveVersion.V1_5 -> OpendriveVersion.V1_4
                    OpendriveVersion.V1_6 -> OpendriveVersion.V1_6
                    OpendriveVersion.V1_7 -> OpendriveVersion.V1_6
                }
            if (opendriveVersion != unmarshallerOpendriveVersion) {
                logger.warn {
                    "No dedicated reader available for OpenDRIVE $opendriveVersion. " +
                        "Using reader for OpenDRIVE $unmarshallerOpendriveVersion as fallback."
                }
            }

            val unmarshaller = OpendriveUnmarshaller.of(unmarshallerOpendriveVersion).bind()
            val opendriveVersionSpecificModel = unmarshaller.jaxbUnmarshaller.unmarshal(inputStream)

            val opendriveModel =
                when (unmarshallerOpendriveVersion) {
                    OpendriveVersion.V1_4 -> {
                        val converter = Mappers.getMapper(Opendrive14Mapper::class.java)
                        converter.mapModel(opendriveVersionSpecificModel as org.asam.opendrive14.OpenDRIVE)
                    }
                    OpendriveVersion.V1_6 -> {
                        val converter = Mappers.getMapper(Opendrive16Mapper::class.java)
                        converter.mapModel(opendriveVersionSpecificModel as org.asam.opendrive16.OpenDRIVE)
                    }
                    else -> throw IllegalStateException("Mapper must be implemented")
                }
            opendriveModel.updateAdditionalIdentifiers()
            opendriveModel
        }

    val supportedFilenameEndings: Set<String> =
        setOf(
            ".xodr",
            ".xodr.${CompressedFileExtension.ZIP.extension}",
            ".xodr.${CompressedFileExtension.GZ.extension}",
            ".xodr.${CompressedFileExtension.ZST.extension}",
        )
}

sealed class OpendriveReaderException(message: String) : BaseException(message) {
    data class FileNotFound(val path: Path) : OpendriveReaderException("File not found at $path")

    data class FileEndingNotSupported(val path: Path) : OpendriveReaderException("File not found at $path")

    data class MalformedXmlDocument(val reason: String) : OpendriveReaderException("OpenDRIVE file cannot be parsed: $reason")

    data class HeaderElementNotFound(val reason: String) : OpendriveReaderException(
        "Header element of OpenDRIVE dataset not found: $reason",
    )

    data class VersionNotIdentifiable(val reason: String) : OpendriveReaderException("Version of OpenDRIVE dataset not deducible: $reason")

    data class NoDedicatedUnmarshallerAvailable(val version: OpendriveVersion) : OpendriveReaderException(
        "No dedicated unmarshaller available for $version",
    )

    data class FatalSchemaValidationError(val reason: String) : OpendriveReaderException("Fatal error during schema validation: $reason")

    data class NumberFormatException(val reason: String) : OpendriveReaderException(
        "Invalid formatting of a number in the dataset: $reason",
    )
}
