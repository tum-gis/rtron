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

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.nio.file.Files
import kotlin.streams.asSequence
import java.io.File as JFile
import java.nio.file.Path as NPath
import java.nio.file.Paths as NPaths

fun NPath.toPath() = Path(this.toString())

/**
 * Representation of a path.
 *
 * @param uri [uniform resource identifier](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier) to the file or directory
 */
class Path(uri: String = "") : Comparable<Path> {

    // Properties and Initializers
    private val _path: NPath = NPaths.get(uri)
    val fileName by lazy { _path.fileName.toPath() }
    val fileNameWithoutExtension by lazy { withoutExtension().fileName }
    val parent: Path by lazy { _path.parent.toPath() }
    val extension: String by lazy { FilenameUtils.getExtension(this.toString()) }

    // Methods

    /** Returns true, if the file or directory exists. */
    fun exists() = Files.exists(_path)

    /** Returns true, if the path represents a regular file. */
    fun isRegularFile() = Files.isRegularFile(_path)

    /** Returns true, if the path represents a directory. */
    fun isDirectory() = Files.isDirectory(_path)

    /**
     * Returns a sequence of subpaths with [maxDepth] by walking the file tree.
     *
     * @param maxDepth maximal depth to be traversed
     * @return sequence of [Path]
     */
    fun walk(maxDepth: Int = Int.MAX_VALUE): Sequence<Path> =
        Files.walk(_path, maxDepth).asSequence().map { it.toPath() }

    /**
     * Resolves the given path against this path.
     *
     * @param other the path to resolve against this path
     * @return resolved path
     */
    fun resolve(other: Path) = _path.resolve(other.toPathN()).toPath()

    /**
     * Returns this path without extension.
     *
     * @return path without extension.
     */
    fun withoutExtension() = Path(FilenameUtils.removeExtension(this.toString()))

    /**
     * Constructs a relative path between this path and the [other] given path.
     *
     * @param other other given path
     * @return relative path between this path and the [other] path
     */
    fun relativize(other: Path) = _path.relativize(other.toPathN()).toPath()

    /**
     * Returns a path with updated [fileName].
     */
    fun updateFileName(fileName: String): Path = this.parent.resolve(Path(fileName))

    /**
     * Creates a directory for this path.
     */
    fun createDirectory() {
        Files.createDirectories(_path)
    }

    /**
     * Deletes the directory of this path.
     */
    fun deleteDirectory() {
        FileUtils.deleteDirectory(toFileJ())
    }

    /**
     * Cleans the content of the directory, but does not delete it.
     */
    fun deleteDirectoryContents() {
        if (exists()) FileUtils.cleanDirectory(toFileJ())
    }

    /**
     * Returns true if this path is a child of the [other] path.
     */
    infix fun isChildOf(other: Path): Boolean = toPathN().startsWith(other.toPathN())

    /**
     * Returns all parent paths as a list until the [baseParent] is matched.
     *
     * @param baseParent limits the list of parents until [baseParent] is met
     * @return list of parent paths of this path
     */
    fun getParents(baseParent: Path): List<Path> {
        require(this isChildOf baseParent) { "Base path must be a parent path." }
        return generateSequence(this.parent) { it.parent }.takeWhile { it isChildOf baseParent }.toList()
    }

    /**
     * Returns the size of a file or directory.
     *
     * @return the length of the file, or recursive size of the directory, provided (in bytes)
     */
    fun getFileSize(): Long = FileUtils.sizeOf(toFileJ())

    /**
     * Returns a human-readable file size like 1MB or 1GB.
     *
     * @return human-readable size of a file
     */
    fun getFileSizeToDisplay(): String = FileUtils.byteCountToDisplaySize(getFileSize())

    override fun compareTo(other: Path): Int = this._path.compareTo(other._path)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Path) return false

        if (_path != other._path) return false

        return true
    }

    override fun hashCode(): Int {
        return _path.hashCode()
    }

    // Conversions
    fun toPathN() = _path
    override fun toString() = _path.toString()

    fun toFileJ(): JFile = _path.toFile()
    fun toFile() = File(this)
}
