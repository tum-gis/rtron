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

import arrow.core.Either
import io.rtron.io.files.FileIdentifier
import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import io.rtron.main.system.JavaVersion
import io.rtron.model.citygml.CitygmlModel
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.roadspaces.RoadspacesModel
import io.rtron.readerwriter.citygml.CitygmlWriter
import io.rtron.readerwriter.citygml.configuration.CitygmlWriterConfigurationBuilder
import io.rtron.readerwriter.opendrive.OpendriveReader
import io.rtron.readerwriter.opendrive.OpendriveReaderException
import io.rtron.readerwriter.opendrive.configuration.OpendriveReaderConfigurationBuilder
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesTransformer
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesTransformerException
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfigurationBuilder
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlTransformer
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfigurationBuilder
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class Project(val projectId: String, val inputFilePath: Path, val outputDirectoryPath: Path) {

    // Properties and Initializers
    init {
        outputDirectoryPath.createDirectory()
    }

    val inputFileIdentifier = FileIdentifier.of(inputFilePath)

    val logger = LogManager.getReportLogger(projectId)

    /** enable concurrent processing during the transformation of a model*/
    var concurrentProcessing: Boolean = false

    // Methods

    fun readOpendriveModel(filePath: Path, setup: OpendriveReaderConfigurationBuilder.() -> Unit = {}): Either<OpendriveReaderException, OpendriveModel> {
        val builder = OpendriveReaderConfigurationBuilder(projectId)
        val configuration = builder.apply(setup).build()
        return OpendriveReader(configuration).read(filePath)
    }

    fun transformOpendrive2Roadspaces(opendriveModel: OpendriveModel, setup: Opendrive2RoadspacesConfigurationBuilder.() -> Unit = {}): Either<Opendrive2RoadspacesTransformerException, RoadspacesModel> {
        val builder = Opendrive2RoadspacesConfigurationBuilder(projectId, inputFileIdentifier, concurrentProcessing)
        val configuration = builder.apply(setup).build()
        return Opendrive2RoadspacesTransformer(configuration).transform(opendriveModel)
    }

    fun transformRoadspaces2Citygml(roadspacesModel: RoadspacesModel, setup: Roadspaces2CitygmlConfigurationBuilder.() -> Unit = {}): CitygmlModel {
        val builder = Roadspaces2CitygmlConfigurationBuilder(projectId, inputFileIdentifier, concurrentProcessing)
        val configuration = builder.apply(setup).build()
        return Roadspaces2CitygmlTransformer(configuration).transform(roadspacesModel)
    }

    fun writeCitygmlModel(citygmlModel: CitygmlModel, setup: CitygmlWriterConfigurationBuilder.() -> Unit = {}) {
        val builder = CitygmlWriterConfigurationBuilder(projectId)
        val configuration = builder.apply(setup).build()
        CitygmlWriter(configuration).write(citygmlModel, outputDirectoryPath)
    }
}

/**
 * @param inInputDirectory path to the directory comprising the input models
 * @param withExtension only process files with this extension
 * @param toOutputDirectory path to the directory where new models and files are written to
 */
fun processAllFiles(inInputDirectory: String, withExtension: String, toOutputDirectory: String = inInputDirectory + "_output", recursive: Boolean = true, setup: Project.() -> Unit) =
    processAllFiles(inInputDirectory, setOf(withExtension), toOutputDirectory, recursive, setup)

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
fun processAllFiles(inInputDirectory: String, withExtensions: Set<String>, toOutputDirectory: String = inInputDirectory + "_output", recursive: Boolean = true, process: Project.() -> Unit) {
    val generalLogger = LogManager.getReportLogger("general")
    if (!JavaVersion.CURRENT.isAtLeast(11, 0)) {
        generalLogger.error("Requiring a Java version of at least 11 (current Java version is ${JavaVersion.CURRENT}).")
        return
    }

    val inputDirectoryPath = Path(inInputDirectory)
    val outputDirectoryPath = Path(toOutputDirectory)
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
            val project = Project(projectId, currentPath, projectOutputDirectoryPath)
            project.apply(process)
        }

        reportLogger.info("Completed project after $timeElapsed. ✔✔✔" + System.lineSeparator())
    }
}
