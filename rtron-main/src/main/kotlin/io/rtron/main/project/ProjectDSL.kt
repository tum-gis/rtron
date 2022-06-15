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

package io.rtron.main.project

import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class Project(val projectConfiguration: ProjectConfiguration) {

    // Properties and Initializers
    init {
        projectConfiguration.outputDirectoryPath.createDirectory()
    }

    val logger = LogManager.getReportLogger(projectConfiguration.projectId)
}

/**
 * @param inInputDirectory path to the directory comprising the input models
 * @param withExtension only process files with this extension
 * @param toOutputDirectory path to the directory where new models and files are written to
 */
fun processAllFiles(inputDirectoryPath: Path, withExtension: String, outputDirectoryPath: Path, recursive: Boolean = true, setup: Project.() -> Unit) =
    processAllFiles(inputDirectoryPath, setOf(withExtension), outputDirectoryPath, recursive, setup)

/**
 * Iterates over all files contained in the [inInputDirectory] and having an extension contained in [withExtensions].
 * The [process] is executed on each of those input files.
 *
 * @param inInputDirectory path to the directory comprising the input models
 * @param withExtensions only process files with these extensions
 * @param toOutputDirectory path to the directory where new models and files are written to
 * @param recursive iterates recursively over the directory
 * @param process user defined process to be executed
 */
@OptIn(ExperimentalTime::class)
fun processAllFiles(inputDirectoryPath: Path, withExtensions: Set<String>, outputDirectoryPath: Path, recursive: Boolean = true, process: Project.() -> Unit) {
    val generalLogger = LogManager.getReportLogger("general")

    if (!inputDirectoryPath.isDirectory()) {
        generalLogger.error("Provided directory does not exist: $inputDirectoryPath")
        return
    }
    if (withExtensions.isEmpty()) {
        generalLogger.error("No extensions have been provided.")
        return
    }
    if (outputDirectoryPath.isRegularFile()) {
        generalLogger.error("Output directory must not be a file: $outputDirectoryPath")
        return
    }

    val recursiveDepth = if (recursive) Int.MAX_VALUE else 1

    val inputFilePaths = inputDirectoryPath
        .walk(recursiveDepth)
        .filter { it.extension in withExtensions }
        .toList()
        .sorted()

    if (inputFilePaths.isEmpty()) {
        generalLogger.error("No files have been found with $withExtensions as extension in input directory: $outputDirectoryPath")
        return
    }

    val totalNumber = inputFilePaths.size

    inputFilePaths.forEachIndexed { index, currentPath ->
        val inputFileRelativePath = inputDirectoryPath.relativize(currentPath)
        val projectId = inputFileRelativePath.toString()
        val projectOutputDirectoryPath = outputDirectoryPath.resolve(inputFileRelativePath)

        val reportLogger = LogManager.getReportLogger(projectId)
        reportLogger.info("Starting project (${index + 1}/$totalNumber) 💪💪💪")

        val timeElapsed = measureTime {
            val projectConfiguration = ProjectConfiguration(projectId, currentPath, projectOutputDirectoryPath)
            val project = Project(projectConfiguration)
            project.apply(process)
        }

        reportLogger.info("Completed project after $timeElapsed. ✔✔✔" + System.lineSeparator())
    }
}
