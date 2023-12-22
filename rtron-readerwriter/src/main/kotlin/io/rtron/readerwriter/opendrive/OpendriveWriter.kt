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
import arrow.core.raise.either
import io.rtron.io.files.getFileSizeToDisplay
import io.rtron.io.files.outputStreamDirectOrCompressed
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.opendrive.writer.OpendriveMarshaller
import io.rtron.std.BaseException
import mu.KotlinLogging
import java.io.OutputStream
import java.nio.file.Path

object OpendriveWriter {
    // Properties and Initializers
    private val logger = KotlinLogging.logger {}

    private val opendriveMarshaller by lazy { OpendriveMarshaller() }

    // Methods
    fun writeToFile(model: OpendriveModel, filePath: Path): Either<OpendriveWriterException, Unit> = either {
        val outputStream: OutputStream = filePath.outputStreamDirectOrCompressed()
        writeToStream(model, outputStream)
        outputStream.close()

        logger.info("Completed writing of file ${filePath.fileName} (around ${filePath.getFileSizeToDisplay()}).")
    }

    fun writeToStream(model: OpendriveModel, outputStream: OutputStream) {
        opendriveMarshaller.writeToStream(model, outputStream)
    }
}

sealed class OpendriveWriterException(message: String) : BaseException(message)
