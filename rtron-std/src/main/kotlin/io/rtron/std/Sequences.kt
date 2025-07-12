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

package io.rtron.std

/**
 * Returns a list of pairs for each two adjacent elements in this collection including a pair of the last and first
 * element.
 *
 * @receiver iterable containing all elements which are zipped
 * @return list of all pairs of two adjacent elements (with last and first element pair), whereby the returned list is
 * empty if the receiver contains less than two elements
 */
fun <T> Iterable<T>.zipWithNextEnclosing(): List<Pair<T, T>> =
    if (this.count() >= 2) {
        zipWithNext { a, b -> a to b } + Pair(last(), first())
    } else {
        zipWithNext { a, b -> a to b }
    }

/**
 * Zips consecutive elements of the original list by the key returned by the given [keySelector] function
 * applied to each element and returns a list where each containing list holds the selected elements.
 *
 * @param keySelector if [keySelector] of consecutive elements return equal objects, zipping is performed
 * @receiver contains the elements which are zipped
 * @return list of all zipped elements (also represented as list), whereas the returned nested lists preserve
 * the iteration order
 */
fun <T, K> Iterable<T>.zipWithConsecutives(keySelector: (T) -> K): List<List<T>> =
    this.fold(listOf<MutableList<T>>()) { acc, element ->
        if (acc.isNotEmpty() && acc.last().isNotEmpty() && keySelector(acc.last().first()) == keySelector(element)) {
            acc.last().add(element)
            acc
        } else {
            acc + listOf(mutableListOf(element))
        }
    }

/**
 * Zips consecutive elements of the original list by the key returned by the given [keySelector] function
 * applied to each element and returns a list where each containing list holds the selected elements. If last
 * zip and first zip have the same key, the last zip is prepended.
 *
 * @param keySelector if [keySelector] of consecutive elements return equal objects, zipping is performed
 * @receiver contains the elements which are zipped
 * @return list of all zipped elements (also represented as list), whereas the returned nested lists preserve
 * the iteration order
 */
fun <T, K> Iterable<T>.zipWithConsecutivesEnclosing(keySelector: (T) -> K): List<List<T>> {
    val zippedConsecutively = this.zipWithConsecutives(keySelector)
    return if (this.count() >= 2 &&
        keySelector(zippedConsecutively.first().first()) == keySelector(zippedConsecutively.last().last())
    ) {
        listOf(zippedConsecutively.last() + zippedConsecutively.first()) +
            zippedConsecutively.subList(1, zippedConsecutively.lastIndex)
    } else {
        zippedConsecutively
    }
}
