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

package io.rtron.io.report

class ContextReport<out V>(
    val value: V,
    val report: Report
) {
    // Properties and Initializers

    // Methods
    fun appendReport(other: Report): ContextReport<V> {
        this.report += other
        return this
    }

    fun handleReport(f: (Report) -> Unit): V {
        f(report)
        return value
    }

    fun <R> map(transform: (V) -> R): ContextReport<R> = ContextReport(transform(value), report)
}

fun <V : Any> List<ContextReport<V>>.mergeReports(): ContextReport<List<V>> {
    val mergedReport = Report()
    this.forEach { mergedReport += it.report }
    return ContextReport(this.map { it.value }, mergedReport)
}

/**
 * Handles a list of reports with [block] and then returns only the list of [ContextReport.value].
 *
 * @receiver list of [ContextReport] to be handled
 * @param block the actual handler for the report message
 * @return remaining list of [ContextReport.value]
 */
inline fun <V : Any> List<ContextReport<V>>.handleReport(block: (ContextReport<V>) -> Unit): List<V> =
    map {
        block(it)
        it.value
    }
