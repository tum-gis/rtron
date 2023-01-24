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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype

import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyHeaderOffset
import io.rtron.model.opendrive.header
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.modifiers.BasicDataTypeModifier

object CoreEvaluator {

    // Methods
    fun evaluate(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        if (modifiedOpendriveModel.road.isEmpty())
            messageList += DefaultMessage("NoRoadsContained", "Document does not contain any roads.", "", Severity.FATAL_ERROR, wasFixed = false)

        OpendriveModel.header.get(modifiedOpendriveModel).also { header ->
            if (header.revMajor < 0)
                messageList += DefaultMessage("UnkownOpendriveMajorVersionNumber", "", "Header element", Severity.FATAL_ERROR, wasFixed = false)

            if (header.revMinor < 0)
                messageList += DefaultMessage("UnkownOpendriveMinorVersionNumber", "", "Header element", Severity.FATAL_ERROR, wasFixed = false)
        }

        modifiedOpendriveModel = OpendriveModel.header.modify(modifiedOpendriveModel) { header ->
            header.name = BasicDataTypeModifier.modifyToOptionalString(header.name, "Header element", "name", messageList)
            header.date = BasicDataTypeModifier.modifyToOptionalString(header.date, "Header element", "date", messageList)
            header.vendor = BasicDataTypeModifier.modifyToOptionalString(header.vendor, "Header element", "vendor", messageList)

            header.east = BasicDataTypeModifier.modifyToOptionalFiniteDouble(header.east, "Header element", "east", messageList)
            header.north = BasicDataTypeModifier.modifyToOptionalFiniteDouble(header.north, "Header element", "north", messageList)
            header.south = BasicDataTypeModifier.modifyToOptionalFiniteDouble(header.south, "Header element", "south", messageList)
            header.west = BasicDataTypeModifier.modifyToOptionalFiniteDouble(header.south, "Header element", "west", messageList)

            header
        }

        modifiedOpendriveModel = everyHeaderOffset.modify(modifiedOpendriveModel) { currentHeaderOffset ->

            currentHeaderOffset.x = BasicDataTypeModifier.modifyToFiniteDouble(currentHeaderOffset.x, "Header element", "x", messageList)
            currentHeaderOffset.y = BasicDataTypeModifier.modifyToFiniteDouble(currentHeaderOffset.y, "Header element", "y", messageList)
            currentHeaderOffset.z = BasicDataTypeModifier.modifyToFiniteDouble(currentHeaderOffset.z, "Header element", "z", messageList)
            currentHeaderOffset.hdg = BasicDataTypeModifier.modifyToFiniteDouble(currentHeaderOffset.hdg, "Header element", "hdg", messageList)

            currentHeaderOffset
        }

        return modifiedOpendriveModel
    }
}
