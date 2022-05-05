package io.rtron.math.geometry

sealed class GeometryException(val message: String, val exceptionIdentifier: String) {

    data class OverlapOrGapInCurve(val suffix: String = "") :
        GeometryException("Overlap or gap in the curve due to its segments not being adjacent to each other.${if (suffix.isNotEmpty()) " $suffix" else ""}", "OverlapOrGapInCurve")

    data class KinkInCurve(val suffix: String = "") :
        GeometryException("Kink in the curve caused by segments having different angles at the transition points.${if (suffix.isNotEmpty()) " $suffix" else ""}", "KinkInCurve")
}

fun GeometryException.toIllegalStateException(): IllegalStateException = IllegalStateException(message)
