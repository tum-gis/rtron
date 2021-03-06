/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

import io.rtron.io.files.Path
import io.rtron.model.opendrive.OpendriveModel
import org.asam.opendrive14.OpenDRIVE
import org.mapstruct.factory.Mappers
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException

class Opendrive14Reader(
    val configuration: OpendriveReaderWriterConfiguration
) {

    fun createOpendriveModel(file: Path): OpendriveModel {
        val opendrive14 = unmarshalFile(file)
        val converter = Mappers.getMapper(Opendrive14Mapper::class.java)
        converter.reportLogger = configuration.getReportLogger()

        return converter.mapModel(opendrive14)
    }

    private fun unmarshalFile(inputFile: Path): OpenDRIVE {

        return try {
            val jaxbContext = JAXBContext.newInstance(OpenDRIVE::class.java)
            val jaxbUnmarshaller = jaxbContext.createUnmarshaller()
            jaxbUnmarshaller.unmarshal(inputFile.toFileJ()) as OpenDRIVE
        } catch (e: JAXBException) {
            e.printStackTrace()
            OpenDRIVE()
        }
    }
}
