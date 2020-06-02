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
 * Returns the cumulative sum.
 *
 * @receiver list of individual [Double] values on which the cumulative sum operation is performed on
 * @return list of accumulated values
 */
@OptIn(ExperimentalStdlibApi::class)
@kotlin.jvm.JvmName("cumulativeSumOfDouble")
fun Iterable<Double>.cumulativeSum(): List<Double> = scan(0.0) { acc, element -> acc + element }

/**
 * Returns the cumulative sum.
 *
 * @receiver list of individual [Float] values on which the cumulative sum operation is performed on
 * @return list of accumulated values
 */
@OptIn(ExperimentalStdlibApi::class)
@kotlin.jvm.JvmName("cumulativeSumOfFloat")
fun Iterable<Float>.cumulativeSum(): List<Float> = scan(0.0f) { acc, element -> acc + element }

/**
 * Returns the cumulative sum.
 *
 * @receiver list of individual [Int] values on which the cumulative sum operation is performed on
 * @return list of accumulated values
 */
@OptIn(ExperimentalStdlibApi::class)
@kotlin.jvm.JvmName("cumulativeSumOfInt")
fun Iterable<Int>.cumulativeSum(): List<Int> = scan(0) { acc, element -> acc + element }

/**
 * Zip each element in this list with the next two elements and zip them to a [Triple].
 *
 * @return list of triples
 */
fun <T> Iterable<T>.zipWithNextToTriples(): List<Triple<T, T, T>> =
        this.windowed(3, 1, false).map { Triple(it[0], it[1], it[2]) }

/**
 * Returns true, if the list is sorted ascending according to the [selector].
 *
 * @param selector the return of the [selector] is used for evaluating whether the list is sorted
 * @return true, if the list is sorted in ascending order
 */
inline fun <T, R : Comparable<R>> Iterable<T>.isSortedBy(crossinline selector: (T) -> R): Boolean =
        this.asSequence().zipWithNext { a, b -> selector(a) <= selector(b) }.all { it }

/**
 * Returns true, if list is sorted in ascending order.
 *
 * @receiver list to be evaluated
 * @return true, if the list is sorted in ascending order
 */
fun <T : Comparable<T>> Iterable<T>.isSorted(): Boolean =
        this.asSequence().zipWithNext { a, b -> a <= b }.all { it }

/**
 * Returns true, if list is sorted in descending order.
 *
 * @receiver list to be evaluated
 * @return true, if the list is sorted in descending order
 */
fun <T : Comparable<T>> Iterable<T>.isSortedDescending(): Boolean =
        this.asSequence().zipWithNext { a, b -> a >= b }.all { it }
