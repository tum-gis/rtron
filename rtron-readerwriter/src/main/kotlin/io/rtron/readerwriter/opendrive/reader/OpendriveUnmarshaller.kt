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

package io.rtron.readerwriter.opendrive.reader

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.rtron.readerwriter.opendrive.OpendriveReaderException
import io.rtron.readerwriter.opendrive.reader.validation.OpendriveValidationEventHandler
import io.rtron.readerwriter.opendrive.version.OpendriveVersion
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Unmarshaller
import javax.xml.XMLConstants
import javax.xml.validation.SchemaFactory
import org.asam.opendrive11.OpenDRIVE as Opendrive11
import org.asam.opendrive12.OpenDRIVE as Opendrive12
import org.asam.opendrive13.OpenDRIVE as Opendrive13
import org.asam.opendrive14.OpenDRIVE as Opendrive14
import org.asam.opendrive15.OpenDRIVE as Opendrive15
import org.asam.opendrive16.OpenDRIVE as Opendrive16
import org.asam.opendrive17.OpenDRIVE as Opendrive17
import org.asam.opendrive18.OpenDRIVE as Opendrive18

class OpendriveUnmarshaller(
    val opendriveVersion: OpendriveVersion,
) {
    // Properties and Initializers
    val jaxbUnmarshaller: Unmarshaller
    val validationEventHandler = OpendriveValidationEventHandler()

    init {
        require(opendriveVersion in SUPPORTED_SCHEMA_VERSIONS) { "The requested OpenDRIVE version is not supported." }

        val jaxbContext = JAXBContext.newInstance(OPENDRIVE_MODEL_CLASSES.getValue(opendriveVersion))
        jaxbUnmarshaller = jaxbContext.createUnmarshaller()

        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        val resourceName = OPENDRIVE_SCHEMA_LOCATIONS.getValue(opendriveVersion)
        val resource = javaClass.classLoader.getResource(resourceName)!!
        val opendriveSchema = schemaFactory.newSchema(resource)

        jaxbUnmarshaller.schema = opendriveSchema
        jaxbUnmarshaller.eventHandler = validationEventHandler
    }

    companion object {
        private val OPENDRIVE_MODEL_CLASSES: Map<OpendriveVersion, Class<out Any>> =
            mapOf(
                OpendriveVersion.V1_1 to Opendrive11::class.java,
                OpendriveVersion.V1_2 to Opendrive12::class.java,
                OpendriveVersion.V1_3 to Opendrive13::class.java,
                OpendriveVersion.V1_4 to Opendrive14::class.java,
                OpendriveVersion.V1_5 to Opendrive15::class.java,
                OpendriveVersion.V1_6 to Opendrive16::class.java,
                OpendriveVersion.V1_7 to Opendrive17::class.java,
                OpendriveVersion.V1_8 to Opendrive18::class.java,
            )
        private val OPENDRIVE_SCHEMA_LOCATIONS: Map<OpendriveVersion, String> =
            mapOf(
                OpendriveVersion.V1_1 to "schemas/opendrive11/OpenDRIVE_1.1D.xsd",
                OpendriveVersion.V1_2 to "schemas/opendrive12/OpenDRIVE_1.2A.xsd",
                OpendriveVersion.V1_3 to "schemas/opendrive13/OpenDRIVE_1.3D.xsd",
                OpendriveVersion.V1_4 to "schemas/opendrive14/OpenDRIVE_1.4H.xsd",
                OpendriveVersion.V1_5 to "schemas/opendrive15/OpenDRIVE_1.5M.xsd",
                OpendriveVersion.V1_6 to "schemas/opendrive16/opendrive_16_core.xsd",
                OpendriveVersion.V1_7 to "schemas/opendrive17/opendrive_17_core.xsd",
                OpendriveVersion.V1_8 to "schemas/opendrive18/OpenDRIVE_Core.xsd",
            )

        init {
            check(OPENDRIVE_MODEL_CLASSES.keys == OPENDRIVE_SCHEMA_LOCATIONS.keys) {
                "All model classes must have a correspondent schema location defined."
            }
        }

        val SUPPORTED_SCHEMA_VERSIONS: Set<OpendriveVersion> = OPENDRIVE_MODEL_CLASSES.keys

        fun of(version: OpendriveVersion): Either<OpendriveReaderException.NoDedicatedUnmarshallerAvailable, OpendriveUnmarshaller> =
            if (version in SUPPORTED_SCHEMA_VERSIONS) {
                OpendriveUnmarshaller(version).right()
            } else {
                OpendriveReaderException.NoDedicatedUnmarshallerAvailable(version).left()
            }
    }
}
