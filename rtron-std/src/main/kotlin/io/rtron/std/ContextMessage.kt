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

/**
 * A class for adding context [messages] to an object of type [V].
 *
 * @param value actual object to be enriched by [messages]
 * @param messages the actual list of messages to be added to the object
 */
class ContextMessage<out V>(
    val value: V,
    messages: List<String> = emptyList()
) {

    // Properties and Initializers
    val messages = messages.filter { it.isNotBlank() }

    // Secondary Constructors
    constructor(value: V, message: String) : this(value, listOf(message))

    // Methods
    fun isEmpty() = messages.isEmpty()
    fun isNotEmpty() = messages.isNotEmpty()

    fun appendMessages(messages: List<String>) = ContextMessage(this.value, this.messages + messages)
    fun appendMessages(message: String) = ContextMessage(this.value, this.messages + message)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContextMessage<*>) return false

        if (value != other.value) return false
        if (messages != other.messages) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + messages.hashCode()
        return result
    }
}

/**
 * Handle a message with [block] and then return only the [ContextMessage.value].
 *
 * @receiver [ContextMessage] to be handled
 * @param block the actual handler for the context message
 * @return only the remaining [ContextMessage.value]
 */
inline fun <V : Any> ContextMessage<V>.handleMessage(block: (List<String>) -> Unit): V {
    block(this.messages)
    return this.value
}

/**
 * Handles a list of messages with [block] and then returns only the list of [ContextMessage.value].
 *
 * @receiver list of [ContextMessage] to be handled
 * @param block the actual handler for the context message
 * @return remaining list of [ContextMessage.value]
 */
inline fun <V : Any> List<ContextMessage<V>>.handleMessage(block: (ContextMessage<V>) -> Unit): List<V> =
    map {
        block(it)
        it.value
    }

/**
 * Unwraps a list of [ContextMessage] to a single [ContextMessage] containing the list of values and messages.
 *
 * @receiver list of [ContextMessage] to be handled
 * @return a [ContextMessage] containing all values and messages of the original list
 */
fun <V : Any> List<ContextMessage<V>>.unwrapMessages() = ContextMessage(map { it.value }, flatMap { it.messages })

inline fun <V : Any, R : Any> ContextMessage<V>.map(transform: (V) -> R): ContextMessage<R> =
    ContextMessage(transform(this.value), this.messages)
