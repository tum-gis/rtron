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
import io.rtron.io.logging.LogManager

/**
 * Configuration of the batch processing layer.
 *
 * @param inputPath path to the directory comprising input models
 * @param outputPath path to the directory where new models and files are written to
 * @param recursive true, if the input models shall be searched recursively in the input directory
 * @param concurrentProcessing enable concurrent processing during the transformation of a model
 * @param clean delete output directory content before transformation starts
 */
class BatchConfiguration(
        val inputPath: Path,
        val outputPath: Path,
        private val recursive: Boolean = true,
        val concurrentProcessing: Boolean = false,
        val clean: Boolean = false
) {

    // Properties and Initializers
    init {
        require(!outputPath.isRegularFile()) { "Output path must not be an existing file." }
    }
    val inputDirectoryPath = if (inputPath.isRegularFile()) inputPath.parent else inputPath
    private val reportLoggerName = "general"
    private val reportLoggingPath: Path = outputPath.resolve(Path("$reportLoggerName.log"))
    /** recursive search depth for input models within the input directory */
    val recursiveDepth get() = if (recursive) Int.MAX_VALUE else 1

    // Methods
    /**
     * Returns the logger for the general batch processing layer.
     *
     * @return logger for the batch processing layer
     */
    fun getReportLogger() = LogManager.getReportLogger(reportLoggerName, reportLoggingPath)
}
