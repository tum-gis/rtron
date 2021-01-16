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

package io.rtron.main.batch

import io.rtron.io.files.Path
import io.rtron.main.project.ProjectConfiguration
import io.rtron.main.project.ProjectTransformationManager
import io.rtron.readerwriter.ReadWriteManager
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Manages and supervises the transformations of all models contained in the [BatchConfiguration].
 *
 * @param configuration configuration for the batch processing
 */
class BatchTransformationManager(
    private val configuration: BatchConfiguration
) {

    // Properties and Initializers
    private val _reportLogger by lazy { configuration.getReportLogger() }

    private val _projectConfigurations = getProcessablePaths().map {
        val outputDirectoryPath = this.configuration.outputPath.resolve(it.withoutExtension())
        ProjectConfiguration(configuration.inputDirectoryPath, it, outputDirectoryPath, configuration.concurrentProcessing)
    }

    // Methods
    /**
     * Execution of the batch transformation defined within the [configuration].
     */
    @OptIn(ExperimentalTime::class)
    fun run() {
        if (configuration.inputPath == configuration.outputPath) {
            _reportLogger.error("Output path must be different to input path.")
            return
        }

        if (configuration.clean) configuration.outputPath.deleteDirectoryContents()
        _reportLogger.info("Starting batch transformations.")

        val timeElapsed = measureTime { _projectConfigurations.forEach { transformFile(it) } }
        _reportLogger.info("Transformation completed in $timeElapsed.")
    }

    /**
     * Transforms a single file and is therefore the transition to the model transformation layer.
     *
     * @param projectConfiguration configuration for the single transformation project
     */
    private fun transformFile(projectConfiguration: ProjectConfiguration) {
        val projectTransformationManager = ProjectTransformationManager(projectConfiguration)
        projectTransformationManager.transformFile()
        _reportLogger.infoParagraph()
    }

    /**
     * Returns a list of all valid input model paths contained in the input path of the [configuration].
     *
     * @return list of paths to supported models within the input path
     */
    private fun getProcessablePaths(): List<Path> =
        when {
            this.configuration.inputPath.isRegularFile() -> listOf(this.configuration.inputPath.fileName)
            this.configuration.inputPath.isDirectory() ->
                this.configuration.inputPath
                    .walk(configuration.recursiveDepth)
                    .filter { it.extension in ReadWriteManager.supportedFileExtensions }
                    .toList()
                    .sorted()
                    .map { this.configuration.inputPath.relativize(it) }
            else -> emptyList()
        }
}
