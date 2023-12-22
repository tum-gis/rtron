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

package io.rtron.io.issues

class ContextIssueList<out V>(
    val value: V,
    val issueList: DefaultIssueList
) {
    // Properties and Initializers

    // Methods
    fun appendReport(other: DefaultIssueList): ContextIssueList<V> {
        this.issueList += other
        return this
    }

    fun handleIssueList(f: (DefaultIssueList) -> Unit): V {
        f(issueList)
        return value
    }

    fun <R> map(transform: (V) -> R): ContextIssueList<R> = ContextIssueList(transform(value), issueList)
}

fun <V : Any> List<ContextIssueList<V>>.mergeIssueLists(): ContextIssueList<List<V>> {
    val mergedIssueList = DefaultIssueList()
    this.forEach { mergedIssueList += it.issueList }
    return ContextIssueList(this.map { it.value }, mergedIssueList)
}

/**
 * Handles a list of reports with [block] and then returns only the list of [ContextIssueList.value].
 *
 * @receiver list of [ContextIssueList] to be handled
 * @param block the actual handler for the report message
 * @return remaining list of [ContextIssueList.value]
 */
inline fun <V : Any> List<ContextIssueList<V>>.handleIssueList(block: (ContextIssueList<V>) -> Unit): List<V> =
    map {
        block(it)
        it.value
    }
