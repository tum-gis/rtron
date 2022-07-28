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

package io.rtron.transformer.evaluator.opendrive.manipulators

import arrow.core.Option
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.additions.identifier.AbstractOpendriveIdentifier
import io.rtron.std.filterToSortingBy
import io.rtron.std.filterToStrictSortingBy

object BasicDataTypeManipulator {

    fun <T, K : Comparable<K>> filterToStrictlySorted(elementList: List<T>, selector: (T) -> K, location: String, attributeName: String, messageList: DefaultMessageList): List<T> {
        val elementListFiltered = elementList.filterToStrictSortingBy(selector)
        val numberOfIgnoredElements = elementList.size - elementListFiltered.size
        if (numberOfIgnoredElements > 0)
            messageList += DefaultMessage("NonStrictlySortedList", "Ignoring $numberOfIgnoredElements entries of attribute '$attributeName' which are not placed in strictly ascending order.", location, Severity.WARNING, true)

        return elementListFiltered
    }

    fun <T, K : Comparable<K>> filterToStrictlySorted(elementList: List<T>, selector: (T) -> K, location: Option<AbstractOpendriveIdentifier>, attributeName: String, messageList: DefaultMessageList): List<T> {
        return filterToStrictlySorted(elementList, selector, location.fold({ "" }, { it.toString() }), attributeName, messageList)
    }

    fun <T, K : Comparable<K>> filterToSorted(elementList: List<T>, selector: (T) -> K, location: String, attributeName: String, messageList: DefaultMessageList): List<T> {
        val elementListFiltered = elementList.filterToSortingBy(selector)
        val numberOfIgnoredElements = elementList.size - elementListFiltered.size
        if (numberOfIgnoredElements > 0)
            messageList += DefaultMessage("NonSortedList", "Ignoring $numberOfIgnoredElements entries of attribute '$attributeName' which are not placed in ascending order.", location, Severity.WARNING, true)

        return elementListFiltered
    }

    fun <T, K : Comparable<K>> filterToSorted(elementList: List<T>, selector: (T) -> K, location: Option<AbstractOpendriveIdentifier>, attributeName: String, messageList: DefaultMessageList): List<T> {
        return filterToSorted(elementList, selector, location.fold({ "" }, { it.toString() }), attributeName, messageList)
    }
}
