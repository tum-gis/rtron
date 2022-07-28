package io.rtron.io.messages

import kotlinx.serialization.Serializable

@Serializable
enum class Severity {
    WARNING,
    ERROR,
    FATAL_ERROR
}
