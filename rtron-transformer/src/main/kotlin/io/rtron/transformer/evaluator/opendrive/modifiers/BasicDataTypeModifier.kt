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

package io.rtron.transformer.evaluator.opendrive.modifiers

import arrow.core.None
import arrow.core.Option
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.opendrive.additions.identifier.AbstractOpendriveIdentifier
import io.rtron.model.opendrive.additions.identifier.toIdentifierText
import io.rtron.std.filterToSortingBy
import io.rtron.std.filterToStrictSortingBy

object BasicDataTypeModifier {
    fun <T, K : Comparable<K>> filterToStrictlySorted(
        elementList: List<T>,
        selector: (T) -> K,
        location: String,
        attributeName: String,
        issueList: DefaultIssueList,
    ): List<T> {
        val elementListFiltered = elementList.filterToStrictSortingBy(selector)
        val numberOfIgnoredElements = elementList.size - elementListFiltered.size
        if (numberOfIgnoredElements > 0) {
            issueList +=
                DefaultIssue(
                    "NonStrictlyAscendingSortedList",
                    "The list entries of the attribute '$attributeName' are not sorted in strictly ascending order. " +
                        "$numberOfIgnoredElements elements are removed to adhere to strictly ascending order.",
                    location,
                    Severity.WARNING,
                    true,
                )
        }

        return elementListFiltered
    }

    fun <T, K : Comparable<K>> filterToStrictlySorted(
        elementList: List<T>,
        selector: (T) -> K,
        location: Option<AbstractOpendriveIdentifier>,
        attributeName: String,
        issueList: DefaultIssueList,
    ): List<T> = filterToStrictlySorted(elementList, selector, location.toIdentifierText(), attributeName, issueList)

    fun <T, K : Comparable<K>> filterToSorted(
        elementList: List<T>,
        selector: (T) -> K,
        location: String,
        attributeName: String,
        issueList: DefaultIssueList,
    ): List<T> {
        val elementListFiltered = elementList.filterToSortingBy(selector)
        val numberOfIgnoredElements = elementList.size - elementListFiltered.size
        if (numberOfIgnoredElements > 0) {
            issueList +=
                DefaultIssue(
                    "NonAscendingSortedList",
                    "The list entries of the attribute '$attributeName' are not sorted in ascending order. " +
                        "$numberOfIgnoredElements elements are removed to adhere to ascending order.",
                    location,
                    Severity.WARNING,
                    true,
                )
        }

        return elementListFiltered
    }

    fun <T, K : Comparable<K>> filterToSorted(
        elementList: List<T>,
        selector: (T) -> K,
        location: Option<AbstractOpendriveIdentifier>,
        attributeName: String,
        issueList: DefaultIssueList,
    ): List<T> = filterToSorted(elementList, selector, location.toIdentifierText(), attributeName, issueList)

    fun modifyToNonBlankString(
        element: String,
        location: Option<AbstractOpendriveIdentifier>,
        attributeName: String,
        issueList: DefaultIssueList,
        fallbackValue: String,
    ): String {
        if (element.isBlank()) {
            issueList +=
                DefaultIssue(
                    "BlankStringAttributeValue",
                    "The value of the attribute '$attributeName' is blank. The attribute is set to '$fallbackValue'.",
                    location.toIdentifierText(),
                    Severity.WARNING,
                    wasFixed = true,
                )
            return fallbackValue
        }

        return element
    }

    fun modifyToOptionalString(
        optionalElement: Option<String>,
        location: Option<AbstractOpendriveIdentifier>,
        attributeName: String,
        issueList: DefaultIssueList,
    ): Option<String> = modifyToOptionalString(optionalElement, location.toIdentifierText(), attributeName, issueList)

    fun modifyToOptionalString(
        optionalElement: Option<String>,
        location: String,
        attributeName: String,
        issueList: DefaultIssueList,
    ): Option<String> {
        if (optionalElement.isSome { it.isBlank() }) {
            issueList +=
                DefaultIssue(
                    "BlankStringAttributeValueForOptionalAttribute",
                    "The value of the attribute '$attributeName' is blank. The attribute is unset as it is optional.",
                    location,
                    Severity.WARNING,
                    wasFixed = true,
                )
            return None
        }

        return optionalElement
    }

    fun modifyToOptionalFiniteDouble(
        optionalElement: Option<Double>,
        location: Option<AbstractOpendriveIdentifier>,
        attributeName: String,
        issueList: DefaultIssueList,
    ): Option<Double> = modifyToOptionalFiniteDouble(optionalElement, location.toIdentifierText(), attributeName, issueList)

    fun modifyToOptionalFiniteDouble(
        optionalElement: Option<Double>,
        location: String,
        attributeName: String,
        issueList: DefaultIssueList,
    ): Option<Double> {
        if (optionalElement.isSome { !it.isFinite() }) {
            issueList +=
                DefaultIssue(
                    "NonFiniteDoubleAttributeValue",
                    "The value of the attribute '$attributeName' is not finite. The attribute is unset as it is optional.",
                    location,
                    Severity.WARNING,
                    wasFixed = true,
                )
            return None
        }

        return optionalElement
    }

    fun modifyToOptionalFinitePositiveDouble(
        optionalElement: Option<Double>,
        location: Option<AbstractOpendriveIdentifier>,
        attributeName: String,
        issueList: DefaultIssueList,
        tolerance: Double = 0.0,
    ): Option<Double> {
        if (optionalElement.isSome { !it.isFinite() || it < tolerance }) {
            issueList +=
                DefaultIssue(
                    "NonFinitePositiveDoubleAttributeValue",
                    "The value of the attribute '$attributeName' is not finite or not positive (applied tolerance: $tolerance). " +
                        "The attribute is unset as it is optional.",
                    location.toIdentifierText(),
                    Severity.WARNING,
                    wasFixed = true,
                )
            return None
        }

        return optionalElement
    }

    fun modifyToFinitePositiveDouble(
        element: Double,
        location: Option<AbstractOpendriveIdentifier>,
        attributeName: String,
        issueList: DefaultIssueList,
    ): Double {
        if (!element.isFinite() || element < 0.0) {
            issueList +=
                DefaultIssue(
                    "NonFinitePositiveDoubleAttributeValue",
                    "The value of the attribute '$attributeName' is not finite or not positive (applied tolerance: 0.0). " +
                        "The attribute value is set to 0.0.",
                    location.toIdentifierText(),
                    Severity.WARNING,
                    wasFixed = true,
                )
            return 0.0
        }

        return element
    }

    fun modifyToFiniteDouble(
        element: Double,
        location: Option<AbstractOpendriveIdentifier>,
        attributeName: String,
        issueList: DefaultIssueList,
    ): Double = modifyToFiniteDouble(element, location.toIdentifierText(), attributeName, issueList)

    fun modifyToFiniteDouble(
        element: Double,
        location: String,
        attributeName: String,
        issueList: DefaultIssueList,
    ): Double {
        if (!element.isFinite()) {
            issueList +=
                DefaultIssue(
                    "NonFiniteAttributeValue",
                    "The value of the attribute '$attributeName' is not finite. " +
                        "The attribute value is set to 0.0.",
                    location,
                    Severity.WARNING,
                    wasFixed = true,
                )
            return 0.0
        }

        return element
    }
}
