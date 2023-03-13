/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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
import arrow.core.getOrElse
import arrow.core.left
import io.rtron.io.files.inputStreamFromDirectOrCompressedFile
import io.rtron.io.messages.MessageList
import io.rtron.readerwriter.opendrive.reader.OpendriveUnmarshaller
import io.rtron.readerwriter.opendrive.report.SchemaValidationReport
import io.rtron.readerwriter.opendrive.report.SchemaValidationReportMessage
import io.rtron.readerwriter.opendrive.version.OpendriveVersion
import io.rtron.readerwriter.opendrive.version.OpendriveVersionUtils
import mu.KotlinLogging
import java.io.InputStream
import java.nio.file.Path

object OpendriveValidator {

    // Properties and Initializers
    private val logger = KotlinLogging.logger {}

    fun validateFromFile(filePath: Path): Either<OpendriveReaderException, SchemaValidationReport> = either.eager {
        val opendriveVersion = OpendriveVersionUtils.getOpendriveVersion(filePath.inputStreamFromDirectOrCompressedFile()).bind()

        val fileInputStream = filePath.inputStreamFromDirectOrCompressedFile()
        val reportResult = validateFromStream(opendriveVersion, fileInputStream)
        fileInputStream.close()

        reportResult.bind()
    }

    fun validateFromStream(opendriveVersion: OpendriveVersion, inputStream: InputStream): Either<OpendriveReaderException, SchemaValidationReport> = either.eager {
        val messageList = runValidation(opendriveVersion, inputStream)
            .getOrElse {
                logger.warn("Schema validation was aborted due the following error: ${it.message}")
                return@eager SchemaValidationReport(opendriveVersion, completedSuccessfully = false, validationAbortMessage = it.message)
            }
        if (!messageList.isEmpty()) {
            logger.warn("Schema validation for OpenDRIVE $opendriveVersion found ${messageList.size} incidents.")
        } else {
            logger.info("Schema validation report for OpenDRIVE $opendriveVersion: Everything ok.")
        }

        SchemaValidationReport(opendriveVersion, messageList)
    }

    private fun runValidation(opendriveVersion: OpendriveVersion, inputStream: InputStream): Either<OpendriveReaderException, MessageList<SchemaValidationReportMessage>> =
        either.eager {
            val unmarshaller = OpendriveUnmarshaller.of(opendriveVersion).bind()

            try {
                unmarshaller.jaxbUnmarshaller.unmarshal(inputStream)
            } catch (e: NumberFormatException) {
                val invalidFormattedNumber = e.message.toString()
                OpendriveReaderException.NumberFormatException(reason = invalidFormattedNumber).left()
                    .bind<OpendriveReaderException.NumberFormatException>()
            } catch (e: Exception) {
                OpendriveReaderException.FatalSchemaValidationError(reason = e.toString()).left()
                    .bind<OpendriveReaderException.FatalSchemaValidationError>()
            }

            val messageList = unmarshaller.validationEventHandler.toMessageList()
            messageList
        }
}
