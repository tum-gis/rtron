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

fun <V> Iterable<ContextReport<V>>.sequenceContextReport(): ContextReport<List<V>> {
    val values = map { it.value }
    val reports = map { it.report }.merge()
    return ContextReport(values, reports)
}

class ContextReport<out V>(
    val value: V,
    val report: Report
) {
    // Properties and Initializers

    // Methods
    fun <R> map(transform: (V) -> R): ContextReport<R> = ContextReport(transform(value), report)
}
