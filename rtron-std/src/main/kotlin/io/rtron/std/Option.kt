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

/**
 * Returns true, if the value of this equals the [otherValue].
 * If no value [isDefined], false is returned.
 */
infix fun <T> Option<T>.equalsValue(otherValue: T): Boolean = handleEmpty { return false } == otherValue

/** Returns a list of values of type [T], whereby the empty [Option] are ignored. */
fun <T> List<Option<T>>.unwrapValues(): List<T> = filter { it.isDefined() }.map { it.orNull()!! }

/** Execute [f] on the value of type [T], if present. */
inline fun <T : Any?> Option<T>.present(f: (T) -> Unit) { if (isDefined()) f(orNull()!!) }

/** Handle the none() of [Option] with [block] and return the [V]. */
inline fun <V : Any?> Option<V>.handleEmpty(block: (Option<V>) -> Nothing): V =
    if (isDefined()) orNull()!! else block(this)

fun <T> Option<T>.getResult(): Either<IllegalStateException, T> =
    if (isDefined()) Either.Right(orNull()!!) else Either.Left(IllegalStateException(""))
