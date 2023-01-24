/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.modifiers.opendrive.offset.adder

import arrow.core.Some
import arrow.core.getOrElse
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.model.opendrive.core.HeaderOffset

class OpendriveOffsetAdder(
    val parameters: OpendriveOffsetAdderParameters
) {

    /**
     * Adds the offset values to the OpenDRIVE header.
     */
    fun modify(opendriveModel: OpendriveModel): Pair<OpendriveModel, OpendriveOffsetAdderReport> {
        val report = OpendriveOffsetAdderReport(parameters)
        val modifiedOpendriveModel = opendriveModel.copy()
        modifiedOpendriveModel.updateAdditionalIdentifiers()

        if (parameters.isZeroOffset())
            return modifiedOpendriveModel to report

        val headerOffset = modifiedOpendriveModel.header.offset.getOrElse { HeaderOffset() }
        headerOffset.x = headerOffset.x + parameters.offsetX
        headerOffset.y = headerOffset.y + parameters.offsetY
        headerOffset.z = headerOffset.z + parameters.offsetZ
        headerOffset.hdg = headerOffset.hdg + parameters.offsetHeading
        modifiedOpendriveModel.header.offset = Some(headerOffset)

        return modifiedOpendriveModel to report
    }
}
