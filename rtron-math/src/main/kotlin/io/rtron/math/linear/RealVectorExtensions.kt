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

package io.rtron.math.linear

/**
 * Returns the dimension of the linear span of a given list of [RealVector].
 * See the wikipedia article on [linear span](https://en.wikipedia.org/wiki/Linear_span).
 *
 * @receiver list of vectors for which the dimension of the span is to be evaluated
 * @return dimension of the span
 */
fun List<RealVector>.dimensionOfSpan(): Int {
    if (this.isEmpty()) return 0
    val singularValueDecomposition = SingularValueDecomposition(RealMatrix(this))
    return singularValueDecomposition.rank
}
