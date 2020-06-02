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

package io.rtron.io.logging

import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.RollingFileAppender
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy
import org.apache.logging.log4j.core.layout.PatternLayout
import io.rtron.io.files.Path
import org.apache.logging.log4j.LogManager as L4JLogManager
import org.apache.logging.log4j.core.Logger as L4JCoreLogger


/**
 * LogManager creates and parametrizes the [Logger] instances.
 */
object LogManager {

    // Properties and Initializers
    init {
        // disable logging of libraries
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "OFF")
    }
    private val loggerContext = LoggerContext.getContext(false)!!
    private val loggerConfiguration = loggerContext.configuration!!

    // Methods

    /**
     * Initializes and returns a new logger.
     *
     * @param name the name or id of the logger
     * @param logFilePath the path to the log file to be created
     */
    fun getReportLogger(name: String, logFilePath: Path): Logger {
        val logger = getReportLogger(name)
        (logger.toL4JLogger() as L4JCoreLogger).addAppender(getAppender(name, logFilePath))

        return logger
    }

    /**
     * Returns a [Logger] for a specific name. It will remember the log file path according to the
     * previously initialized logger with the same [name].
     *
     * @param name if a logger with the same name has already be initialized
     */
    fun getReportLogger(name: String) = Logger(L4JLogManager.getLogger(name))


    private fun getAppender(name: String, logFilePath: Path): Appender {
        val layout = PatternLayout.newBuilder()
                .withConfiguration(loggerConfiguration)
                .withPattern("%d{ISO8601} %-5level %msg%n")
                .build()

        val appender = (RollingFileAppender.newBuilder<RollingFileAppenderBuilder>()
                as RollingFileAppender.Builder<RollingFileAppenderBuilder>).apply {
            setConfiguration(loggerConfiguration)
            setName(name)
            withFileName(logFilePath.toString())
            setLayout(layout)
            withFilePattern("build/logs/rtron-%d{MM-dd-yyyy}.log.gz")
            withPolicy(SizeBasedTriggeringPolicy.createPolicy("10KB"))
            withPolicy(TimeBasedTriggeringPolicy.newBuilder().withInterval(1).build())
        }.build()

        return appender.apply { start() }
    }

}

/**
 * Necessary workaround for adding log4j appenders in Kotlin.
 * See [stackoverflow](https://stackoverflow.com/a/50565929) for the workaround.
 */
private class RollingFileAppenderBuilder : RollingFileAppender.Builder<RollingFileAppenderBuilder>()
