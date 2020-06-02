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

package io.rtron.std

/**
 * Returns a list without consecutive elements which have the same return by the given [selector] function.
 *
 * @receiver the base list for which the consecutive duplicates according to the [selector] are being removed
 * @param selector if the resulting elements of the selector is equal, the duplicates are being removed
 * @receiver a list without consecutive duplicates, whereby the receiver's order is preserved
 * @return list without consecutive duplicates
 */
inline fun <T, K> Iterable<T>.distinctConsecutive(crossinline selector: (T) -> K): List<T> {
    if (this is Collection && isEmpty()) return emptyList()
    if (this.count() == 1) return this.toList()

    return this.toList().filterWindowed(listOf(true, false)) { selector(it[0]) == selector(it[1]) } + this.last()
}

/**
 * Returns a list without consecutive elements which have the same return by the given [selector] function.
 * The operation is performed enclosing around the list, meaning that the selector(lastElement) ==
 * selector(firstElement) is also evaluated.
 *
 * @receiver the base list for which the consecutive duplicates according to the [selector] are being removed
 * @param selector if the resulting elements of the selector is equal, the duplicates are being removed
 * @receiver a list without consecutive duplicates, whereby the receiver's order is preserved
 * @return list without consecutive duplicates (also potentially enclosing duplicates)
 */
inline fun <T, K> Iterable<T>.distinctConsecutiveEnclosing(crossinline selector: (T) -> K): List<T> {
    if (this is Collection && isEmpty()) return emptyList()
    if (this.count() == 1) return this.toList()

    return this.toList().filterWindowedEnclosing(listOf(true, false)) { selector(it[0]) == selector(it[1]) }
}

/**
 * Creates a sequence of lists with [size], iterating through the (receiver) sequence.
 *
 * @receiver the base sequence used to generate the sublists of [size]
 * @param size the size of the sublists to be returned
 * @param step the number of elements to move on
 * @return the sequence of sublists
 */
fun <T> Sequence<T>.windowedEnclosing(size: Int, step: Int = 1): Sequence<List<T>> =
        (this + this.take(size - 1)).windowed(size, step)

/**
 * Returns true, if all lists have the same number of elements.
 *
 * @receiver first list used for comparing the sizes of the lists
 * @param others other lists used for the size comparison
 * @return true, if all lists have the same size
 */
fun <T, K> Collection<T>.hasSameSizeAs(vararg others: Collection<K>) = others.all { it.size == this.size }
