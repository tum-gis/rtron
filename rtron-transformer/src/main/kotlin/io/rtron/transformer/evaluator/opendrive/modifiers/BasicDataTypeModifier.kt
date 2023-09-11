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

package io.rtron.transformer.evaluator.opendrive.modifiers

import arrow.core.None
import arrow.core.Option
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.additions.identifier.AbstractOpendriveIdentifier
import io.rtron.model.opendrive.additions.identifier.toIdentifierText
import io.rtron.std.filterToSortingBy
import io.rtron.std.filterToStrictSortingBy

object BasicDataTypeModifier {

    fun <T, K : Comparable<K>> filterToStrictlySorted(elementList: List<T>, selector: (T) -> K, location: String, attributeName: String, messageList: DefaultMessageList): List<T> {
        val elementListFiltered = elementList.filterToStrictSortingBy(selector)
        val numberOfIgnoredElements = elementList.size - elementListFiltered.size
        if (numberOfIgnoredElements > 0) {
            messageList += DefaultMessage("NonStrictlyAscendingSortedList", "The list entries of the attribute '$attributeName' are not sorted in strictly ascending order. $numberOfIgnoredElements elements are removed to adhere to strictly ascending order.", location, Severity.WARNING, true)
        }

        return elementListFiltered
    }

    fun <T, K : Comparable<K>> filterToStrictlySorted(elementList: List<T>, selector: (T) -> K, location: Option<AbstractOpendriveIdentifier>, attributeName: String, messageList: DefaultMessageList): List<T> {
        return filterToStrictlySorted(elementList, selector, location.toIdentifierText(), attributeName, messageList)
    }

    fun <T, K : Comparable<K>> filterToSorted(elementList: List<T>, selector: (T) -> K, location: String, attributeName: String, messageList: DefaultMessageList): List<T> {
        val elementListFiltered = elementList.filterToSortingBy(selector)
        val numberOfIgnoredElements = elementList.size - elementListFiltered.size
        if (numberOfIgnoredElements > 0) {
            messageList += DefaultMessage("NonAscendingSortedList", "The list entries of the attribute '$attributeName' are not sorted in ascending order. $numberOfIgnoredElements elements are removed to adhere to ascending order.", location, Severity.WARNING, true)
        }

        return elementListFiltered
    }

    fun <T, K : Comparable<K>> filterToSorted(elementList: List<T>, selector: (T) -> K, location: Option<AbstractOpendriveIdentifier>, attributeName: String, messageList: DefaultMessageList): List<T> {
        return filterToSorted(elementList, selector, location.toIdentifierText(), attributeName, messageList)
    }

    fun modifyToNonBlankString(element: String, location: Option<AbstractOpendriveIdentifier>, attributeName: String, messageList: DefaultMessageList, fallbackValue: String): String {
        if (element.isBlank()) {
            messageList += DefaultMessage("BlankStringAttributeValue", "The value of the attribute '$attributeName' is blank. The attribute is set to '$fallbackValue'.", location.toIdentifierText(), Severity.WARNING, wasFixed = true)
            return fallbackValue
        }

        return element
    }

    fun modifyToOptionalString(optionalElement: Option<String>, location: Option<AbstractOpendriveIdentifier>, attributeName: String, messageList: DefaultMessageList): Option<String> =
        modifyToOptionalString(optionalElement, location.toIdentifierText(), attributeName, messageList)

    fun modifyToOptionalString(optionalElement: Option<String>, location: String, attributeName: String, messageList: DefaultMessageList): Option<String> {
        if (optionalElement.isSome { it.isBlank() }) {
            messageList += DefaultMessage(
                "BlankStringAttributeValueForOptionalAttribute",
                "The value of the attribute '$attributeName' is blank. The attribute is unset as it is optional.",
                location,
                Severity.WARNING,
                wasFixed = true
            )
            return None
        }

        return optionalElement
    }

    fun modifyToOptionalFiniteDouble(optionalElement: Option<Double>, location: Option<AbstractOpendriveIdentifier>, attributeName: String, messageList: DefaultMessageList): Option<Double> =
        modifyToOptionalFiniteDouble(optionalElement, location.toIdentifierText(), attributeName, messageList)

    fun modifyToOptionalFiniteDouble(optionalElement: Option<Double>, location: String, attributeName: String, messageList: DefaultMessageList): Option<Double> {
        if (optionalElement.isSome { !it.isFinite() }) {
            messageList += DefaultMessage(
                "NonFiniteDoubleAttributeValue",
                "The value of the attribute '$attributeName' is not finite. The attribute is unset as it is optional.",
                location,
                Severity.WARNING,
                wasFixed = true
            )
            return None
        }

        return optionalElement
    }

    fun modifyToOptionalFinitePositiveDouble(optionalElement: Option<Double>, location: Option<AbstractOpendriveIdentifier>, attributeName: String, messageList: DefaultMessageList, tolerance: Double = 0.0): Option<Double> {
        if (optionalElement.isSome { !it.isFinite() || it < tolerance }) {
            messageList += DefaultMessage(
                "NonFinitePositiveDoubleAttributeValue",
                "The value of the attribute '$attributeName' is not finite or not positive (applied tolerance: $tolerance). The attribute is unset as it is optional.",
                location.toIdentifierText(),
                Severity.WARNING,
                wasFixed = true
            )
            return None
        }

        return optionalElement
    }

    fun modifyToFinitePositiveDouble(element: Double, location: Option<AbstractOpendriveIdentifier>, attributeName: String, messageList: DefaultMessageList): Double {
        if (!element.isFinite() || element < 0.0) {
            messageList += DefaultMessage("NonFinitePositiveDoubleAttributeValue", "The value of the attribute '$attributeName' is not finite or not positive (applied tolerance: 0.0). The attribute value is set to 0.0.", location.toIdentifierText(), Severity.WARNING, wasFixed = true)
            return 0.0
        }

        return element
    }

    fun modifyToFiniteDouble(element: Double, location: Option<AbstractOpendriveIdentifier>, attributeName: String, messageList: DefaultMessageList): Double =
        modifyToFiniteDouble(element, location.toIdentifierText(), attributeName, messageList)

    fun modifyToFiniteDouble(element: Double, location: String, attributeName: String, messageList: DefaultMessageList): Double {
        if (!element.isFinite()) {
            messageList += DefaultMessage("NonFiniteAttributeValue", "The value of the attribute '$attributeName' is not finite. The attribute value is set to 0.0.", location, Severity.WARNING, wasFixed = true)
            return 0.0
        }

        return element
    }
}
