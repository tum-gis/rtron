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

package io.rtron.model.opendrive.additions.exceptions

sealed class OpendriveException(val message: String, val exceptionIdentifier: String) {
    data class EmptyList(val attributeName: String, val suffix: String = "") :
        OpendriveException("List for attribute '$attributeName' is empty, but it has to contain at least one element.${if (suffix.isNotEmpty()) " $suffix" else ""}", "EmptyList")

    data class NonSortedList(val attributeName: String, val suffix: String = "") :
        OpendriveException("List of attribute '$attributeName' is not sorted.${if (suffix.isNotEmpty()) " $suffix" else ""}", "NonSortedList")

    data class NonStrictlySortedList(val attributeName: String, val suffix: String = "") :
        OpendriveException("List of attribute '$attributeName' is not strictly sorted.${if (suffix.isNotEmpty()) " $suffix" else ""}", "NonStrictlySortedList")
    data class MissingValue(val attributeName: String) : OpendriveException("Missing value for attribute '$attributeName'.", "MissingValue")
    data class UnexpectedValue(val attributeName: String, val attributeValue: String, val suffix: String = "") :
        OpendriveException(
            "Unexpected value ${if (attributeValue.isNotEmpty()) "($attributeValue)" else ""} " +
                "for attribute '$attributeName'.${if (suffix.isNotEmpty()) " $suffix" else ""}",
            "UnexpectedValue"
        )

    data class EmptyValueForOptionalAttribute(val attributeName: String) : OpendriveException("Attribute '$attributeName' is set with an empty value even though the attribute itself is optional.", "EmptyValueForOptionalAttribute")
}

fun OpendriveException.toIllegalStateException(): IllegalStateException = IllegalStateException(message)
