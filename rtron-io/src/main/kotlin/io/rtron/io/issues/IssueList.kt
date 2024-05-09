/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class IssueList<T>(private val issues: MutableList<T> = mutableListOf()) {
    // Properties
    val size: Int
        get() = issues.size

    // Operators
    operator fun plusAssign(other: IssueList<T>) {
        append(other.issues)
    }

    operator fun plusAssign(other: T) {
        append(other)
    }

    // Methods
    fun getIssues(): List<T> = issues

    fun isEmpty(): Boolean = issues.isEmpty()

    fun isNotEmpty(): Boolean = issues.isNotEmpty()

    fun append(issues: T) {
        this.issues += issues
    }

    fun append(issues: List<T>) {
        this.issues += issues
    }

    companion object {
        fun <T> of(issues: List<T>): IssueList<T> = IssueList(issues as MutableList<T>)

        fun <T> of(issue: T): IssueList<T> = IssueList(mutableListOf(issue))
    }
}

fun <T> List<T>.mergeToReport(): IssueList<T> = IssueList.of(this)

fun <T> List<IssueList<T>>.merge(): IssueList<T> = IssueList.of(flatMap { it.getIssues() })
