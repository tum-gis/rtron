package io.rtron.io.issues

import kotlinx.serialization.Serializable

@Serializable
enum class Severity {
    WARNING,
    ERROR,
    FATAL_ERROR,
}
