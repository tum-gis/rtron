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

package io.rtron.io.messages

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class MessageList<T>(private val messages: MutableList<T> = mutableListOf()) {

    // Properties
    val size: Int
        get() = messages.size

    // Operators
    operator fun plusAssign(other: MessageList<T>) {
        append(other.messages)
    }

    operator fun plusAssign(other: T) {
        append(other)
    }

    // Methods
    fun getMessages(): List<T> = messages

    fun isEmpty(): Boolean = messages.isEmpty()
    fun isNotEmpty(): Boolean = messages.isNotEmpty()

    fun append(message: T) { this.messages += message }
    fun append(messages: List<T>) { this.messages += messages }

    companion object {

        fun <T> of(messages: List<T>): MessageList<T> = MessageList(messages as MutableList<T>)
        fun <T> of(message: T): MessageList<T> = MessageList(mutableListOf(message))
    }
}

fun <T> List<T>.mergeToReport(): MessageList<T> = MessageList.of(this)
fun <T> List<MessageList<T>>.merge(): MessageList<T> = MessageList.of(flatMap { it.getMessages() })
