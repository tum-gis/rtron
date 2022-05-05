package io.rtron.io.report

import kotlinx.serialization.Serializable

@Serializable
enum class MessageSeverity {
    WARNING,
    ERROR,
    FATAL_ERROR
}
