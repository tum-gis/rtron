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

package io.rtron.io.files

import java.nio.file.Files
import java.io.BufferedReader as JBufferedReader

/**
 * Reads text from a character stream.
 *
 * @param filePath path to the file to be read
 */
class BufferedReader(
        val filePath: Path
) {

    // Properties and Initializers
    private val _bufferedReader: JBufferedReader = Files.newBufferedReader(filePath.toPathN())!!

    // Conversions
    /**
     * Returns adapted [java.io.BufferedReader].
     *
     * @return adapted [java.io.BufferedReader]
     */
    fun toBufferedReaderJ(): JBufferedReader = this._bufferedReader
}
