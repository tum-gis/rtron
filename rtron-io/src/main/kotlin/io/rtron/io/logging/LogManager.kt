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

package io.rtron.io.logging

import mu.KotlinLogging
import java.nio.file.Path

/**
 * LogManager creates and parametrizes the [Logger] instances.
 */
object LogManager {

    // Properties and Initializers
    private val loggers = mutableMapOf<String, Logger>()

    // Methods

    /**
     * Initializes and returns a new logger.
     *
     * @param name the name or id of the logger
     * @param logFilePath the path to the log file to be created
     */
    fun getReportLogger(name: String, logFilePath: Path): Logger {
        val logger = getReportLogger(name)
        // (logger.toL4JLogger() as L4JCoreLogger).addAppender(getAppender(name, logFilePath))

        return logger
    }

    /**
     * Returns a [Logger] for a specific name. It will remember the log file path according to the
     * previously initialized logger with the same [name].
     *
     * @param name if a logger with the same name has already be initialized
     */
    fun getReportLogger(name: String) = loggers.getOrPut(name) { Logger(KotlinLogging.logger(name)) }
}
