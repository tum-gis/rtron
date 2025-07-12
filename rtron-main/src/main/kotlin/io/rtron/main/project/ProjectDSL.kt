/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.main.project

import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.io.files.walk
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Iterates over all files contained in the [inInputDirectory] and having an extension contained in [withFilenameEndings].
 * The [process] is executed on each of those input files.
 *
 * @param inputDirectoryPath path to the directory comprising the input models
 * @param withFilenameEndings only process files with these extensions
 * @param outputDirectoryPath path to the directory where new models and files are written to
 * @param recursive iterates recursively over the directory
 * @param process user defined process to be executed
 */
@OptIn(ExperimentalTime::class)
fun processAllFiles(
    inputDirectoryPath: Path,
    withFilenameEndings: Set<String>,
    outputDirectoryPath: Path,
    recursive: Boolean = true,
    process: Project.() -> Unit,
) {
    val logger = KotlinLogging.logger {}

    if (!inputDirectoryPath.isDirectory()) {
        logger.error { "Provided directory does not exist: $inputDirectoryPath" }
        return
    }
    if (withFilenameEndings.isEmpty()) {
        logger.error { "No extensions have been provided." }
        return
    }
    if (outputDirectoryPath.isRegularFile()) {
        logger.error { "Output directory must not be a file: $outputDirectoryPath" }
        return
    }

    val recursiveDepth = if (recursive) Int.MAX_VALUE else 1

    val inputFilePaths =
        inputDirectoryPath
            .walk(recursiveDepth)
            .filter { path -> withFilenameEndings.any { path.fileName.toString().endsWith(it) } }
            .toList()
            .sorted()

    if (inputFilePaths.isEmpty()) {
        logger.error { "No files have been found with $withFilenameEndings as extension in input directory: $outputDirectoryPath" }
        return
    }

    val totalNumber = inputFilePaths.size

    inputFilePaths.forEachIndexed { index, currentPath ->
        val inputFileRelativePath = inputDirectoryPath.relativize(currentPath)
        val projectOutputDirectoryPath = outputDirectoryPath.resolve(inputFileRelativePath)

        logger.info { "Starting project (${index + 1}/$totalNumber): $inputFileRelativePath" }

        val timeElapsed =
            measureTime {
                val project = Project(currentPath, projectOutputDirectoryPath)
                project.apply(process)
            }

        logger.info { "Completed project after $timeElapsed." + System.lineSeparator() }
    }
}
