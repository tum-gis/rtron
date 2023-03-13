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

package io.rtron.std

import kotlin.math.max

/**
 * Shape of a moving window operation to be returned.
 */
enum class MovingWindowShape {
    /** Size of the returning list is the base list size plus the size of the window. */
    FULL,

    /** Same size of returning list like the base list. */
    SAME
}

/**
 * Returns a list, over which [window] was moved/ slided over. This function is an abstract implementation of the
 * [moving average](https://en.wikipedia.org/wiki/Moving_average), but it can also be used to realize boolean window
 * filters.
 *
 * @receiver the list over which the [window] is moved
 * @param window the [window] is moved or slided over the receiver
 * @param multiplication higher order function for multiplying the elements of the receiver list with elements of
 * the [window]
 * @param addition higher order function for [addition] the results to reduce them to the list with elements of type [K]
 * @param shape the resulting list is as long as the base list if [MovingWindowShape.SAME] or base.size + other.size,
 * if [MovingWindowShape.FULL]
 * @return the size of the resulting list depends on the [shape]
 */
fun <S, T, K> List<S>.moveWindow(
    window: List<T>,
    multiplication: (baseElement: S, otherElement: T) -> K,
    addition: (K, K) -> K,
    shape: MovingWindowShape = MovingWindowShape.FULL
): List<K> {
    require(this.isNotEmpty()) { "Base list requires elements and thus must not be empty." }
    require(window.isNotEmpty()) { "Other list requires elements and thus must not be empty." }

    val multipliedSubLists = this.fold(emptyList<List<K>>()) { acc, s ->
        acc + listOf(window.map { multiplication(s, it) })
    }

    val rowIndices = window.indices.reversed().toList()

    val resultingIndexEnd = if (shape == MovingWindowShape.FULL) size + window.lastIndex else size

    return (0 until resultingIndexEnd).fold(emptyList()) { acc, curColInd ->
        val relevantColIndices = (max(curColInd - window.lastIndex, 0)..curColInd).toList()
        val relevantRowIndices = rowIndices.drop(rowIndices.size - relevantColIndices.size)
        val indices = relevantColIndices.zip(relevantRowIndices)
        val startValue = multipliedSubLists[indices.first().first][indices.first().second]
        val resultElement = indices.drop(0).fold(startValue) { sum, pair ->
            val curRow = multipliedSubLists.getOrNull(pair.first)
            if (curRow.isNullOrEmpty()) {
                sum
            } else {
                addition(sum, curRow[pair.second])
            }
        }
        acc + listOf(resultElement)
    }
}

/**
 * Moving a [window] over a [Boolean] list with boolean [window].
 *
 * @receiver the list over which the [window] is moved
 * @param window the [window] is moved or slided over the receiver
 * @param shape the resulting list is as long as the base list if [MovingWindowShape.SAME] or base.size + other.size,
 * if [MovingWindowShape.FULL]
 * @return an element of the returned list is true, if the multiplication of at least one element of the receiver
 * and [window] list is true
 */
fun List<Boolean>.moveWindow(window: List<Boolean>, shape: MovingWindowShape = MovingWindowShape.FULL) =
    this.moveWindow(
        window,
        { baseElement, otherElement -> baseElement && otherElement },
        { a, b -> a || b },
        shape
    )

/**
 * Returns a list containing all elements not matching the given [predicate]. The predicate is operated on a sublist
 * and if the pattern matches the elements with indices [dropIndices] are dropped.
 *
 * Example
 * Given a list: A, A, B, C, A, A, B, C, A
 * Predicate: it[0] == A && it[1] == B && it[2] == C
 * DropIndices: (false, true, true)
 * Return: A, B, A, B
 *
 * @receiver list on which the window filter is applied
 * @param dropIndices the list of [Boolean] indicating the indices that are dropped in the case of a
 * matching [predicate]
 * @param predicate if the [predicate] returns true, the indices are dropped according to the [dropIndices]
 * @return list without elements which match the [predicate]
 */
fun <T> List<T>.filterWindowed(dropIndices: List<Boolean>, predicate: (List<T>) -> Boolean): List<T> {
    require(dropIndices.size <= this.size) { "Dropping indices list must be smaller than base list." }
    if (isEmpty()) return emptyList()

    val windowedList = this.windowed(dropIndices.size)
    val predicateSatisfaction = windowedList.map(predicate)
    val passingList = predicateSatisfaction.moveWindow(dropIndices, MovingWindowShape.SAME)
    return this.subList(0, passingList.size).filterIndexed { index, _ -> !passingList[index] }
}

/**
 * Filters out all elements, when the [predicate] with [windowSize] is matching.
 *
 * @receiver list on which the window filter is applied
 * @param windowSize size of the window
 * @param predicate if the [predicate] returns true, all elements are filtered out
 * @return list without elements which match the [predicate]
 */
fun <T> List<T>.filterWindowedEnclosing(windowSize: Int, predicate: (List<T>) -> Boolean): List<T> {
    val dropIndices = List(windowSize) { true }
    return this.filterWindowedEnclosing(dropIndices, predicate)
}

/**
 * Filter windows that follow which match the [predicate]. The filter window is executed enclosing the list.
 *
 * Example
 * Given a list: A, A, B, C, A
 * Predicate: it[0] == it[1]
 * DropIndices: (true, false) -> A, B, C (edge pattern also dropped)
 * DropIndices: (false, true) -> A, B, C, A (edge pattern not dropped)
 *
 * @receiver list on which the window filter is applied
 * @param dropIndices the list of [Boolean] indicating the the indices that are dropped in the case of a
 * matching [predicate]
 * @param predicate if the [predicate] returns true, the indices are dropped according to the [dropIndices]
 * @return list without elements which match the [predicate]
 */
fun <T> List<T>.filterWindowedEnclosing(dropIndices: List<Boolean>, predicate: (List<T>) -> Boolean): List<T> {
    require(dropIndices.size <= this.size) { "Dropping indices list must be smaller than base list." }
    if (isEmpty()) return emptyList()

    val windowedBase = this.asSequence().windowedEnclosing(dropIndices.size).toList()
    val predicateSatisfaction = windowedBase.map(predicate)
    val passingList = predicateSatisfaction.moveWindow(dropIndices, MovingWindowShape.SAME)
    return this.subList(0, passingList.size).filterIndexed { index, _ -> !passingList[index] }
}
