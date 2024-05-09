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

package io.rtron.std.date

import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneId

@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalDateTime

/**
 * Representation of a point in time.
 *
 * @param localDateTime adapting class of [java.time.LocalDateTime]
 */
@ExperimentalDateTime
class DateTime(
    private val localDateTime: LocalDateTime,
) {
    // Properties and Initializers

    companion object {
        fun of(text: String) = DateTime(LocalDateTime.parse(text))

        fun of(text: FileTime): DateTime {
            val ldt = LocalDateTime.ofInstant(text.toInstant(), ZoneId.systemDefault())!!
            return DateTime(ldt)
        }
    }
}
