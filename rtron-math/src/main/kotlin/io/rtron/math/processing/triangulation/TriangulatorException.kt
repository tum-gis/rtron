/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.processing.triangulation

sealed class TriangulatorException(val message: String) {
    data class Poly2TriException(val reason: String) : TriangulatorException("Poly2Tri-Triangulation failure: $reason")
    data class DifferentVertices(val suffix: String = "") : TriangulatorException("Triangulation algorithm produced different vertices.")
    data class ColinearVertices(val suffix: String = "") : TriangulatorException("Triangulation failure (colinear vertices).")
    data class FirstVertexDuplicated(val suffix: String = "") : TriangulatorException("First vertex has duplicate vertices.")
}
