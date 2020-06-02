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

package io.rtron.transformer.opendrive2roadspaces.roadspaces

import io.rtron.io.files.FileIdentifier
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesParameters
import io.rtron.model.opendrive.common.EUnitSpeed
import io.rtron.model.roadspaces.ModelIdentifier
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.UnitOfMeasure
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObjectIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.LaneSectionIdentifier


/**
 * Transforms units of the OpenDRIVE data model to units of the RoadSpaces data model.
 */
fun EUnitSpeed.toUnitOfMeasure(): UnitOfMeasure = when (this) {
    EUnitSpeed.METER_PER_SECOND -> UnitOfMeasure.METER_PER_SECOND
    EUnitSpeed.MILES_PER_HOUR -> UnitOfMeasure.MILES_PER_HOUR
    EUnitSpeed.KILOMETER_PER_HOUR -> UnitOfMeasure.KILOMETER_PER_HOUR
    EUnitSpeed.UNKNOWN -> UnitOfMeasure.UNKNOWN
}


/**
 * Builder for [AttributeList] mainly for identifier parameters.
 */
class AttributesBuilder(
        private val parameters: Opendrive2RoadspacesParameters
) {
    // Methods
    fun toAttributes(fileIdentifier: FileIdentifier): AttributeList =
            attributes(parameters.attributesPrefix + "identifier_") {
                attribute("sourceFileName", fileIdentifier.fileName)
                attribute("sourceFileHashSha256", fileIdentifier.fileHashSha256)
            }

    fun toAttributes(modelIdentifier: ModelIdentifier): AttributeList =
            attributes(parameters.attributesPrefix + "identifier_") {
                attribute("modelName", modelIdentifier.modelName)
                attribute("modelDate", modelIdentifier.modelDate)
                attribute("modelVendor", modelIdentifier.modelVendor)
            } + toAttributes(modelIdentifier.sourceFileIdentifier)

    fun toAttributes(roadspaceIdentifier: RoadspaceIdentifier): AttributeList =
            attributes(parameters.attributesPrefix + "identifier_") {
                attribute("roadName", roadspaceIdentifier.roadspaceName)
                attribute("roadId", roadspaceIdentifier.roadspaceId)
            } + toAttributes(roadspaceIdentifier.modelIdentifier)

    fun toAttributes(laneSectionIdentifier: LaneSectionIdentifier): AttributeList =
            attributes(parameters.attributesPrefix + "identifier_") {
                attribute("laneSectionId", laneSectionIdentifier.laneSectionId)
            } + toAttributes(laneSectionIdentifier.roadspaceIdentifier)

    fun toAttributes(laneIdentifier: LaneIdentifier): AttributeList =
            attributes(parameters.attributesPrefix + "identifier_") {
                attribute("laneId", laneIdentifier.laneId)
            } + toAttributes(laneIdentifier.laneSectionIdentifier)

    fun toAttributes(roadspaceObjectIdentifier: RoadspaceObjectIdentifier): AttributeList =
            attributes(parameters.attributesPrefix + "identifier_") {
                attribute("roadObjectId", roadspaceObjectIdentifier.roadspaceObjectId)
                attribute("roadObjectName", roadspaceObjectIdentifier.roadspaceObjectName)
            } + toAttributes(roadspaceObjectIdentifier.roadspaceIdentifier)
}
