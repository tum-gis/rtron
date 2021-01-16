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

package io.rtron.main.project

import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import io.rtron.io.scripting.ScriptLoader
import io.rtron.main.project.configuration.ProjectUserConfiguration
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.ReadWriteManager
import io.rtron.readerwriter.citygml.CitygmlReaderWriterConfiguration
import io.rtron.readerwriter.opendrive.OpendriveReaderWriterConfiguration
import io.rtron.transformer.opendrive2roadspaces.Opendrive2RoadspacesTransformer
import io.rtron.transformer.opendrive2roadspaces.parameter.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.roadspace2citygml.Roadspaces2CitygmlTransformer
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Manages and supervises a single transformation project that can comprise multiple transformation processing steps.
 *
 * @param configuration configuration of the project
 */
class ProjectTransformationManager(
    private val configuration: ProjectConfiguration
) {

    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(this.configuration.reportLoggerName, this.configuration.reportLoggingPath)

    private val _opendriveReaderWriterConfiguration = OpendriveReaderWriterConfiguration(configuration.projectId)
    private val _citygmlReaderWriterConfiguration = CitygmlReaderWriterConfiguration(configuration.projectId)
    private val _readWriteManager = ReadWriteManager.of(configuration.projectId, _opendriveReaderWriterConfiguration, _citygmlReaderWriterConfiguration)

    private val userConfiguration = loadConfig()

    private val _opendrive2RoadspacesConfiguration = Opendrive2RoadspacesConfiguration(configuration.projectId, configuration.sourceFileIdentifier, configuration.concurrentProcessing, userConfiguration.opendrive2RoadspacesParameters)
    private val _opendrive2RoadspacesTransformer = Opendrive2RoadspacesTransformer(_opendrive2RoadspacesConfiguration)
    private val _roadspaces2CitygmlConfiguration = Roadspaces2CitygmlConfiguration(configuration.projectId, configuration.sourceFileIdentifier, configuration.concurrentProcessing, userConfiguration.roadspaces2CitygmlParameters)
    private val _roadspaces2CitygmlTransformer = Roadspaces2CitygmlTransformer(_roadspaces2CitygmlConfiguration)

    // Methods

    /**
     * Executes the transformation of a project.
     */
    @OptIn(ExperimentalTime::class)
    fun transformFile() {

        _reportLogger.info("Starting transformation chain. 💪💪💪")

        val timeElapsed = measureTime {
            // read
            val opendriveModel = _readWriteManager.read(configuration.absoluteSourceFilePath) as OpendriveModel

            // transform
            val roadspacesModel = _opendrive2RoadspacesTransformer.transform(opendriveModel)
            val citygmlModel = _roadspaces2CitygmlTransformer.transform(roadspacesModel)

            // write
            configuration.outputDirectoryPath.createDirectory()
            _readWriteManager.write(citygmlModel, configuration.outputDirectoryPath)
        }

        _reportLogger.info("Completed transformation chain after $timeElapsed. ✔✔✔")
    }

    /**
     * Loads all applicable configurations in the project directory and then merges them.
     */
    private fun loadConfig(): ProjectUserConfiguration {
        val loadedConfigurations = getConfigurationFilePaths().map { ScriptLoader.load<ProjectUserConfiguration>(it) }
        return if (loadedConfigurations.isEmpty()) ProjectUserConfiguration() else
            loadedConfigurations.reduce { acc, projectConfig -> acc leftMerge projectConfig }
    }

    /**
     * Returns a list of file paths to the configuration scripts contained in the directory of the input file, but also
     * in parent directories, if they are part of the batch transformation project.
     */
    private fun getConfigurationFilePaths(): List<Path> {
        val directories = configuration.absoluteSourceFilePath.getParents(configuration.baseSourceFilePath)
        return directories.map { it.resolve(Path("configuration.kts")) }.filter { it.isRegularFile() }
    }
}
