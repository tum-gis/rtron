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

package io.rtron.readerwriter.opendrive.version

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import io.rtron.io.files.inputStreamFromDirectOrCompressedFile
import io.rtron.readerwriter.opendrive.OpendriveReaderException
import org.w3c.dom.Document
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

object OpendriveVersionUtils {

    fun getOpendriveVersion(filePath: Path): Either<OpendriveReaderException, OpendriveVersion> = either.eager {

        val inputStream = filePath.inputStreamFromDirectOrCompressedFile()

        val xmlDoc: Document = Either.catch { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream) }
            .mapLeft { OpendriveReaderException.MalformedXmlDocument(it.message ?: "") }
            .bind()

        val header = xmlDoc.getElementsByTagName("header")
        if (header.length <= 0)
            OpendriveReaderException.HeaderElementNotFound("No header element available").left().bind<OpendriveVersion>()
        if (header.length > 1)
            OpendriveReaderException.HeaderElementNotFound("Multiple header elements available").left().bind<OpendriveVersion>()

        val revMajor = Either.catch { header.item(0).attributes.getNamedItem("revMajor").nodeValue.toInt() }
            .mapLeft { OpendriveReaderException.VersionNotIdentifiable("Major version is not identifiable") }
            .bind()
        val revMinor = Either.catch { header.item(0).attributes.getNamedItem("revMinor").nodeValue.toInt() }
            .mapLeft { OpendriveReaderException.VersionNotIdentifiable("Minor version is not identifiable") }
            .bind()

        inputStream.close()
        OpendriveVersion.ofRevision(revMajor, revMinor).mapLeft { OpendriveReaderException.VersionNotIdentifiable(it.message) }.bind()
    }
}
