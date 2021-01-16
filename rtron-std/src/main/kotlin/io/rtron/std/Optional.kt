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

import com.github.kittinunf.result.Result

/**
 * A container object which optionally contains a [value] of type [T].
 *
 * @param value if null, the container is considered empty
 */
data class Optional<T>(
    private val value: T?
) {

    // Operators

    /**
     * Returns true, if the [value] of this equals the [otherValue].
     * If no value [isPresent], false is returned.
     */
    infix fun equalsValue(otherValue: T): Boolean = getResult().handleFailure { return false } == otherValue

    // Methods

    /**
     * Returns true, if no value is present.
     */
    fun isEmpty(): Boolean = value == null

    /**
     * Returns true, if a value is present.
     */
    fun isPresent(): Boolean = value != null

    /**
     * Returns a value, if available; otherwise null is returned.
     */
    fun getOrNull(): T? = value

    /**
     * Returns [Result.success] of the [value], if available; otherwise [Result.error] is returned.
     */
    fun getResult(): Result<T, IllegalStateException> =
        if (isPresent()) Result.success(value!!) else Result.error(IllegalStateException(""))

    // Conversions
    override fun toString() = if (value != null) "Optional[$value]" else "Optional.empty"

    fun toList(): List<T> = if (value != null) listOf(value) else emptyList()

    companion object {
        fun <T> empty() = Optional<T>(null)

        fun <T> of(result: Result<T, Exception>): Optional<T> = result.fold({ Optional(it) }, { empty() })
    }
}

inline fun <T, R> Optional<T>.map(transform: (T) -> R): Optional<R> =
    if (isEmpty()) Optional.empty()
    else Optional(transform(this.getOrNull()!!))

infix fun <T> Optional<T>.getOrElse(defaultValue: T): T = getOrNull() ?: defaultValue

/**
 * Returns a list of values of type [T], whereby the empty [Optional] are ignored.
 */
fun <T> List<Optional<T>>.unwrapValues(): List<T> = filter { it.isPresent() }.map { it.getOrNull()!! }
