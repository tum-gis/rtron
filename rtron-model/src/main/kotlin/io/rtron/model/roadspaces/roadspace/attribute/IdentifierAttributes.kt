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

package io.rtron.model.roadspaces.roadspace.attribute

import io.rtron.io.files.FileIdentifier
import io.rtron.model.roadspaces.ModelIdentifier
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObjectIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneSectionIdentifier


fun FileIdentifier.toAttributes(prefix: String): AttributeList {
    val fileIdentifier = this
    return attributes(prefix) {
        attribute("sourceFileName", fileIdentifier.fileName)
        attribute("sourceFileExtension", fileIdentifier.fileExtension)
        attribute("sourceFileHashSha256", fileIdentifier.fileHashSha256)
    }
}

fun ModelIdentifier.toAttributes(prefix: String): AttributeList {
    val modelIdentifier = this
    return attributes(prefix) {
        attribute("modelName", modelIdentifier.modelName)
        attribute("modelDate", modelIdentifier.modelDate)
        attribute("modelVendor", modelIdentifier.modelVendor)
    } + this.sourceFileIdentifier.toAttributes(prefix)
}

fun RoadspaceIdentifier.toAttributes(prefix: String): AttributeList {
    val roadspaceIdentifier = this
    return attributes(prefix) {
        attribute("roadName", roadspaceIdentifier.roadspaceName)
        attribute("roadId", roadspaceIdentifier.roadspaceId)
    } + roadspaceIdentifier.modelIdentifier.toAttributes(prefix)
}

fun LaneSectionIdentifier.toAttributes(prefix: String): AttributeList {
    val laneSectionIdentifier = this
    return attributes(prefix) {
        attribute("laneSectionId", laneSectionIdentifier.laneSectionId)
    } + laneSectionIdentifier.roadspaceIdentifier.toAttributes(prefix)
}

fun LaneIdentifier.toAttributes(prefix: String): AttributeList {
    val laneIdentifier = this
    return attributes(prefix) {
        attribute("laneId", laneIdentifier.laneId)
    } + laneIdentifier.laneSectionIdentifier.toAttributes(prefix)
}

fun RoadspaceObjectIdentifier.toAttributes(prefix: String): AttributeList {
    val roadspaceObjectIdentifier = this
    return attributes(prefix) {
        attribute("roadObjectId", roadspaceObjectIdentifier.roadspaceObjectId)
        attribute("roadObjectName", roadspaceObjectIdentifier.roadspaceObjectName)
    } + roadspaceObjectIdentifier.roadspaceIdentifier.toAttributes(prefix)
}
