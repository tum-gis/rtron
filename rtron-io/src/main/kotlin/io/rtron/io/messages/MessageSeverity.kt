package io.rtron.io.messages

import kotlinx.serialization.Serializable

@Serializable
enum class MessageSeverity {
    WARNING,
    ERROR,
    FATAL_ERROR
}
