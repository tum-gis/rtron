/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.geometry

sealed class GeometryException(
    val message: String,
    val exceptionIdentifier: String,
) {
    data class BoundaryRepresentationGenerationError(
        val reason: String,
    ) : GeometryException("Cannot generate bounding representation: $reason.", "BoundaryRepresentationGenerationError")

    data class NotEnoughValidLinearRings(
        val suffix: String = "",
    ) : GeometryException(
            "Not enough valid linear rings could be constructed$suffix.",
            "NotEnoughValidLinearRings",
        )

    data class NotEnoughVertices(
        val suffix: String = "",
    ) : GeometryException(
            "Not enough valid linear rings could be constructed$suffix.",
            "NotEnoughVertices",
        )

    data class ValueNotContainedInDomain(
        val value: Double,
    ) : GeometryException(
            "Value ($value) not contained in domain of geometry.",
            "ValueNotContainedInDomain",
        )
}

fun GeometryException.toIllegalStateException(): IllegalStateException = IllegalStateException(message)
