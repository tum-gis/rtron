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

import com.github.kittinunf.result.Result
import com.vdurmont.emoji.EmojiParser
import org.apache.commons.lang3.SystemUtils
import org.apache.logging.log4j.LogManager
import io.rtron.std.ContextMessage
import org.apache.logging.log4j.Logger as L4JLogger


/**
 * Logger for operation messages. Building of [Logger]s is provided by [LogManager].
 *
 * @param logger adapted [org.apache.logging.log4j.Logger] logger.
 */
class Logger(
        private val logger: L4JLogger = LogManager.getLogger()
) {

    // Properties and Initializers
    private val _infoOnceMessages: MutableSet<String> = mutableSetOf()

    // Methods
    /**
     * Log an info message only once. Later log requests are ignored.
     *
     * @param message info message to be logged once
     */
    fun infoOnce(message: String) {
        if (message !in _infoOnceMessages) {
            _infoOnceMessages.add(message)
            info(message)
        }
    }

    /**
     * Log one info [message] with a [prefix] and [suffix].
     */
    fun info(message: String, prefix: String = "", suffix: String = "") {
        info(combineMessage(message, prefix, suffix))
    }

    /**
     * Log a list of info [messages] with a [prefix] and [suffix].
     */
    fun info(messages: List<String>, prefix: String = "", suffix: String = "") {
        info(combineMessage(messages, prefix, suffix))
    }

    /**
     * Start a new logging paragraph.
     */
    fun infoParagraph() {
        println()
    }

    /**
     * Log one warn [message] with a [prefix] and [suffix].
     */
    fun warn(message: String, prefix: String = "", suffix: String = "") {
        warn(combineMessage(message, prefix, suffix))
    }

    /**
     * Log a list of warn [messages] with a [prefix] and [suffix].
     */
    fun warn(messages: List<String>, prefix: String = "", suffix: String = "") {
        warn(combineMessage(messages, prefix, suffix))
    }

    /**
     * Log out the exception of a [failure].
     *
     * @param failure failure message to be logged
     */
    fun log(failure: Result.Failure<Exception>, prefix: String = "", suffix: String = "") {
        log(failure.getException(), prefix, suffix)
    }

    /**
     * Log out an [exception] message as a warning.
     *
     * @param exception exception message to be logged
     */
    fun log(exception: Exception, prefix: String = "", suffix: String = "") {
        val warningMessage = if (exception.message.isNullOrEmpty()) exception.toString() else exception.message!!
        warn(warningMessage, prefix, suffix)
    }

    /**
     * Log the messages of a [ContextMessage].
     *
     * @param contextMessage message to be logged
     */
    fun log(contextMessage: ContextMessage<Any>, prefix: String = "", suffix: String = "") {
        info(contextMessage.messages, prefix, suffix)
    }

    /**
     * Log the messages of a [ContextMessage] within a [Result].
     *
     * @param result contains the message to be logged
     */
    fun log(result: Result<ContextMessage<Any>, Exception>, prefix: String = "", suffix: String = "") {
        result.fold({ log(it, prefix, suffix) }, { log(it, prefix, suffix) })
    }

    private fun combineMessage(messages: List<String>, prefix: String, suffix: String) =
            combineMessage(messages.joinToString(), prefix, suffix)

    private fun combineMessage(message: String, prefix: String, suffix: String): String {
        if (message.isEmpty()) return ""

        val prefixedMessage = if (prefix.isEmpty()) message else "$prefix: $message"
        return if (suffix.isEmpty()) prefixedMessage else "$prefixedMessage $suffix"
    }

    private fun warn(message: String, force: Boolean = false) {
        if (message.isNotEmpty() || force) logger.warn(message)
    }

    private fun info(message: String, force: Boolean = false) {
        if (message.isNotEmpty() || force) logger.info(prepareMessage(message))
    }

    private fun prepareMessage(message: String): String {
        return if(SystemUtils.IS_OS_WINDOWS) EmojiParser.removeAllEmojis(message).trim()
        else message
    }

    // Conversions
    /**
     * Reveals the adapted logger.
     */
    fun toL4JLogger() = logger
}
