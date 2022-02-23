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

package io.rtron.std

import arrow.core.Either
import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import com.github.kittinunf.result.Result

fun <E : Exception, V : Any?> Either<E, V>.toResult(): Result<V, E> = this.fold({ Result.Failure(it) }, { Result.Success(it) })
fun <E : Exception, V : Any?> Result<V, E>.toEither(): Either<E, V> = this.fold({ Either.Right(it) }, { Either.Left(it) })

/**
 * Handle the [Result.Success] with [block] and return the [Result.Failure].
 *
 * @receiver the [Result] to be handled
 * @param block the actual handler of the [Result.Success]
 * @return remaining [Result.Failure]
 */
inline fun <V : Any?, E : Exception> Result<V, E>.handleSuccess(block: (Result.Success<V>) -> Nothing): E =
    when (this) {
        is Result.Success -> block(this)
        is Result.Failure -> error
    }

/**
 * Handle the [Result.Failure] with [block] and return the [Result.Success].
 *
 * @receiver the [Result] to be handled
 * @param block the actual handler of the [Result.Failure]
 * @return remaining [Result.Success]
 */
inline fun <V : Any?, E : Exception> Result<V, E>.handleFailure(block: (Result.Failure<E>) -> Nothing): V =
    when (this) {
        is Result.Success -> value
        is Result.Failure -> block(this)
    }

/**
 * A failure within the list is handled with the [block] operation, otherwise the value list of [Result.Success]
 * is returned.
 *
 * @receiver the list of [Result] to be handled
 * @param block the handler in case of an [Result.Failure]
 * @return the list of values of the [Result.Success]
 */
inline fun <V : Any?, E : Exception> Iterable<Result<V, E>>.handleFailure(block: (Result.Failure<E>) -> Nothing): List<V> =
    map {
        when (it) {
            is Result.Success -> it.value
            is Result.Failure -> block(it)
        }
    }

/**
 * Handle all [Result.Failure] with [block] and return only the values of [Result.Success].
 *
 * @receiver the list of [Result] to be handled
 * @param block the handler in case of an [Result.Failure]
 * @return the list of values of the [Result.Success]
 */
inline fun <V : Any?, E : Exception> Iterable<Result<V, E>>.handleAndRemoveFailure(
    block: (Result.Failure<E>) -> Unit
): List<V> =
    fold(emptyList()) { acc, result ->
        when (result) {
            is Result.Success -> acc + result.value
            is Result.Failure -> {
                block(result); acc
            }
        }
    }

/**
 * Handle all [Result.Failure] with [block] containing the index and exception and return
 * only the values of [Result.Success].
 *
 * @receiver the list of [Result] to be handled
 * @param block the handler in case of an [Result.Failure] with index information
 * @return the list of values of the [Result.Success]
 */
inline fun <V : Any?, E : Exception> Iterable<Result<V, E>>.handleAndRemoveFailureIndexed(
    block: (index: Int, Result.Failure<E>) -> Unit
): List<V> =
    foldIndexed(emptyList()) { index, acc, result ->
        when (result) {
            is Result.Success -> acc + result.value
            is Result.Failure -> {
                block(index, result); acc
            }
        }
    }

/**
 * Ignore and filter out all [Result.Failure].
 *
 * @receiver the list of [Result] to be handled
 * @return the list of values of the [Result.Success]
 */
fun <V : Any?, E : Exception> Iterable<Result<V, E>>.ignoreFailure(): List<V> =
    filterIsInstance<Result.Success<V>>().map { it.value }

/**
 * Returns the value of map[key] as [Result.Success], if it exists, or as [Result.Failure] otherwise.
 *
 * @receiver the map on which the [key] is requested
 * @param key requested key to be accessed
 * @return [Result] of the request
 */
fun <K : Any, V : Any?> Map<K, V>.getValueResult(key: K): Result<V, IllegalArgumentException> {
    val value = this[key] ?: return Result.error(IllegalArgumentException("Map does not contain requested key."))
    return Result.success(value)
}

/**
 * Returns the value of list[index] as [Result.Success], if it exists, or as [Result.Failure] otherwise.
 *
 * @receiver the map on which the [index] is requested
 * @param index requested key to be accessed
 * @return [Result] of the request
 */
fun <V : Any?> List<V>.getValueResult(index: Int): Result<V, IllegalArgumentException> {
    val value = this.getOrNull(index) ?: return Result.error(IllegalArgumentException("List does not contain index."))
    return Result.success(value)
}

/**
 * Map a list of [T] with [transform] and handle the [Result.Failure] with [failureHandler].
 * The [failureHandler] has access to the [Result.Failure] as well as to the original [T].
 *
 * @receiver the list to be mapped
 * @param transform mapping function from [T] to [Result]
 * @param failureHandler the handler of a [Result.Failure] with access to the original [T]
 * @return the list of values which have been successfully [Result.Success] transformed by [transform]
 */
inline fun <T, V : Any?, E : Exception> Iterable<T>.mapAndHandleFailureOnOriginal(
    transform: (T) -> Result<V, E>,
    failureHandler: (result: (Result.Failure<E>), original: T) -> Unit
): List<V> = fold(emptyList()) { acc, element ->
    when (val result = transform(element)) {
        is Result.Success -> acc + result.value
        is Result.Failure -> {
            failureHandler(result, element); acc
        }
    }
}

fun <V : Any?, E : Exception> Result<V, E>.toOption(): Option<V> = fold({ Some(it) }, { none() })
