/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.opendrive.version.OpendriveVersion
import io.rtron.readerwriter.opendrive.writer.mapper.opendrive17.Opendrive17Mapper
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller
import org.mapstruct.factory.Mappers
import java.io.OutputStream
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMResult
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.asam.opendrive17.OpenDRIVE as Opendrive17

class OpendriveMarshaller {
    // Properties and Initializers
    val supportedVersion: OpendriveVersion = OpendriveVersion.V1_7
    private val jaxbMarshaller: Marshaller

    init {
        val jaxbContext = JAXBContext.newInstance(Opendrive17::class.java)
        jaxbMarshaller = jaxbContext.createMarshaller()

        // jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    }

    // Methods

    fun writeToStream(
        model: OpendriveModel,
        outputStream: OutputStream,
    ) {
        val converter = Mappers.getMapper(Opendrive17Mapper::class.java)

        val opendrive17Model = converter.mapModel(model)

        opendrive17Model.header.revMajor = supportedVersion.rev.first
        opendrive17Model.header.revMinor = supportedVersion.rev.second

        val domResult = DOMResult()
        jaxbMarshaller.marshal(opendrive17Model, domResult)

        // see: https://stackoverflow.com/a/48201559
        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        transformer.transform(DOMSource(domResult.node), StreamResult(outputStream))
    }
}
