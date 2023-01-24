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

package io.rtron.io.messages

class ContextMessageList<out V>(
    val value: V,
    val messageList: DefaultMessageList
) {
    // Properties and Initializers

    // Methods
    fun appendReport(other: DefaultMessageList): ContextMessageList<V> {
        this.messageList += other
        return this
    }

    fun handleMessageList(f: (DefaultMessageList) -> Unit): V {
        f(messageList)
        return value
    }

    fun <R> map(transform: (V) -> R): ContextMessageList<R> = ContextMessageList(transform(value), messageList)
}

fun <V : Any> List<ContextMessageList<V>>.mergeMessageLists(): ContextMessageList<List<V>> {
    val mergedMessageList = DefaultMessageList()
    this.forEach { mergedMessageList += it.messageList }
    return ContextMessageList(this.map { it.value }, mergedMessageList)
}

/**
 * Handles a list of reports with [block] and then returns only the list of [ContextMessageList.value].
 *
 * @receiver list of [ContextMessageList] to be handled
 * @param block the actual handler for the report message
 * @return remaining list of [ContextMessageList.value]
 */
inline fun <V : Any> List<ContextMessageList<V>>.handleMessageList(block: (ContextMessageList<V>) -> Unit): List<V> =
    map {
        block(it)
        it.value
    }
