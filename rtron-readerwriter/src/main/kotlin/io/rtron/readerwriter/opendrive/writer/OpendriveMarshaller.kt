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

package io.rtron.readerwriter.opendrive.writer

import arrow.core.Either
import arrow.core.right
import io.rtron.io.files.Path
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.opendrive.OpendriveVersion
import io.rtron.readerwriter.opendrive.OpendriveWriterException
import io.rtron.readerwriter.opendrive.writer.mapper.opendrive17.Opendrive17Mapper
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller
import org.mapstruct.factory.Mappers
import java.io.FileOutputStream
import java.io.OutputStream

class OpendriveMarshaller {

    // Properties and Initializers
    val supportedVersion: OpendriveVersion = OpendriveVersion.V_1_7
    private val _jaxbMarshaller: Marshaller

    init {
        val jaxbContext = JAXBContext.newInstance(org.asam.opendrive17.OpenDRIVE::class.java)
        _jaxbMarshaller = jaxbContext.createMarshaller()

        _jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    }

    // Methods
    fun writeToFile(model: OpendriveModel, directoryPath: Path): Either<OpendriveWriterException, Path> {
        val converter = Mappers.getMapper(Opendrive17Mapper::class.java)

        val opendrive17Model = converter.mapModel(model)

        opendrive17Model.header.revMajor = supportedVersion.rev.first
        opendrive17Model.header.revMinor = supportedVersion.rev.second

        val filePath = directoryPath.resolve(Path("opendrive17.xml"))
        val os: OutputStream = FileOutputStream(filePath.toFileJ())
        _jaxbMarshaller.marshal(opendrive17Model, os)

        return filePath.right()
    }
}
