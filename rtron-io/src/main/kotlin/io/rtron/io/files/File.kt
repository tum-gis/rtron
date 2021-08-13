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

package io.rtron.io.files

import io.rtron.std.date.DateTime
import io.rtron.std.date.ExperimentalDateTime
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import com.google.common.hash.Hashing as GHashing
import com.google.common.io.Files as GFiles
import java.io.File as JFile

/**
 * Representation of a file or directory.
 *
 * @param path path to the file or directory
 */
class File(
    val path: Path
) {

    // Properties and Initializers
    private val _file = JFile(path.toString())

    /**
     * Name of the file or directory with potential file extension.
     */
    val name: String = _file.name

    /**
     * Name of file or directory without extension.
     */
    val nameWithoutExtension: String = _file.nameWithoutExtension

    /**
     * Extension of the file, in case it has one.
     */
    val extension: String = _file.extension

    /**
     * SHA256 hash of the file or directory.
     */
    val hashSha256 by lazy {
        try {
            GFiles.asByteSource(_file).hash(GHashing.sha256()).toString()
        } catch (e: Exception) {
            ""
        }
    }

    private val attributes by lazy { Files.readAttributes(path.toPathN(), BasicFileAttributes::class.java) }

    /**
     * Time of file or directory creation.
     */
    @OptIn(ExperimentalDateTime::class)
    val creationTime by lazy { DateTime.of(attributes.creationTime()) }

    /**
     * Time of file or directory last access.
     */
    @OptIn(ExperimentalDateTime::class)
    val lastAccessTime by lazy { DateTime.of(attributes.lastAccessTime()) }

    /**
     * Time of file or directory last modification.
     */
    @OptIn(ExperimentalDateTime::class)
    val lastModified by lazy { DateTime.of(attributes.lastModifiedTime()) }

    // Methods
    /**
     * Reads and returns the text of the file.
     *
     * @return text of the file
     */
    fun readText(): String = _file.readText()
}
