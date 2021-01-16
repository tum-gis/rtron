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

import io.rtron.io.files.FileIdentifier
import io.rtron.io.files.Path

/**
 * Configuration for a single transformation project of one source model.
 *
 * @param baseSourceFilePath the absolute base path of the source directory
 * @param relativeSourceFilePath the relative path to the actual file of the source model
 * @param outputDirectoryPath the absolute base path of the output directory
 * @param concurrentProcessing enable concurrent processing during the transformation of a model
 */
class ProjectConfiguration(
    val baseSourceFilePath: Path,
    val relativeSourceFilePath: Path,
    val outputDirectoryPath: Path,
    val concurrentProcessing: Boolean
) {
    // Properties and Initializers

    /** unique id of the transformation project */
    val projectId: String = relativeSourceFilePath.withoutExtension().toString()
    /** absolute file path to the source file of the model */
    val absoluteSourceFilePath = baseSourceFilePath.resolve(relativeSourceFilePath)
    /** identifier of the source file */
    val sourceFileIdentifier = FileIdentifier.of(absoluteSourceFilePath)

    /** name of the report logger for the transformation project */
    val reportLoggerName: String = projectId
    /** output path for the report logger of the transformation project */
    val reportLoggingPath: Path = outputDirectoryPath.resolve(Path("report.log"))
}
