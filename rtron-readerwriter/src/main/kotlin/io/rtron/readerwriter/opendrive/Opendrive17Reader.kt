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
import arrow.core.computations.either
import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.opendrive.configuration.OpendriveReaderConfiguration
import io.rtron.readerwriter.opendrive.validation.OpendriveSchemaValidationReport
import io.rtron.readerwriter.opendrive.validation.OpendriveValidationEventHandler
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Unmarshaller
import org.asam.opendrive17.OpenDRIVE
import org.mapstruct.factory.Mappers
import java.io.File
import javax.xml.XMLConstants
import javax.xml.validation.SchemaFactory

class Opendrive17Reader(
    val configuration: OpendriveReaderConfiguration
) : AbstractOpendriveVersionSpecificReader() {

    // Properties and Initializers
    private val _jaxbUnmarshaller: Unmarshaller
    private val _validationEventHandler = OpendriveValidationEventHandler()

    init {
        val jaxbContext = JAXBContext.newInstance(OpenDRIVE::class.java)
        _jaxbUnmarshaller = jaxbContext.createUnmarshaller()

        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

        val resource = javaClass.classLoader.getResource("schemas/opendrive17/opendrive_17_core.xsd")!!
        val opendriveSchema = schemaFactory.newSchema(File(resource.toURI()))
        _jaxbUnmarshaller.schema = opendriveSchema
        _jaxbUnmarshaller.eventHandler = _validationEventHandler
    }

    // Methods
    override fun createOpendriveModel(filePath: Path): Either<AbstractOpendriveVersionSpecificReaderException, Pair<OpendriveModel, OpendriveSchemaValidationReport>> =
        either.eager {
            val converter = Mappers.getMapper(Opendrive17Mapper::class.java)
            converter.reportLogger = LogManager.getReportLogger(configuration.projectId)
            val opendriveModel = _jaxbUnmarshaller.unmarshal(filePath.toFileJ()) as OpenDRIVE
            val report = OpendriveSchemaValidationReport.of(OpendriveVersion.V_1_7, _validationEventHandler)

            _validationEventHandler.clear()
            val model = converter.mapModel(opendriveModel)
            Pair(model, report)
        }
}
