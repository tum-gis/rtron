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

package io.rtron.transformer.converter.opendrive2roadspaces.header

import arrow.core.None
import arrow.core.Option
import arrow.core.flatten
import arrow.core.some
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.Message
import io.rtron.io.messages.MessageList
import io.rtron.io.messages.MessageSeverity
import io.rtron.math.projection.CoordinateReferenceSystem
import io.rtron.model.opendrive.core.HeaderGeoReference
import io.rtron.model.roadspaces.Header
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.model.opendrive.core.Header as OdrHeader

class HeaderBuilder(
    private val configuration: Opendrive2RoadspacesConfiguration
) {
    // Methods
    fun buildHeader(header: OdrHeader): ContextMessageList<Header> {
        val messageList = MessageList()

        val crs = header.geoReference.map { buildCoordinateSystem(it).handleMessageList { messageList += it } }.flatten()
        val roadspacesHeader = Header(coordinateReferenceSystem = crs)

        return ContextMessageList(roadspacesHeader, messageList)
    }

    /**
     * Builds the [CoordinateReferenceSystem] for the [Header].
     */
    private fun buildCoordinateSystem(geoReference: HeaderGeoReference): ContextMessageList<Option<CoordinateReferenceSystem>> {
        val messageList = MessageList()

        CoordinateReferenceSystem.of(configuration.crsEpsg).tap { return ContextMessageList(it.some(), messageList) }
        CoordinateReferenceSystem.of(geoReference.content).tap { return ContextMessageList(it.some(), messageList) }

        val message = Message("Unknown coordinate reference system.", MessageSeverity.WARNING)
        return ContextMessageList(None, MessageList.of(message))
    }
}
