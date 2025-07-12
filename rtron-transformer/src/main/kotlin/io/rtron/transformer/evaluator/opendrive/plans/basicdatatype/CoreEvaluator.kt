/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyHeaderGeoReference
import io.rtron.model.opendrive.additions.optics.everyHeaderOffset
import io.rtron.model.opendrive.header
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.modifiers.BasicDataTypeModifier

object CoreEvaluator {
    // Methods
    fun evaluate(
        opendriveModel: OpendriveModel,
        parameters: OpendriveEvaluatorParameters,
        issueList: DefaultIssueList,
    ): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        if (modifiedOpendriveModel.road.isEmpty()) {
            issueList +=
                DefaultIssue(
                    "NoRoadsContained",
                    "Document does not contain any roads.",
                    "",
                    Severity.FATAL_ERROR,
                    wasFixed = false,
                )
        }

        val duplicateRoadIds =
            modifiedOpendriveModel.road
                .map { it.id }
                .groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
        if (duplicateRoadIds.isNotEmpty()) {
            issueList +=
                DefaultIssue(
                    "DuplicateRoadIds",
                    "Multiple road elements are using the same ID (affected IDs: ${duplicateRoadIds.keys.joinToString()}).",
                    "",
                    Severity.FATAL_ERROR,
                    wasFixed = false,
                )
        }

        OpendriveModel.header.get(modifiedOpendriveModel).also { header ->
            if (header.revMajor < 0) {
                issueList +=
                    DefaultIssue(
                        "UnkownOpendriveMajorVersionNumber",
                        "",
                        "Header element",
                        Severity.FATAL_ERROR,
                        wasFixed = false,
                    )
            }

            if (header.revMinor < 0) {
                issueList +=
                    DefaultIssue(
                        "UnkownOpendriveMinorVersionNumber",
                        "",
                        "Header element",
                        Severity.FATAL_ERROR,
                        wasFixed = false,
                    )
            }
        }

        modifiedOpendriveModel =
            OpendriveModel.header.modify(modifiedOpendriveModel) { header ->
                header.name =
                    BasicDataTypeModifier.modifyToOptionalString(
                        header.name,
                        "Header element",
                        "name",
                        issueList,
                    )
                header.date =
                    BasicDataTypeModifier.modifyToOptionalString(
                        header.date,
                        "Header element",
                        "date",
                        issueList,
                    )
                header.vendor =
                    BasicDataTypeModifier.modifyToOptionalString(
                        header.vendor,
                        "Header element",
                        "vendor",
                        issueList,
                    )

                header.east =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        header.east,
                        "Header element",
                        "east",
                        issueList,
                    )
                header.north =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        header.north,
                        "Header element",
                        "north",
                        issueList,
                    )
                header.south =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        header.south,
                        "Header element",
                        "south",
                        issueList,
                    )
                header.west =
                    BasicDataTypeModifier.modifyToOptionalFiniteDouble(
                        header.south,
                        "Header element",
                        "west",
                        issueList,
                    )

                header
            }

        modifiedOpendriveModel =
            everyHeaderOffset.modify(modifiedOpendriveModel) { currentHeaderOffset ->

                currentHeaderOffset.x =
                    BasicDataTypeModifier.modifyToFiniteDouble(
                        currentHeaderOffset.x,
                        "Header element",
                        "x",
                        issueList,
                    )
                currentHeaderOffset.y =
                    BasicDataTypeModifier.modifyToFiniteDouble(
                        currentHeaderOffset.y,
                        "Header element",
                        "y",
                        issueList,
                    )
                currentHeaderOffset.z =
                    BasicDataTypeModifier.modifyToFiniteDouble(
                        currentHeaderOffset.z,
                        "Header element",
                        "z",
                        issueList,
                    )
                currentHeaderOffset.hdg =
                    BasicDataTypeModifier.modifyToFiniteDouble(
                        currentHeaderOffset.hdg,
                        "Header element",
                        "hdg",
                        issueList,
                    )

                currentHeaderOffset
            }

        modifiedOpendriveModel =
            everyHeaderGeoReference.modify(modifiedOpendriveModel) { currentHeaderGeoReference ->

                val contentTrimmed = currentHeaderGeoReference.content.trim()
                if (currentHeaderGeoReference.content.length > contentTrimmed.length) {
                    issueList +=
                        DefaultIssue(
                            "GeoReferenceContainsLeadingAndTrailingWhitespace",
                            "GeoReference element contains leading and trailing whitespace.",
                            "GeoReference of header element",
                            Severity.WARNING,
                            wasFixed = true,
                        )
                    currentHeaderGeoReference.content = currentHeaderGeoReference.content.trim()
                }

                currentHeaderGeoReference
            }

        return modifiedOpendriveModel
    }
}
